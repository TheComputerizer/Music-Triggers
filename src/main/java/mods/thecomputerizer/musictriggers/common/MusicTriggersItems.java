package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MusicTriggersItems {

    public static final Identifier BLANK_RECORD_ID = new Identifier(MusicTriggersCommon.MODID,"blank_record");
    public static final Item BLANK_RECORD = new BlankRecord(new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC));
    public static void init() {
        Registry.register(Registry.ITEM,BLANK_RECORD_ID,BLANK_RECORD);
        if(ConfigRegistry.registerDiscs) {
            for (SoundEvent s : SoundHandler.allSoundEvents) {
                String name = Objects.requireNonNull(s.getId()).toString().replaceAll("musictriggers:", "");
                Registry.register(Registry.ITEM, s.getId(), new MusicTriggersRecord(15, s, new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC)));
                buildModel(name,SoundHandler.allSoundEventsWithTriggers.get(s));
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void buildModel(String name, String trigger) {
        try {
            File modelsFolder = new File("config/MusicTriggers/songs/assets/musictriggers", "models");
            if (!modelsFolder.exists()) {
                modelsFolder.mkdir();
            }
            File itemFolder = new File(modelsFolder, "item");
            if (!itemFolder.exists()) {
                itemFolder.mkdir();
            }
            File model = new File(itemFolder, name + ".json");
            if (model.exists()) {
                model.delete();
            }
            List<String> fb = new ArrayList<>();
            fb.add("{");
            fb.add("\t\"parent\": \"item/generated\",");
            fb.add("\t\"textures\": {");
            fb.add("\t\t\"layer0\": \"musictriggers:item/record_"+trigger+"\"");
            fb.add("\t}");
            fb.add("}");
            Files.write(Paths.get(model.getPath()), fb, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
