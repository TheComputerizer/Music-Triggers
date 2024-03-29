package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI.State;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MessageTriggerStates<CTX> extends MessageAPI<CTX> { //TODO Combine this into a single packet for all channels

    private final ChannelAPI channel;
    private final Collection<StateSnapshot> snapshots;

    public MessageTriggerStates(ChannelAPI channel, Collection<TriggerAPI> triggers) {
        this.channel = channel;
        this.snapshots = StateSnapshot.of(triggers);
    }

    public MessageTriggerStates(ByteBuf buf) {
        String playerID = NetworkHelper.readString(buf);
        String channelName = NetworkHelper.readString(buf);
        this.channel = ChannelHelper.findChannel(playerID,channelName);
        this.snapshots = NetworkHelper.readCollection(buf,() -> {
            String name = NetworkHelper.readString(buf);
            String id = NetworkHelper.readString(buf);
            String state = NetworkHelper.readString(buf);
            return new StateSnapshot(TriggerHelper.decodeTrigger(this.channel,name,id),state);
        });
    }

    @Override
    public void encode(ByteBuf buf) {
        NetworkHelper.writeString(buf,this.channel.getHelper().getSyncID());
        NetworkHelper.writeString(buf,this.channel.getName());
        NetworkHelper.writeCollection(buf,this.snapshots,snapshot -> snapshot.trigger.encode(buf));
    }

    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        for(StateSnapshot snapshot : this.snapshots) snapshot.apply();
        return null;
    }

    private static class StateSnapshot {

        private static Collection<StateSnapshot> of(Collection<TriggerAPI> triggers) {
            List<StateSnapshot> snapshots = new ArrayList<>();
            for(TriggerAPI trigger : triggers) snapshots.add(new StateSnapshot(trigger,trigger.getState().name()));
            return snapshots;
        }

        private final TriggerAPI trigger;
        private final String state;
        private StateSnapshot(TriggerAPI trigger, String state) {
            this.trigger = trigger;
            this.state = state;
        }

        private void apply() {
            this.trigger.setState(State.get(this.state));
        }
    }
}