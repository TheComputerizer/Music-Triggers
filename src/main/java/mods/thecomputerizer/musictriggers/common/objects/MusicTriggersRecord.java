package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketJukeBoxCustom;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Settings s) {
        super(s);
    }

    @Override
    public @NotNull ActionResult useOnBlock(ItemUsageContext ctx) {
        BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (state.getBlock() instanceof MusicRecorder mr) {
            if(!ctx.getWorld().isClient && ctx.getPlayer()!=null && !state.get(MusicRecorder.HAS_RECORD) && !state.get(MusicRecorder.HAS_DISC)) {
                ItemStack stack = ctx.getPlayer().getStackInHand(ctx.getHand());
                mr.insertRecord(ctx.getWorld(),ctx.getBlockPos(),state,stack,ctx.getPlayer().getUuid());
                stack.decrement(1);
            }
            return ActionResult.SUCCESS;
        } else if (state.getBlock() instanceof JukeboxBlock && !(Boolean)state.get(JukeboxBlock.HAS_RECORD)) {
            if (!ctx.getWorld().isClient && ctx.getPlayer()!=null) {
                ItemStack stack = ctx.getPlayer().getStackInHand(ctx.getHand());
                if(stack.getOrCreateNbt().contains("trackID") && stack.getOrCreateNbt().contains("channelFrom")) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(ctx.getWorld(),ctx.getBlockPos(),state,stack);
                    PacketHandler.sendTo(new PacketJukeBoxCustom(ctx.getBlockPos(),stack.getOrCreateNbt().getString("channelFrom"),stack.getOrCreateNbt().getString("trackID")),(ServerPlayerEntity) ctx.getPlayer());
                    stack.decrement(1);
                    ctx.getPlayer().incrementStat(Stats.PLAY_RECORD);
                }
            }
            return ActionResult.SUCCESS;
        } else return super.useOnBlock(ctx);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if(stack.getOrCreateNbt().contains("trackID"))
            tooltip.add(new LiteralText(getDescription().getString()+": "+stack.getOrCreateNbt().getString("trackID")));
        else tooltip.add(new TranslatableText("item.musictriggers.music_triggers_record.blank_description"));
    }

    public MutableText getDescription() {
        return new TranslatableText(this.getTranslationKey() + ".desc");
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