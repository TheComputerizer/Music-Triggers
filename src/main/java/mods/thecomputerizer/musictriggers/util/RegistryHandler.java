package mods.thecomputerizer.musictriggers.util;

import mezz.jei.startup.NetworkHandler;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
public final class RegistryHandler {
    //public static NetworkHandler network;

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


    @SubscribeEvent
    public static void onModelRegister(ModelRegistryEvent event) {
        for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            //ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        }
    }

    public static void init() {
        //network = NetworkHandler.INSTANCE.newSimpleChannel(MusicTriggers.MODID);
        registerPackets();
    }

    private static void registerPackets() {
        //network.registerMessage(packetToClient.class, packetToClient.packetToClientMessage.class, 0, Side.CLIENT);
        //network.registerMessage(packet.class, packet.packetMessage.class, 1, Side.SERVER);
    }
}
