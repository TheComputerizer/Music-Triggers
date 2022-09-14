package mods.thecomputerizer.musictriggers.common;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.objects.BlankRecord;
import mods.thecomputerizer.musictriggers.common.objects.MusicTriggersRecord;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;


public class MusicTriggersItems {

    public static final Identifier BLANK_RECORD_ID = new Identifier(MusicTriggers.MODID,"blank_record");
    public static final Identifier MUSIC_TRIGGERS_RECORD_ID = new Identifier(MusicTriggers.MODID,"music_trigger_record");
    public static final Item BLANK_RECORD = new BlankRecord(new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC));
    public static final Item MUSIC_TRIGGERS_RECORD = new MusicTriggersRecord(new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC));
    public static final Item MUSIC_RECORDER = new BlockItem(MusicTriggersBlocks.MUSIC_RECORDER, new Item.Settings().rarity(Rarity.EPIC).fireproof().group(ItemGroup.MISC));
    public static void init() {
        Registry.register(Registry.ITEM,BLANK_RECORD_ID,BLANK_RECORD);
        Registry.register(Registry.ITEM,MUSIC_TRIGGERS_RECORD_ID,MUSIC_TRIGGERS_RECORD);
        Registry.register(Registry.ITEM,MusicTriggersBlocks.MUSIC_RECORDER_ID,MUSIC_RECORDER);
    }
}
