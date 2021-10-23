package mods.thecomputerizer.musictriggers.util;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.ModSounds;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.item.Item;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public final class RegistryHandler {
    //public static NetworkHandler network;
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MusicTriggers.MODID);

    public static void registerItems(IEventBus eventBus) {
        MusicTriggers.logger.info("Loading Items from Music Triggers");
        MusicTriggersItems.INSTANCE.init();
        MusicTriggersItems.ITEMS.register(eventBus);
    }

    public static void registerSoundEvents(IEventBus eventBus)
    {
        MusicTriggers.logger.info("Loading Sounds from Music Triggers");
        ModSounds.INSTANCE.init();
        ModSounds.SOUNDS.register(eventBus);
    }


    public static void onModelRegister(ModelRegistryEvent event) {
        //for(Item i: MusicTriggersItems.INSTANCE.getItems()) {
            //ModelLoader.setCustomModelResourceLocation(i, 0, new ModelResourceLocation("musictriggers:record", "inventory"));
        //}
    }

    public static void init(IEventBus eventBus) {
        registerItems(eventBus);
        registerSoundEvents(eventBus);
        //network = NetworkHandler.INSTANCE.newSimpleChannel(MusicTriggers.MODID);
        registerPackets();
    }

    private static void registerPackets() {
        //network.registerMessage(packetToClient.class, packetToClient.packetToClientMessage.class, 0, Side.CLIENT);
        //network.registerMessage(packet.class, packet.packetMessage.class, 1, Side.SERVER);
    }
}
