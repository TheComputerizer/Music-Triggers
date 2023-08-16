package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.server.TriggerCommand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class RegistryHandler {

    public static final CreativeModeTab MUSIC_TRIGGERS_TAB = new CreativeModeTab("musictriggers") {
        @OnlyIn(Dist.CLIENT)
        @Nonnull
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.MUSIC_RECORDER.get());
        }
    };

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent ev) {
        TriggerCommand.register(ev.getDispatcher());
    }

    public static void init(IEventBus eventBus) {
        Constants.MAIN_LOG.info("Loading Music Triggers discs and blocks");
        BlockRegistry.registerBlocks(eventBus);
        ItemRegistry.registerItems(eventBus);
        TileRegistry.registerTiles(eventBus);
    }
}
