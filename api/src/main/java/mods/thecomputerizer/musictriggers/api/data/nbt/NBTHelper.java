package mods.thecomputerizer.musictriggers.api.data.nbt;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.nbt.mode.*;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.TagHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;

public class NBTHelper {

    private static final Map<String,NBTMode> REGISTERED_MODES = getDefaultModes();

    private static Map<String,NBTMode> getDefaultModes() {
        Map<String,NBTMode> map = new HashMap<>();
        putMode(map,new NBTModeEqual("EQUAL"));
        putMode(map,new NBTModeGreater("GREATER"));
        putMode(map,new NBTModeInvert("INVERT"));
        putMode(map,new NBTModeKeyPresent("KEY_PRESENT"));
        putMode(map,new NBTModeLesser("LESSER"));
        putMode(map,new NBTModeValPresent("VAL_PRESENT"));
        return map;
    }

    public static @Nullable NBTMode getMode(String name) {
        return REGISTERED_MODES.get(name);
    }

    public static @Nullable NBTMode getAndInitMode(String ... split) {
        NBTMode mode = split.length>1 ? REGISTERED_MODES.get(split[0]) : null;
        if(Objects.nonNull(mode)) mode.setSplit(Arrays.copyOfRange(split,1,split.length));
        return mode;
    }

    private static <M extends NBTMode> void putMode(Map<String,NBTMode> map, M mode) {
        map.put(mode.getName(),mode);
    }
    
    public static CompoundTagAPI<?> readGlobalData(ChannelHelper helper) {
        CompoundTagAPI<?> globalData = null;
        String uuid = helper.getPlayerID();
        try {
            globalData = TagHelper.getOrCreateCompound(TagHelper.getGlobalData(MODID,true),uuid);
            helper.onLoaded(TagHelper.getOrCreateCompound(TagHelper.getGlobalData(MODID,true),uuid));
        } catch(IOException ex) {
            ChannelHelper.logGlobalError("Failed to read persistent global data!",ex);
        }
        return Objects.nonNull(globalData) ? globalData : TagHelper.makeCompoundTag();
    }
    
    public static CompoundTagAPI<?> readWorldData(ChannelHelper helper) {
        CompoundTagAPI<?> worldData = null;
        String uuid = helper.getPlayerID();
        try {
            worldData = TagHelper.getOrCreateCompound(TagHelper.getWorldData(MODID),uuid);
            helper.onConnected(worldData);
        } catch(IOException ex) {
            ChannelHelper.logGlobalError("Failed to read data for uuid {}",uuid);
        }
        return Objects.nonNull(worldData) ? worldData : TagHelper.makeCompoundTag();
    }

    public static <M extends NBTMode> void registerMode(M mode, boolean overrideDefault) {
        String name = mode.getName();
        if(REGISTERED_MODES.containsKey(name)) {
            if(overrideDefault) REGISTERED_MODES.put(name,mode);
            else ChannelHelper.logGlobalWarn("There is already a NBT mode with the name `{}` registered to `{}`! If you know "+
                    "what you are doing and want to override it anyways make sure to call NBTHelper#registerMode with "+
                    "overrideDefault set to true.",name,REGISTERED_MODES.get(name));
        } else REGISTERED_MODES.put(name,mode);
    }
    
    public static void saveGlobalData(ChannelHelper helper) {
        String uuid = helper.getPlayerID();
        CompoundTagAPI<?> globalData = TagHelper.getWorldData(MODID);
        if(Objects.isNull(globalData)) return;
        try {
            CompoundTagAPI<?> playerData = TagHelper.getOrCreateCompound(globalData,uuid);
            helper.saveWorldTo(playerData);
            if(!globalData.isEmpty()) TagHelper.writeGlobalData(globalData,MODID);
        } catch(IOException ex) {
            ChannelHelper.logGlobalFatal("Failed to save persistent global data!",ex);
        }
    }
    
    public static void saveWorldData(ChannelHelper helper) {
        String uuid = helper.getPlayerID();
        CompoundTagAPI<?> worldData = TagHelper.getWorldData(MODID);
        if(Objects.isNull(worldData)) return;
        try {
            CompoundTagAPI<?> playerData = TagHelper.getOrCreateCompound(worldData,uuid);
            ChannelHelper.logGlobalInfo("Player data for uuid {} is {}",uuid,playerData);
            helper.saveWorldTo(playerData);
            ChannelHelper.logGlobalInfo("Player data for uuid {} is now {}",uuid,playerData);
            if(!worldData.isEmpty()) TagHelper.writeWorldData(worldData,MODID);
        } catch(IOException ex) {
            ChannelHelper.logGlobalFatal("Failed to save persistent world data!",ex);
        }
    }
}