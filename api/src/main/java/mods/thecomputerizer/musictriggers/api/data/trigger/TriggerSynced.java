package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import java.util.Map;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.*;

public class TriggerSynced extends TriggerAPI {

    @Getter private final TriggerAPI reference;
    private State syncedState;

    public TriggerSynced(ChannelAPI channel, TriggerAPI reference) {
        super(channel,"synced");
        this.reference = reference;
        this.syncedState = reference.getState();
    }

    @Override
    public String getNameWithID() {
        return this.reference.getNameWithID();
    }

    @Override
    protected void initExtraParameters(Map<String,Parameter<?>> map) {}

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }

    @Override
    public boolean isPlayableContext(TriggerContextAPI<?,?> context) {
        return this.syncedState==PLAYABLE || this.syncedState==ACTIVE;
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        return this.reference.matches(trigger);
    }

    public void sync(State state) {
        this.syncedState = state;
    }
}