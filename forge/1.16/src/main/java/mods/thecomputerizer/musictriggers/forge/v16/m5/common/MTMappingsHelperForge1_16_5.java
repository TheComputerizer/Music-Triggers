package mods.thecomputerizer.musictriggers.forge.v16.m5.common;

import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTDevResourceFinderForge1_16_5;
import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTMusicTickerForge1_16_5;
import mods.thecomputerizer.musictriggers.forge.v16.m5.client.MTSoundHandlerForge1_16_5;
import mods.thecomputerizer.musictriggers.shared.v16.m5.common.MTMappingsHelper1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Function;

@IndirectCallers
public class MTMappingsHelperForge1_16_5 implements MTMappingsHelper1_16_5 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        mc.getResourcePackRepository().addPackFinder(new MTDevResourceFinderForge1_16_5(resourceDir));
    }
    
    @Override public <T> Field findField(Class<T> clazz, String srgName, String normalName, Class<?> type) {
        return ObfuscationReflectionHelper.findField(clazz,srgName);
    }
    
    @Override public Object makeMusicTicker(Minecraft mc) {
        return new MTMusicTickerForge1_16_5(mc);
    }
    
    @Override public Object makeSoundHandler(Minecraft mc) {
        return new MTSoundHandlerForge1_16_5(mc.getSoundManager(),mc.getResourceManager(),mc.options);
    }
    
    @Override public Function<Minecraft,Object> musicTickerGetter() {
        return Minecraft::getMusicManager;
    }
    
    @Override public Function<Minecraft,Object> soundHandlerGetter() {
        return Minecraft::getSoundManager;
    }
}