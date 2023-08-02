package mods.thecomputerizer.musictriggers.registry.items;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.network.PacketJukeBoxCustom;
import mods.thecomputerizer.musictriggers.registry.BlockRegistry;
import mods.thecomputerizer.musictriggers.registry.blocks.MusicRecorder;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord(Item.Properties p) {
        super(p);
    }

    protected String tagString(ItemStack stack,String key) {
        String ret = stack.getOrCreateTag().getString(key);
        return ret.matches("") ? null : ret;
    }


    @Override
    @Nonnull
    public ActionResultType useOn(ItemUseContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if(state.getBlock()==BlockRegistry.MUSIC_RECORDER.get() && !state.getValue(MusicRecorder.HAS_RECORD)
                && !state.getValue(MusicRecorder.HAS_DISC)) {
            if (!ctx.getLevel().isClientSide) {
                MusicRecorder mr = (MusicRecorder) state.getBlock();
                ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
                mr.insertRecord(ctx.getLevel(), ctx.getClickedPos(), stack, state, ctx.getPlayer());
                stack.shrink(1);
            }
            return ActionResultType.SUCCESS;
        } else if (state.getBlock() == Blocks.JUKEBOX && !state.getValue(JukeboxBlock.HAS_RECORD)) {
            PlayerEntity player = ctx.getPlayer();
            if (!ctx.getLevel().isClientSide && player instanceof ServerPlayerEntity) {
                ItemStack stack = player.getItemInHand(ctx.getHand());
                String channel = tagString(stack,"channelFrom");
                String trackID = tagString(stack,"trackID");
                if (Objects.nonNull(channel) && Objects.nonNull(trackID)) {
                    ((JukeboxBlock) state.getBlock()).setRecord(ctx.getLevel(), ctx.getClickedPos(), state, stack);
                    new PacketJukeBoxCustom(ctx.getClickedPos(),channel,trackID).addPlayers((ServerPlayerEntity)player).send();
                    stack.shrink(1);
                    player.awardStat(Stats.PLAY_RECORD);
                }
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @OnlyIn(Dist.CLIENT)
    protected ITextComponent getLang(String ... elements) {
        if(Objects.isNull(elements) || elements.length==0) return new StringTextComponent(Constants.MODID);
        if(elements.length==1) return new TranslationTextComponent(elements[0]+"."+Constants.MODID);
        StringBuilder builder = new StringBuilder(elements[0]+"."+Constants.MODID+".");
        for(int i=1;i<elements.length;i++) {
            builder.append(elements[i]);
            if(i<elements.length-1) builder.append(".");
        }
        return AssetUtil.customLang(builder.toString(),false);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable World world, @Nonnull List<ITextComponent> components, @Nonnull ITooltipFlag flag) {
        String trackID = tagString(stack,"trackID");
        if(Objects.nonNull(trackID))
            components.add(new StringTextComponent(getLang("item","music_triggers_record","description")
                    .getString()+": "+trackID));
        else components.add(getLang("item","music_triggers_record","blank_description"));
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
            case "time":
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
            case "pet":
                return 0.28f;
            case "pvp":
                return 0.29f;
            case "raining":
                return 0.3f;
            case "rainintensity":
                return 0.31f;
            case "riding":
                return 0.32f;
            case "sandstorm":
                return 0.33f;
            case "season":
                return 0.34f;
            case "snowing":
                return 0.35f;
            case "spectator":
                return 0.36f;
            case "storming":
                return 0.37f;
            case "structure":
                return 0.38f;
            case "tornado":
                return 0.39f;
            case "underwater":
                return 0.4f;
            case "victory":
                return 0.41f;
            case "zones":
                return 0.42f;
            default:
                return 0f;
        }
    }
}