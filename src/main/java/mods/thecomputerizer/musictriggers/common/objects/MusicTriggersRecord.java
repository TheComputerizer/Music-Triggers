package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.util.PacketHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketJukeBoxCustom;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Properties p) {
        super(p);
    }

    @Override
    public ActionResultType useOn(ItemUseContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (state.getBlock() instanceof MusicRecorder) {
            MusicRecorder mr = (MusicRecorder) state.getBlock();
            if(!ctx.getLevel().isClientSide && ctx.getPlayer()!=null && !state.getValue(MusicRecorder.HAS_RECORD) && !state.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(),ctx.getClickedPos(),state,stack,ctx.getPlayer().getUUID());
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        } else if (state.getBlock() instanceof JukeboxBlock && !(Boolean)state.getValue(JukeboxBlock.HAS_RECORD)) {
            if (!ctx.getLevel().isClientSide && ctx.getPlayer()!=null) {
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                if(stack.getOrCreateTag().contains("trackID") && stack.getOrCreateTag().contains("channelFrom")) {
                    ((JukeboxBlock) Blocks.JUKEBOX).setRecord(ctx.getLevel(),ctx.getClickedPos(),state,stack);
                    PacketHandler.sendTo(new PacketJukeBoxCustom(ctx.getClickedPos(),stack.getOrCreateTag().getString("channelFrom"),stack.getOrCreateTag().getString("trackID")),(ServerPlayerEntity) ctx.getPlayer());
                    stack.shrink(1);
                    ctx.getPlayer().awardStat(Stats.PLAY_RECORD);
                }
            }
            return ActionResultType.SUCCESS;
        } else return super.useOn(ctx);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, List<ITextComponent> components, ITooltipFlag flag) {
        if(stack.getOrCreateTag().contains("trackID"))
            components.add(new StringTextComponent(getDescription().getString()+": "+stack.getOrCreateTag().getString("trackID")));
        else components.add(new TranslationTextComponent("item.musictriggers.music_triggers_record.blank_description"));
    }

    public static float mapTriggerToFloat(String trigger) {
        switch (trigger) {
            case "acidrain":
                return 0.01f;
            case "biome":
                return 0.02f;
            case "blizzard":
                return 0.03f;
            case "bloodmoon":
                return 0.04f;
            case "bluemoon":
                return 0.05f;
            case "cloudy":
                return 0.06f;
            case "command":
                return 0.07f;
            case "creative":
                return 0.08f;
            case "day":
                return 0.09f;
            case "dead":
                return 0.1f;
            case "difficulty":
                return 0.11f;
            case "dimension":
                return 0.12f;
            case "effect":
                return 0.13f;
            case "elytra":
                return 0.14f;
            case "fallingstars":
                return 0.15f;
            case "fishing":
                return 0.16f;
            case "gamestages":
                return 0.17f;
            case "generic":
                return 0.18f;
            case "gui":
                return 0.19f;
            case "harvestmoon":
                return 0.2f;
            case "height":
                return 0.21f;
            case "hurricane":
                return 0.22f;
            case "light":
                return 0.23f;
            case "loading":
                return 0.24f;
            case "lowhp":
                return 0.25f;
            case "menu":
                return 0.26f;
            case "mob":
                return 0.27f;
            case "night":
                return 0.28f;
            case "pet":
                return 0.29f;
            case "pvp":
                return 0.3f;
            case "raining":
                return 0.31f;
            case "rainintensity":
                return 0.32f;
            case "riding":
                return 0.33f;
            case "sandstorm":
                return 0.34f;
            case "season":
                return 0.35f;
            case "snowing":
                return 0.36f;
            case "spectator":
                return 0.37f;
            case "storming":
                return 0.38f;
            case "structure":
                return 0.39f;
            case "sunrise":
                return 0.4f;
            case "sunset":
                return 0.41f;
            case "tornado":
                return 0.42f;
            case "underwater":
                return 0.43f;
            case "victory":
                return 0.44f;
            case "zones":
                return 0.45f;
            default:
                return 0f;
        }
    }
}