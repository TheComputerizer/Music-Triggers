package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioHelper;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.server.ChannelServer;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;

public class MessageCurrentSong<CTX> extends ChannelHelperMessage<CTX> {
    
    private final ChannelAPI channel;
    private final AudioRef song;
    
    public MessageCurrentSong(ChannelHelper helper, ChannelAPI channel, AudioRef song) {
        super(helper);
        this.channel = channel;
        this.song = song;
    }
    
    public MessageCurrentSong(ByteBuf buf) {
        super(buf);
        this.channel = this.helper.decodeChannel(buf);
        this.song = AudioHelper.decodeAudio(this.channel,buf);
    }
    
    @Override public void encode(ByteBuf buf) {
        super.encode(buf);
        this.channel.encode(buf);
        this.song.encode(buf);
    }
    
    @Override public MessageAPI<CTX> handle(CTX ctx) {
        if(this.channel instanceof ChannelServer) {
            ((ChannelServer)this.channel).setCurrentSong(this.song);
        } else MTRef.logError("Tried to handle MessageCurrentSong on the client side which shouldn't be possible??");
        return null;
    }
}