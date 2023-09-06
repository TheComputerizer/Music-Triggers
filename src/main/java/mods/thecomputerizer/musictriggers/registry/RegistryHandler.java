package mods.thecomputerizer.musictriggers.registry;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.registry.tiles.MusicRecorderEntity;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.server.MTCommand;
import mods.thecomputerizer.musictriggers.server.MusicTriggersCommand;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.registry.ItemRegistry.*;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class RegistryHandler {

    public static final CreativeTabs MUSIC_TRIGGERS_TAB = new CreativeTabs("musictriggers") {
        @SideOnly(Side.CLIENT)
        @Nonnull
        public ItemStack createIcon() {
            return new ItemStack(MUSIC_RECORDER);
        }
    };

    public static void registerCommands(FMLServerStartingEvent event) {
        if(!ConfigRegistry.CLIENT_SIDE_ONLY) {
            event.registerServerCommand(new MusicTriggersCommand());
            event.registerServerCommand(new MTCommand());
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        if(ConfigRegistry.REGISTER_DISCS)
            event.getRegistry().registerAll(BLANK_RECORD,MUSIC_TRIGGERS_RECORD,CUSTOM_RECORD,MUSIC_RECORDER);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        if(ConfigRegistry.REGISTER_DISCS) {
            event.getRegistry().register(BlockRegistry.MUSIC_RECORDER);
            GameRegistry.registerTileEntity(MusicRecorderEntity.class, Constants.res("tile.music_recorder"));
        }
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        if(ConfigRegistry.REGISTER_DISCS) {
            registerGenericItemModel(MUSIC_TRIGGERS_RECORD);
            registerGenericItemModel(CUSTOM_RECORD);
            registerGenericItemModel(BLANK_RECORD);
            registerGenericItemModel(MUSIC_RECORDER);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void registerGenericItemModel(Item item) {
        ResourceLocation resource  = item.getRegistryName();
        if(Objects.nonNull(resource)) {
            ModelResourceLocation modelLoc = new ModelResourceLocation(resource,"inventory");
            ModelLoader.setCustomModelResourceLocation(item,0,modelLoc);
        }
    }
}
