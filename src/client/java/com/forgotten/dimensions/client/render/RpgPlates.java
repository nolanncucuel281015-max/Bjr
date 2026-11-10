package com.forgotten.dimensions.client.render;

import com.forgotten.dimensions.client.ClientData;
import com.forgotten.dimensions.content.CreatureCategory;
import com.forgotten.dimensions.entity.creature.FdCreature;
import com.forgotten.dimensions.entity.summon.SummonedCreatureEntity;
import com.forgotten.dimensions.progression.CreatureLevels;
import com.forgotten.dimensions.util.config.FdConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface de combat façon RPG au-dessus des créatures :
 * <ul>
 *   <li>plaque avec niveau (coloré selon l'écart avec le joueur), nom, rang (élite, mini-boss) et
 *   barre de vie avec traînée des dégâts récents ;</li>
 *   <li>chiffres de dégâts et de soins flottants (déduits des variations de vie synchronisées :
 *   aucun paquet réseau).</li>
 * </ul>
 * Les données sont calculées dans updateRenderState (mixin) et attachées à l'état de rendu.
 */
public final class RpgPlates {
    public static final RenderStateDataKey<Plate> KEY = RenderStateDataKey.create(() -> "forgotten_dimensions:rpg_plate");

    /** Chiffre flottant : valeur, âge (ticks + fraction), décalage horizontal, couleur, échelle. */
    public record Num(String text, int color, float age, float dx, float scale) {}

    public record Plate(boolean bar, Text title, float health, float max, float trail, int fill, int frame, List<Num> numbers) {}

    private static final class Floating {
        final float value;
        final boolean heal;
        final boolean big;
        final float dx;
        int age;

        Floating(float value, boolean heal, boolean big, float dx) {
            this.value = value;
            this.heal = heal;
            this.big = big;
            this.dx = dx;
        }
    }

    private static final Map<Integer, Float> LAST_HEALTH = new HashMap<>();
    private static final Map<Integer, Float> TRAIL = new HashMap<>();
    private static final Map<Integer, Integer> LAST_HURT = new HashMap<>();
    private static final Map<Integer, List<Floating>> NUMBERS = new HashMap<>();
    private static int clock;

    private RpgPlates() {}

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(RpgPlates::tick);
    }

    private static void tick(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            LAST_HEALTH.clear();
            TRAIL.clear();
            LAST_HURT.clear();
            NUMBERS.clear();
            return;
        }
        clock++;
        Set<Integer> seen = new HashSet<>();
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof LivingEntity e) || e == client.player || e.squaredDistanceTo(client.player) > 40 * 40) continue;
            int id = e.getId();
            seen.add(id);
            float h = e.getHealth();
            Float prev = LAST_HEALTH.put(id, h);
            if (prev != null) {
                float d = h - prev;
                if (d <= -0.5F) {
                    LAST_HURT.put(id, clock);
                    addNumber(id, -d, false, -d >= Math.max(8F, e.getMaxHealth() * 0.25F));
                } else if (d >= 1.0F) {
                    addNumber(id, d, true, false);
                }
            }
            // Traînée : reste sur l'ancienne valeur un court instant puis rejoint la vie actuelle.
            float trail = TRAIL.getOrDefault(id, h);
            if (trail < h) trail = h;
            else if (clock - LAST_HURT.getOrDefault(id, -100) > 8) trail = Math.max(h, trail - e.getMaxHealth() * 0.03F);
            TRAIL.put(id, trail);
        }
        NUMBERS.values().forEach(list -> {
            list.forEach(f -> f.age++);
            list.removeIf(f -> f.age > 32);
        });
        NUMBERS.values().removeIf(List::isEmpty);
        if (clock % 100 == 0) {
            LAST_HEALTH.keySet().retainAll(seen);
            TRAIL.keySet().retainAll(seen);
            LAST_HURT.keySet().retainAll(seen);
        }
    }

    private static void addNumber(int id, float value, boolean heal, boolean big) {
        List<Floating> list = NUMBERS.computeIfAbsent(id, k -> new ArrayList<>());
        if (list.size() > 6) list.remove(0);
        float dx = ((clock * 37 + list.size() * 53) % 21 - 10) * 1.4F;
        list.add(new Floating(value, heal, big, dx));
    }

    // ------------------------------------------------------------------ extraction (thread de jeu)

    public static void extract(LivingEntity e, LivingEntityRenderState s, float tickDelta) {
        s.setData(KEY, null);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || e == client.player) return;
        int id = e.getId();
        List<Num> nums = List.of();
        List<Floating> fl = NUMBERS.get(id);
        if (fl != null && FdConfig.get().damageNumbers) {
            nums = new ArrayList<>();
            for (Floating f : fl) {
                String text = (f.heal ? "+" : "") + format(f.value);
                int color = f.heal ? 0xFF6CFF6C : f.big ? 0xFFFF8A2A : 0xFFFFF4D0;
                float age = f.age + tickDelta;
                float pop = age < 3 ? 1.6F - age * 0.15F : 1.15F;
                nums.add(new Num(text, color, age, f.dx, (f.big ? 1.45F : 1.0F) * pop));
            }
        }
        boolean bar = FdConfig.get().rpgNameplates && showBar(client, e);
        if (!bar && nums.isEmpty()) return;
        Text title = bar ? title(e) : Text.empty();
        float max = Math.max(1F, e.getMaxHealth());
        int fill, frame;
        CreatureCategory cat = CreatureLevels.category(e);
        if (e instanceof SummonedCreatureEntity) {
            fill = 0xFF40D8B0;
            frame = 0xFF1A6A58;
        } else {
            float r = e.getHealth() / max;
            fill = r > 0.5F ? 0xFF4CD44C : r > 0.25F ? 0xFFE8C030 : 0xFFE03A2A;
            frame = cat == CreatureCategory.MINIBOSS ? 0xFFC060FF : cat == CreatureCategory.ELITE ? 0xFFFFC840 : 0xFF101010;
        }
        s.setData(KEY, new Plate(bar, title, e.getHealth(), max, TRAIL.getOrDefault(id, e.getHealth()), fill, frame, nums));
    }

    private static boolean showBar(MinecraftClient client, LivingEntity e) {
        if (!(e instanceof MobEntity) || e.isInvisible() || !e.isAlive()) return false;
        double d2 = e.squaredDistanceTo(client.player);
        if (e instanceof SummonedCreatureEntity) return d2 < 18 * 18;
        if (d2 > 22 * 22) return false;
        boolean recentlyHurt = clock - LAST_HURT.getOrDefault(e.getId(), -1000) < 120;
        return e instanceof FdCreature || e instanceof Monster || recentlyHurt || client.targetedEntity == e;
    }

    private static Text title(LivingEntity e) {
        if (e instanceof SummonedCreatureEntity) {
            return Text.literal("✦ ").formatted(Formatting.AQUA).append(e.getDisplayName().copy().formatted(Formatting.GREEN));
        }
        int lvl = CreatureLevels.of(e);
        int player = ClientData.progress().level();
        MutableText t = Text.empty();
        CreatureCategory cat = CreatureLevels.category(e);
        if (cat == CreatureCategory.MINIBOSS) t.append(Text.literal("☠ ").formatted(Formatting.LIGHT_PURPLE));
        else if (cat == CreatureCategory.ELITE) t.append(Text.literal("★ ").formatted(Formatting.GOLD));
        String lv = lvl - player >= 15 ? "??" : String.valueOf(lvl);
        t.append(Text.translatable("plate.forgotten_dimensions.level", lv).withColor(CreatureLevels.color(player, lvl) & 0xFFFFFF));
        t.append(Text.literal(" "));
        MutableText name = e.getDisplayName().copy();
        if (cat == CreatureCategory.MINIBOSS) name = name.withColor(0xE8A8FF);
        else if (cat == CreatureCategory.ELITE) name = name.formatted(Formatting.GOLD);
        else name = name.formatted(e instanceof Monster ? Formatting.WHITE : Formatting.GRAY);
        return t.append(name);
    }

    private static String format(float v) {
        return v >= 10 || Math.abs(v - Math.round(v)) < 0.05F ? String.valueOf(Math.round(v)) : String.format(java.util.Locale.ROOT, "%.1f", v);
    }

    // ------------------------------------------------------------------ rendu (file de commandes)

    public static void render(LivingEntityRenderState s, MatrixStack m, OrderedRenderCommandQueue q, CameraRenderState camera) {
        Plate p = s.getData(KEY);
        if (p == null) return;
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        m.push();
        m.translate(0, s.height + (s.displayName != null ? 0.85F : 0.55F), 0);
        m.multiply(camera.orientation);
        m.scale(0.03F, -0.03F, 0.03F);
        if (p.bar()) {
            float frac = MathHelper.clamp(p.health() / p.max(), 0F, 1F);
            float trail = MathHelper.clamp(p.trail() / p.max(), 0F, 1F);
            int w = 40, x0 = -w / 2;
            q.submitCustom(m, RenderLayers.textBackground(), (entry, vc) -> {
                rect(vc, entry, x0 - 1.5F, -1.5F, x0 + w + 1.5F, 5.5F, p.frame());
                rect(vc, entry, x0 - 0.5F, -0.5F, x0 + w + 0.5F, 4.5F, 0xFF000000);
                rect(vc, entry, x0, 0, x0 + w, 4, 0xFF3A0E0E);
                if (trail > frac) rect(vc, entry, x0 + w * frac, 0, x0 + w * trail, 4, 0xFFF0F0F0);
                rect(vc, entry, x0, 0, x0 + w * frac, 4, p.fill());
                rect(vc, entry, x0, 0, x0 + w * frac, 1.3F, brighten(p.fill()));
                for (int i = 1; i < 4; i++) rect(vc, entry, x0 + w * i / 4F - 0.25F, 0, x0 + w * i / 4F + 0.25F, 4, 0x60000000);
            });
            var title = p.title().asOrderedText();
            int tw = tr.getWidth(title);
            q.submitText(m, -tw / 2F, -12F, title, false, TextRenderer.TextLayerType.POLYGON_OFFSET, light, 0xFFFFFFFF, 0x66000000, 0);
        }
        for (Num n : p.numbers()) {
            float a = n.age() > 20 ? Math.max(0F, 1F - (n.age() - 20) / 12F) : 1F;
            int alpha = Math.max(8, Math.round(255 * a));
            m.push();
            m.translate(n.dx(), -18F - n.age() * 1.1F, -0.5F);
            m.scale(n.scale(), n.scale(), 1F);
            var t = Text.literal(n.text()).formatted(Formatting.BOLD).asOrderedText();
            int tw = tr.getWidth(t);
            q.submitText(m, -tw / 2F, 0, t, false, TextRenderer.TextLayerType.POLYGON_OFFSET, light,
                    (alpha << 24) | (n.color() & 0xFFFFFF), 0, (alpha << 24));
            m.pop();
        }
        m.pop();
    }

    private static int brighten(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 70), g = Math.min(255, ((argb >> 8) & 0xFF) + 70), b = Math.min(255, (argb & 0xFF) + 70);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    /** Rectangle plein, émis dans les deux sens (indépendant de l'orientation des faces). */
    private static void rect(VertexConsumer vc, MatrixStack.Entry e, float x0, float y0, float x1, float y1, int argb) {
        int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        vc.vertex(e, x0, y0, 0).color(argb).light(light);
        vc.vertex(e, x0, y1, 0).color(argb).light(light);
        vc.vertex(e, x1, y1, 0).color(argb).light(light);
        vc.vertex(e, x1, y0, 0).color(argb).light(light);
        vc.vertex(e, x1, y0, 0).color(argb).light(light);
        vc.vertex(e, x1, y1, 0).color(argb).light(light);
        vc.vertex(e, x0, y1, 0).color(argb).light(light);
        vc.vertex(e, x0, y0, 0).color(argb).light(light);
    }
}
