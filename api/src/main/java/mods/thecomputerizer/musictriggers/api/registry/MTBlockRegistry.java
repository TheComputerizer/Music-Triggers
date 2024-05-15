package mods.thecomputerizer.musictriggers.api.registry;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.BlockAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.BlockHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.BlockStateAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.MaterialHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemStackAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.RegistryHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.tag.CompoundTagAPI;

import java.util.Objects;

import static mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult.PASS;
import static mods.thecomputerizer.theimpossiblelibrary.api.common.item.ActionResult.SUCCESS;

public class MTBlockRegistry {
    
    public static final BlockAPI<?> MUSIC_RECORDER = RegistryHelper.makeBlockBuilder()
            .setRegistryName(MTRef.res("music_recorder"))
            .setMaterial(MaterialHelper.getByName("wood"))
            .setMaterialColor(MaterialHelper.getColorByName("dirt"))
            .addDefaultProperty(BlockHelper.createProperty("recording",false),false)
            .addDefaultProperty(BlockHelper.createProperty("recording_special",false),false)
            .setUseFunc(ctx -> {
                if(!ctx.getWorld().isClient()) {
                    BlockStateAPI<?> state = ctx.getState();
                    if(MTRef.res("music_recorder").equals(state.getBlock().getRegistryName())) {
                        if(state.getPropertyBool("recording") || state.getPropertyBool("recording_special"))
                            return PASS;
                        ItemStackAPI<?> stack = ctx.getPlayer().getStackInHand(ctx.getHand());
                        if(MTRef.res("record").equals(stack.getItem().getRegistryName())) {
                            CompoundTagAPI<?> tag = stack.getTag();
                            boolean isSpecial = Objects.nonNull(tag) && tag.contains("channel") &&
                                                tag.contains("triggerID") && (tag.contains("audio") ||
                                                                              tag.contains("custom"));
                            ctx.getWorld().setState(ctx.getPos(),state.withProperty(
                                    isSpecial ? "recording_special" : "recording",true));
                            stack.decrement();
                            return SUCCESS;
                        }
                    }
                }
                return PASS;
            })
            .setBlockEntityCreator(MTBlockEntityRegistry::createRecorderEntity).build();
}