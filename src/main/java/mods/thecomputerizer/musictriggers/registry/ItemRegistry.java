package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.registry.items.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@GameRegistry.ObjectHolder(Constants.MODID)
public class ItemRegistry {
    public static final Item MUSIC_TRIGGERS_RECORD = makeEpicItem("music_triggers_record", MusicTriggersRecord::new,
            item -> item.setCreativeTab(RegistryHandler.MUSIC_TRIGGERS_TAB).setMaxStackSize(1));
    public static final Item CUSTOM_RECORD = makeEpicItem("custom_record", CustomRecord::new,
            item -> item.setCreativeTab(RegistryHandler.MUSIC_TRIGGERS_TAB).setMaxStackSize(1));
    public static final Item BLANK_RECORD = makeEpicItem("blank_record", BlankRecord::new,
            item -> item.setCreativeTab(RegistryHandler.MUSIC_TRIGGERS_TAB).setMaxStackSize(1));
    public static final Item MUSIC_RECORDER = makeEpicItemBlock(BlockRegistry.MUSIC_RECORDER,
            item -> item.setCreativeTab(RegistryHandler.MUSIC_TRIGGERS_TAB).setMaxStackSize(1));

    private static EpicItem makeEpicItem(final String name, final Supplier<EpicItem> constructor, final Consumer<EpicItem> config) {
        final EpicItem item = constructor.get();
        config.accept(item);
        item.setRegistryName(Constants.MODID, name);
        item.setTranslationKey(Constants.MODID+"."+name);
        return item;
    }

    @SuppressWarnings("SameParameterValue")
    private static EpicItemBlock makeEpicItemBlock(final @Nonnull Block constructor, final Consumer<EpicItemBlock> config) {
        final EpicItemBlock item = new EpicItemBlock(constructor);
        config.accept(item);
        item.setRegistryName(Objects.requireNonNull(constructor.getRegistryName()));
        item.setTranslationKey(constructor.getTranslationKey());
        return item;
    }
}
