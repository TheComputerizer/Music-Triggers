package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.EVENT_RUNNER;

public interface ChannelEventRunner {
    
    boolean checkRun(String type);
    String getLogPrefix();
    boolean isClient();
    boolean isServer();
    void run();
    boolean tick();
    
    class EventInstance extends ParameterWrapper {
        
        @Getter protected final ChannelEventRunner parent;
        @Setter protected ChannelAPI channel;
        private String event;
        private String song;
        private int interval;
        private int start;
        private int end;
        private int timer;
        
        public EventInstance(ChannelAPI channel, ChannelEventRunner parent) {
            super("event");
            this.channel = channel;
            this.parent = parent;
        }
        
        public boolean canRun(String event) {
            if(event.equalsIgnoreCase(this.event)) {
                if(this.parent.isServer()) return true;
                if("_".equals(this.song)) return true;
                String song = this.channel.getPlayingSongName();
                return Objects.nonNull(song) && song.equals(this.song);
            }
            return false;
        }
        
        public boolean checkSide() {
            if(this.parent.isClient()) return this.channel.isClientChannel();
            if(this.parent.isServer()) return !this.channel.isClientChannel();
            logInfo("Wrong side");
            return false;
        }
        
        @Override protected String getLogPrefix() {
            return this.parent.getLogPrefix()+" Event Handler: ";
        }
        
        @Override public TableRef getReferenceData() {
            return EVENT_RUNNER;
        }
        
        @Override public Class<? extends ParameterWrapper> getTypeClass() {
            return EventInstance.class;
        }
        
        @Override public boolean parse(Toml table) {
            if(super.parse(table)) {
                this.event = getParameterAsString("name");
                this.song = getParameterAsString("song");
                this.interval = getParameterAsInt("interval");
                this.start = getParameterAsInt("start");
                this.end = getParameterAsInt("end");
                return true;
            }
            return false;
        }
        
        public void resetTimer() {
            this.timer = 0;
        }
        
        public boolean tick() {
            this.timer++;
            return (this.timer<=0 || this.interval==0 || this.timer%this.interval==0) &&
                   this.timer>=this.start && (this.end<=0 || this.timer<this.end);
        }
    }
}