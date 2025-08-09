package com.matata.server.accessor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public interface ServerPlayerAccessor {

    void tvc$setIsChangingDimension(boolean isChangingDimension);

    void tvc$setEnteredNetherPosition(Vec3 netherPosition);

    void tvc$triggerDimensionChangeTriggers(ServerLevel level);

    void tvc$setLastSentExp(int tmp);

    void tvc$setLastSentHealth(float tmp);

    void tvc$setLastSentFood(int tmp);

}
