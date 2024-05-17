package mods.thecomputerizer.musictriggers.api.data.global;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.*;

public class Toggle extends GlobalElement {

    private final ChannelHelper helper;
    private final Toml table;
    private final List<From> fromThese;
    private final List<To> toThese;

    public Toggle(ChannelHelper helper, Toml table) {
        super("Toggle");
        this.helper = helper;
        this.table = table;
        this.fromThese = new ArrayList<>();
        this.toThese = new ArrayList<>();
    }

    public void close() {
        this.fromThese.forEach(From::close);
        this.fromThese.clear();
        this.toThese.forEach(To::close);
        this.toThese.clear();
    }
    
    @Override protected TableRef getReferenceData() {
        return MTDataRef.TOGGLE;
    }
    
    @Override
    public Class<? extends ParameterWrapper> getTypeClass() {
        return Toggle.class;
    }
    
    public boolean parse() {
        if(Objects.nonNull(this.table)) {
            if(this.table.hasTable("from")) {
                for(Toml table : this.table.getTableArray("from")) {
                    From from = new From(this);
                    if(from.parse(table)) this.fromThese.add(from);
                }
            }
            if(this.table.hasTable("to")) {
                for(Toml table : this.table.getTableArray("to")) {
                    To to = new To(this);
                    if(to.parse(table)) this.toThese.add(to);
                }
            }
            return parse(this.table);
        }
        logError("Failed to parse missing table");
        return false;
    }
    
    public void run() {
        logDebug("Running toggle!!!!!");
        this.toThese.forEach(To::run);
    }

    @Override
    public boolean verifyRequiredParameters() {
        int errors = 0;
        for(From from : this.fromThese) if(!from.verifyRequiredParameters()) errors++;
        if(errors==this.fromThese.size()) {
            logError("At least 1 `from` table is required to be parsed successfully");
            return false;
        }
        errors = 0;
        for(To to : this.toThese) if(!to.verifyRequiredParameters()) errors++;
        if(errors==this.toThese.size()) {
            logError("At least 1 `to` table is required to be parsed successfully");
            return false;
        }
        this.fromThese.forEach(from -> from.channel.getData().addActiveTriggers(from,from.getTriggers(),true));
        return true;
    }

    public static class From extends GlobalEventRunner {

        private final Toggle parent;
        @Getter private final Set<TriggerAPI> triggers;
        private ChannelAPI channel;

        public From(Toggle parent) {
            super("Toggle_From");
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        public void close() {
            this.triggers.clear();
        }
        
        @Override protected ChannelAPI getChannelReference() {
            return this.channel;
        }
        
        @Override public String getName() {
            StringJoiner joiner = new StringJoiner("+");
            this.triggers.forEach(trigger -> joiner.add(trigger.getNameWithID()));
            return String.format("From(%1$s) = %2$s",this.channel.getName(),joiner);
        }
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.FROM;
        }
        
        @Override public String getLogPrefix() {
            return "Toggle";
        }
        
        @Override public boolean isClient() {
            return true;
        }
        
        @Override public boolean isServer() { //TODO Check if these should run on both sides
            return false;
        }
        
        @Override
        public boolean parse(Toml table) {
            return super.parse(table) && parseTriggers(this.channel,this.triggers);
        }
        
        @Override public void run() {
            this.parent.run();
        }
        
        @Override
        public boolean verifyRequiredParameters() {
            this.channel = this.parent.helper.findChannel(this,getParameterAsString("channel"));
            return Objects.nonNull(this.channel);
        }
    }

    public static class To extends GlobalElement {

        private static final List<String> VALID_CONDITIONS = Arrays.asList("true","false","switch");

        private final Toggle parent;
        @Getter private final Set<TriggerAPI> triggers;
        private ChannelAPI channel;
        private String condition;

        public To(Toggle parent) {
            super("Toggle_To");
            this.parent = parent;
            this.triggers = new HashSet<>();
        }

        private void close() {
            this.triggers.clear();
        }
        
        @Override public String getName() {
            StringJoiner joiner = new StringJoiner("+");
            this.triggers.forEach(trigger -> joiner.add(trigger.getNameWithID()));
            return String.format("To(%1$s) = %2$s",this.channel.getName(),this.triggers.isEmpty() ? "?" : joiner);
        }
        
        @Override protected TableRef getReferenceData() {
            return MTDataRef.TO;
        }
        
        @Override public String getLogPrefix() {
            return "Toggle";
        }

        @Override
        public boolean parse(Toml table) {
            return super.parse(table) && parseTriggers(false,this.channel,this.triggers);
        }
        
        public void run() {
            if(this.condition.equals("switch")) {
                if(this.triggers.isEmpty()) this.channel.setEnabled(!this.channel.isEnabled());
                this.triggers.forEach(TriggerAPI::switchToggle);
            }
            else {
                boolean on = Boolean.parseBoolean(this.condition);
                if(this.triggers.isEmpty()) this.channel.setEnabled(on);
                else this.triggers.forEach(trigger -> trigger.setToggle(on));
            }
        }

        @Override
        public boolean verifyRequiredParameters() {
            this.channel = this.parent.helper.findChannel(this,getParameterAsString("channel"));
            if(Objects.isNull(this.channel)) return false;
            String condition = getParameterAsString("condition");
            if(!VALID_CONDITIONS.contains(condition)) {
                logError("Could not match condition {} against the accepted conditions {}",condition,VALID_CONDITIONS);
                return false;
            }
            this.condition = condition;
            return true;
        }
    }
}