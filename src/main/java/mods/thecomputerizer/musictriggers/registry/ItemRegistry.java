package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.registry.items.MusicTriggersRecord;
import mods.thecomputerizer.musictriggers.registry.items.BlankRecord;
import mods.thecomputerizer.musictriggers.registry.items.CustomRecord;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;

public final class ItemRegistry {

    public static final CreativeModeTab MUSIC_TRIGGERS_TAB = FabricItemGroupBuilder.build(Constants.res("musictriggers"),
            () -> new ItemStack(ItemRegistry.MUSIC_RECORDER));

    public static final Map<ResourceLocation, Item> ITEM_MAP = new HashMap<>();
    public static final Item MUSIC_TRIGGERS_RECORD = addItemRegistry("music_triggers_record",
            new MusicTriggersRecord(new Item.Properties().stacksTo(1).tab(MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final Item CUSTOM_RECORD = addItemRegistry("custom_record",
            new CustomRecord(new Item.Properties().stacksTo(1).tab(MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final Item BLANK_RECORD = addItemRegistry("blank_record",
            new BlankRecord(new Item.Properties().stacksTo(1).tab(MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));
    public static final Item MUSIC_RECORDER = addItemRegistry("music_recorder",
            new BlockItem(BlockRegistry.MUSIC_RECORDER, new Item.Properties().stacksTo(1).tab(MUSIC_TRIGGERS_TAB).rarity(Rarity.EPIC)));

    private static Item addItemRegistry(String id, Item item) {
        ITEM_MAP.put(Constants.res(id),item);
        return item;
    }

    public static void register() {
        for(Map.Entry<ResourceLocation, Item> entry : ITEM_MAP.entrySet())
            Registry.register(Registry.ITEM,entry.getKey(),entry.getValue());
    }
}
