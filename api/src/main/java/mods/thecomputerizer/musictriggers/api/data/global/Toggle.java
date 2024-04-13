package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.*;

public class Toggle extends GlobalElement {

    private final ChannelHelper helper;
    private final Table table;
    private final From from;
    private final To to;

    public Toggle(ChannelHelper helper, Table table) {
        this.helper = helper;
        this.table = table;
        this.from = new From(this);
        this.to = new To(this);
    }

    public void close() {
        this.from.close();
        this.to.close();
    }

    @Override
    public String getTypeName() {
        return "Toggle";
    }

    public boolean parse() {
        return Objects.nonNull(this.table) && parse(this.table);
    }

    @Override
    protected void supplyParameters(Map<String, Parameter<?>> map) {
        map.put("play_once",new ParameterBoolean(false));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return this.from.verifyRequiredParameters() && this.to.verifyRequiredParameters();
    }

    public static class From extends GlobalElement {

        private static final List<String> VALID_CONDITIONS = Arrays.asList("ACTIVE","PLAYABLE","TOGGLED");

        private final Toggle parent;
        private final Set<TriggerAPI> triggers;

        public From(Toggle parent) {
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        private void close() {
            this.triggers.clear();
        }

        @Override
        public String getTypeName() {
            return "Toggle_From";
        }

        @Override
        public boolean parse(Table table) {
            if(!super.parse(table)) return false;
            if(!TriggerHelper.findTriggers(this.parent.helper,this,getParameterAsString("channel"),this.triggers,table)) {
                logError("Failed to parse 1 or more triggers in `from` table");
                return false;
            }
            return true;
        }

        @Override
        protected void supplyParameters(Map<String,Parameter<?>> map) {
            map.put("channel",new ParameterString("not_set"));
            map.put("condition",new ParameterString("ACTIVE"));
        }

        @Override
        public boolean verifyRequiredParameters() {
            if(Objects.isNull(this.parent.helper.findChannel(this,getParameterAsString("channel"))))
                return false;
            String condition = getParameterAsString("condition");
            if(!VALID_CONDITIONS.contains(condition)) {
                logWarn("Invalid condition `{}`! Accepted conditions are as follows: `{}`",condition,VALID_CONDITIONS);
                return false;
            }
            return true;
        }
    }

    public static class To extends GlobalElement {

        private static final List<String> VALID_CONDITIONS = Arrays.asList("TRUE","FALSE","SWITCH");

        private final Toggle parent;
        private final Set<TriggerAPI> triggers;

        public To(Toggle parent) {
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        private void close() {
            this.triggers.clear();
        }

        @Override
        public String getTypeName() {
            return "Toggle_To";
        }

        @Override
        public boolean parse(Table table) {
            if(!super.parse(table)) return false;
            if(!TriggerHelper.findTriggers(this.parent.helper,this,getParameterAsString("channel"),this.triggers,table)) {
                logError("Failed to parse 1 or more triggers in `to` table");
                return false;
            }
            return true;
        }

        @Override
        protected void supplyParameters(Map<String,Parameter<?>> map) {
            map.put("channel",new ParameterString("not_set"));
            map.put("condition",new ParameterString("SWITCH"));
        }

        @Override
        public boolean verifyRequiredParameters() {
            if(Objects.isNull(this.parent.helper.findChannel(this,getParameterAsString("channel"))))
                return false;
            String condition = getParameterAsString("condition");
            if(!VALID_CONDITIONS.contains(condition)) {
                logWarn("Invalid condition `{}`! Accepted conditions are as follows: `{}`",condition,VALID_CONDITIONS);
                return false;
            }
            return true;
        }
    }
}