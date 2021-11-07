package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;

public class fromServer {
    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static String curStruct;

    public static void clientSync(boolean b,String s) {
        inStructure.put(s,b);
    }
}
