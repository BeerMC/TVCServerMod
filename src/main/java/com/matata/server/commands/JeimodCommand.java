package com.matata.server.commands;

import com.matata.network.SetJeiSearchPacket;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.neoforged.neoforge.network.PacketDistributor;

import static net.minecraft.commands.Commands.argument;

public class JeimodCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("jeimod")
                .then(argument("mod_name", StringArgumentType.string())
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if(player != null) {
                        String mod_name = StringArgumentType.getString(context, "mod_name");
                        PacketDistributor.sendToPlayer(player, new SetJeiSearchPacket(mod_name));
                    }
                    return Command.SINGLE_SUCCESS;
                })
                );
        dispatcher.register(builder);
    }

}