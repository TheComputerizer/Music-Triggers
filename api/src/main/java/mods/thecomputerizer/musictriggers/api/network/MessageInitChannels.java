package mods.thecomputerizer.musictriggers.api.network;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.SneakyThrows;
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
public class MessageInitChannels<CTX> extends PlayerMessage<CTX> {
    
    final boolean client;
    final Toml global;
    final Toml toggles;
    final Map<String,ChannelMessage> channels;
    
    public MessageInitChannels(Toml global, Toml toggles, ChannelHelper helper) {
        super(helper.getPlayerID());
        this.client = helper.isClient();
        this.global = global;
        this.toggles = toggles;
        this.channels = getChannelMap(helper);
        ChannelHelper.logGlobalInfo("Constructued init message");
    }
    
    @SneakyThrows
    public MessageInitChannels(ByteBuf buf) {
        super(buf);
        this.client = !buf.readBoolean();
        this.global = Toml.readBuf(buf);
        this.toggles = Toml.readBuf(buf);
        this.channels = NetworkHelper.readMapEntries(buf,() -> {
            String key = NetworkHelper.readString(buf);
            return IterableHelper.getMapEntry(key,new ChannelMessage(buf));
        });
        ChannelHelper.logGlobalInfo("Decodeded init message on the {} side",this.client ? "client" : "server");
    }
    
    @Override
    public void encode(ByteBuf buf) {
        super.encode(buf);
        buf.writeBoolean(this.client);
        this.global.write(buf);
        this.toggles.write(buf);
        NetworkHelper.writeMap(buf,this.channels,name -> NetworkHelper.writeString(buf,name),channel -> channel.write(buf));
    }
    
    Map<String,ChannelMessage> getChannelMap(ChannelHelper helper) {
        Map<String,ChannelMessage> map = new HashMap<>();
        for(ChannelAPI channel : helper.getChannels().values()) {
            String name = channel.getName();
            if(!"jukebox".equals(name) && !"preview".equals(name)) map.put(name,new ChannelMessage(channel));
        }
        return map;
    }
    
    @SuppressWarnings("unchecked") @Override
    public MessageAPI<CTX> handle(CTX ctx) {
        return (MessageAPI<CTX>)ChannelHelper.loadMessage(this);
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
                    ChannelHelper.logGlobalError("Failed to read TOML from buffer!",ex);
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