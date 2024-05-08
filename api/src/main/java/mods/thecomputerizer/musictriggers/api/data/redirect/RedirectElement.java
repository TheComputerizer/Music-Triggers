package mods.thecomputerizer.musictriggers.api.data.redirect;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
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
    private String key;
    private String value;
    private boolean remote;

    public RedirectElement(ChannelAPI channel, String line) {
        super(channel,"redirect_element");
        this.valid = parse(line);
    }

    @Override
    public void close() {
        this.key = null;
        this.value = null;
    }
    
    @Override
    public String getName() {
        return this.key+" "+(this.remote ? "=" : "==")+" "+this.value;
    }
    
    @Override protected TableRef getReferenceData() {
        return null;
    }
    
    @Override protected String getSubTypeName() {
        return "Redirect";
    }
    
    @Override
    protected Class<? extends ChannelElement> getTypeClass() {
        return RedirectElement.class;
    }

    @Override
    public boolean isResource() {
        return !this.remote;
    }

    private boolean parse(String line) {
        if(line.startsWith("#") || !line.contains("=")) return false;
        this.key = line.substring(0,line.indexOf('=')-1);
        if(line.contains("==")) this.value = line.substring(line.indexOf('=')+2).trim();
        else {
            this.remote = true;
            this.value = line.substring(line.indexOf('=')+1).trim();
        }
        if(StringUtils.isBlank(this.key)) {
            logWarn("Skipping blank redirect name from line `{}`",line);
            return false;
        }
        if(StringUtils.isBlank(this.value)) {
            logWarn("Skipping blank redirect value from line `{}`",line);
            return false;
        }
        logInfo("Successfully stored {} location",this.remote ? "remote" : "resource");
        return true;
    }
}
