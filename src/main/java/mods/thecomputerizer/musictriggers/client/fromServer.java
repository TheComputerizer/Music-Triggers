package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static mods.thecomputerizer.musictriggers.MusicTriggersCommon.stringBreaker;

public class FromServer {
    private static final ExecutorService executorThread = Executors.newSingleThreadExecutor();
    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static HashMap<String, Boolean> inBiome = new HashMap<>();
    public static boolean inSnow = false;
    public static boolean inHomeRange = false;
    public static HashMap<String, Boolean> mob = new HashMap<>();
    public static HashMap<Integer, Boolean> mobVictory = new HashMap<>();
    public static HashMap<String, Boolean> isRaid = new HashMap<>();
    public static String curStruct;
    public static String curBiome;

    public static void clientSync(String triggerData) {
        executorThread.execute(() -> {
            String[] triggerCategories = stringBreaker(triggerData,"#");
            String snow = triggerCategories[0];
            snow = snow.replaceAll("&","");
            if(!snow.isEmpty()) syncSnow(Boolean.parseBoolean(snow));
            String home = triggerCategories[1];
            home = home.replaceAll("&","");
            if(!home.isEmpty()) syncHome(Boolean.parseBoolean(home));
            for(String biomes : stringBreaker(triggerCategories[2],"\\$")) {
                biomes = biomes.replaceAll("&","");
                if(!biomes.isEmpty()) {
                    String[] biomeParameters = stringBreaker(biomes,"@");
                    syncBiomes(biomeParameters[0], Boolean.parseBoolean(biomeParameters[1]), biomeParameters[2]);
                }
            }
            for(String structures : stringBreaker(triggerCategories[3],"\\$")) {
                structures = structures.replaceAll("&","");
                if(!structures.isEmpty()) {
                    String[] structureParameters = stringBreaker(structures,"@");
                    syncStructures(structureParameters[0], Boolean.parseBoolean(structureParameters[1]), structureParameters[2]);
                }
            }
            for(String mobs : stringBreaker(triggerCategories[4],"\\$")) {
                mobs = mobs.replaceAll("&","");
                if(!mobs.isEmpty()) {
                    String[] mobParameters = stringBreaker(mobs,"@");
                    syncMobs(mobParameters[0], Boolean.parseBoolean(mobParameters[1]), Integer.parseInt(mobParameters[2]), Boolean.parseBoolean(mobParameters[3]));
                }
            }
            for(String raids : stringBreaker(triggerCategories[5],"\\$")) {
                raids = raids.replaceAll("&","");
                if(!raids.isEmpty()) {
                    String[] raidParameters = stringBreaker(raids,"@");
                    syncRaids(raidParameters[0], Boolean.parseBoolean(raidParameters[1]));
                }
            }
        });
    }

    private static void syncSnow(boolean b) {
        inSnow = b;
    }
    private static void syncHome(boolean pass) {
        inHomeRange = pass;
    }
    private static void syncBiomes(String s,boolean b,String d) {
        inBiome.put(s,b);
        curBiome = d;
    }
    private static void syncStructures(String triggerID, boolean pass, String d) {
        inStructure.put(triggerID,pass);
        curStruct = d;
    }
    private static void syncMobs(String s,boolean b, int i, boolean v) {
        mob.put(s,b);
        mobVictory.put(i,v);
    }
    private static void syncRaids(String s,boolean b) {
        isRaid.put(s,b);
    }
}