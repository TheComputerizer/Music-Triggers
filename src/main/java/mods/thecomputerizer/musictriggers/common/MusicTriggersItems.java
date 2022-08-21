package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MusicTriggersItems {
    public static final Item MUSIC_TRIGGERS_RECORD = makeItem("music_triggers_record", MusicTriggersRecord::new, item -> item.setCreativeTab(CreativeTabs.MISC));
    public static final Item BLANK_RECORD = makeItem("blank_record", BlankRecord::new, item -> item.setCreativeTab(CreativeTabs.MISC));
    public static final Item MUSIC_RECORDER = makeItemBlock(MusicTriggersBlocks.MUSIC_RECORDER, item -> item.setCreativeTab(CreativeTabs.MISC));

    private static Item makeItem(final String name, final Supplier<Item> constructor, final Consumer<Item> config) {
        final Item item = constructor.get();
        config.accept(item);
        item.setUnlocalizedName(MusicTriggers.MODID + "." + name);
        item.setRegistryName(MusicTriggers.MODID, name);
        item.setMaxStackSize(1);
        return item;
    }

    private static Item makeItemBlock(final Block constructor, final Consumer<Item> config) {
        final Item item = new ItemBlock(constructor);
        config.accept(item);
        item.setUnlocalizedName(constructor.getUnlocalizedName());
        item.setRegistryName(Objects.requireNonNull(constructor.getRegistryName()));
        item.setMaxStackSize(1);
        return item;
    }
}
