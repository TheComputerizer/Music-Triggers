package mods.thecomputerizer.musictriggers.config;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Universal;
import mods.thecomputerizer.musictriggers.client.gui.instance.Main;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.*;


public class ConfigMain {
    private final File file;
    public final Map<TomlHolders.Type, Audio> parsedFile = new HashMap<>();
    private final Map<String, MutableInt> doubleBracketedSongs = new HashMap<>();
    public Universal universalParameters;

    public Main copyToGui(String channelName) {
        return new Main(this.file,channelName,MusicTriggers.clone(this.universalParameters),
                MusicTriggers.clone(this.parsedFile));
    }

    public ConfigMain(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.universalParameters = null;
    }

    public List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding the main config file");
    }

    public void parse(String channel) {
        MusicTriggers.logExternally(Level.INFO,"Reading main config file");
        Toml toml = new Toml().read(this.file);
        for (TomlHolders.Type type : TomlHolders.readCompleteToml(this.file)) {
            if(type instanceof TomlHolders.Table) {
                TomlHolders.Table table = (TomlHolders.Table)type;
                String songName = table.getName();
                if (songName.matches("universal"))
                    this.universalParameters = new Universal(toml.getTable("universal"));
                else {
                    try {
                        Audio audio = null;
                        if (toml.containsTable(songName))
                            audio = new Audio(toml.getTable(songName),channel,songName, table.getIndex(),-1);
                        else if (toml.containsTableArray(songName)) {
                            this.doubleBracketedSongs.putIfAbsent(songName, new MutableInt(0));
                            int ml = this.doubleBracketedSongs.get(songName).getValue();
                            audio = new Audio(toml.getTables(songName).get(ml),channel,songName, table.getIndex(),ml);
                            this.doubleBracketedSongs.get(songName).increment();
                        }
                        this.parsedFile.put(type,audio);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Failed to initialize song block in channel " + channel +
                                " from song " + songName + "  (Internally: File " +
                                e.getStackTrace()[0].getFileName() + " at line " +
                                e.getStackTrace()[0].getLineNumber() + ")");
                    }
                }
            }
        }
        if(Objects.isNull(this.universalParameters)) this.universalParameters = new Universal(null);
        for(Audio audio : parsedFile.values())
            audio.initializeTriggerPersistence(channel);
    }

    public void clearMaps() {
        this.parsedFile.clear();
        this.doubleBracketedSongs.clear();
    }
}
