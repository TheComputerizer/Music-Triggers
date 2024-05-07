package mods.thecomputerizer.musictriggers.api.data.redirect;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
public class RedirectElement extends ChannelElement {

    @Getter private static final List<String> headerLines = Arrays.asList("# Format this like name = url",
            "# If you are trying to redirect to an already registered resource location Format it like name == location instead",
            "# Any lines that begin with `#` or do not contain `=` will not be read in",
            "# Make sure each new entry is on a new line",
            "# Here are 2 examples:",
            "# thx = https://youtu.be/z3Q4WBpCXhs",
            "# title == minecraft:sounds/music/menu/menu1.ogg");

    private final boolean valid;
    private String name;
    private String value;
    private boolean remote;

    public RedirectElement(ChannelAPI channel, String line) {
        super(channel,"redirect_element");
        this.valid = parse(line);
    }
    
    public RedirectElement(ChannelAPI channel, ByteBuf buf) {
        super(channel,"redirect_element");
        this.valid = buf.readBoolean();
        this.name = NetworkHelper.readString(buf);
        this.value = NetworkHelper.readString(buf);
        this.remote = buf.readBoolean();
    }

    @Override
    public void close() {
        this.name = null;
        this.value = null;
    }

    @Override
    public boolean isResource() {
        return !this.remote;
    }

    private boolean parse(String line) {
        if(line.startsWith("#") || !line.contains("=")) return false;
        this.name = line.substring(0,line.indexOf('=')-1);
        if(line.contains("==")) this.value = line.substring(line.indexOf('=')+2).trim();
        else {
            this.remote = true;
            this.value = line.substring(line.indexOf('=')+1).trim();
        }
        if(StringUtils.isBlank(this.name)) {
            logWarn("Skipping blank redirect name from line `{}`",line);
            return false;
        }
        if(StringUtils.isBlank(this.value)) {
            logWarn("Skipping blank redirect value from line `{}`",line);
            return false;
        }
        logInfo("Successfully stored `{}` from {} in key `{}`",this.value,this.remote ? "remote source" :
                "resource",this.name);
        return true;
    }
    
    public void write(ByteBuf buf) {
        buf.writeBoolean(this.valid);
        NetworkHelper.writeString(buf,this.name);
        NetworkHelper.writeString(buf,this.value);
        buf.writeBoolean(this.remote);
    }
}
