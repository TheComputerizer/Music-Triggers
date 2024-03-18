package mods.thecomputerizer.musictriggers.api.data.channel;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.HashMap;
import java.util.Map;

public abstract class ChannelEventRunner extends ParameterWrapper {

    private int timer;

    protected ChannelEventRunner(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public void activate() {
        if(canRun("ACTIVATE")) run();
    }

    public boolean canRun(String event) {
        return event.equals(getParameterAsString("event").toUpperCase());
    }

    @Override
    public void deactivate() {
        if(canRun("DEACTIVATE")) run();
    }

    @Override
    protected Map<String, Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"event",new ParameterString("ACTIVATE"));
        addParameter(map,"event_interval",new ParameterInt(0));
        initExtraParameters(map);
        return map;
    }

    @Override
    public void play() {
        if(canRun("PLAY")) run();
    }

    @Override
    public void playable() {
        if(canRun("PLAYABLE")) run();
    }

    @Override
    public void playing() {
        if(canRun("PLAYING")) tick();
    }

    @Override
    public void queue() {
        if(canRun("QUEUE")) run();
    }

    protected abstract void run();

    @Override
    public void stop() {
        if(canRun("STOP")) run();
    }

    @Override
    public void stopped() {
        if(canRun("STOPPED")) run();
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
        if(canRun("TICK_ACTIVE")) tick();
    }

    @Override
    public void tickPlayable() {
        if(canRun("TICK_PLAYABLE")) tick();
    }

    @Override
    public void unplayable() {
        if(canRun("UNPLAYABLE")) run();
    }
}
