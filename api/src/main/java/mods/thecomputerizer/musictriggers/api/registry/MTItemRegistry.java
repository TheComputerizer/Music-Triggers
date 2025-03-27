package mods.thecomputerizer.musictriggers.api.registry;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClient;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.RegistryHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.item.DiscBuilderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.tab.CreativeTabAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.tab.CreativeTabBuilderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;

import java.util.Collections;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult.SUCCESS;
import static mods.thecomputerizer.theimpossiblelibrary.api.registry.item.ItemBuilderAPI.ItemType.DISC;

public class MTItemRegistry {
    
    public static CreativeTabAPI<?> CREATIVE_TAB = MTItemRegistry.makeCreativeTab();
    
    public static final ItemAPI<?> ENHANCED_MUSIC_DISC = discBuilder("record", 1)
            .setSoundNameSupplier(stack -> {
                if(Objects.isNull(stack)) return MTClient.getTranslated("item","record.desc");
                CompoundTagAPI<?> tag = stack.getTag();
                if(Objects.nonNull(tag) && tag.contains("audio"))
                    return MTClient.getTranslated("item","record.tooltip",tag.getString("audio"));
                return MTClient.getTranslated("item","record.desc");
            }).setTooltipFunction((stack,world) -> {
                CompoundTagAPI<?> tag = stack.getTag();
                if(Objects.nonNull(tag)) {
                    String audio = null;
                    if(tag.contains("custom") && tag.contains("channel")) {
                        ChannelHelper helper = ChannelHelper.getClientHelper();
                        if(Objects.nonNull(helper)) {
                            ChannelAPI channel = helper.findChannel(ChannelHelper.getGlobalData(),tag.getString("channel"));
                            if(Objects.nonNull(channel)) {
                                String custom = tag.getString("custom");
                                for(RecordElement record : channel.getData().getRecords()) {
                                    if(record.getKey().equals(custom)) {
                                        audio = record.getValue();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if(Objects.nonNull(audio)) audio = MTClient.getTranslated("record",audio).getApplied();
                    else if(tag.contains("audio")) audio = tag.getString("audio");
                    if(Objects.nonNull(audio))
                        return Collections.singleton(MTClient.getTranslated("item","record.tooltip",audio));
                }
                return Collections.singleton(MTClient.getTranslated("item","record.blank"));
            })
            .setUseFunc(ctx -> {
                ActionResult superResult = ctx.getSuperResult();
                if(superResult==SUCCESS && ctx.getWorld().isClient()) {
                    CompoundTagAPI<?> tag = ctx.getPlayer().getStackInHand(ctx.getHand()).getTag();
                    if(Objects.nonNull(tag)) {
                        String channel = tag.getString("channel");
                        String audio = tag.contains("custom") ? tag.getString("custom") : tag.getString("audio");
                        if(TextHelper.isNotBlank(channel) && TextHelper.isNotBlank(audio))
                            ChannelHelper.getClientHelper().playToJukebox(ctx.getPos(),channel,audio);
                    }
                }
                return superResult;
            }).setCreativeTab(CREATIVE_TAB).build();
    
    public static final ItemAPI<?> MUSIC_RECORDER = RegistryHelper.makeItemBlockBuilder()
            .setBlock(() -> MTBlockRegistry.MUSIC_RECORDER)
            .setRegistryName(MTRef.res("music_recorder"))
            .setStackSize(1)
            .setTooltipFunction((stack,world) -> Collections.singleton(
                    MTClient.getTranslated("tile","music_recorder.tooltip")))
            .setCreativeTab(CREATIVE_TAB).build();
    
    public static DiscBuilderAPI discBuilder(String name, int stackSize) {
        return TriggerRegistry.buildProperties(
                RegistryHelper.makeDiscBuilder(null).setRegistryName(MTRef.res(name))
                        .setItemType(DISC).setStackSize(stackSize));
    }
    
    static CreativeTabAPI<?> makeCreativeTab() {
        ResourceLocationAPI<?> registryName = MTRef.res(MODID);
        CreativeTabBuilderAPI<?> builder = RegistryHelper.makeCreativeTabBuilder();
        builder.setRegistryName(registryName);
        builder.setIconItem(() -> ENHANCED_MUSIC_DISC);
        return builder.build();
    }
}