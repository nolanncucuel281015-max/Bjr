package com.forgotten.dimensions.gametest;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.content.ArtifactDef;
import com.forgotten.dimensions.content.BlockDef;
import com.forgotten.dimensions.content.WeaponDef;
import com.forgotten.dimensions.entity.MagicBoltEntity;
import com.forgotten.dimensions.item.ArtifactEffects;
import com.forgotten.dimensions.item.LegendaryEffects;
import com.forgotten.dimensions.magic.ManaManager;
import com.forgotten.dimensions.magic.ManaState;
import com.forgotten.dimensions.magic.SpellCaster;
import com.forgotten.dimensions.magic.SpellExecutor;
import com.forgotten.dimensions.magic.Spells;
import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.registry.ModAttachments;
import com.forgotten.dimensions.registry.ModBlocks;
import com.forgotten.dimensions.registry.ModEntities;
import com.forgotten.dimensions.registry.ModItems;
import com.forgotten.dimensions.registry.generated.ContentTables;
import com.forgotten.dimensions.util.PlayerCache;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Tests exécutés dans un vrai serveur Minecraft (./gradlew runGameTest). */
public class FdGameTests {
    private static final String NS = ForgottenDimensions.MOD_ID;

    private static FakePlayer player(TestContext ctx, String name) {
        FakePlayer p = FakePlayer.get(ctx.getWorld(), new GameProfile(UUID.randomUUID(), name));
        Vec3d pos = ctx.getAbsolute(new Vec3d(1.5, 2, 1.5));
        p.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        return p;
    }

    @GameTest
    public void registriesAreComplete(TestContext ctx) {
        long blocks = Registries.BLOCK.getIds().stream().filter(id -> id.getNamespace().equals(NS)).count();
        long items = Registries.ITEM.getIds().stream().filter(id -> id.getNamespace().equals(NS)).count();
        ctx.assertEquals((long) ContentTables.blocks().size(), blocks, "nombre de blocs");
        long expectedItems = ContentTables.blocks().size() + ContentTables.items().size() + ContentTables.weapons().size()
                + ContentTables.armorSets().size() * 4L + ContentTables.artifacts().size() + ContentTables.creatures().size();
        ctx.assertEquals(expectedItems, items, "nombre d'objets");
        ctx.assertTrue(Registries.ENTITY_TYPE.getId(ModEntities.MAGIC_BOLT).getNamespace().equals(NS), "entité magic_bolt");
        ctx.complete();
    }

    @GameTest
    public void recipesLootAndAdvancementsLoaded(TestContext ctx) {
        MinecraftServer server = ctx.getWorld().getServer();
        long recipes = server.getRecipeManager().values().stream().filter(r -> r.id().getValue().getNamespace().equals(NS)).count();
        ctx.assertTrue(recipes >= 600, "recettes chargées : " + recipes);
        ctx.assertTrue(server.getRecipeManager().get(RegistryKey.of(RegistryKeys.RECIPE, ForgottenDimensions.id("world_ender"))).isPresent(),
                "recette de Fin des Mondes");
        for (BlockDef b : ContentTables.blocks()) {
            LootTable table = server.getReloadableRegistries().getLootTable(
                    RegistryKey.of(RegistryKeys.LOOT_TABLE, ForgottenDimensions.id("blocks/" + b.id())));
            ctx.assertTrue(table != LootTable.EMPTY, "loot table vide : " + b.id());
        }
        for (String adv : List.of("root", "first_spell", "level_10", "level_100", "skill_adept", "specialization", "crown")) {
            ctx.assertTrue(server.getAdvancementLoader().get(ForgottenDimensions.id(adv)) != null, "succès " + adv);
        }
        ctx.assertTrue(server.getCommandManager().getDispatcher().getRoot().getChild("fdimension") != null, "commande /fdimension");
        ctx.complete();
    }

