package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.network.NetworkHandler;
import mods.thecomputerizer.musictriggers.network.packets.PacketJukeBoxCustom;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Properties p) {
        super(p);
    }

    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if(state.getBlock()==BlockRegistry.MUSIC_RECORDER.get() && !state.getValue(MusicRecorder.HAS_RECORD)
                && !state.getValue(MusicRecorder.HAS_DISC)) {
            if (!ctx.getLevel().isClientSide) {
                MusicRecorder mr = (MusicRecorder) state.getBlock();
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(), ctx.getClickedPos(), stack, state, ctx.getPlayer());
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        } else if (state.getBlock() == Blocks.JUKEBOX && !state.getValue(JukeboxBlock.HAS_RECORD)) {
            Player player = ctx.getPlayer();
            if (!ctx.getLevel().isClientSide && player instanceof ServerPlayer) {
                ItemStack stack = player.getItemInHand(ctx.getHand());
                CompoundTag tag = stack.getOrCreateTag();
                if (tag.contains("trackID") && tag.contains("channelFrom")) {
                    ((JukeboxBlock) state.getBlock()).setRecord(player, ctx.getLevel(), ctx.getClickedPos(), state, stack);
                    NetworkHandler.sendTo(new PacketJukeBoxCustom(ctx.getClickedPos(),tag.getString("channelFrom"),
                            tag.getString("trackID")),(ServerPlayer)player);
                    stack.shrink(1);
                    player.awardStat(Stats.PLAY_RECORD);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, @Nonnull List<Component> components, @Nonnull TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        if(tag.contains("trackID"))
            components.add(MutableComponent.create(new LiteralContents(MutableComponent.create(
                    AssetUtil.extraLang(Constants.MODID,"item","music_triggers_record",
                            "description",false)).getString() +": "+tag.getString("trackID"))));
        else components.add(MutableComponent.create(AssetUtil.extraLang(Constants.MODID,"item","music_triggers_record",
                        "blank_description",false)));
    }

    public static float mapTriggerToFloat(String trigger) {
        return switch (trigger) {
            case "acidrain" -> 0.01f;
            case "biome" -> 0.02f;
            case "blizzard" -> 0.03f;
            case "bloodmoon" -> 0.04f;
            case "bluemoon" -> 0.05f;
            case "cloudy" -> 0.06f;
            case "command" -> 0.07f;
            case "creative" -> 0.08f;
            case "time" -> 0.09f;
            case "dead" -> 0.1f;
            case "difficulty" -> 0.11f;
            case "dimension" -> 0.12f;
            case "effect" -> 0.13f;
            case "elytra" -> 0.14f;
            case "fallingstars" -> 0.15f;
            case "fishing" -> 0.16f;
            case "gamestages" -> 0.17f;
            case "generic" -> 0.18f;
            case "gui" -> 0.19f;
            case "harvestmoon" -> 0.2f;
            case "height" -> 0.21f;
            case "hurricane" -> 0.22f;
            case "light" -> 0.23f;
            case "loading" -> 0.24f;
            case "lowhp" -> 0.25f;
            case "menu" -> 0.26f;
            case "mob" -> 0.27f;
            case "pet" -> 0.28f;
            case "pvp" -> 0.29f;
            case "raining" -> 0.3f;
            case "rainintensity" -> 0.31f;
            case "riding" -> 0.32f;
            case "sandstorm" -> 0.33f;
            case "season" -> 0.34f;
            case "snowing" -> 0.35f;
            case "spectator" -> 0.36f;
            case "storming" -> 0.37f;
            case "structure" -> 0.38f;
            case "tornado" -> 0.39f;
            case "underwater" -> 0.4f;
            case "victory" -> 0.41f;
            case "zones" -> 0.42f;
            default -> 0f;
        };
    }
}