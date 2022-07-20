package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MusicTriggersItems {
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);

    public void init() {
        /*
        SoundHandler.registerSounds();
        if(ConfigRegistry.registerDiscs) {
            for (SoundEvent s : SoundHandler.allSoundEvents) {
                String name = Objects.requireNonNull(s.getRegistryName()).toString().replaceAll("musictriggers:", "");
                ITEMS.register(name, () -> new MusicTriggersRecord(15, s, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));
                buildModel(name,SoundHandler.allSoundEventsWithTriggers.get(s));
            }
            ITEMS.register("blank_record", () -> new BlankRecord(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
        }

         */
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
