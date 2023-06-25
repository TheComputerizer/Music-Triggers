package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {
    public CustomRecord() {
        addPropertyOverride(new ResourceLocation(Constants.MODID, "custom_record"),
                (stack, worldIn, entityIn) -> {
                    if(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("trackID"))
                        return mapTriggerToFloat(stack.getTagCompound().getString("channelFrom"),
                                stack.getTagCompound().getString("trackID"));
                    return 0f;
                });
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, @Nonnull List<String> tooltip,
                               @Nonnull ITooltipFlag flag) {
        if(stack.hasTagCompound() && Objects.requireNonNull(stack.getTagCompound()).hasKey("songName"))
            tooltip.add(
                    AssetUtil.extraLang(Constants.MODID,"record","custom_record",
                            ChannelManager.getChannel(stack.getTagCompound().getString("channelFrom")).getRecordMap()
                                    .get(stack.getTagCompound().getString("trackID"))));
        else tooltip.add(
                AssetUtil.extraLang(Constants.MODID,"item","music_triggers_record","blank_description",false));
    }

    private float mapTriggerToFloat(String channel, String song) {
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
