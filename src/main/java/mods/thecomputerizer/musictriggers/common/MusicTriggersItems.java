package mods.thecomputerizer.musictriggers.common;

import com.google.common.collect.Lists;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.configRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.SoundEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MusicTriggersItems {
    public static List<Item> allItems;
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();
    public static final Item BLANK_RECORD = makeItem("blank_record", BlankRecord::new, item -> item.setCreativeTab(CreativeTabs.MISC));
    public static final Item MUSIC_RECORDER = makeItemBlock(MusicTriggersBlocks.MUSIC_RECORDER, item -> item.setCreativeTab(CreativeTabs.MISC));

    public void init() {
        SoundHandler.registerSounds();
        if(configRegistry.registry.registerDiscs) {
            allItems = Lists.newArrayList();
            for (SoundEvent s : SoundHandler.allSoundEvents) {
                Item i = (new MusicTriggersRecord(Objects.requireNonNull(s.getRegistryName()).toString().replace("musictriggers:", ""), s));
                if (!allItems.contains(i)) {
                    allItems.add(i);
                }
            }
        }
    }

    private static Item makeItem(final String name, final Supplier<Item> constructor, final Consumer<Item> config) {
        final Item item = constructor.get();
        config.accept(item);
        item.setTranslationKey(MusicTriggers.MODID + "." + name);
        item.setRegistryName(MusicTriggers.MODID, name);
        item.setMaxStackSize(1);
        return item;
    }

    private static Item makeItemBlock(final Block constructor, final Consumer<Item> config) {
        final Item item = new ItemBlock(constructor);
        config.accept(item);
        item.setTranslationKey(constructor.getTranslationKey());
        item.setRegistryName(Objects.requireNonNull(constructor.getRegistryName()));
        item.setMaxStackSize(1);
        return item;
    }

    public List<Item> getItems(){
        return allItems;
    }
}
