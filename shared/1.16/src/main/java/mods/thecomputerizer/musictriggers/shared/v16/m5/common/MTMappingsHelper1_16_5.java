package mods.thecomputerizer.musictriggers.shared.v16.m5.common;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Function;

public interface MTMappingsHelper1_16_5 {
    
    void addDevResources(Minecraft mc, File resourceDir);
    Field findField(Class<?> clazz, String srgName, String normalName, Class<?> type);
    Object makeMusicTicker(Minecraft mc);
    Object makeSoundHandler(Minecraft mc);
    Function<Minecraft,Object> musicTickerGetter();
    Function<Minecraft,Object> soundHandlerGetter();
}