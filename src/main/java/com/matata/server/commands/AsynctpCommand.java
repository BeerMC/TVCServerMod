package com.matata.server.commands;

import com.matata.server.utils.Util;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.DoubleArgumentType.getDouble;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.arguments.DimensionArgument.dimension;
import static net.minecraft.commands.arguments.DimensionArgument.getDimension;
import static net.minecraft.commands.arguments.EntityArgument.getPlayer;
import static net.minecraft.commands.arguments.EntityArgument.player;

public class AsynctpCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("asynctp")
                .requires(source -> source.hasPermission(2))
                .then(argument("target", player())
                        .then(argument("dimension", dimension())
                                .then(argument("x", doubleArg())
                                        .then(argument("y", doubleArg())
                                                .then(argument("z", doubleArg())
                                                        .executes(context -> {
                                                            ServerPlayer targetPlayer = getPlayer(context, "target");
                                                            ServerLevel targetWorld = getDimension(context, "dimension");
                                                            double x = getDouble(context, "x");
                                                            double y = getDouble(context, "y");
                                                            double z = getDouble(context, "z");
                                                            return handleAsynctp(context.getSource(), targetPlayer, targetWorld, x, y, z);
                                                        })
                                                )
                                        )
                                )
                        )
                );
        dispatcher.register(builder);
    }

    private static int handleAsynctp(CommandSourceStack source, ServerPlayer targetPlayer, ServerLevel targetLevel, double x, double y, double z) {
        CompletableFuture.runAsync(() -> {
            try {
                targetPlayer.getServer().execute(() -> {
                    if (targetLevel == null) {
                        source.sendFailure(Component.literal("目标世界不存在!"));
                    } else {
                        Util.asynctp(targetPlayer, targetLevel, x, y, z);
                    }
                });
            } catch (Exception e) {
                source.sendFailure(Component.literal("传送过程中发生错误: " + e.getMessage()));
                e.printStackTrace();
            }
        }, targetPlayer.getServer());

        return Command.SINGLE_SUCCESS;
    }


}