    @GameTest
    public void levelUpGrantsSkillPoints(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_level");
        ProgressionManager.addXp(p, ExperienceCurve.xpToNext(1) + 3, com.forgotten.dimensions.progression.XpSource.COMMAND);
        PlayerProgress prog = ProgressionManager.get(p);
        ctx.assertEquals(2, prog.level(), "niveau");
        ctx.assertEquals(3, prog.xp(), "xp restante");
        ctx.assertEquals(1, prog.skillPoints(), "points");
        ProgressionManager.setLevel(p, 10);
        ctx.assertEquals(ExperienceCurve.totalSkillPoints(10), ProgressionManager.get(p).skillPoints(), "points au niveau 10");
        ctx.complete();
    }

    @GameTest
    public void skillPrerequisitesAndAttributes(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_skill2");
        ProgressionManager.setLevel(p, 10);
        float before = p.getMaxHealth();
        ctx.assertTrue(ProgressionManager.upgradeSkill(p, "vigor") == null, "vigueur");
        ctx.assertEquals(before + 2.0F, p.getMaxHealth(), "vie max +2");
        ctx.assertEquals("prerequisite", ProgressionManager.upgradeSkill(p, "cleave"), "prérequis vérifié");
        ProgressionManager.resetSkills(p);
        ctx.assertEquals(before, p.getMaxHealth(), "réinitialisation");
        ctx.complete();
    }

    @GameTest
    public void spellCastUsesManaAndCooldown(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_cast");
        p.setAttached(ModAttachments.MANA, new ManaState(100F, 100F));
        ctx.assertEquals(SpellCaster.Result.OK, SpellCaster.tryCast(p, "fire_bolt"), "premier lancement");
        ctx.assertEquals(90F, ManaManager.get(p).current(), "mana consommé");
        ctx.assertEquals(SpellCaster.Result.COOLDOWN, SpellCaster.tryCast(p, "fire_bolt"), "recharge");
        ctx.assertEquals(SpellCaster.Result.LOCKED_LEVEL, SpellCaster.tryCast(p, "meteor"), "sort verrouillé par le niveau");
        ctx.assertEquals(SpellCaster.Result.LOCKED_LEVEL, SpellCaster.tryCast(p, "summon_wolves"), "invocation verrouillée");
        List<MagicBoltEntity> bolts = ctx.getWorld().getEntitiesByType(ModEntities.MAGIC_BOLT, new Box(p.getBlockPos()).expand(4), e -> true);
        ctx.assertTrue(!bolts.isEmpty(), "projectile magique créé");
        bolts.forEach(net.minecraft.entity.Entity::discard);
        p.setAttached(ModAttachments.MANA, new ManaState(5F, 100F));
        ctx.assertEquals(SpellCaster.Result.NO_MANA, SpellCaster.tryCast(p, "ice_shard"), "mana insuffisant");
        ctx.complete();
    }

    @GameTest
    public void spellDamagesAndAppliesSchoolEffect(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_hit");
        ZombieEntity zombie = ctx.spawnEntity(EntityType.ZOMBIE, 2, 2, 2);
        float hp = zombie.getHealth();
        SpellExecutor.hit(p, Spells.get("fire_bolt"), zombie, 5F);
        ctx.assertTrue(zombie.getHealth() < hp, "dégâts infligés");
        ctx.assertTrue(zombie.getFireTicks() > 0, "cible enflammée");
        zombie.discard();
        ctx.complete();
    }

