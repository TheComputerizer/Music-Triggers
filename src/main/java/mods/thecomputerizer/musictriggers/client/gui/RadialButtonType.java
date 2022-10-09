package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public enum RadialButtonType {
    LOG(Arrays.asList("Log visualizer", "- See important information since the last reload"),
            MusicTriggers.getIcon("gui/button","log"),null,RadialButton::new),
    PLAYBACK(Arrays.asList("Playback Information", "- Shows the time and name of the currently song","- Allows you to skip or reset the current song"),
            MusicTriggers.getIcon("gui/button","playback"),null,RadialButton::new),
    EDIT(Arrays.asList("Edit Song Configurations", "- Edit existing song configurations or add new ones","- All config files can be edited here"),
            MusicTriggers.getIcon("gui/button","edit"),null,RadialButton::new),
    RELOAD(Arrays.asList("Reload Configurations", "- Reloads all of the config files from the mod","- Applies any changes made within the gui",
            "- Text edits will also be applied as long as the file has been saved"), MusicTriggers.getIcon("gui/button","reload"),
            null,RadialButton::new);

    private final RadialButton.CreatorFunction<List<String>, ResourceLocation, String, RadialButton> creatorFunction;
    private final List<String> tooltipLines;
    private final ResourceLocation iconLocation;
    private final String centerText;
    RadialButtonType(List<String> tooltipLines, @Nullable ResourceLocation centerIcon, @Nullable String centerText,
                     RadialButton.CreatorFunction<List<String>, ResourceLocation, String, RadialButton> creatorFunction) {
        this.creatorFunction = creatorFunction;
        this.tooltipLines = tooltipLines;
        this.iconLocation = centerIcon;
        this.centerText = centerText;
    }

    public RadialButton getButton() {
        return this.creatorFunction.apply(this.tooltipLines,this.iconLocation,this.centerText);
    }
}
