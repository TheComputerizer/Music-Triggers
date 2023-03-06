package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CustomRecord extends MusicTriggersRecord {

    public CustomRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, @Nonnull List<Component> components, @Nonnull TooltipFlag flag) {
        if(stack.getOrCreateTag().contains("trackID"))
            components.add(AssetUtil.customLang("record.musictriggers.custom_record."+
                    ChannelManager.getChannel(stack.getOrCreateTag().getString("channelFrom")).getRecordMap()
                            .get(stack.getOrCreateTag().getString("trackID")),false));
        else components.add(AssetUtil.extraLang(Constants.MODID,"item","music_triggers_record","blank_description",false));
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