    @GameTest
    public void artifactsAndArmorSetsApply(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_artifact");
        float baseHealth = p.getMaxHealth();
        p.getInventory().insertStack(new ItemStack(ModItems.get("crown_of_seven_worlds")));
        PlayerCache.Snapshot snap = PlayerCache.refresh(p);
        ctx.assertTrue(snap.has("crown_of_seven_worlds"), "artefact détecté");
        ArtifactEffects.applyPassives(p, snap, 20);
        ctx.assertEquals(baseHealth + 10F, p.getMaxHealth(), "couronne : +10 vie max");
        ctx.assertEquals(ManaManager.maxMana(1, 0, false, 100), ManaManager.computeMax(p), "couronne : +100 mana max");
        for (String piece : new String[]{"helmet", "chestplate", "leggings", "boots"}) {
            EquipmentSlot slot = switch (piece) {
                case "helmet" -> EquipmentSlot.HEAD;
                case "chestplate" -> EquipmentSlot.CHEST;
                case "leggings" -> EquipmentSlot.LEGS;
                default -> EquipmentSlot.FEET;
            };
            p.equipStack(slot, new ItemStack(ModItems.get("frost_queen_" + piece)));
        }
        snap = PlayerCache.refresh(p);
        ctx.assertTrue(snap.fullSet() != null && snap.fullSet().id().equals("frost_queen"), "ensemble complet détecté");
        p.getInventory().clear();
        ArtifactEffects.applyPassives(p, PlayerCache.refresh(p), 40);
        ctx.assertEquals(baseHealth, p.getMaxHealth(), "bonus retiré quand l'artefact quitte l'inventaire");
        ctx.complete();
    }

    @GameTest
    public void everySpecialEffectIsImplemented(TestContext ctx) {
        for (ArtifactDef a : ContentTables.artifacts()) {
            ctx.assertTrue(ArtifactEffects.isImplemented(a.id()), "artefact sans effet : " + a.id());
        }
        for (WeaponDef w : ContentTables.weapons()) {
            if (w.isLegendary()) ctx.assertTrue(LegendaryEffects.IMPLEMENTED.contains(w.legendary()), "légendaire sans effet : " + w.id());
            ctx.assertTrue(ModItems.get(w.id()).getDefaultStack().getMaxDamage() > 0, "durabilité : " + w.id());
        }
        ctx.complete();
    }

    @GameTest
    public void progressPersistsThroughNbt(TestContext ctx) {
        PlayerProgress p = new PlayerProgress(37, 120, 4, Map.of("vigor", 2), "warrior", "fireball",
                List.of("minecraft:desert"), Map.of("fire", 80));
        NbtElement nbt = PlayerProgress.CODEC.encodeStart(NbtOps.INSTANCE, p).getOrThrow();
        ctx.assertEquals(p, PlayerProgress.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(), "aller-retour NBT");
        ctx.complete();
    }

    @GameTest
    public void ancientPowerCoreEmitsRedstone(TestContext ctx) {
        BlockPos core = new BlockPos(1, 1, 1);
        ctx.setBlockState(core, ModBlocks.get("architect_power_core").getDefaultState());
        BlockPos next = ctx.getAbsolutePos(core.east());
        ctx.assertTrue(ctx.getWorld().getEmittedRedstonePower(next.west(), Direction.WEST) == 15, "puissance redstone 15");
        ctx.complete();
    }

    @GameTest
    public void fragmentOresGenerateInOverworld(TestContext ctx) {
        var biomes = ctx.getWorld().getRegistryManager().getOrThrow(RegistryKeys.BIOME);
        var plains = biomes.getValueOrThrow(net.minecraft.world.biome.BiomeKeys.PLAINS);
        java.util.Set<String> placed = new java.util.HashSet<>();
        for (var list : plains.getGenerationSettings().getFeatures()) {
            for (var entry : list) entry.getKey().ifPresent(k -> placed.add(k.getValue().toString()));
        }
        for (String ore : ContentTables.overworldOres()) {
            ctx.assertTrue(placed.contains(NS + ":" + ore), "minerai absent des plaines : " + ore);
        }
        ctx.complete();
    }

