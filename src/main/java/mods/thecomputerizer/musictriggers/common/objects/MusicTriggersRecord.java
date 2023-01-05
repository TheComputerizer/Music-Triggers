package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketJukeBoxCustom;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JukeboxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Properties s) {
        super(s);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (state.getBlock() instanceof MusicRecorder mr) {
            if(!ctx.getLevel().isClientSide && ctx.getPlayer()!=null && !state.getValue(MusicRecorder.HAS_RECORD) && !state.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),state,stack,ctx.getPlayer().getUUID());
                stack.shrink(1);
            }
            return InteractionResult.SUCCESS;
        } else if (state.getBlock() instanceof JukeboxBlock && !(Boolean)state.getValue(JukeboxBlock.HAS_RECORD)) {
            if (!ctx.getLevel().isClientSide && ctx.getPlayer()!=null) {
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                if(stack.getOrCreateTag().contains("trackID") && stack.getOrCreateTag().contains("channelFrom")) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(ctx.getLevel(),ctx.getClickedPos(),state,stack);
                    PacketHandler.sendTo(new PacketJukeBoxCustom(ctx.getClickedPos(),stack.getOrCreateTag().getString("channelFrom"),stack.getOrCreateTag().getString("trackID")),(ServerPlayer) ctx.getPlayer());
                    stack.shrink(1);
                    ctx.getPlayer().awardStat(Stats.PLAY_RECORD);
                }
            }
            return InteractionResult.SUCCESS;
        } else return super.useOn(ctx);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        if(stack.getOrCreateTag().contains("trackID"))
            tooltip.add(new TextComponent(getDescription().getString()+": "+stack.getOrCreateTag().getString("trackID")));
        else tooltip.add(new TranslatableComponent("item.musictriggers.music_triggers_record.blank_description"));
    }

    public MutableComponent getDescription() {
        return new TranslatableComponent(this.getDescriptionId() + ".desc");
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
            case "day" -> 0.09f;
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
            case "night" -> 0.28f;
            case "pet" -> 0.29f;
            case "pvp" -> 0.3f;
            case "raining" -> 0.31f;
            case "rainintensity" -> 0.32f;
            case "riding" -> 0.33f;
            case "sandstorm" -> 0.34f;
            case "season" -> 0.35f;
            case "snowing" -> 0.36f;
            case "spectator" -> 0.37f;
            case "storming" -> 0.38f;
            case "structure" -> 0.39f;
            case "sunrise" -> 0.4f;
            case "sunset" -> 0.41f;
            case "tornado" -> 0.42f;
            case "underwater" -> 0.43f;
            case "victory" -> 0.44f;
            case "zones" -> 0.45f;
            default -> 0f;
        };
    }
}