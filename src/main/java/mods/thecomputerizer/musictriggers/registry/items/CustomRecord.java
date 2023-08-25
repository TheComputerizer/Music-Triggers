package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {

    public CustomRecord(Item.Properties p) {
        super(p);
    }

    private static Map<String,String> getRecordMap(String channel) {
        return ChannelManager.getNonDefaultChannel(channel).getRecordMap();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        if(Objects.nonNull(tagString(stack,"songName")))
            components.add(getLang("record","custom_record",getRecordMap(tagString(stack,"channelFrom"))
                    .get(tagString(stack,"trackID"))));
        else components.add(getLang("item","custom_record","blank_description"));
    }

    public static float mapTriggerToFloat(String channel, String song) {
        float index = 1f;
        Map<String,String> recordMap = getRecordMap(channel);
        String name = recordMap.get(song);
        if(Objects.isNull(name)) return 0f;
        for(String key : recordMap.values()) {
            if(name.matches(key))
                return 0.01f*index;
            index++;
        }
        return 0f;
    }
}
