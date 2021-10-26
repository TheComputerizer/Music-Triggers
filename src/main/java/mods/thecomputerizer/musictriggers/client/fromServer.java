package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;

public class fromServer {
    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static HashMap<String, Boolean> inBiome = new HashMap<>();

    public static void clientSyncStruct(boolean b,String s) {
        inStructure.put(s,b);
    }
    public static void clientSyncBiome(boolean b,String s) {
        inBiome.put(s,b);
    }
}
