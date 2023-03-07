package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {

    public CustomRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Level world, List<Component> components, TooltipFlag flag) {
        if(stack.getOrCreateTag().contains("trackID"))
            components.add(MutableComponent.create(AssetUtil.customLang("record.musictriggers.custom_record."+
                    ChannelManager.getChannel(stack.getOrCreateTag().getString("channelFrom")).getRecordMap()
                            .get(stack.getOrCreateTag().getString("trackID")),false)));
        else components.add(MutableComponent.create(AssetUtil.extraLang(Constants.MODID,"item",
                "music_triggers_record","blank_description",false)));
    }

    public static float mapTriggerToFloat(String channel, String song) {
        float index = 1f;
        String name = ChannelManager.getChannel(channel).getRecordMap().get(song);
        if(Objects.isNull(name)) return 0f;
        for(String key : ChannelManager.getChannel(channel).getRecordMap().values()) {
            if(name.matches(key))
                return 0.01f*index;
            index++;
        }
        return 0f;
    }
}
