package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;

public class fromServer {
    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static HashMap<String, Boolean> inBiome = new HashMap<>();
    public static HashMap<String, Boolean> mob = new HashMap<>();
    public static String curStruct;
    public static String curBiome;

    public static void clientSyncStruct(boolean b,String s,String d) {
        inStructure.put(s,b);
        curStruct = d;
    }
    public static void clientSyncBiome(boolean b,String s,String d) {
        inBiome.put(s,b);
        curBiome = d;
    }
    public static void clientSyncMob(String s,boolean b) {
        mob.put(s,b);
    }
}
