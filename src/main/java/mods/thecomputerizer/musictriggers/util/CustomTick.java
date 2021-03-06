package mods.thecomputerizer.musictriggers.util;


import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomTick extends Event {

    public CustomTick(){}

    public static void setUp() {
        Runnable tickTimer = () -> MinecraftForge.EVENT_BUS.post(new CustomTick());
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0, 20, TimeUnit.MILLISECONDS);
    }
}
