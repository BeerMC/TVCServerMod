package com.matata.server.mixin;

import net.blay09.mods.balm.api.block.entity.BalmBlockEntityBase;
import net.blay09.mods.balm.api.provider.BalmProviderHolder;
import net.blay09.mods.balm.common.BalmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BalmBlockEntity.class)
public abstract class BalmMixin extends BalmBlockEntityBase implements BalmProviderHolder {

    public BalmMixin(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(
            method = "sync",
            at = @At("HEAD"),
            cancellable = true
    )
    private void sync(CallbackInfo ci) {
        try{
            if (this.getLevel() != null && !this.getLevel().isClientSide) {
                ((ServerLevel)this.getLevel()).getChunkSource().blockChanged(this.getBlockPos());
            }
        }catch (Throwable ignored){}
        ci.cancel();
    }
}