    private static final Map<String, List<String>> BIOMES = Map.of(
            "celestial", List.of("sky_meadow", "cloud_forest", "floating_peaks", "star_garden", "suspended_ruins"),
            "corrupted", List.of("scarlet_forest", "corrupted_swamp", "root_plains", "infected_mountains", "red_ash_desert"),
            "ocean", List.of("luminous_reef", "abyss", "coral_forest", "sunken_plains", "dark_trench"),
            "chaos", List.of("lava_ocean", "ember_forest", "volcanic_mountains", "magma_desert", "chaos_citadel"),
            "eternal", List.of("frozen_forest", "eternal_glacier", "white_mountains", "frozen_lake", "ice_kingdom"),
            "depths", List.of("crystal_caverns", "fungal_forest", "underground_abyss", "buried_ruins", "shadow_realm"),
            "void", List.of("void_desert", "stellar_islands", "astral_ocean", "dimensional_ruins", "void_realm"));

    /**
     * Le serveur GameTest crée son monde avec les seules dimensions vanilla : on vérifie ici que les
     * types de dimension, réglages de bruit et 35 biomes du mod sont chargés (JSON valides). La
     * génération réelle et le voyage sont testés dans FdClientGameTest, sur un vrai monde.
     */
    @GameTest
    public void dimensionDataLoaded(TestContext ctx) {
        var reg = ctx.getWorld().getRegistryManager();
        var types = reg.getOrThrow(RegistryKeys.DIMENSION_TYPE);
        var noise = reg.getOrThrow(RegistryKeys.CHUNK_GENERATOR_SETTINGS);
        var biomes = reg.getOrThrow(RegistryKeys.BIOME);
        for (String dim : com.forgotten.dimensions.dimension.FdDimensions.IDS) {
            ctx.assertTrue(types.containsId(ForgottenDimensions.id(dim)), "type de dimension : " + dim);
            ctx.assertTrue(noise.containsId(ForgottenDimensions.id(dim)), "réglages de bruit : " + dim);
            for (String b : BIOMES.get(dim)) ctx.assertTrue(biomes.containsId(ForgottenDimensions.id(b)), "biome : " + b);
        }
        ctx.complete();
    }

    @GameTest
    public void keysOpenTheirDimension(TestContext ctx) {
        for (String dim : com.forgotten.dimensions.dimension.FdDimensions.IDS) {
            ctx.assertEquals(dim, com.forgotten.dimensions.dimension.FdDimensions.dimensionOfKey(ModItems.get(dim + "_key")), "clé " + dim);
        }
        ctx.assertTrue(com.forgotten.dimensions.dimension.FdDimensions.dimensionOfKey(ModItems.get("celestial_crystal")) == null, "objet ordinaire");
        MinecraftServer server = ctx.getWorld().getServer();
        ctx.assertTrue(server.getRecipeManager().get(RegistryKey.of(RegistryKeys.RECIPE, ForgottenDimensions.id("void_key"))).isPresent(), "recette clé du Néant");
        ctx.complete();
    }

    // ------------------------------------------------------------------ Phase 5 : créatures

    @GameTest
    public void allCreaturesSpawnWithTheirStats(TestContext ctx) {
        for (var e : com.forgotten.dimensions.registry.ModCreatures.all().entrySet()) {
            var def = com.forgotten.dimensions.registry.ModCreatures.definitions().get(e.getKey());
            net.minecraft.entity.mob.MobEntity mob = ctx.spawnEntity(e.getValue(), new Vec3d(3.5, 2, 3.5));
            ctx.assertTrue(mob.isAlive(), "créature morte au spawn : " + def.id());
            ctx.assertEquals((float) def.maxHealth(), mob.getMaxHealth(), def.id() + " : vie max");
            ctx.assertTrue(mob instanceof com.forgotten.dimensions.entity.creature.FdCreature, def.id() + " : classe");
            ctx.assertTrue(mob.getEntityWorld().getServer().getReloadableRegistries().getLootTable(
                    RegistryKey.of(RegistryKeys.LOOT_TABLE, ForgottenDimensions.id("entities/" + def.id()))) != LootTable.EMPTY,
                    def.id() + " : loot table");
            mob.discard();
        }
        ctx.complete();
    }

