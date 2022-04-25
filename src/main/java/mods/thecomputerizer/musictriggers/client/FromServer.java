package mods.thecomputerizer.musictriggers.client;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static mods.thecomputerizer.musictriggers.MusicTriggers.stringBreaker;

public class FromServer {
    private static final ExecutorService executorThread = Executors.newSingleThreadExecutor();

    public static HashMap<String, Boolean> inStructure = new HashMap<>();
    public static HashMap<String, Boolean> mob = new HashMap<>();
    public static HashMap<Integer, Boolean> mobVictory = new HashMap<>();
    public static boolean home = false;
    public static String curStruct;

    public static void clientSync(String triggerData) {
        executorThread.execute(() -> {
            String[] triggerCategories = stringBreaker(triggerData,"#");
            String home = triggerCategories[0];
            home = home.replaceAll("&","");
            if(!home.isEmpty()) syncHome(Boolean.parseBoolean(home));
            for(String structures : stringBreaker(triggerCategories[1],"\\$")) {
                structures = structures.replaceAll("&","");
                if(!structures.isEmpty()) {
                    String[] structureParameters = stringBreaker(structures,"@");
                    syncStructures(structureParameters[0], Boolean.parseBoolean(structureParameters[1]));
                }
            }
            for(String mobs : stringBreaker(triggerCategories[2],"\\$")) {
                mobs = mobs.replaceAll("&","");
                if(!mobs.isEmpty()) {
                    String[] mobParameters = stringBreaker(mobs,"@");
                    syncMobs(mobParameters[0], Boolean.parseBoolean(mobParameters[1]), Integer.parseInt(mobParameters[2]), Boolean.parseBoolean(mobParameters[3]));
                }
            }
        });
    }

    private static void syncHome(boolean pass) {
        home = pass;
    }

    private static void syncStructures(String triggerID, boolean pass) {
        inStructure.put(triggerID,pass);
    }

    private static void syncMobs(String s,boolean b, int i, boolean v) {
        mob.put(s,b);
        mobVictory.put(i,v);
    }
}
