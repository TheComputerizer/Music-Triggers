package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;

public abstract class TriggerContextClient<PLAYER,WORLD> extends TriggerContextAPI<PLAYER,WORLD> {

    protected TriggerContextClient(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public boolean isActiveBiome(TriggerBiome trigger) {
        return false;
    }

    @Override
    public boolean isActiveHome(int range, float yRatio) {
        return false;
    }

    @Override
    public boolean isActiveMob(TriggerMob<?> trigger) {
        return false;
    }

    @Override
    public boolean isActivePVP() {
        return false;
    }

    @Override
    public boolean isActiveRaid(int wave) {
        return false;
    }

    @Override
    public boolean isActiveSnowing() {
        return false;
    }

    @Override
    public boolean isActiveStructure(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveVictory(int timeout) {
        return false;
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
