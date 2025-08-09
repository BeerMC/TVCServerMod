package com.matata.server.mixin;

import com.matata.server.util.Util;
import net.blay09.mods.waystones.core.WaystoneTeleportManager;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaystoneTeleportManager.class)
public abstract class WaystoneMixin {

    @Inject(
            method = "teleportEntity",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void onTeleportEntity(Entity entity, ServerLevel targetWorld, Vec3 targetPos3d, Direction direction, CallbackInfoReturnable<Entity> cir) {
        if(entity instanceof ServerPlayer player){
            Util.asyncTeleportAvoidCallEvent(player, targetWorld, targetPos3d.x, targetPos3d.y, targetPos3d.z, direction.toYRot(), entity.getXRot());
            player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
            cir.setReturnValue(entity);
        }
    }

}
