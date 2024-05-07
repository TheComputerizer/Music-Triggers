package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.theimpossiblelibrary.api.iterator.IterableHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MessageInitChannels<CTX> extends MessageAPI<CTX> {
    
    final String uuid;
    final Toml global;
    final Toml toggles;
    final Map<String,ChannelMessage> channels;
    
    public MessageInitChannels(Toml global, Toml toggles, ChannelHelper helper) {
        this.uuid = helper.getPlayerID();
        this.global = global;
        this.toggles = toggles;
        this.channels = getChannelMap(helper);
    }
    
    public MessageInitChannels(ByteBuf buf) {
        this.uuid = NetworkHelper.readString(buf);
        this.global = Toml.readBuf(buf);
        this.toggles = Toml.readBuf(buf);
        this.channels = NetworkHelper.readMapEntries(buf,() -> {
            String key = NetworkHelper.readString(buf);
            ChannelHelper helper = ChannelHelper.getServerHelper(key);
            ChannelAPI channel = helper.addEmptyChannel(key,this.global.getTable("channels").getTable(key));
            return IterableHelper.getMapEntry(key,new ChannelMessage(channel,buf));
        });
    }
    
    Map<String,ChannelMessage> getChannelMap(ChannelHelper helper) {
        Map<String,ChannelMessage> map = new HashMap<>();
        for(ChannelAPI channel : helper.getChannels().values()) map.put(channel.getName(),new ChannelMessage(channel));
        return map;
    }
    
    @Override
    public void encode(ByteBuf buf) {
        this.global.write(buf);
        this.toggles.write(buf);
        NetworkHelper.writeMap(buf,this.channels,name -> NetworkHelper.writeString(buf,name),
                               channel -> channel.write(buf));
    }
    
    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        return null;
    }
    
    static class ChannelMessage {
        
        final Map<String,Toml> tomls;
        final Set<RedirectElement> redirects;
        final Set<RecordElement> records;
        
        ChannelMessage(ChannelAPI channel) {
            this.tomls = channel.getSourceMap();
            this.redirects = channel.getData().getRedirects();
            this.records = channel.getData().getRecords();
        }
        
        ChannelMessage(ChannelAPI channel, ByteBuf buf) {
            this.tomls = NetworkHelper.readMap(buf,() -> NetworkHelper.readString(buf),() -> Toml.readBuf(buf));
            this.redirects = NetworkHelper.readSet(buf,() -> new RedirectElement(channel,buf));
            this.records = NetworkHelper.readSet(buf,() -> new RecordElement(channel,buf));
        }
        
        void write(ByteBuf buf) {
            NetworkHelper.writeMap(buf,this.tomls,key -> NetworkHelper.writeString(buf,key),toml -> toml.write(buf));
            NetworkHelper.writeSet(buf,this.redirects,redirect -> redirect.write(buf));
            NetworkHelper.writeSet(buf,this.records,record -> record.write(buf));
        }
    }
}