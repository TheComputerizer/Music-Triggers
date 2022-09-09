package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class MusicTriggersItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);
    public static final RegistryObject<Item> MUSIC_TRIGGERS_RECORD = ITEMS.register("music_trigger_record", () -> new MusicTriggersRecord(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BLANK_RECORD = ITEMS.register("blank_record", () -> new BlankRecord(new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MUSIC_RECORDER = ITEMS.register("music_recorder", () -> new BlockItem(MusicTriggersBlocks.MUSIC_RECORDER.get(), new Item.Properties().stacksTo(1).tab(ItemGroup.TAB_MISC).rarity(Rarity.EPIC)));

    public static void registerItems(IEventBus bus) {
        ITEMS.register(bus);
    }
}
