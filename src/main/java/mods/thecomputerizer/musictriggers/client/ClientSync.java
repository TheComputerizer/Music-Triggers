package mods.thecomputerizer.musictriggers.client;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ClientSync {

    private final String channel;
    private Home homeResult;
    private final HashMap<String,Structure> structureResults;
    private final HashMap<String,Mob> mobResults;

    public ClientSync(String channel) {
        this.channel = channel;
        this.homeResult = new Home(false);
        this.structureResults = new HashMap<>();
        this.mobResults = new HashMap<>();
    }

    public ClientSync(ByteBuf buf) {
        int i;
        this.channel = (String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8);
        int homeSize = buf.readInt();
        for(i=0;i<homeSize;i++) homeResult = new Home(buf.readBoolean());
        this.structureResults = new HashMap<>();
        int structureSize = buf.readInt();
        for(i=0;i<structureSize;i++) structureResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Structure(buf.readBoolean()));
        this.mobResults = new HashMap<>();
        int mobSize = buf.readInt();
        for(i=0;i<mobSize;i++) mobResults.put((String) buf.readCharSequence(buf.readInt(), StandardCharsets.UTF_8), new Mob(buf.readBoolean(), buf.readBoolean()));
    }

    public String getChannel() {
        return this.channel;
    }

    public boolean isHomeTriggerActive() {
        return this.homeResult.isActive();
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

    public static class Home {
        private final boolean pass;

        public Home(boolean pass) {
            this.pass = pass;
        }

        public boolean isActive() {
            return this.pass;
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
}
