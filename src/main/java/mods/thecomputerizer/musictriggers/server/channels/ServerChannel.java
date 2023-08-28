package mods.thecomputerizer.musictriggers.server.channels;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.config.ConfigJukebox;
import mods.thecomputerizer.musictriggers.config.ConfigRedirect;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.TomlPart;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class ServerChannel {

    private final Table info;
    private final Holder main;
    private final Holder transitions;
    private final Holder commands;
    private final Holder toggles;
    private final ConfigRedirect redirect;
    private final ConfigJukebox jukebox;

    private static Holder getConfigHolder(Table info, String type) throws IOException {
        File file = MusicTriggers.configFile(info.getValOrDefault(
                type,info.getName()+"/"+type), "toml",true);
        return TomlUtil.readFully(file);
    }

    private static ConfigRedirect getRedirect(Table info) {
        File file = MusicTriggers.configFile(info.getValOrDefault(
                "redirect",info.getName()+"/redirect"), "txt",true);
        return file.exists() ? new ConfigRedirect(true,file,info.getName()) : null;
    }

    private static ConfigJukebox getJukebox(Table info) {
        File file = MusicTriggers.configFile(info.getValOrDefault(
                "jukebox",info.getName()+"/jukebox"), "txt",true);
        return file.exists() ? new ConfigJukebox(true,file,info.getName()) : null;
    }

    public ServerChannel(Table info) throws IOException {
        this.info = info;
        this.main = getConfigHolder(info,"main");
        this.transitions = getConfigHolder(info,"transitions");
        this.commands = getConfigHolder(info,"commands");
        this.toggles = getConfigHolder(info,"toggles");
        this.redirect = getRedirect(info);
        this.jukebox = getJukebox(info);
        MusicTriggers.logExternally(Level.INFO,"Channel[{}] - Successfully registered server channel!",info.getName());
    }

    public boolean isValid() {
        return Objects.nonNull(this.main) && Objects.nonNull(this.transitions) && Objects.nonNull(this.commands) &&
                Objects.nonNull(this.toggles);
    }

    public void encode(ByteBuf buf) {
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

    @SideOnly(Side.CLIENT)
    public ServerChannel(ByteBuf buf) {
        this.info = (Table)TomlPart.getByID(NetworkUtil.readString(buf)).decode(buf,null);
        this.main = Holder.decoded(buf);
        this.transitions = Holder.decoded(buf);
        this.commands = Holder.decoded(buf);
        this.toggles = Holder.decoded(buf);
        this.redirect = new ConfigRedirect(buf,this.info.getName());
        this.jukebox = new ConfigJukebox(buf);
    }

    @SideOnly(Side.CLIENT)
    public Channel convertToClient() throws IOException {
        return new Channel(this.info,this.main,this.transitions,this.commands,this.toggles,this.redirect,
                this.jukebox);
    }
}
