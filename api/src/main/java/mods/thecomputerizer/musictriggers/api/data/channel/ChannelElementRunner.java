package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.MTDataRef.EVENT_RUNNER;

public abstract class ChannelElementRunner extends ChannelElement implements ChannelEventRunner {

    private String event;
    private String song;
    private int interval;
    private int start;
    private int end;
    private int timer;

    protected ChannelElementRunner(ChannelAPI channel, String name) {
        super(channel,name);
    }

    public boolean canRun(String event) {
        if(event.equalsIgnoreCase(this.event)) {
            if(isServer()) return true;
            if("_".equals(this.song)) return true;
            String song = this.channel.getPlayingSongName();
            return Objects.nonNull(song) && song.equals(this.song);
        }
        return false;
    }
    
    @Override
    public boolean checkSide() {
        if(isClient()) return this.channel.isClientChannel() && checkResource();
        if(isServer()) return !this.channel.isClientChannel();
        return false;
    }
    
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
    
    @Override
    public void run() {
        this.timer = 0;
    }

    @Override
    public boolean tick() {
        this.timer++;
        return (this.timer<=0 || this.timer%this.interval==0) &&
               this.timer>=this.start && (this.end<=0 || this.timer<this.end);
    }
}
