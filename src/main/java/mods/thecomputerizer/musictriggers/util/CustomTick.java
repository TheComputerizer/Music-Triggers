package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomTick {
    private static final ArrayList<Long> registeredTickEvents = new ArrayList<>();

    public static void addCustomTickEvent(long millis) {
        Runnable tickTimer = ChannelManager::tickChannels;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0, millis, TimeUnit.MILLISECONDS);
        registeredTickEvents.add(millis);
    }

    public static void addCustomTickEvent(int ticksPerSecond) {
        long millis = (long)(1000f/ticksPerSecond);
        Runnable tickTimer = ChannelManager::tickChannels;
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
