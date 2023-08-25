package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.config.ConfigJukebox;
import mods.thecomputerizer.musictriggers.config.ConfigRedirect;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Level;

import java.io.IOException;

public class ServerChannel {

    private final Table info;
    private final Holder main;
    private final Holder transitions;
    private final Holder commands;
    private final Holder toggles;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;

    private static Holder getConfigHolder(Table info, ResourceManager manager, String type) throws IOException {
        String path = "config/"+info.getValOrDefault(type,info.getName()+"/"+type)+".toml";
        return TomlUtil.readFully(manager.getResource(Constants.res(path)).getInputStream());
    }

    private static ConfigRedirect getRedirect(Table info, ResourceManager manager) throws IOException {
        String path = "config/"+info.getValOrDefault("redirect",info.getName()+"/redirect")+".txt";
        return new ConfigRedirect(manager, Constants.res(path),info.getName());
    }

    private static ConfigJukebox getJukebox(Table info, ResourceManager manager) throws IOException {
        String path = "config/"+info.getValOrDefault("jukebox",info.getName()+"/jukebox")+".txt";
        return new ConfigJukebox(manager, Constants.res(path),info.getName());
    }

    public ServerChannel(Table info, ResourceManager manager) throws IOException {
        this.info = info;
        this.main = getConfigHolder(info,manager,"main");
        this.transitions = getConfigHolder(info,manager,"transitions");
        this.commands = getConfigHolder(info,manager,"commands");
        this.toggles = getConfigHolder(info,manager,"toggles");
        this.redirect = getRedirect(info,manager);
        this.jukebox = getJukebox(info,manager);
        MusicTriggers.logExternally(Level.INFO,"Channel[{}] -Successfully registered datapack channel!",info.getName());
    }

    public void encode(FriendlyByteBuf buf) {
        this.info.write(buf);
        this.main.encode(buf,false,false);
        this.transitions.encode(buf,false,false);
        this.commands.encode(buf,false,false);
        this.toggles.encode(buf,false,false);
        NetworkUtil.writeGenericMap(buf,this.redirect.urlMap,NetworkUtil::writeString,NetworkUtil::writeString);
        NetworkUtil.writeGenericMap(buf,this.redirect.resourceLocationMap,NetworkUtil::writeString,
                (buf1,resource) -> NetworkUtil.writeString(buf1,resource.toString()));
        NetworkUtil.writeGenericMap(buf,this.jukebox.recordMap,NetworkUtil::writeString,NetworkUtil::writeString);
    }

    public String getName() {
        return this.info.getName();
    }

    @Environment(EnvType.CLIENT)
    public ServerChannel(FriendlyByteBuf buf) {
        this.info = (Table)TomlPart.getByID(NetworkUtil.readString(buf)).decode(buf,null);
        this.main = Holder.decoded(buf);
        this.transitions = Holder.decoded(buf);
        this.commands = Holder.decoded(buf);
        this.toggles = Holder.decoded(buf);
        this.redirect = new ConfigRedirect(buf);
        this.jukebox = new ConfigJukebox(buf);
    }

    @Environment(EnvType.CLIENT)
    public Channel convertToClient() throws IOException {
        return new Channel(this.info,this.main,this.transitions,this.commands,this.toggles,this.redirect,
                this.jukebox);
    }
}