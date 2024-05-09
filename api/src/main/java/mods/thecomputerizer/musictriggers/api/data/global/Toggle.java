package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.*;

public class Toggle extends GlobalElement {

    private final ChannelHelper helper;
    private final Toml table;
    private final From from;
    private final To to;

    public Toggle(ChannelHelper helper, Toml table) {
        super("Toggle");
        this.helper = helper;
        this.table = table;
        this.from = new From(this);
        this.to = new To(this);
    }

    public void close() {
        this.from.close();
        this.to.close();
    }
    
    @Override protected TableRef getReferenceData() {
        return MTDataRef.TOGGLE;
    }
    
    @Override
    public Class<? extends ParameterWrapper> getTypeClass() {
        return Toggle.class;
    }
    
    public boolean parse() {
        return Objects.nonNull(this.table) && parse(this.table);
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
            super("Toggle_From");
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        private void close() {
            this.triggers.clear();
        }
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.FROM;
        }

        @Override
        public boolean parse(Toml table) {
            return super.parse(table) && parseTriggers(this.parent.helper,getParameterAsString("channel"),this.triggers);
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
            super("Toggle_To");
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        private void close() {
            this.triggers.clear();
        }
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.TO;
        }

        @Override
        public boolean parse(Toml table) {
            return super.parse(table) && parseTriggers(this.parent.helper,getParameterAsString("channel"),this.triggers);
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