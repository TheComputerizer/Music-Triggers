package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.registry.items.BlankRecord;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public final class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Constants.MODID);
    public static final RegistryObject<Item> MUSIC_TRIGGERS_RECORD = ITEMS.register("music_triggers_record",
            () -> new MusicTriggersRecord(new Item.Properties().stacksTo(1).tab(Tabs.MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> CUSTOM_RECORD = ITEMS.register("custom_record",
            () -> new CustomRecord(new Item.Properties().stacksTo(1).tab(Tabs.MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> BLANK_RECORD = ITEMS.register("blank_record",
            () -> new BlankRecord(new Item.Properties().stacksTo(1).tab(Tabs.MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final RegistryObject<Item> MUSIC_RECORDER = ITEMS.register("music_recorder",
            () -> new BlockItem(BlockRegistry.MUSIC_RECORDER.get(), new Item.Properties().stacksTo(1).tab(Tabs.MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));

    public static void registerItems(IEventBus bus) {
        ITEMS.register(bus);
    }
}
