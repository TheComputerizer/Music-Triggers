package mods.thecomputerizer.musictriggers.legacy.v12.m2.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.SoundCategory;

import javax.annotation.Nonnull;
import java.util.Objects;

public class MTSoundHandler1_12_2 extends SoundHandler {
    
    public MTSoundHandler1_12_2(IResourceManager manager, GameSettings settings) {
        super(manager,settings);
    }
    
    @Override
    public void setSoundLevel(@Nonnull SoundCategory category, float volume) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        if(Objects.nonNull(helper)) helper.setCategoryVolume(category.getName(),volume);
        super.setSoundLevel(category,volume);
    }
}
