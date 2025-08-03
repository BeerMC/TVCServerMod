package com.matata.server.mixin;

import com.matata.server.utils.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract EntityType getType();

    @Inject(
        method = "push(Lnet/minecraft/world/entity/Entity;)V",
        at = {@At("HEAD")},
        cancellable = true
    )
    private void onPush(@NotNull Entity entity, CallbackInfo ci) {
        if(!(this.getType() == EntityType.PLAYER) && this.getType().equals(entity.getType())){
            ci.cancel();
        }
    }

    @Inject(
        method = {"canCollideWith(Lnet/minecraft/world/entity/Entity;)Z"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void checkCollide(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if(!(this.getType() == EntityType.PLAYER) && this.getType().equals(entity.getType())){
            cir.setReturnValue(false);
        }

    }
}