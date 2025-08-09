package com.matata.server.accessor;


import net.minecraft.world.phys.Vec3;

public interface ServerGamePacketListenerImplAccessor {

    Vec3 tvc$getAwaitingPositionFromClient();

    void tvc$setAwaitingPositionFromClient(Vec3 tmp);

    int tvc$getAwaitingTeleport();

    void tvc$setAwaitingTeleport(int tmp);

    int tvc$getAwaitingTeleportTime();

    void tvc$setAwaitingTeleportTime(int tmp);

    int tvc$getTickCount();

}
