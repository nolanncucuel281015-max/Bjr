package com.forgotten.dimensions.command;

import com.forgotten.dimensions.ForgottenDimensions;
import com.forgotten.dimensions.dimension.FdDimensions;
import com.forgotten.dimensions.progression.ExperienceCurve;
import com.forgotten.dimensions.progression.PlayerProgress;
import com.forgotten.dimensions.progression.ProgressionManager;
import com.forgotten.dimensions.progression.XpSource;
import com.forgotten.dimensions.registry.ModBlocks;
import com.forgotten.dimensions.registry.ModItems;
import com.forgotten.dimensions.util.config.FdConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

/** /fdimension — commandes de test réservées aux opérateurs (niveau de permission « maître du jeu »). */
public final class FdCommand {
    private FdCommand() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static List<String> allIds() {
        List<String> ids = new ArrayList<>(ModItems.all().keySet());
        ids.addAll(ModBlocks.all().keySet());
        return ids;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("fdimension")
                .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                .then(literal("list").executes(FdCommand::list))
                .then(literal("tp").then(argument("dimension", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(FdDimensions.IDS, b))
                        .executes(FdCommand::tp)))
                .then(literal("give").then(argument("item", StringArgumentType.word())
                        .suggests((c, b) -> CommandSource.suggestMatching(allIds(), b))
                        .executes(c -> give(c, 1))
                        .then(argument("count", IntegerArgumentType.integer(1, 6400))
                                .executes(c -> give(c, IntegerArgumentType.getInteger(c, "count"))))))
                .then(literal("level")
                        .then(literal("set").then(argument("player", EntityArgumentType.player())
                                .then(argument("level", IntegerArgumentType.integer(1, PlayerProgress.MAX_LEVEL)).executes(FdCommand::levelSet))))
                        .then(literal("get").then(argument("player", EntityArgumentType.player()).executes(FdCommand::levelGet))))
                .then(literal("xp").then(literal("add").then(argument("player", EntityArgumentType.player())
                        .then(argument("amount", IntegerArgumentType.integer(1)).executes(FdCommand::xpAdd)))))
                .then(literal("skills").then(literal("reset").then(argument("player", EntityArgumentType.player()).executes(FdCommand::skillsReset))))
                .then(literal("reload").executes(FdCommand::reload)));
    }

    private static int list(CommandContext<ServerCommandSource> c) {
        ServerCommandSource src = c.getSource();
        src.sendFeedback(() -> Text.translatable("command.forgotten_dimensions.list.header").formatted(Formatting.GOLD), false);
        for (String id : FdDimensions.IDS) {
            ServerWorld world = src.getServer().getWorld(FdDimensions.key(id));
            Text status = world != null
                    ? Text.translatable("command.forgotten_dimensions.list.available").formatted(Formatting.GREEN)
                    : Text.translatable("command.forgotten_dimensions.list.missing").formatted(Formatting.GRAY);
            src.sendFeedback(() -> Text.literal(" • ").append(Text.translatable("dimension.forgotten_dimensions." + id))
                    .append(Text.literal(" (" + ForgottenDimensions.MOD_ID + ":" + id + ") ")).append(status), false);
        }
        return FdDimensions.IDS.size();
    }

    private static int tp(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, "dimension");
        ServerPlayerEntity player = c.getSource().getPlayerOrThrow();
        if (!FdDimensions.IDS.contains(id)) {
            c.getSource().sendError(Text.translatable("command.forgotten_dimensions.tp.unknown", id));
            return 0;
        }
        ServerWorld target = c.getSource().getServer().getWorld(FdDimensions.key(id));
        if (target == null) {
            c.getSource().sendError(Text.translatable("command.forgotten_dimensions.tp.missing", id));
            return 0;
        }
        BlockPos top = target.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, BlockPos.ORIGIN);
        player.teleport(target, top.getX() + 0.5, Math.max(top.getY(), target.getBottomY() + 1) + 1, top.getZ() + 0.5,
                Set.of(), player.getYaw(), player.getPitch(), true);
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.tp.done",
                Text.translatable("dimension.forgotten_dimensions." + id)), true);
        return 1;
    }

    private static int give(CommandContext<ServerCommandSource> c, int count) throws CommandSyntaxException {
        String id = StringArgumentType.getString(c, "item");
        Item item = ModItems.get(id);
        if (item == null && ModBlocks.get(id) != null) item = ModBlocks.get(id).asItem();
        if (item == null) {
            c.getSource().sendError(Text.translatable("command.forgotten_dimensions.give.unknown", id));
            return 0;
        }
        ServerPlayerEntity player = c.getSource().getPlayerOrThrow();
        int remaining = count;
        while (remaining > 0) {
            int n = Math.min(remaining, item.getMaxCount());
            ItemStack stack = new ItemStack(item, n);
            if (!player.getInventory().insertStack(stack)) player.dropItem(stack, false);
            remaining -= n;
        }
        Item finalItem = item;
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.give.done", count, finalItem.getName()), true);
        return count;
    }

    private static int levelSet(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(c, "player");
        int level = IntegerArgumentType.getInteger(c, "level");
        ProgressionManager.setLevel(player, level);
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.level.set", player.getDisplayName(), level), true);
        return level;
    }

    private static int levelGet(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(c, "player");
        PlayerProgress p = ProgressionManager.get(player);
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.level.get", player.getDisplayName(),
                p.level(), p.xp(), ExperienceCurve.xpToNext(p.level()), p.skillPoints()), false);
        return p.level();
    }

    private static int xpAdd(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(c, "player");
        int amount = IntegerArgumentType.getInteger(c, "amount");
        ProgressionManager.addXp(player, amount, XpSource.COMMAND);
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.xp.add", amount, player.getDisplayName()), true);
        return amount;
    }

    private static int skillsReset(CommandContext<ServerCommandSource> c) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(c, "player");
        ProgressionManager.resetSkills(player);
        c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.skills.reset", player.getDisplayName()), true);
        return 1;
    }

    private static int reload(CommandContext<ServerCommandSource> c) {
        boolean ok = FdConfig.load();
        if (ok) c.getSource().sendFeedback(() -> Text.translatable("command.forgotten_dimensions.reload.done"), true);
        else c.getSource().sendError(Text.translatable("command.forgotten_dimensions.reload.error"));
        return ok ? 1 : 0;
    }
}
