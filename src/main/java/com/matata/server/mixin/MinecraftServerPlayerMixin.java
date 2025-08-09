package com.matata.server.mixin;

import com.matata.server.accessor.ServerPlayerAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerPlayer.class)
public abstract class MinecraftServerPlayerMixin implements ServerPlayerAccessor {

    @Shadow private boolean isChangingDimension;
    @Shadow private Vec3 enteredNetherPosition;
    @Shadow protected abstract void triggerDimensionChangeTriggers(ServerLevel level);
    @Shadow private int lastSentExp;
    @Shadow private float lastSentHealth;
    @Shadow private int lastSentFood;

    @Unique
    @Override
    public void tvc$setIsChangingDimension(boolean isChangingDimension){
        this.isChangingDimension = isChangingDimension;
    }

    @Unique
    @Override
    public void tvc$setEnteredNetherPosition(Vec3 netherPosition){
        this.enteredNetherPosition = netherPosition;
    }

    @Unique
    @Override
    public void tvc$triggerDimensionChangeTriggers(ServerLevel level){
        this.triggerDimensionChangeTriggers(level);
    }

    @Unique
    @Override
    public void tvc$setLastSentExp(int tmp){
        this.lastSentExp = tmp;
    }

    @Unique
    @Override
    public void tvc$setLastSentHealth(float tmp){
        this.lastSentHealth = tmp;
    }

    @Unique
    @Override
    public void tvc$setLastSentFood(int tmp){
        this.lastSentFood = tmp;
    }




}
