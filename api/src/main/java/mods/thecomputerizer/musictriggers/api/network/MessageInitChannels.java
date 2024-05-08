package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.SneakyThrows;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.iterator.IterableHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlParsingException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
public class MessageInitChannels<CTX> extends MessageAPI<CTX> {
    
    final boolean client;
    final String uuid;
    final Toml global;
    final Toml toggles;
    final Map<String,ChannelMessage> channels;
    
    public MessageInitChannels(Toml global, Toml toggles, ChannelHelper helper) {
        this.client = helper.isClient();
        this.uuid = helper.getPlayerID();
        this.global = global;
        this.toggles = toggles;
        this.channels = getChannelMap(helper);
        helper.setSyncable(true);
    }
    
    @SneakyThrows
    public MessageInitChannels(ByteBuf buf) {
        this.client = !buf.readBoolean();
        this.uuid = NetworkHelper.readString(buf);
        this.global = Toml.readBuf(buf);
        this.toggles = Toml.readBuf(buf);
        this.channels = NetworkHelper.readMapEntries(buf,() -> {
            String key = NetworkHelper.readString(buf);
            return IterableHelper.getMapEntry(key, new ChannelMessage(buf));
        });
    }
    
    Map<String,ChannelMessage> getChannelMap(ChannelHelper helper) {
        Map<String,ChannelMessage> map = new HashMap<>();
        for(ChannelAPI channel : helper.getChannels().values()) map.put(channel.getName(),new ChannelMessage(channel));
        return map;
    }
    
    @Override
    public void encode(ByteBuf buf) {
        buf.writeBoolean(this.client);
        NetworkHelper.writeString(buf,this.uuid);
        this.global.write(buf);
        this.toggles.write(buf);
        NetworkHelper.writeMap(buf,this.channels,name -> NetworkHelper.writeString(buf,name),channel -> channel.write(buf));
    }
    
    @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        ChannelHelper.loadMessage(this);
        return null;
    }
    
    @Getter
    public static class ChannelMessage {
        
        final Map<String,Toml> tomls;
        final Set<String> redirects;
        final Set<String> records;
        
        ChannelMessage(ChannelAPI channel) {
            this.tomls = channel.getSourceMap();
            this.redirects = channel.getRedirectLines();
            this.records = channel.getRecordLines();
        }
        
        ChannelMessage(ByteBuf buf) {
            this.tomls = NetworkHelper.readMap(buf,() -> NetworkHelper.readString(buf),() -> {
                try {
                    return Toml.readBuf(buf);
                } catch(TomlParsingException ex) {
                    MTRef.logError("Failed to read TOML from buffer!",ex);
                    return Toml.getEmpty();
                }
            });
            this.redirects = NetworkHelper.readSet(buf,() -> NetworkHelper.readString(buf));
            this.records = NetworkHelper.readSet(buf,() -> NetworkHelper.readString(buf));
        }
        
        void write(ByteBuf buf) {
            NetworkHelper.writeMap(buf,this.tomls,key -> NetworkHelper.writeString(buf,key),toml -> toml.write(buf));
            NetworkHelper.writeSet(buf,this.redirects,redirect -> NetworkHelper.writeString(buf,redirect));
            NetworkHelper.writeSet(buf,this.records,record -> NetworkHelper.writeString(buf,record));
        }
    }
}