package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.client.eventsClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CustomTick {

    public CustomTick(){}

    public static void setUp() {
        Runnable tickTimer = eventsClient::onCustomTick;
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(tickTimer, 0, 20, TimeUnit.MILLISECONDS);
    }
}
