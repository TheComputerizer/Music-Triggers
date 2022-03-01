package mods.thecomputerizer.musictriggers.mixin;

import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class MixinResourcePack {

    @Inject(method = "register", at = @At("TAIL"))
    private void register(Consumer<ResourcePackProfile> consumer, ResourcePackProfile.Factory factory, CallbackInfo ci) {
        File resourcepack = new File(".","config/MusicTriggers/songs");
        if(resourcepack.exists()) {
            MusicTriggersCommon.logger.info("Resource Packkkkkkkkkkkkkkkkk enabled: "+resourcepack.getAbsolutePath());
            Supplier<ResourcePack> resourcePackSupplier = () -> new DirectoryResourcePack(resourcepack);
            ResourcePackProfile profile = new ResourcePackProfile("songs",true,resourcePackSupplier, Text.of("Music Triggers Songs"),Text.of("Can you believe this was generated automatically?"), ResourcePackCompatibility.COMPATIBLE, ResourcePackProfile.InsertionPosition.TOP, true, ResourcePackSource.PACK_SOURCE_BUILTIN);
            consumer.accept(profile);
        }
    }
}
