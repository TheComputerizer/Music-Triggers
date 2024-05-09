package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelEventRunner;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Objects;

public abstract class GlobalEventRunner extends GlobalElement implements ChannelEventRunner {
    
    private String event;
    private String song;
    private int interval;
    private int start;
    private int end;
    private int timer;
    
    protected GlobalEventRunner(String name) {
        super(name);
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
        return false;
    }
    
    protected abstract ChannelAPI getChannelReference();
    
    @Override
    public boolean parse(Toml table) {
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
    
    @Override
    public boolean tick() {
        this.timer++;
        return (this.timer<=0 || this.timer%this.interval==0) &&
               this.timer>=this.start && (this.end<=0 || this.timer<this.end);
    }
}