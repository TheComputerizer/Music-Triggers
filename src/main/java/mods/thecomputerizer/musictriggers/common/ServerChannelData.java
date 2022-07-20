package mods.thecomputerizer.musictriggers.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerChannelData {

    private final String channel;
    private final UUID playerUUID;
    private final String currentSong;
    private final List<Home> homeTriggers;
    private final List<Snow> snowTriggers;
    private final List<Biome> biomeTriggers;
    private final List<Structure> structureTriggers;
    private final List<Mob> mobTriggers;
    private final List<Raid> raidTriggers;

    private final List<String> allTriggers;
    private final List<String> menuSongs;

    private ServerChannelData(String channel, String uuid, String currentSong) {
        this.channel = channel;
        this.playerUUID = UUID.fromString(uuid);
        this.currentSong = currentSong;
        this.snowTriggers = new ArrayList<>();
        this.homeTriggers = new ArrayList<>();
        this.biomeTriggers = new ArrayList<>();
        this.structureTriggers = new ArrayList<>();
        this.mobTriggers = new ArrayList<>();
        this.raidTriggers = new ArrayList<>();
        this.allTriggers = new ArrayList<>();
        this.menuSongs = new ArrayList<>();
    }

    public List<Snow> getSnowTriggers() {
        return this.snowTriggers;
    }

    public List<Home> getHomeTriggers() {
        return this.homeTriggers;
    }

    public List<Biome> getBiomeTriggers() {
        return this.biomeTriggers;
    }

    public List<Structure> getStructureTriggers() {
        return this.structureTriggers;
    }

    public List<Mob> getMobTriggers() {
        return this.mobTriggers;
    }

    public List<Raid> getRaidTriggers() {
        return this.raidTriggers;
    }

    public List<String> getAllTriggers() {
        return this.allTriggers;
    }

    public List<String> getMenuSongs() {
        return this.menuSongs;
    }

    public void addSnowTrigger(Snow snow) {
        this.snowTriggers.add(snow);
    }

    public void addHomeTrigger(Home home) {
        this.homeTriggers.add(home);
    }

    public void addBiomeTrigger(Biome biome) {
        this.biomeTriggers.add(biome);
    }

    public void addStructureTrigger(Structure structure) {
        this.structureTriggers.add(structure);
    }

    public void addMobTrigger(Mob mob) {
        this.mobTriggers.add(mob);
    }

    public void addRaidTrigger(Raid raid) {
        this.raidTriggers.add(raid);
    }

    public void addGenericTriggerName(String trigger) {
        this.allTriggers.add(trigger);
    }

    public void addMenuSong(String song) {
        this.menuSongs.add(song);
    }

    public UUID getPlayerUUID() {
        return this.playerUUID;
    }

    public String getCurrentSong() {
        return this.currentSong;
    }

    public void encode(ByteBuf buf) {
        buf.writeInt(this.channel.length());
        buf.writeCharSequence(this.channel, StandardCharsets.UTF_8);
        buf.writeInt(this.getSnowTriggers().size());
        for(Snow snow : this.getSnowTriggers()) snow.encode(buf);
        buf.writeInt(this.getHomeTriggers().size());
        for(Home home : this.getHomeTriggers()) home.encode(buf);
        buf.writeInt(this.getBiomeTriggers().size());
        for(Biome biome : this.getBiomeTriggers()) biome.encode(buf);
        buf.writeInt(this.getStructureTriggers().size());
        for(Structure structure : this.getStructureTriggers()) structure.encode(buf);
        buf.writeInt(this.getMobTriggers().size());
        for(Mob mob : this.getMobTriggers()) mob.encode(buf);
        buf.writeInt(this.getRaidTriggers().size());
        for(Raid raid : this.getRaidTriggers()) raid.encode(buf);
    }

    public static ServerChannelData decode(ByteBuf buf) {
        int channelNameLength = buf.readInt();
        int uuidLength = buf.readInt();
        int songNameLength = buf.readInt();
        ServerChannelData data = new ServerChannelData((String)buf.readCharSequence(channelNameLength, StandardCharsets.UTF_8), (String)buf.readCharSequence(uuidLength, StandardCharsets.UTF_8),(String)buf.readCharSequence(songNameLength, StandardCharsets.UTF_8));
        int i;
        int commandsSize = buf.readInt();
        if(commandsSize!=0) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            for(i=0;i<commandsSize;i++) {
                int commandLength = buf.readInt();
                server.getCommands().performCommand(server.createCommandSourceStack(), (String)buf.readCharSequence(commandLength, StandardCharsets.UTF_8));
            }
        }
        int menuSongsSize = buf.readInt();
        for(i=0;i<menuSongsSize;i++) {
            int songLength = buf.readInt();
            data.addMenuSong((String)buf.readCharSequence(songLength, StandardCharsets.UTF_8));
        }
        int triggerListSize = buf.readInt();
        for(i=0;i<triggerListSize;i++) {
            int triggerLength = buf.readInt();
            data.addGenericTriggerName((String)buf.readCharSequence(triggerLength, StandardCharsets.UTF_8));
        }
        int snowSize = buf.readInt();
        for(i=0;i<snowSize;i++) data.addSnowTrigger(Snow.decode());
        int homeSize = buf.readInt();
        for(i=0;i<homeSize;i++) data.addHomeTrigger(Home.decode(buf));
        int biomeSize = buf.readInt();
        for(i=0;i<biomeSize;i++) data.addBiomeTrigger(Biome.decode(buf));
        int structureSize = buf.readInt();
        for(i=0;i<structureSize;i++) data.addStructureTrigger(Structure.decode(buf));
        int mobSize = buf.readInt();
        for(i=0;i<mobSize;i++) data.addMobTrigger(Mob.decode(buf));
        int raidSize = buf.readInt();
        for(i=0;i<raidSize;i++) data.addRaidTrigger(Raid.decode(buf));
        return data;
    }

    public static class Home {
        private boolean pass = false;
        private final int range;

        public Home(int range) {
            this.range = range;
        }

        public int getRange() {
            return this.range;
        }

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public static Home decode(ByteBuf buf) {
            return new Home(buf.readInt());
        }

        public void encode(ByteBuf buf) {
            buf.writeBoolean(this.isActive());
        }
    }

    public static class Snow {
        private boolean pass = false;

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public static Snow decode() {
            return new Snow();
        }

        public void encode(ByteBuf buf) {
            buf.writeBoolean(this.isActive());
        }
    }

    public static class Biome {
        private boolean pass = false;
        private String currentBiome = null;
        private final String triggerID;
        private final String biome;
        private final String category;
        private final String rainType;
        private final float temperature;
        private final boolean cold;
        private final float rainfall;
        private final boolean togglerainfall;

        public Biome(String triggerID, String biome, String category, String rainType, float temperature, boolean cold, float rainfall, boolean togglerainfall) {
            this.triggerID = triggerID;
            this.biome = biome;
            this.category = category;
            this.rainType = rainType;
            this.temperature = temperature;
            this.cold = cold;
            this.rainfall = rainfall;
            this.togglerainfall = togglerainfall;
        }

        public String getTrigger() {
            return this.triggerID;
        }

        public String getBiome() {
            return this.biome;
        }

        public String getCategory() {
            return this.category;
        }

        public String getRainType() {
            return this.rainType;
        }

        public float getTemperature() {
            return this.temperature;
        }

        public boolean isCold() {
            return this.cold;
        }

        public float getRainfall() {
            return this.rainfall;
        }

        public boolean isTogglerainfall() {
            return this.togglerainfall;
        }

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public void setCurrentBiome(String currentBiome) {
            this.currentBiome = currentBiome;
        }

        public String getCurrentBiome() {
            return this.currentBiome;
        }

        public void encode(ByteBuf buf) {
            buf.writeInt(this.getTrigger().length());
            buf.writeCharSequence(this.getTrigger(), StandardCharsets.UTF_8);
            buf.writeInt(this.getCurrentBiome().length());
            buf.writeCharSequence(this.getCurrentBiome(), StandardCharsets.UTF_8);
            buf.writeBoolean(this.isActive());
        }

        public static Biome decode(ByteBuf buf) {
            int triggerLength = buf.readInt();
            int biomeLength = buf.readInt();
            int categoryLength = buf.readInt();
            int rainTypeLength = buf.readInt();
            return new Biome((String)buf.readCharSequence(triggerLength, StandardCharsets.UTF_8),(String)buf.readCharSequence(biomeLength, StandardCharsets.UTF_8),
                    (String)buf.readCharSequence(categoryLength, StandardCharsets.UTF_8),(String)buf.readCharSequence(rainTypeLength, StandardCharsets.UTF_8),
                    buf.readFloat(),buf.readBoolean(),buf.readFloat(),buf.readBoolean());
        }
    }

    public static class Structure {
        private boolean pass = false;
        private String currentStructure = null;
        private final String triggerID;
        private final String structure;

        public Structure(String triggerID, String structure) {
            this.triggerID = triggerID;
            this.structure = structure;
        }

        public String getTrigger() {
            return this.triggerID;
        }

        public String getStructure() {
            return this.structure;
        }

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public void setCurrentStructure(String currentStructure) {
            this.currentStructure = currentStructure;
        }

        public String getCurrentStructure() {
            return this.currentStructure;
        }

        public void encode(ByteBuf buf) {
            buf.writeInt(this.getTrigger().length());
            buf.writeCharSequence(this.getTrigger(), StandardCharsets.UTF_8);
            buf.writeBoolean(this.isActive());
        }

        public static Structure decode(ByteBuf buf) {
            int triggerLength = buf.readInt();
            int structureLength = buf.readInt();
            return new Structure((String)buf.readCharSequence(triggerLength, StandardCharsets.UTF_8),(String)buf.readCharSequence(structureLength, StandardCharsets.UTF_8));
        }
    }

    public static class Mob {
        private boolean pass = false;
        private boolean isVictory = false;
        private final String triggerID;
        private final String mobName;
        private final int range;
        private final boolean targetting;
        private final int targettingPercentage;
        private final int health;
        private final int healthPercentage;
        private final boolean victory;
        private final int victoryID;
        private final String infernal;
        private final int mobLevel;
        private final int victoryTimeout;
        private final String nbtKey;
        private final String champion;

        public Mob(String triggerID, String mobName, int range, boolean targetting, int targettingPercentage, int health, int healthPercentage, boolean victory, int victoryID, String infernal, int mobLevel, int victoryTimeout, String nbtKey, String champion) {
            this.triggerID = triggerID;
            this.mobName = mobName;
            this.range = range;
            this.targetting = targetting;
            this.targettingPercentage = targettingPercentage;
            this.health = health;
            this.healthPercentage = healthPercentage;
            this.victory = victory;
            this.victoryID = victoryID;
            this.infernal = infernal;
            this.mobLevel = mobLevel;
            this.victoryTimeout = victoryTimeout;
            this.nbtKey = nbtKey;
            this.champion = champion;
        }

        public String getTrigger() {
            return this.triggerID;
        }

        public String getName() {
            return this.mobName;
        }

        public int getRange() {
            return this.range;
        }

        public boolean getTargetting() {
            return this.targetting;
        }

        public int getTargettingPercentage() {
            return this.targettingPercentage;
        }

        public int getHealth() {
            return this.health;
        }

        public int getHealthPercentage() {
            return this.healthPercentage;
        }

        public boolean getVictory() {
            return this.victory;
        }

        public int getVictoryID() {
            return this.victoryID;
        }

        public String getInfernal() {
            return this.infernal;
        }

        public int getMobLevel() {
            return this.mobLevel;
        }

        public int getVictoryTimeout() {
            return this.victoryTimeout;
        }

        public String getNbtKey() {
            return this.nbtKey;
        }

        public String getChampion() {
            return this.champion;
        }

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public void setVictory(boolean victory) {
            this.isVictory = victory;
        }

        public boolean isVictory() {
            return this.isVictory;
        }

        public void encode(ByteBuf buf) {
            buf.writeInt(this.getTrigger().length());
            buf.writeCharSequence(this.getTrigger(), StandardCharsets.UTF_8);
            buf.writeBoolean(this.isActive());
            buf.writeBoolean(this.isVictory());
        }

        public static Mob decode(ByteBuf buf) {
            int triggerLength = buf.readInt();
            int nameLength = buf.readInt();
            int infernalLength = buf.readInt();
            int nbtLength = buf.readInt();
            int championLength = buf.readInt();
            return new Mob((String) buf.readCharSequence(triggerLength, StandardCharsets.UTF_8), (String) buf.readCharSequence(nameLength, StandardCharsets.UTF_8),
                    buf.readInt(), buf.readBoolean(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean(), buf.readInt(), (String) buf.readCharSequence(infernalLength, StandardCharsets.UTF_8),
                    buf.readInt(), buf.readInt(), (String) buf.readCharSequence(nbtLength, StandardCharsets.UTF_8), (String) buf.readCharSequence(championLength, StandardCharsets.UTF_8));
        }
    }

    public static class Raid {
        private boolean pass = false;
        private final String triggerID;
        private final int wave;

        public Raid(String triggerID, int wave) {
            this.triggerID = triggerID;
            this.wave = wave;
        }

        public String getTrigger() {
            return this.triggerID;
        }

        public int getWave() {
            return this.wave;
        }

        public void setActive(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
        }

        public void encode(ByteBuf buf) {
            buf.writeInt(this.getTrigger().length());
            buf.writeCharSequence(this.getTrigger(), StandardCharsets.UTF_8);
            buf.writeBoolean(this.isActive());
        }

        public static Raid decode(ByteBuf buf) {
            int triggerLength = buf.readInt();
            return new Raid((String)buf.readCharSequence(triggerLength, StandardCharsets.UTF_8), buf.readInt());
        }
    }
}
