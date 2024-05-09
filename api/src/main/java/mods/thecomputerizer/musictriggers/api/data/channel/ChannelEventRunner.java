package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Map;

public abstract class ChannelEventRunner extends ChannelElement {

    private int timer;

    protected ChannelEventRunner(ChannelAPI channel, String name) {
        super(channel,name);
    }

    @Override
    public void activate() {
        if(canRun("activate")) run();
    }

    public boolean canRun(String event) {
        return this.channel.isClientChannel()==isClient() && checkResource() &&
               event.equalsIgnoreCase(getParameterAsString("event"));
    }

    @Override
    public void deactivate() {
        if(canRun("deactivate")) run();
    }
    
    protected void initExtraParameters(Map<String,Parameter<?>> map) {
        addParameter(map,"event",new ParameterString("activate"));
        addParameter(map,"event_interval",new ParameterInt(0));
    }
    
    public abstract boolean isClient();

    @Override
    public void play() {
        if(canRun("play")) run();
    }

    @Override
    public void playable() {
        if(canRun("playable")) run();
    }

    @Override
    public void playing() {
        if(canRun("playing")) tick();
    }

    @Override
    public void queue() {
        if(canRun("queue")) run();
    }

    protected abstract void run();

    @Override
    public void stop() {
        if(canRun("stop")) run();
    }

    @Override
    public void stopped() {
        if(canRun("stopped")) run();
    }

    private void tick() {
        this.timer--;
        if(this.timer<=0) {
            run();
            this.timer = getParameterAsInt("event_interval");
        }
    }

    @Override
    public void tickActive() {
        if(canRun("tick_active")) tick();
    }

    @Override
    public void tickPlayable() {
        if(canRun("tick_playable")) tick();
    }

    @Override
    public void unplayable() {
        if(canRun("unplayable")) run();
    }
}
