package com.matata.server.mixin;

import com.matata.server.accessor.ServerGamePacketListenerImplAccessor;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MinecraftServerGamePacketListenerImplMixin implements ServerGamePacketListenerImplAccessor {

    @Shadow private Vec3 awaitingPositionFromClient;
    @Shadow private int awaitingTeleport;
    @Shadow private int awaitingTeleportTime;
    @Shadow private int tickCount;

    @Unique
    @Override
    public Vec3 tvc$getAwaitingPositionFromClient() {
        return this.awaitingPositionFromClient;
    }

    @Unique
    @Override
    public void tvc$setAwaitingPositionFromClient(Vec3 tmp) {
        this.awaitingPositionFromClient = tmp;
    }

    @Unique
    @Override
    public int tvc$getAwaitingTeleport() {
        return this.awaitingTeleport;
    }

    @Unique
    @Override
    public void tvc$setAwaitingTeleport(int tmp) {
        this.awaitingTeleport = tmp;
    }

    @Unique
    @Override
    public int tvc$getAwaitingTeleportTime() {
        return this.awaitingTeleportTime;
    }

    @Unique
    @Override
    public void tvc$setAwaitingTeleportTime(int tmp) {
        this.awaitingTeleportTime = tmp;
    }

    @Unique
    @Override
    public int tvc$getTickCount() {
        return this.tickCount;
    }

}


