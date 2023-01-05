package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.CustomRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;


public class MusicTriggersItems {

    public static final ResourceLocation BLANK_RECORD_ID = new ResourceLocation(Constants.MODID,"blank_record");
    public static final ResourceLocation MUSIC_TRIGGERS_RECORD_ID = new ResourceLocation(Constants.MODID,"music_trigger_record");
    public static final ResourceLocation CUSTOM_RECORD_ID = new ResourceLocation(Constants.MODID,"custom_record");
    public static final Item BLANK_RECORD = new BlankRecord(new Item.Properties().rarity(Rarity.EPIC).fireResistant().tab(CreativeModeTab.TAB_MISC));
    public static final Item MUSIC_TRIGGERS_RECORD = new MusicTriggersRecord(new Item.Properties().rarity(Rarity.EPIC).fireResistant().tab(CreativeModeTab.TAB_MISC));
    public static final Item CUSTOM_RECORD = new CustomRecord(new Item.Properties().rarity(Rarity.EPIC).fireResistant().tab(CreativeModeTab.TAB_MISC));
    public static final Item MUSIC_RECORDER = new BlockItem(MusicTriggersBlocks.MUSIC_RECORDER, new Item.Properties().rarity(Rarity.EPIC).fireResistant().tab(CreativeModeTab.TAB_MISC));
    public static void init() {
        Registry.register(Registry.ITEM,BLANK_RECORD_ID,BLANK_RECORD);
        Registry.register(Registry.ITEM,MUSIC_TRIGGERS_RECORD_ID,MUSIC_TRIGGERS_RECORD);
        Registry.register(Registry.ITEM,CUSTOM_RECORD_ID,CUSTOM_RECORD);
        Registry.register(Registry.ITEM,MusicTriggersBlocks.MUSIC_RECORDER_ID,MUSIC_RECORDER);
    }
}
