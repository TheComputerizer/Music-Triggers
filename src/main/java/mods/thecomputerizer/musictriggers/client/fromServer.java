package mods.thecomputerizer.musictriggers.client;

public class fromServer {
    public static boolean inStructure;

    public static void clientSync(boolean b) {
        inStructure = b;
    }
}
