package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.PacketJukeBoxCustom;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Properties p) {
        super(p);
    }

    protected String tagString(ItemStack stack, String key) {
        String ret = stack.getOrCreateTag().getString(key);
        return ret.matches("") ? null : ret;
    }


    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if(state.getBlock()==BlockRegistry.MUSIC_RECORDER && !state.getValue(MusicRecorder.HAS_RECORD)
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
                String channel = tagString(stack,"channelFrom");
                String trackID = tagString(stack,"trackID");
                if (Objects.nonNull(channel) && Objects.nonNull(trackID)) {
                    ((JukeboxBlock) state.getBlock()).setRecord(ctx.getPlayer(),ctx.getLevel(),ctx.getClickedPos(),state,stack);
                    new PacketJukeBoxCustom(ctx.getClickedPos(),channel,trackID).addPlayers((ServerPlayer)player).send();
                    stack.shrink(1);
                    player.awardStat(Stats.PLAY_RECORD);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    protected Component getLang(String ... elements) {
        if(Objects.isNull(elements) || elements.length==0) return Component.literal(Constants.MODID);
        if(elements.length==1) return Component.translatable(elements[0]+"."+Constants.MODID);
        StringBuilder builder = new StringBuilder(elements[0]+"."+Constants.MODID+".");
        for(int i=1;i<elements.length;i++) {
            builder.append(elements[i]);
            if(i<elements.length-1) builder.append(".");
        }
        return Component.translatable(builder.toString());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, @NotNull List<Component> components, @NotNull TooltipFlag flag) {
        String trackID = tagString(stack,"trackID");
        if(Objects.nonNull(trackID))
            components.add(Component.literal(getLang("item","music_triggers_record","description")
                    .getString()+": "+trackID));
        else components.add(getLang("item","music_triggers_record","blank_description"));
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