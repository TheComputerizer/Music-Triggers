package mods.thecomputerizer.musictriggers.server;

public class ServerEvents {
    private static int TIMER = 0;

    public static void onServerTick() {
        TIMER++;
        if (TIMER >= 5) {
            ServerData.runServerChecks();
            TIMER = 0;
        }
    }
}
