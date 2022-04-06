package mods.thecomputerizer.musictriggers.core;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("Music Triggers Core")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("mods.thecomputerizer.musictriggers.core.LoadingPlugin")
@IFMLLoadingPlugin.SortingIndex(1000000)
public class LoadingPlugin implements IFMLLoadingPlugin {
    private static final Logger logger = LogManager.getLogger("MusicTriggersMixin");

    static {
        logger.info("Initializing Music Triggers Mixin");
        MixinBootstrap.init();
        Mixins.addConfiguration("musictriggers.mixin.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
