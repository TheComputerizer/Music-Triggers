package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
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

@GameRegistry.ObjectHolder(MusicTriggers.MODID)
@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {
    public static SimpleNetworkWrapper network;

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e)
    {
        MusicTriggersItems.INSTANCE.init();
        e.getRegistry().registerAll(MusicTriggersItems.INSTANCE.getItems().toArray(new Item[0]));
    }

    @SubscribeEvent
    public static void onRegisterSoundEvents(RegistryEvent.Register<SoundEvent> e)
    {
        ModSounds.INSTANCE.init();
        e.getRegistry().registerAll(ModSounds.INSTANCE.getSounds().toArray(new SoundEvent[0]));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        }
    }

    public static void init() {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MusicTriggers.MODID);
        registerPackets();
    }

    private static void registerPackets() {
        network.registerMessage(packet.class, packet.packetMessage.class, 0, Side.CLIENT);
        network.registerMessage(packet.class, packet.packetMessage.class, 1, Side.SERVER);
    }
}
