package mods.thecomputerizer.musictriggers.server;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;
import java.util.Optional;

public class RegUtil {

    public static <T> Optional<String> get(MinecraftServer server, ResourceKey<Registry<T>> reg, T obj) {
        Optional<? extends Registry<T>> optionalReg = server.registryAccess().registry(reg);
        if(optionalReg.isEmpty()) return Optional.empty();
        ResourceLocation id = optionalReg.get().getKey(obj);
        return Objects.nonNull(id) ? Optional.of(id.toString()) : Optional.empty();
    }
}
