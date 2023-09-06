package mods.thecomputerizer.musictriggers.core;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Constants {
    public static final String MODID = "musictriggers";
    public static final ResourceLocation ICON_LOCATION = new ResourceLocation(MODID,"textures/logo.png");
    public static final String NAME = "Music Triggers";
    public static final String VERSION = "1.12.2-6.3";
    public static final String DEPENDENCIES = "required-after:theimpossiblelibrary;required-after:mixinbooter;";
    public static final Logger MAIN_LOG = LogManager.getLogger(NAME);
    public static final File CONFIG_DIR = new File("config/MusicTriggers");
    public static final char[] BLACKLISTED_TABLE_CHARACTERS = new char[]{' ','.',')','(','[',']'};

    /**
     * In case I forget to or choose not remove some log spam, this will only ensure it only happens in dev
     */
    private static final boolean IS_DEV = false;

    /**
     * Used for dev purposes only for easier debuging purposes
     */
    @SideOnly(Side.CLIENT)
    public static void debugError(String message, Object ... parameters) {
        if(IS_DEV && !Minecraft.getMinecraft().isGamePaused()) MAIN_LOG.error(message,parameters);
    }

    /**
     * Minecraft is a client only class so it cant be checked from the server without packets
     */
    public static void debugErrorServer(String message, Object ... parameters) {
        if(IS_DEV) MAIN_LOG.error(message,parameters);
    }

    public static ResourceLocation res(String path) {
        return new ResourceLocation(MODID,path);
    }
}
