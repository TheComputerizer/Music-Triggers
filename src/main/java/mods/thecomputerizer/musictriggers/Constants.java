package mods.thecomputerizer.musictriggers;

import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Constants {
    public static final String MODID = "musictriggers";
    public static final ResourceLocation ICON_LOCATION = new ResourceLocation(MODID,"textures/logo.png");
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "1.12.2-6.0";
    public static final String DEPENDENCIES = "required-after:theimpossiblelibrary;";
    public static final Logger MAIN_LOG = LogManager.getLogger(MODID);
    public static final File CONFIG_DIR = new File("config/MusicTriggers");
}
