package mods.thecomputerizer.musictriggers.fabric.v16.m5.common;

import mods.thecomputerizer.musictriggers.fabric.v16.m5.client.MTDevResourceFinderFabric1_16_5;
import mods.thecomputerizer.musictriggers.fabric.v16.m5.client.MTMusicTickerFabric1_16_5;
import mods.thecomputerizer.musictriggers.fabric.v16.m5.client.MTSoundHandlerFabric1_16_5;
import mods.thecomputerizer.musictriggers.shared.v16.m5.common.MTMappingsHelper1_16_5;
import mods.thecomputerizer.theimpossiblelibrary.api.core.annotation.IndirectCallers;
import mods.thecomputerizer.theimpossiblelibrary.fabric.core.FabricHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Function;

@IndirectCallers
public class MTMappingsHelperFabric1_16_5 implements MTMappingsHelper1_16_5 {
    
    @Override public void addDevResources(Minecraft mc, File resourceDir) {
        RepositorySource source = new MTDevResourceFinderFabric1_16_5(resourceDir); //TODO Figure out how to apply this
    }
    
    @Override public <T> Field findField(Class<T> clazz, String srgName, String normalName, Class<?> type) {
        return FabricHelper.getObfField(normalName,clazz,type.getSuperclass());
    }
    
    @Override public Object makeMusicTicker(Minecraft mc) {
        return new MTMusicTickerFabric1_16_5(mc);
    }
    
    @Override public Object makeSoundHandler(Minecraft mc) {
        return new MTSoundHandlerFabric1_16_5(mc.getSoundManager(),mc.getResourceManager(),mc.options);
    }
    
    @Override public Function<Minecraft,Object> musicTickerGetter() {
        return Minecraft::getMusicManager;
    }
    
    @Override public Function<Minecraft,Object> soundHandlerGetter() {
        return Minecraft::getSoundManager;
    }
}