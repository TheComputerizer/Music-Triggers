package mods.thecomputerizer.musictriggers.api.registry;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.EventHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.event.events.RegisterItemsEventWrapper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.RegistryHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.item.DiscBuilderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.event.CommonEventWrapper.CommonType.REGISTER_ITEMS;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult.SUCCESS;
import static mods.thecomputerizer.theimpossiblelibrary.api.registry.item.ItemBuilderAPI.ItemType.DISC;

public class MTRegistry {
    
    public static final ItemAPI<?> MT_RECORD = discBuilder("record",1)
            .setSoundNameSupplier(stack -> {
                if(Objects.isNull(stack)) return MTClient.getTranslated("item","record.desc");
                CompoundTagAPI tag = stack.getTag();
                if(Objects.nonNull(tag) && tag.contains("audio"))
                    return MTClient.getTranslated("item","record.tooltip",tag.getString("audio"));
                return MTClient.getTranslated("item","record.desc");
            })
            .setTootltipFunction((stack,world) -> {
                CompoundTagAPI tag = stack.getTag();
                if(Objects.nonNull(tag) && tag.contains("audio"))
                    return Collections.singleton(
                            MTClient.getTranslated("item","record.tooltip",tag.getString("audio")));
                return Collections.emptySet();
            })
            .setUseFunc(ctx -> {
                ActionResult superResult = ctx.getSuperResult();
                if(superResult==SUCCESS && ctx.getWorld().isClient()) {
                    CompoundTagAPI tag = ctx.getPlayer().getStackInHand(ctx.getHand()).getTag();
                    if(Objects.nonNull(tag)) {
                        String channel = tag.getString("channel");
                        String audio = tag.getString("audio");
                        if(StringUtils.isNotBlank(channel) && StringUtils.isNotBlank(audio))
                            ChannelHelper.getClientHelper().playToJukebox(channel,audio);
                    }
                }
                return superResult;
            }).build();
    
    public static DiscBuilderAPI discBuilder(String name, int stackSize) {
        return TriggerRegistry.buildProperties(
                RegistryHelper.getHandler().makeDiscBuilder(null).setRegistryName(MTRef.res(name))
                        .setItemType(DISC).setStackSize(stackSize));
    }
    
    public static void init() {
        MTRef.logInfo("Initializing registry events");
        EventHelper.addListener(REGISTER_ITEMS,MTRegistry::onRegisterItems);
    }
    
    public static void onRegisterItems(RegisterItemsEventWrapper<?> wrapper) {
        wrapper.register(MT_RECORD);
    }
}