package com.matata.server.util;

import com.matata.server.accessor.ServerGamePacketListenerImplAccessor;
import com.matata.server.accessor.ServerPlayerAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Util {

    private static final int PRELOAD_RADIUS = 3;

    public static void asynctp(ServerPlayer targetPlayer, ServerLevel targetLevel, double x, double y, double z) {
        ChunkPos targetChunkPos = new ChunkPos(BlockPos.containing(x, y, z));
        List<CompletableFuture<ChunkResult<ChunkAccess>>> chunkLoadFutures = new ArrayList<>();
        //targetLevel.getChunkSource().addRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
        for(int dx = -PRELOAD_RADIUS; dx <= PRELOAD_RADIUS; ++dx) {
            for(int dz = -PRELOAD_RADIUS; dz <= PRELOAD_RADIUS; ++dz) {
                CompletableFuture<ChunkResult<ChunkAccess>> future = targetLevel.getChunkSource().getChunkFuture(targetChunkPos.x + dx, targetChunkPos.z + dz, ChunkStatus.FULL, true);
                chunkLoadFutures.add(future);
            }
        }
        CompletableFuture.allOf(chunkLoadFutures.toArray(new CompletableFuture[0])).thenRunAsync(() -> {
            if (targetPlayer.isAlive()) {
                teleportToAvoidCallEvent(targetPlayer, targetLevel, x, y, z, targetPlayer.getYRot(), targetPlayer.getXRot());
                targetLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, targetChunkPos, 1, targetPlayer.getId());
            }
            //targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
        }, targetPlayer.getServer()).exceptionally((throwable) -> {
            //targetLevel.getChunkSource().removeRegionTicket(TicketType.PLAYER, targetChunkPos, PRELOAD_RADIUS, targetChunkPos);
            return null;
        });
    }


    public static void teleportToAvoidCallEvent(ServerPlayer player, ServerLevel newLevel, double x, double y, double z, float yaw, float pitch) {
        if (player.isSleeping()) {
            player.stopSleepInBed(true, true);
        }
        player.setCamera(player);
        player.stopRiding();
        if (newLevel == player.level()) {
            Util.teleportAvoidCallEvent(player, x, y, z, yaw, pitch, Collections.emptySet());
        } else {
            Util.changeDimensionAvoidCallEvent(player, new DimensionTransition(newLevel, new Vec3(x, y, z), Vec3.ZERO, yaw, pitch, DimensionTransition.DO_NOTHING));
        }
    }

    private static void teleportAvoidCallEvent(ServerPlayer player, double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> relativeSet){
        double d0 = relativeSet.contains(RelativeMovement.X) ? player.getX() : (double)0.0F;
        double d1 = relativeSet.contains(RelativeMovement.Y) ? player.getY() : (double)0.0F;
        double d2 = relativeSet.contains(RelativeMovement.Z) ? player.getZ() : (double)0.0F;
        float f = relativeSet.contains(RelativeMovement.Y_ROT) ? player.getYRot() : 0.0F;
        float f1 = relativeSet.contains(RelativeMovement.X_ROT) ? player.getXRot() : 0.0F;
        ServerGamePacketListenerImplAccessor accessor = (ServerGamePacketListenerImplAccessor)player.connection;
        accessor.tvc$setAwaitingPositionFromClient(new Vec3(x, y, z));
        accessor.tvc$setAwaitingTeleport(accessor.tvc$getAwaitingTeleport() + 1);
        if (accessor.tvc$getAwaitingTeleport() == Integer.MAX_VALUE) {
            accessor.tvc$setAwaitingTeleport(0);
        }
        accessor.tvc$setAwaitingTeleportTime(accessor.tvc$getTickCount());
        player.absMoveTo(x, y, z, yaw, pitch);
        player.connection.send(new ClientboundPlayerPositionPacket(x - d0, y - d1, z - d2, yaw - f, pitch - f1, relativeSet, accessor.tvc$getAwaitingTeleport()));
    }

    private static Entity changeDimensionAvoidCallEvent(ServerPlayer player, DimensionTransition transition) {
        if (!CommonHooks.onTravelToDimension(player, transition.newLevel().dimension())) {
            return null;
        } else if (player.isRemoved()) {
            return null;
        } else {
            if (transition.missingRespawnBlock()) {
                player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.NO_RESPAWN_BLOCK_AVAILABLE, 0.0F));
            }
            ServerLevel serverlevel = transition.newLevel();
            ServerLevel serverlevel1 = player.serverLevel();
            ResourceKey<Level> resourcekey = serverlevel1.dimension();
            if (serverlevel.dimension() == resourcekey) {
                Util.teleportAvoidCallEvent(player, transition.pos().x, transition.pos().y, transition.pos().z, transition.yRot(), transition.xRot(), Collections.emptySet());
                player.connection.resetPosition();
                transition.postDimensionTransition().onTransition(player);
                return player;
            } else {
                ServerPlayerAccessor accessor = (ServerPlayerAccessor)player;
                accessor.tvc$setIsChangingDimension(true);
                LevelData leveldata = serverlevel.getLevelData();
                player.connection.send(new ClientboundRespawnPacket(player.createCommonSpawnInfo(serverlevel), (byte)3));
                player.connection.send(new ClientboundChangeDifficultyPacket(leveldata.getDifficulty(), leveldata.isDifficultyLocked()));
                PlayerList playerlist = player.server.getPlayerList();
                playerlist.sendPlayerPermissionLevel(player);
                serverlevel1.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
                player.revive();
                serverlevel1.getProfiler().push("moving");
                if (resourcekey == Level.OVERWORLD && serverlevel.dimension() == Level.NETHER) {
                    accessor.tvc$setEnteredNetherPosition(player.position());
                }

                serverlevel1.getProfiler().pop();
                serverlevel1.getProfiler().push("placing");
                player.setServerLevel(serverlevel);
                Util.teleportAvoidCallEvent(player, transition.pos().x, transition.pos().y, transition.pos().z, transition.yRot(), transition.xRot(), Collections.emptySet());
                player.connection.resetPosition();
                serverlevel.addDuringTeleport(player);
                serverlevel1.getProfiler().pop();
                accessor.tvc$triggerDimensionChangeTriggers(serverlevel1);
                player.connection.send(new ClientboundPlayerAbilitiesPacket(player.getAbilities()));
                playerlist.sendLevelInfo(player, serverlevel);
                playerlist.sendAllPlayerInfo(player);
                playerlist.sendActivePlayerEffects(player);
                transition.postDimensionTransition().onTransition(player);
                accessor.tvc$setLastSentExp(-1);
                accessor.tvc$setLastSentHealth(-1.0F);
                accessor.tvc$setLastSentFood(-1);
                EventHooks.firePlayerChangedDimensionEvent(player, resourcekey, transition.newLevel().dimension());
                return player;
            }
        }
    }
}
