package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.core.Constants;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {
    public CustomRecord() {
        addPropertyOverride(Constants.res("custom_record"),
                (stack, worldIn, entityIn) -> {
                    String trackID = tagString(stack,"trackID");
                    return Objects.nonNull(trackID) ? mapTriggerToFloat(tagString(stack,"channelFrom"),trackID) : 0f;
                });
    }

    private Map<String,String> getRecordMap(String channel) {
        return ChannelManager.getNonDefaultChannel(channel).getRecordMap();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip,
                               @Nonnull ITooltipFlag flag) {
        if(Objects.nonNull(tagString(stack,"songName")))
            tooltip.add(getLang("record","custom_record",getRecordMap(tagString(stack,"channelFrom"))
                    .get(tagString(stack,"trackID"))));
        else tooltip.add(getLang("item","custom_record","blank_description"));
    }

    private float mapTriggerToFloat(String channel, String song) {
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
