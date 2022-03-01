package mods.thecomputerizer.musictriggers.config;


import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

public class configDebug implements ModMenuApi {
    public static boolean showDebug = false;
    public static boolean showJustCurSong = false;
    public static boolean silenceIsBad = false;
    public static List<String> modList = new ArrayList<>();


    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> this.build();
    }

    public Screen build() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(new TranslatableText("title.musictriggers.config"));

        ConfigCategory debug = builder.getOrCreateCategory(new TranslatableText("category.musictriggers.debug"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.musictriggers.showinfo"), showDebug)
                .setDefaultValue(false)
                .setTooltip(new TranslatableText("Show the debug info"))
                .setSaveConsumer(newValue -> showDebug = newValue)
                .build());
        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.musictriggers.showjustsong"), showJustCurSong)
                .setDefaultValue(false)
                .setTooltip(new TranslatableText("If ShowDebugInfo is set to true, but you only want to see the song name"))
                .setSaveConsumer(newValue -> showJustCurSong = newValue)
                .build());
        debug.addEntry(entryBuilder.startStrList(new TranslatableText("option.musictriggers.blockedmods"), modList)
                .setDefaultValue(new ArrayList<>())
                .setTooltip(new TranslatableText("List of mod ids to remove the music from so there is not any overlap"))
                .setSaveConsumer(newValue -> modList = newValue)
                .build());
        debug.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("option.musictriggers.silenceisbad"), silenceIsBad)
                .setDefaultValue(false)
                .setTooltip(new TranslatableText("Only silence blocked music when there is music from Music Triggers already playing"))
                .setSaveConsumer(newValue -> silenceIsBad = newValue)
                .build());

        return builder.build();
    }
}