    @GameTest(maxTicks = 200)
    public void hostileCreatureHuntsWithRangedAbility(TestContext ctx) {
        var demon = ctx.spawnEntity(com.forgotten.dimensions.registry.ModCreatures.get("magma_demon"), new Vec3d(1.5, 2, 1.5));
        var pig = ctx.spawnEntity(EntityType.PIG, new Vec3d(6.5, 2, 6.5));
        pig.setInvulnerable(false);
        demon.setTarget(pig);
        float hp = pig.getHealth();
        // Réussit dès que la cible est blessée ; échoue par expiration (200 ticks) sinon.
        ctx.runAtEveryTick(() -> {
            demon.setTarget(pig);
            if (pig.getHealth() < hp || !pig.isAlive()) ctx.complete();
        });
    }

    @GameTest(maxTicks = 100)
    public void miniBossSummonsMinions(TestContext ctx) {
        var knight = ctx.spawnEntity(com.forgotten.dimensions.registry.ModCreatures.get("blight_knight"), new Vec3d(2.5, 2, 2.5));
        var pig = ctx.spawnEntity(EntityType.PIG, new Vec3d(5.5, 2, 5.5));
        knight.setTarget(pig);
        ctx.waitAndRun(10, () -> {
            int minions = ctx.getWorld().getEntitiesByType(com.forgotten.dimensions.registry.ModCreatures.get("corrupted_zombie"),
                    knight.getBoundingBox().expand(8), e -> true).size();
            ctx.assertTrue(minions >= 2, "invocations : " + minions);
            ctx.complete();
        });
    }

