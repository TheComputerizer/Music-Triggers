package mods.thecomputerizer.musictriggers.client;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ClientSync {

    private final String channel;

    private Snow snowResult;
    private Home homeResult;
    private final HashMap<String,Biome> biomeResults;
    private final HashMap<String,Structure> structureResults;
    private final HashMap<String,Mob> mobResults;
    private final HashMap<String,Raid> raidResults;

    public ClientSync(String channel) {
        this.channel = channel;
        this.snowResult = new Snow(false);
        this.homeResult = new Home(false);
        this.biomeResults = new HashMap<>();
        this.structureResults = new HashMap<>();
        this.mobResults = new HashMap<>();
        this.raidResults = new HashMap<>();
    }

    public ClientSync(ByteBuf buf) {
        int i;
        this.channel = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        int snowSize = buf.readInt();
        for(i=0;i<snowSize;i++) snowResult = new Snow(buf.readBoolean());
        int homeSize = buf.readInt();
        for(i=0;i<homeSize;i++) homeResult = new Home(buf.readBoolean());
        this.biomeResults = new HashMap<>();
        int biomeSize = buf.readInt();
        for(i=0;i<biomeSize;i++) biomeResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Biome(buf.readBoolean(),(String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8)));
        this.structureResults = new HashMap<>();
        int structureSize = buf.readInt();
        for(i=0;i<structureSize;i++) structureResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Structure(buf.readBoolean()));
        this.mobResults = new HashMap<>();
        int mobSize = buf.readInt();
        for(i=0;i<mobSize;i++) mobResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Mob(buf.readBoolean(), buf.readBoolean()));
        this.raidResults = new HashMap<>();
        int raidSize = buf.readInt();
        for(i=0;i<raidSize;i++) raidResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Raid(buf.readBoolean()));
    }

    public String getChannel() {
        return this.channel;
    }

    public boolean isSnowTriggerActive() {
        return this.snowResult.isActive();
    }

    public boolean isHomeTriggerActive() {
        return this.homeResult.isActive();
    }

    public boolean isBiomeTriggerActive(String triggerID) {
        this.biomeResults.putIfAbsent(triggerID, new Biome(false,null));
        return this.biomeResults.get(triggerID).isActive();
    }

    public String getCurrentBiomeForBiomeTrigger(String triggerID) {
        this.biomeResults.putIfAbsent(triggerID, new Biome(false, null));
        return this.biomeResults.get(triggerID).getCurrentBiome();
    }

    public boolean isStructureTriggerActive(String triggerID) {
        this.structureResults.putIfAbsent(triggerID, new Structure(false));
        return this.structureResults.get(triggerID).isActive();
    }

    public boolean isMobTriggerActive(String triggerID) {
        this.mobResults.putIfAbsent(triggerID, new Mob(false, false));
        return this.mobResults.get(triggerID).isActive();
    }

    public boolean getVictoryStatusForMobTrigger(String triggerID) {
        this.mobResults.putIfAbsent(triggerID, new Mob(false, false));
        return this.mobResults.get(triggerID).getVictory();
    }

    public boolean isRaidTriggerActive(String triggerID) {
        this.raidResults.putIfAbsent(triggerID, new Raid(false));
        return this.raidResults.get(triggerID).isActive();
    }

    public static class Snow {
        private final boolean pass;

        public Snow(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }
    }

    public static class Home {
        private final boolean pass;

        public Home(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }
    }

    public static class Biome {
        private final boolean pass;
        private final String currentBiome;

        public Biome(boolean pass, String currentBiome) {
            this.pass = pass;
            this.currentBiome = currentBiome;
        }

        public boolean isActive() {
            return this.pass;
        }

        public String getCurrentBiome() {
            return this.currentBiome;
        }
    }

    public static class Structure {
        private final boolean pass;

        public Structure(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }
    }

    public static class Mob {
        private final boolean pass;
        private final boolean victory;

        public Mob(boolean pass, boolean victory) {
            this.pass = pass;
            this.victory = victory;
        }

        public boolean isActive() {
            return this.pass;
        }

        public boolean getVictory() {
            return this.victory;
        }
    }

    public static class Raid {
        private final boolean pass;

        public Raid(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }
    }
}
