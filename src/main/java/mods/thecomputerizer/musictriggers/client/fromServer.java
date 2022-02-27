package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;

public class fromServer {
    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static HashMap<String, Boolean> inBiome = new HashMap<>();
    public static HashMap<String, Boolean> inSnow = new HashMap<>();
    public static HashMap<String, Boolean> inHomeRange = new HashMap<>();
    public static HashMap<String, Boolean> mob = new HashMap<>();
    public static HashMap<Integer, Boolean> mobVictory = new HashMap<>();
    public static HashMap<String, Boolean> isRaid = new HashMap<>();
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
    public static void clientSyncSnow(boolean b,String s) {
        inSnow.put(s,b);
    }
    public static void clientSyncHome(boolean b,String s) {
        inHomeRange.put(s,b);
    }
    public static void clientSyncMob(String s,boolean b, int i, boolean v) {
        mob.put(s,b);
        mobVictory.put(i,v);
    }
    public static void clientSyncRaid(String s,boolean b) {
        isRaid.put(s,b);
    }
}
