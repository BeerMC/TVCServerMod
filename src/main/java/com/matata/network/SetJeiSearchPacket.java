package com.matata.network;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;


public record SetJeiSearchPacket(String mod_name) implements CustomPacketPayload {

    public static final Type<SetJeiSearchPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ModIds.JEI_ID, "neoforge_gui_tvc_setjeisearch"));

    public static final StreamCodec<FriendlyByteBuf, SetJeiSearchPacket> CODEC = StreamCodec.ofMember(SetJeiSearchPacket::write, SetJeiSearchPacket::read);

    public static SetJeiSearchPacket read(FriendlyByteBuf buf) {
        return new SetJeiSearchPacket(buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(mod_name);
    }

    public static void handle(SetJeiSearchPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            Internal.getJeiRuntime().getIngredientFilter().setFilterText("@" + packet.mod_name);
            mc.player.getInventory().startOpen(mc.player);
        });
    }

    public @NotNull Type<SetJeiSearchPacket> type() {
        return TYPE;
    }
}
