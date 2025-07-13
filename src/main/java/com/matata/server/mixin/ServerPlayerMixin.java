package com.matata.server.mixin;


import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin{

//    private static final int PRELOAD_RADIUS = 2;
//
//    @Inject(
//            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
//            at = @At("HEAD"),
//            cancellable = true
//    )
//    private void onTeleportAsync(ServerLevel targetLevel, double x, double y, double z, Set<RelativeMovement> relativeMovements, float yaw, float pitch, CallbackInfoReturnable<Boolean> cir){
//        ServerPlayer self = (ServerPlayer)(Object)this;
//        MinecraftServer server = self.server;
//        self.sendSystemMessage(Component.literal("正在加载区块, 请稍后"));
//        ChunkPos targetChunkPos = new ChunkPos(BlockPos.containing(x, y, z));
//        List<CompletableFuture<ChunkResult<ChunkAccess>>> chunkLoadFutures = new ArrayList<>();
//        targetLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, targetChunkPos, 3, targetChunkPos);
////        targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, self.getId());
//        for(int dx = -PRELOAD_RADIUS; dx <= PRELOAD_RADIUS; ++dx) {
//            for(int dz = -PRELOAD_RADIUS; dz <= PRELOAD_RADIUS; ++dz) {
//                ChunkPos currentChunkToLoad = new ChunkPos(targetChunkPos.getRegionX() + dx, targetChunkPos.getRegionZ() + dz);
//                CompletableFuture<ChunkResult<ChunkAccess>> future = targetLevel.getChunkSource().getChunkFuture(currentChunkToLoad.getRegionX(), currentChunkToLoad.getRegionZ(), ChunkStatus.FULL, true);
//                chunkLoadFutures.add(future);
//            }
//        }
//
//        CompletableFuture.allOf(chunkLoadFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
//            self.stopRiding();
//            if (self.isSleeping()) {
//                self.stopSleepInBed(true, true);
//            }
//
//            if (targetLevel == self.level()) {
//                self.connection.teleport(x, y, z, yaw, pitch, relativeMovements);
//            } else {
//                self.teleportTo(targetLevel, x, y, z, yaw, pitch);
//            }
//
//            self.setYHeadRot(yaw);
//            targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, self.getId());
//            targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, 3, targetChunkPos);
//        }, server).exceptionally((throwable) -> {
//            targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, 3, targetChunkPos);
//            return null;
//        });
//        cir.setReturnValue(true);
//        cir.cancel();
//    }

}
