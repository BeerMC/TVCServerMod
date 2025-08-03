package com.matata.server.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Util {

    private static final int PRELOAD_RADIUS = 1;

    public static void asynctp(ServerPlayer targetPlayer, ServerLevel targetLevel, double x, double y, double z) {
        ChunkPos targetChunkPos = new ChunkPos(BlockPos.containing(x, y, z));
        List<CompletableFuture<ChunkResult<ChunkAccess>>> chunkLoadFutures = new ArrayList<>();
        targetLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
//        targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, targetPlayer.getId());
        for(int dx = -PRELOAD_RADIUS; dx <= PRELOAD_RADIUS; ++dx) {
            for(int dz = -PRELOAD_RADIUS; dz <= PRELOAD_RADIUS; ++dz) {
                ChunkPos currentChunkToLoad = new ChunkPos(targetChunkPos.getRegionX() + dx, targetChunkPos.getRegionZ() + dz);
                CompletableFuture<ChunkResult<ChunkAccess>> future = targetLevel.getChunkSource().getChunkFuture(currentChunkToLoad.getRegionX(), currentChunkToLoad.getRegionZ(), ChunkStatus.FULL, true);
                chunkLoadFutures.add(future);
            }
        }
        CompletableFuture.allOf(chunkLoadFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
            targetPlayer.stopRiding();
            if (targetPlayer.isSleeping()) {
                targetPlayer.stopSleepInBed(true, true);
            }

            if (targetLevel == targetPlayer.level()) {
                targetPlayer.connection.teleport(x, y, z, targetPlayer.getYRot(), targetPlayer.getXRot(), RelativeMovement.ROTATION);
            } else {
                targetPlayer.teleportTo(targetLevel, x, y, z, targetPlayer.getYRot(), targetPlayer.getXRot());
            }

            targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, PRELOAD_RADIUS, targetPlayer.getId());
            targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
        }, targetPlayer.getServer()).exceptionally((throwable) -> {
            targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
            return null;
        });
    }

    public static boolean canSkip(@NotNull Entity one, @NotNull Entity another) {
        return one.getType().equals(another.getType());
    }

}
