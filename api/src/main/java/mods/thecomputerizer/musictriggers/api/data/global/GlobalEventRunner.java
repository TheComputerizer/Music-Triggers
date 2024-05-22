package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventHandler;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventRunner;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.EVENT_RUNNER;

public abstract class GlobalEventRunner extends GlobalElement implements ChannelEventRunner, ChannelEventHandler {
    
    private String event;
    private String song;
    private int interval;
    private int start;
    private int end;
    private int timer;
    
    protected GlobalEventRunner(String name) {
        super(name);
    }
    
    @Override public void activate() {
        if(checkSide() && canRun("activate")) run();
    }
    
    public boolean canRun(String event) {
        if(event.equalsIgnoreCase(this.event)) {
            if(isServer()) return true;
            String song = getChannelReference().getPlayingSongName();
            return Objects.nonNull(song) ? song.equals(this.song) : "_".equals(this.song);
        }
        this.timer = 0;
        return false;
    }
    
    @Override
    public boolean checkSide() {
        ChannelAPI channel = getChannelReference();
        if(isClient()) return channel.isClientChannel();
        if(isServer()) return !channel.isClientChannel();
        logInfo("Wrong side");
        return false;
    }
    
    @Override public void deactivate() {
        if(checkSide() && canRun("deactivate")) run();
    }
    
    protected abstract ChannelAPI getChannelReference();
    
    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        for(ParameterRef<?> ref : EVENT_RUNNER.getParameters()) addParameter(map,ref.getName(),ref.toParameter());
    }
    
    @Override
    public boolean parse(Toml table) {
        if(super.parse(table)) {
            Toml event = table.getTable("event");
            if(Objects.nonNull(event)) {
                for(ParameterRef<?> ref : EVENT_RUNNER.getParameters()) {
                    String key = ref.getName();
                    if(event.hasEntry(key)) setParameterValue(key,event.getEntry(key).getValue(),getParameter(key));
                }
            }
            this.event = getParameterAsString("name");
            this.song = getParameterAsString("song");
            this.interval = getParameterAsInt("interval");
            this.start = getParameterAsInt("start");
            this.end = getParameterAsInt("end");
            return true;
        }
        return false;
    }
    
    @Override public void play() {
        if(checkSide() && canRun("play")) run();
    }
    
    @Override public void playable() {
        if(checkSide() && canRun("playable")) run();
    }
    
    @Override public void playing() {
        if(checkSide() && canRun("playing") && tick()) run();
    }
    
    @Override public void queue() {
        if(checkSide() && canRun("queue")) run();
    }
    
    @Override public void stop() {
        if(checkSide() && canRun("stop")) run();
    }
    
    @Override public void stopped() {
        if(checkSide() && canRun("stopped")) run();
    }
    
    @Override
    public boolean tick() {
        this.timer++;
        return (this.timer<=0 || this.timer%this.interval==0) &&
               this.timer>=this.start && (this.end<=0 || this.timer<this.end);
    }
    
    @Override public void tickActive() {
        if(checkSide() && canRun("tick_active") && tick()) run();
    }
    
    @Override public void tickPlayable() {
        if(checkSide() && canRun("tick_playable") && tick()) run();
    }
    
    @Override public void unplayable() {
        if(checkSide() && canRun("unplayable")) run();
    }
}