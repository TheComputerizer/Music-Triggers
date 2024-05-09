package mods.thecomputerizer.musictriggers.api.data.jukebox;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelElement;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@Getter
public class RecordElement extends ChannelElement {

    @Getter private static final List<String> headerLines = Arrays.asList("# Format this like `name = key`",
            "# The key refers to a lang key in the format of record.musictriggers.key which ",
            "# determines the description of the registered disc",
            "# Any lines that begin with `#` or do not contain `=` will not be read in",
            "# Make sure each new entry is on a new line",
            "# Here is an example",
            "# song1 = dragon");

    private final boolean valid;
    private String name;
    private String key;

    public RecordElement(ChannelAPI channel, String line) {
        super(channel,"jukebox_element");
        this.valid = parse(line);
    }

    @Override
    public void close() {
        this.name = null;
        this.key = null;
    }
    
    @Override
    public String getName() {
        return this.key+" = "+this.name;
    }

    @Override
    public boolean isResource() {
        return true;
    }

    private boolean parse(String line) {
        if(line.startsWith("#") || !line.contains("=") || line.contains("==")) return false;
        this.name = line.substring(0,line.indexOf('=')-1);
        this.key = line.substring(line.indexOf('=')+1).trim();
        if(StringUtils.isBlank(this.name)) {
            logWarn("Skipping blank redirect name from line `{}`",line);
            return false;
        }
        if(StringUtils.isBlank(this.key)) {
            logWarn("Skipping blank redirect key from line `{}`",line);
            return false;
        }
        logInfo("Successfully registed `{}` as a record from key `{}`",this.name,this.key);
        return true;
    }
    
    @Override protected TableRef getReferenceData() {
        return null;
    }
    
    @Override protected String getSubTypeName() {
        return "Jukebox";
    }
    
    @Override public Class<? extends ChannelElement> getTypeClass() {
        return RecordElement.class;
    }
}