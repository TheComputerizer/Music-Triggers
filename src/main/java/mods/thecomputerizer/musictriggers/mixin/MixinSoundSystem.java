package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.util.audio.SoundSourceExtension;
import net.minecraft.client.audio.SoundSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(targets = "net.minecraft.client.audio.SoundSystem$HandlerImpl")
public class MixinSoundSystem {

    @Final
    @Shadow
    private Set<SoundSource> activeChannels;

    @Final
    @Shadow
    private int limit;

    /**
     * @author The_Computerizer
     */
    @Nullable
    @Overwrite
    public SoundSource acquire() {
        if (this.activeChannels.size() >= this.limit) {
            MusicTriggers.logger.warn("Maximum sound pool size {} reached", this.limit);
            return null;
        } else {
            SoundSourceExtension soundsource = SoundSourceExtension.create();
            if (soundsource != null) this.activeChannels.add(soundsource);
            return soundsource;
        }
    }
}
