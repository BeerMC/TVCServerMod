package com.matata;

import com.matata.events.PermanentEventSubscriptions;
import com.matata.network.SetJeiSearchPacket;
import com.matata.server.command.*;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.registration.HandlerThread;

@Mod(TVCServerMod.MODID)
public class TVCServerMod
{
    public static final String MODID = "tvcservermod";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean client = false;

    public TVCServerMod(IEventBus modEventBus, Dist dist) {
        IEventBus eventBus = NeoForge.EVENT_BUS;
        PermanentEventSubscriptions subscriptions = new PermanentEventSubscriptions(eventBus, modEventBus);
        registerPacketHandlers(subscriptions);
        registerCommands(subscriptions);
        if(dist.isClient()){
            client = true;
        }
    }

    public void registerPacketHandlers(PermanentEventSubscriptions subscriptions) {
        subscriptions.register(RegisterPayloadHandlersEvent.class, ev ->
                ev.registrar("1")
                        .executesOn(HandlerThread.MAIN)
                        .optional()
                        .playToClient(SetJeiSearchPacket.TYPE, SetJeiSearchPacket.CODEC, SetJeiSearchPacket::handle)
        );
    }

    public void registerCommands(PermanentEventSubscriptions subscriptions) {
        subscriptions.register(RegisterCommandsEvent.class, ev -> {
                    AsynctpCommand.register(ev.getDispatcher());
                    JeimodCommand.register(ev.getDispatcher());
                }
        );
    }

    public static boolean isClient(){
        return client;
    }
}
