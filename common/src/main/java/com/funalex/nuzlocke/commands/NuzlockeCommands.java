package com.funalex.nuzlocke.commands;

import com.funalex.nuzlocke.NuzlockeManager;
import com.funalex.nuzlocke.config.NuzlockeConfig;
import com.funalex.nuzlocke.state.NuzlockeRunState;
import com.funalex.nuzlocke.state.NuzlockeStateManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class NuzlockeCommands {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy HH:mm");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext context,
            Commands.CommandSelection environment) {
        dispatcher.register(
                literal("nuzlocke")
                        .then(literal("start")
                                .executes(NuzlockeCommands::startRun))
                        .then(literal("stop")
                                .executes(ctx -> stopRun(ctx, false))
                                .then(literal("confirm")
                                        .executes(ctx -> stopRun(ctx, true))))
                        .then(literal("status")
                                .executes(NuzlockeCommands::showStatus))
                        .then(literal("graveyard")
                                .executes(NuzlockeCommands::showGraveyard))
                        .then(literal("rules")
                                .executes(NuzlockeCommands::showRules))

                        // Admin config commands (require permission level 2)
                        .then(literal("config")
                                .requires(source -> source.hasPermission(2))
                                .then(literal("reload")
                                        .executes(NuzlockeCommands::reloadConfig))
                                .then(literal("permadeath")
                                        .then(argument("enabled", BoolArgumentType.bool())
                                                .executes(NuzlockeCommands::setPermaDeath)))
                                .then(literal("announcedeaths")
                                        .then(argument("enabled", BoolArgumentType.bool())
                                                .executes(NuzlockeCommands::setAnnounceDeaths)))
                                .then(literal("deathhandling")
                                        .then(literal("release")
                                                .executes(ctx -> setDeathHandling(ctx,
                                                        NuzlockeConfig.DeathHandlingMode.RELEASE)))
                                        .then(literal("markonly")
                                                .executes(ctx -> setDeathHandling(ctx,
                                                        NuzlockeConfig.DeathHandlingMode.MARK_ONLY))))
                                .then(literal("deathmessage")
                                        .then(argument("message", StringArgumentType.greedyString())
                                                .executes(NuzlockeCommands::setDeathMessage)))
                                .then(literal("show")
                                        .executes(NuzlockeCommands::showConfig))));
    }

    private static int startRun(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }

        boolean success = NuzlockeManager.getInstance().startRun(player);
        return success ? 1 : 0;
    }

    private static int stopRun(CommandContext<CommandSourceStack> ctx, boolean confirmed) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }

        if (!NuzlockeManager.getInstance().isNuzlockeActive(player)) {
            player.sendSystemMessage(Component.literal("You don't have an active Nuzlocke run!"));
            return 0;
        }

        if (!confirmed) {
            player.sendSystemMessage(Component.literal("Are you sure you want to end your Nuzlocke run?"));
            player.sendSystemMessage(Component.literal("Type /nuzlocke stop confirm to confirm."));
            return 0;
        }

        NuzlockeManager.getInstance().endRun(player, true);
        return 1;
    }

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }

        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(player.getUUID());

        if (state == null || !state.isActive()) {
            player.sendSystemMessage(Component.literal("You don't have an active Nuzlocke run."));
            player.sendSystemMessage(Component.literal("Use /nuzlocke start to begin one!"));
            return 1;
        }

        player.sendSystemMessage(Component.literal("§7Nuzlocke Status"));

        long duration = System.currentTimeMillis() - state.getStartTimestamp();
        long hours = duration / (1000 * 60 * 60);
        long minutes = (duration / (1000 * 60)) % 60;
        player.sendSystemMessage(Component.literal("§7Duration: §f" + hours + "h " + minutes + "m"));

        player.sendSystemMessage(Component.literal("§cPokémon Lost: §f" + state.getTotalDeaths()));

        return 1;
    }

    private static int showGraveyard(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }

        NuzlockeRunState state = NuzlockeStateManager.getInstance().getState(player.getUUID());

        if (state == null) {
            player.sendSystemMessage(Component.literal("You don't have any Nuzlocke data."));
            return 0;
        }

        Map<UUID, NuzlockeRunState.DeadPokemonInfo> graveyard = state.getGraveyard();

        if (graveyard.isEmpty()) {
            player.sendSystemMessage(Component.literal("§7Your graveyard is empty."));
            return 1;
        }

        player.sendSystemMessage(Component.literal("§8Graveyard"));
        player.sendSystemMessage(Component.literal(""));

        for (NuzlockeRunState.DeadPokemonInfo info : graveyard.values()) {
            String dateStr = DATE_FORMAT.format(new Date(info.deathTimestamp()));
            player.sendSystemMessage(Component.literal(
                    "§c✝ §f" + info.nickname() + " §7(" + info.species() + " Lv." + info.level() + ")"));
            player.sendSystemMessage(Component.literal("§8  Fell on " + dateStr));
        }

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§7Total fallen: §c" + graveyard.size()));

        return 1;
    }

    private static int showRules(CommandContext<CommandSourceStack> ctx) {
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            ctx.getSource().sendFailure(Component.literal("This command must be run by a player!"));
            return 0;
        }

        NuzlockeConfig config = NuzlockeConfig.getInstance();

        String pdr = config.permaDeathRule ? "§aEnabled" : "§cDisabled";
        player.sendSystemMessage(Component.literal("§7Perma-Death: " + pdr));
        if (config.permaDeathRule) {
            player.sendSystemMessage(Component.literal("§7Fainted Pokémon are considered dead."));
            player.sendSystemMessage(Component.literal("§7Death handling: §f" + config.deathHandling.name()));
        }

        return 1;
    }

    // === Admin Config Commands ===

    private static int reloadConfig(CommandContext<CommandSourceStack> ctx) {
        NuzlockeConfig.reload();
        ctx.getSource().sendSuccess(() -> Component.literal("Config reloaded!"), true);
        return 1;
    }

    private static int setPermaDeath(CommandContext<CommandSourceStack> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        NuzlockeConfig.getInstance().permaDeathRule = enabled;
        NuzlockeConfig.save();
        ctx.getSource().sendSuccess(
                () -> Component.literal("Perma-death rule: " + (enabled ? "enabled" : "disabled")), true);
        return 1;
    }

    private static int setAnnounceDeaths(CommandContext<CommandSourceStack> ctx) {
        boolean enabled = BoolArgumentType.getBool(ctx, "enabled");
        NuzlockeConfig.getInstance().announceDeaths = enabled;
        NuzlockeConfig.save();
        ctx.getSource().sendSuccess(
                () -> Component.literal("Announce deaths: " + (enabled ? "enabled" : "disabled")), true);
        return 1;
    }

    private static int setDeathHandling(CommandContext<CommandSourceStack> ctx, NuzlockeConfig.DeathHandlingMode mode) {
        NuzlockeConfig.getInstance().deathHandling = mode;
        NuzlockeConfig.save();
        ctx.getSource().sendSuccess(() -> Component.literal("Death handling set to: " + mode.name()),
                true);
        return 1;
    }

    private static int setDeathMessage(CommandContext<CommandSourceStack> ctx) {
        String message = StringArgumentType.getString(ctx, "message");
        NuzlockeConfig.getInstance().deathMessage = message;
        NuzlockeConfig.save();
        ctx.getSource().sendSuccess(() -> Component.literal("Death message set to: " + message), true);
        return 1;
    }

    private static int showConfig(CommandContext<CommandSourceStack> ctx) {
        NuzlockeConfig config = NuzlockeConfig.getInstance();

        ctx.getSource().sendSuccess(() -> Component.literal("permaDeathRule: " + config.permaDeathRule), false);
        ctx.getSource().sendSuccess(() -> Component.literal("announceDeaths: " + config.announceDeaths), false);
        ctx.getSource().sendSuccess(() -> Component.literal("deathHandling: " + config.deathHandling.name()),
                false);
        ctx.getSource().sendSuccess(() -> Component.literal("deathMessage: " + config.deathMessage), false);

        return 1;
    }
}
