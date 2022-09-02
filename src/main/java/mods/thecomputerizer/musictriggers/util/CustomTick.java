package mods.thecomputerizer.musictriggers.util;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomTick extends Event {
    private static final ArrayList<Long> registeredTickEvents = new ArrayList<>();

    public static void addCustomTickEvent(long millis) {
        Runnable tickTimer = () -> MinecraftForge.EVENT_BUS.post(new CustomTick(millis));
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0, millis, TimeUnit.MILLISECONDS);
        registeredTickEvents.add(millis);
    }

    public static void addCustomTickEvent(int ticksPerSecond) {
        long millis = (long)(1000f/ticksPerSecond);
        Runnable tickTimer = () -> MinecraftForge.EVENT_BUS.post(new CustomTick(millis));
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0, millis, TimeUnit.MILLISECONDS);
        registeredTickEvents.add(millis);
    }

    public static boolean isRegistered(long millis) {
        return registeredTickEvents.contains(millis);
    }

    public static boolean isRegistered(int ticks) {
        return registeredTickEvents.contains((long)(1000f/ticks));
    }

    private final long tickRate;
    private CustomTick(long tickRate){
        this.tickRate = tickRate;
    }

    public boolean checkTickRate(long tickRate) {
        return this.tickRate==tickRate;
    }

    public boolean checkTickRate(int tickRate) {
        return this.tickRate==(long)(1000f/tickRate);
    }
}