    @GameTest
    public void meleeHitEffectsAndReinforcements(TestContext ctx) {
        var zombie = ctx.spawnEntity(com.forgotten.dimensions.registry.ModCreatures.get("corrupted_zombie"), new Vec3d(1.5, 2, 1.5));
        var pig = ctx.spawnEntity(EntityType.PIG, new Vec3d(5.5, 2, 5.5));
        com.forgotten.dimensions.entity.creature.CreatureAbilities.onHit(zombie,
                ((com.forgotten.dimensions.entity.creature.FdCreature) zombie).definition(), pig);
        ctx.assertTrue(pig.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.WITHER), "flétrissement au contact");
        var w1 = ctx.spawnEntity(com.forgotten.dimensions.registry.ModCreatures.get("mutant_wolf"), new Vec3d(2.5, 2, 4.5));
        var w2 = ctx.spawnEntity(com.forgotten.dimensions.registry.ModCreatures.get("mutant_wolf"), new Vec3d(3.5, 2, 4.5));
        w1.damage(ctx.getWorld(), ctx.getWorld().getDamageSources().mobAttack(pig), 1.0F);
        ctx.assertTrue(w2.getTarget() == pig, "la meute n'a pas été alertée");
        ctx.complete();
    }

    // ------------------------------------------------------------------ Sorts : bâton et effets 3D

    @GameTest
    public void staffRightClickCastsSelectedSpell(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_staff");
        ProgressionManager.setLevel(p, 20);
        ProgressionManager.set(p, ProgressionManager.get(p).withSelectedSpell("frost_lance"));
        p.setAttached(ModAttachments.MANA, new ManaState(200F, 200F));
        ItemStack staff = new ItemStack(ModItems.get("frost_steel_staff"));
        p.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, staff);
        staff.getItem().use(ctx.getWorld(), p, net.minecraft.util.Hand.MAIN_HAND);
        long now = ctx.getWorld().getTime();
        var cds = p.getAttached(ModAttachments.SPELL_COOLDOWNS);
        ctx.assertTrue(cds != null && cds.remaining("frost_lance", now) > 0, "le bâton n'a pas lancé le sort actif (frost_lance)");
        ctx.assertEquals(200F - 18F, ManaManager.get(p).current(), "mana du sort actif consommé");
        // Changer de sort (G) : le clic droit suivant lance le nouveau sort.
        ProgressionManager.set(p, ProgressionManager.get(p).withSelectedSpell("fire_bolt"));
        p.getItemCooldownManager().remove(p.getItemCooldownManager().getGroup(staff));
        staff.getItem().use(ctx.getWorld(), p, net.minecraft.util.Hand.MAIN_HAND);
        cds = p.getAttached(ModAttachments.SPELL_COOLDOWNS);
        ctx.assertTrue(cds.remaining("fire_bolt", now) > 0, "après changement de sort, le bâton doit lancer fire_bolt");
        // Baguette : -25 % de mana.
        ItemStack wand = new ItemStack(ModItems.get("frost_steel_wand"));
        p.setStackInHand(net.minecraft.util.Hand.MAIN_HAND, wand);
        ProgressionManager.set(p, ProgressionManager.get(p).withSelectedSpell("ice_shard"));
        float before = ManaManager.get(p).current();
        wand.getItem().use(ctx.getWorld(), p, net.minecraft.util.Hand.MAIN_HAND);
        ctx.assertEquals(before - 7.5F, ManaManager.get(p).current(), "baguette : 75 % du coût");
        ctx.getWorld().getEntitiesByType(ModEntities.MAGIC_BOLT, new Box(p.getBlockPos()).expand(8), e -> true).forEach(net.minecraft.entity.Entity::discard);
        ctx.complete();
    }

    @GameTest(maxTicks = 120)
    public void spellsSpawnTheir3DEffects(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_fx");
        com.forgotten.dimensions.magic.SpellExecutor.execute(p, Spells.get("fire_nova"), 5F);
        com.forgotten.dimensions.magic.SpellExecutor.execute(p, Spells.get("ice_armor"), 0F);
        var fx = ctx.getWorld().getEntitiesByType(ModEntities.SPELL_EFFECT, new Box(p.getBlockPos()).expand(6), e -> true);
        java.util.Set<com.forgotten.dimensions.magic.SpellVisual> kinds = new java.util.HashSet<>();
        fx.forEach(e -> kinds.add(e.kind()));
        ctx.assertTrue(kinds.contains(com.forgotten.dimensions.magic.SpellVisual.CAST_CIRCLE), "cercle d'incantation");
        ctx.assertTrue(kinds.contains(com.forgotten.dimensions.magic.SpellVisual.NOVA), "onde de choc de la nova");
        ctx.assertTrue(kinds.contains(com.forgotten.dimensions.magic.SpellVisual.BARRIER), "dôme de l'armure de glace");
        ctx.assertTrue(!ModEntities.SPELL_EFFECT.isSaveable(), "les effets visuels ne sont jamais sauvegardés");
        ctx.waitAndRun(70, () -> {
            int left = ctx.getWorld().getEntitiesByType(ModEntities.SPELL_EFFECT, new Box(p.getBlockPos()).expand(6), e -> true).size();
            ctx.assertEquals(0, left, "les effets disparaissent seuls après leur durée");
            ctx.complete();
        });
    }

    @GameTest(maxTicks = 60)
    public void summonSpellsCallAlliedCreatures(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_summoner");
        ProgressionManager.setLevel(p, 100);
        ctx.assertTrue(Spells.get("summon_fire_elemental").isSummon() && !Spells.get("fireball").isSummon(), "grimoires attaque / invocation");
        ctx.assertTrue(SpellExecutor.execute(p, Spells.get("summon_fire_elemental"), 6F), "invocation de l'élémentaire");
        ctx.assertTrue(SpellExecutor.execute(p, Spells.get("summon_wolves"), 4F), "invocation des loups");
        var type = com.forgotten.dimensions.entity.summon.SummonEntities.get(com.forgotten.dimensions.magic.SummonType.FIRE_ELEMENTAL);
        var found = ctx.getWorld().getEntitiesByType(type, new Box(p.getBlockPos()).expand(8), e -> true);
        ctx.assertEquals(1, found.size(), "un élémentaire de feu");
        var elemental = found.get(0);
        ctx.assertEquals(p.getUuid(), elemental.ownerId(), "lié à son maître");
        ctx.assertTrue(com.forgotten.dimensions.magic.SummonManager.isSummon(elemental), "suivi par le gestionnaire d'invocations");
        var wolves = ctx.getWorld().getEntitiesByType(com.forgotten.dimensions.entity.summon.SummonEntities.get(com.forgotten.dimensions.magic.SummonType.SPIRIT_WOLF),
                new Box(p.getBlockPos()).expand(8), e -> true);
        ctx.assertTrue(wolves.size() >= 2, "au moins deux loups spectraux");
        float hp = elemental.getHealth();
        elemental.damage(ctx.getWorld(), ctx.getWorld().getDamageSources().playerAttack(p), 10F);
        ctx.assertEquals(hp, elemental.getHealth(), "le maître ne blesse pas son invocation");
        ctx.assertTrue(!type.isSaveable(), "invocations non sauvegardées");
        found.forEach(net.minecraft.entity.Entity::discard);
        wolves.forEach(net.minecraft.entity.Entity::discard);
        ctx.complete();
    }

    @GameTest(maxTicks = 200)
    public void summonsAttackNearbyMonsters(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_summoner2");
        ProgressionManager.setLevel(p, 100);
        ZombieEntity zombie = ctx.spawnEntity(EntityType.HUSK, new BlockPos(3, 1, 3)); // ne brûle pas au soleil
        zombie.setAiDisabled(true);
        float max = zombie.getHealth();
        SpellExecutor.execute(p, Spells.get("summon_storm_spirit"), 4F);
        ctx.waitAndRun(80, () -> {
            ctx.assertTrue(zombie.getHealth() < max || !zombie.isAlive(), "l'esprit de l'orage foudroie le zombie proche");
            ctx.getWorld().getEntitiesByClass(com.forgotten.dimensions.entity.summon.SummonedCreatureEntity.class,
                    new Box(p.getBlockPos()).expand(16), e -> true).forEach(net.minecraft.entity.Entity::discard);
            zombie.discard();
            ctx.complete();
        });
    }

    @GameTest
    public void spellsHitOtherPlayers(TestContext ctx) {
        FakePlayer caster = player(ctx, "fd_pvp_a");
        FakePlayer other = player(ctx, "fd_pvp_b");
        ctx.assertTrue(com.forgotten.dimensions.magic.SpellTargeting.isEnemy(caster, other), "un autre joueur est une cible des sorts");
        ctx.assertTrue(!com.forgotten.dimensions.magic.SpellTargeting.isEnemy(caster, caster), "jamais soi-même");
        ctx.assertTrue(com.forgotten.dimensions.magic.SpellTargeting.alliesAround(caster, 5).stream().noneMatch(p -> p == other),
                "un adversaire n'est pas soigné par les sorts de soin");
        other.changeGameMode(net.minecraft.world.GameMode.CREATIVE);
        ctx.assertTrue(!com.forgotten.dimensions.magic.SpellTargeting.isEnemy(caster, other), "épargne les joueurs en créatif");
        other.changeGameMode(net.minecraft.world.GameMode.SURVIVAL);
        // Les coéquipiers sans tir allié sont épargnés (SpellTargeting) ; non testable ici : les FakePlayer de Fabric
        // n'ont jamais d'équipe (getScoreboardTeam renvoie null).
        ctx.complete();
    }

    @GameTest
    public void modRecipesAreInTheRecipeBook(TestContext ctx) {
        FakePlayer p = player(ctx, "fd_recipes");
        int unlocked = com.forgotten.dimensions.event.WorldEvents.unlockModRecipes(p);
        ctx.assertTrue(unlocked > 600, "toutes les recettes du mod débloquées (" + unlocked + ")");
        var key = net.minecraft.registry.RegistryKey.of(net.minecraft.registry.RegistryKeys.RECIPE, ForgottenDimensions.id("iron_greatsword"));
        ctx.assertTrue(p.getRecipeBook().isUnlocked(key), "la grande épée de fer est dans le livre de recettes");
        ctx.complete();
    }
}
