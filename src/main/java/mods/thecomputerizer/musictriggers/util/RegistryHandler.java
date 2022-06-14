package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersBlocks;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.config.ConfigRegistry;
import mods.thecomputerizer.musictriggers.util.packets.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.common.MusicTriggersItems.BLANK_RECORD;
import static mods.thecomputerizer.musictriggers.common.MusicTriggersItems.MUSIC_RECORDER;


@GameRegistry.ObjectHolder(MusicTriggers.MODID)
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {
    public static SimpleNetworkWrapper network;

    /*
    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        MusicTriggersItems.INSTANCE.init();
        if(ConfigRegistry.registerDiscs) {
            e.getRegistry().registerAll(MusicTriggersItems.INSTANCE.getItems().toArray(new Item[0]));
            e.getRegistry().register(BLANK_RECORD);
            e.getRegistry().register(MUSIC_RECORDER);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> e) {
        if(ConfigRegistry.registerDiscs) {
            e.getRegistry().register(MusicTriggersBlocks.MUSIC_RECORDER);
        }
    }

    @SubscribeEvent
    public static void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> e) {
        ModSounds.INSTANCE.init();
        e.getRegistry().registerAll(ModSounds.INSTANCE.getSounds().toArray(new SoundEvent[0]));
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        if(ConfigRegistry.registerDiscs) {
            for (Map.Entry<Item, String> itemStringEntry : MusicTriggersItems.allItemsWithTrigger.entrySet()) {
                Item record = itemStringEntry.getKey();
                ModelLoader.setCustomModelResourceLocation(record, 0, new ModelResourceLocation("musictriggers:record_"+MusicTriggersItems.allItemsWithTrigger.get(record), "inventory"));
            }
            ModelLoader.setCustomModelResourceLocation(BLANK_RECORD, 0, new ModelResourceLocation(Objects.requireNonNull(BLANK_RECORD.getRegistryName()), "inventory"));
            ModelLoader.setCustomModelResourceLocation(MUSIC_RECORDER, 0, new ModelResourceLocation(Objects.requireNonNull(MUSIC_RECORDER.getRegistryName()), "inventory"));
        }
    }
     */

    public static void init() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MusicTriggers.MODID);
        registerPackets();
    }

    private static void registerPackets() {
        int id = 0;
        network.registerMessage(PacketBossInfo.class, PacketBossInfo.packetBossInfoMessage.class, id++, Side.SERVER);
        network.registerMessage(PacketQueryServerInfo.class, PacketQueryServerInfo.PacketQueryServerInfoMessage.class, id++, Side.SERVER);
        network.registerMessage(PacketSyncServerInfo.class, PacketSyncServerInfo.PacketSyncServerInfoMessage.class, id, Side.CLIENT);
    }
}
