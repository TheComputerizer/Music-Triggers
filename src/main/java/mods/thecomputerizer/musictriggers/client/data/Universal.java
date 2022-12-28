package mods.thecomputerizer.musictriggers.client.data;

import com.moandjiezana.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.util.file.TomlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Universal {

    private String fadeIn;
    private String fadeOut;
    private String persistence;
    private String triggerDelay;
    private String songDelay;
    private String startDelay;

    public Universal(Toml table) {
        String[] data = new String[]{"0","0","0","0","0","0"};
        if(Objects.nonNull(table)) data = readTable(table);
        this.fadeIn = data[0];
        this.fadeOut = data[1];
        this.persistence = data[2];
        this.triggerDelay = data[3];
        this.songDelay = data[4];
        this.startDelay = data[5];
    }

    private String[] readTable(Toml table) {
        return new String[]{TomlUtil.sneakyInt(table,"fade_in",0),
                TomlUtil.sneakyInt(table,"fade_out",0),
                TomlUtil.sneakyInt(table,"persistence",0),
                TomlUtil.sneakyInt(table,"trigger_delay",0),
                TomlUtil.sneakyInt(table,"song_delay",0),
                TomlUtil.sneakyInt(table,"start_delay",0)};
    }

    private boolean isDefault(String val) {
        return val.matches("0");
    }

    public List<String> getAsTomlLines() {
        List<String> lines = new ArrayList<>();
        boolean allDefault = isDefault(this.fadeIn);
        if(!isDefault(this.fadeOut)) allDefault = false;
        if(!isDefault(this.persistence)) allDefault = false;
        if(!isDefault(this.triggerDelay)) allDefault = false;
        if(!isDefault(this.songDelay)) allDefault = false;
        if(!isDefault(this.startDelay)) allDefault = false;
        if(allDefault) return lines;
        lines.add("[universal]");
        if(!isDefault(this.fadeIn)) lines.add("\tfade_in = \""+this.fadeIn+"\"");
        if(!isDefault(this.fadeOut)) lines.add("\tfade_in = \""+this.fadeOut+"\"");
        if(!isDefault(this.persistence)) lines.add("\tfade_in = \""+this.persistence+"\"");
        if(!isDefault(this.triggerDelay)) lines.add("\tfade_in = \""+this.triggerDelay+"\"");
        if(!isDefault(this.songDelay)) lines.add("\tfade_in = \""+this.songDelay+"\"");
        if(!isDefault(this.startDelay)) lines.add("\tfade_in = \""+this.startDelay+"\"");
        return lines;
    }

    public String getFadeIn() {
        return fadeIn;
    }

    public void setFadeIn(String fadeIn) {
        this.fadeIn = fadeIn;
    }

    public String getFadeOut() {
        return fadeOut;
    }

    public void setFadeOut(String fadeOut) {
        this.fadeOut = fadeOut;
    }

    public String getPersistence() {
        return persistence;
    }

    public void setPersistence(String persistence) {
        this.persistence = persistence;
    }

    public String getTriggerDelay() {
        return triggerDelay;
    }

    public void setTriggerDelay(String triggerDelay) {
        this.triggerDelay = triggerDelay;
    }

    public String getSongDelay() {
        return songDelay;
    }

    public void setSongDelay(String songDelay) {
        this.songDelay = songDelay;
    }

    public String getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(String startDelay) {
        this.startDelay = startDelay;
    }
}
