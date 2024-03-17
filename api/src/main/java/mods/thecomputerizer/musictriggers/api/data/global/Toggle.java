package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.*;

public class Toggle extends GlobalElement {

    private final From from;
    private final To to;

    public Toggle() {
        this.from = new From();
        this.to = new To();
    }

    @Override
    public String getTypeName() {
        return "Toggle";
    }

    @Override
    protected void supplyParameters(Map<String, Parameter<?>> map) {
        map.put("play_once",new ParameterBoolean(false));
    }

    @Override
    public boolean parse(Table table) {
        return parseParameters(table);
    }

    @Override
    public boolean parse(Holder holder) {
        return false;
    }

    @Override
    public boolean verifyRequiredParameters() {
        return this.from.verifyRequiredParameters() && this.to.verifyRequiredParameters();
    }

    public static class From extends GlobalElement {

        private static final List<String> VALID_CONDITIONS = Arrays.asList("ACTIVE","PLAYABLE","TOGGLED");

        private final Set<TriggerAPI> triggers;

        public From() {
            this.triggers = new HashSet<>();
        }

        @Override
        public String getTypeName() {
            return "Toggle_From";
        }

        @Override
        public boolean parse(Table table) {
            if(!parseParameters(table)) return false;
            if(!TriggerHelper.findTriggers(this,getParameterAsString("channel"),this.triggers,table)) {
                logError("Failed to parse 1 or more triggers in `from` table");
                return false;
            }
            return true;
        }

        @Override
        public boolean parse(Holder holder) {
            return false;
        }

        @Override
        protected void supplyParameters(Map<String,Parameter<?>> map) {
            map.put("channel",new ParameterString("not_set"));
            map.put("condition",new ParameterString("ACTIVE"));
        }

        @Override
        public boolean verifyRequiredParameters() {
            if(Objects.isNull(ChannelHelper.findChannel(this,getParameterAsString("channel"))))
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

        private final Set<TriggerAPI> triggers;

        public To() {
            this.triggers = new HashSet<>();
        }

        @Override
        public String getTypeName() {
            return "Toggle_To";
        }

        @Override
        public boolean parse(Table table) {
            if(!parseParameters(table)) return false;
            if(!TriggerHelper.findTriggers(this,getParameterAsString("channel"),this.triggers,table)) {
                logError("Failed to parse 1 or more triggers in `to` table");
                return false;
            }
            return true;
        }

        @Override
        public boolean parse(Holder holder) {
            return false;
        }

        @Override
        protected void supplyParameters(Map<String,Parameter<?>> map) {
            map.put("channel",new ParameterString("not_set"));
            map.put("condition",new ParameterString("SWITCH"));
        }

        @Override
        public boolean verifyRequiredParameters() {
            if(Objects.isNull(ChannelHelper.findChannel(this,getParameterAsString("channel"))))
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