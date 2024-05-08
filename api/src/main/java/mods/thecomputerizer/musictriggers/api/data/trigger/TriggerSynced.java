package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State.*;

public class TriggerSynced extends TriggerAPI {

    @Getter private final TriggerAPI reference;
    private State syncedState;

    public TriggerSynced(ChannelAPI channel, TriggerAPI reference) {
        super(channel,"synced");
        this.reference = reference;
        this.syncedState = Objects.nonNull(reference.getState()) ? reference.getState() : IDLE;
    }

    @Override
    public String getNameWithID() {
        return this.reference.getNameWithID();
    }
    
    @Override
    public Parameter<?> getParameter(String name) {
        return this.reference.getParameter(name);
    }

    @Override
    public boolean isPlayableContext(TriggerContext context) {
        return this.syncedState==PLAYABLE || this.syncedState==ACTIVE;
    }

    @Override
    public boolean isSynced() {
        return true;
    }
    
    @Override
    public void setState(State state) {
        if(this.syncedState.isPlayable() && state.isPlayable()) this.syncedState=state;
    }

    @Override
    public boolean matches(TriggerAPI trigger) {
        return this.reference.matches(trigger);
    }

    public void sync(State state) {
        this.syncedState = state;
    }
}