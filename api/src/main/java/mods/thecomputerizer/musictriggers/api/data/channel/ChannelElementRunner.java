package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.client.gui.MTScreenInfo;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class ChannelElementRunner extends ChannelElement implements ChannelEventRunner {

    protected EventInstance instance;

    protected ChannelElementRunner(ChannelAPI channel, String name) {
        super(channel,name);
    }
    
    @Override public void activate() {
        if(checkRun("activate")) run();
    }
    
    public boolean checkRun(String type) {
        return Objects.nonNull(this.instance) && this.instance.checkSide() && this.instance.canRun(type);
    }
    
    @Override public void deactivate() {
        if(checkRun("deactivate")) run();
    }
    
    @Override public Collection<DataLink> getChildWrappers(MTScreenInfo parent) {
        if(Objects.isNull(this.instance)) this.instance = new EventInstance(this.channel,this);
        DataLink link = this.instance.getLink();
        link.setType(parent.next("event",link));
        return Collections.singletonList(link);
    }
    
    @Override public String getLogPrefix() {
        return super.getLogPrefix();
    }
    
    @Override
    public boolean parse(Toml table) {
        if(super.parse(table)) {
            if(table.hasTable("event")) {
                EventInstance instance = new EventInstance(this.channel,this);
                if(instance.parse(table.getTable("event"))) this.instance = instance;
                else logError("Failed to parse event instance");
            }
            return true;
        }
        return false;
    }
    
    @Override public void play(boolean unpaused) {
        if(checkRun("play")) run();
    }
    
    @Override public void playable() {
        if(checkRun("playable")) run();
    }
    
    @Override public void playing(boolean unpaused) {
        if(checkRun("playing")) run();
    }
    
    @Override public void queue() {
        if(checkRun("queue")) run();
    }
    
    @Override
    public void run() {
        this.instance.resetTimer();
    }
    
    @Override public void stop() {
        if(checkRun("stop")) run();
    }
    
    @Override public void stopped() {
        if(checkRun("stopped")) run();
    }
    
    @Override
    public boolean tick() {
        return this.instance.tick();
    }
    
    @Override public void tickActive(boolean unpaused) {
        if(checkRun("tick_active")) run();
    }
    
    @Override public void tickPlayable(boolean unpaused) {
        if(checkRun("tick_playable")) run();
    }
    
    @Override public void unplayable() {
        if(checkRun("unplayable")) run();
    }
}
