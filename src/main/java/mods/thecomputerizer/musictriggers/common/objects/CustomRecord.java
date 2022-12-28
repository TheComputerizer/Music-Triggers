package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {

    public CustomRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> components, ITooltipFlag flag) {
        if(stack.getOrCreateTag().contains("trackID"))
            components.add(AssetUtil.customLang("record.musictriggers.custom_record."+
                    ChannelManager.getChannel(stack.getOrCreateTag().getString("channelFrom")).getRecordMap()
                            .get(stack.getOrCreateTag().getString("trackID")),false));
        else components.add(AssetUtil.extraLang(Constants.MODID,"item","music_triggers_record","blank_description"));
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
