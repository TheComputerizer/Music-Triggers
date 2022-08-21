package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.config.ConfigMain;
import mods.thecomputerizer.musictriggers.util.RegistryHandler;
import mods.thecomputerizer.musictriggers.util.packets.PacketJukeBoxCustom;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

public class MusicTriggersRecord extends Item {

    public MusicTriggersRecord() {
        for(String trigger : ConfigMain.triggers) {
            addPropertyOverride(new ResourceLocation(MusicTriggers.MODID, "trigger_"+trigger),
                    (stack, worldIn, entityIn) -> addTriggerPropertyGetter(trigger,stack));
        }
        for(String trigger : ConfigMain.modtriggers) {
            addPropertyOverride(new ResourceLocation(MusicTriggers.MODID, "trigger_"+trigger),
                    (stack, worldIn, entityIn) -> addTriggerPropertyGetter(trigger,stack));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        if (iblockstate.getBlock() instanceof MusicRecorder) {
            MusicRecorder mr = (MusicRecorder) iblockstate.getBlock();
            if(!worldIn.isRemote && !iblockstate.getValue(MusicRecorder.HAS_RECORD) && !iblockstate.getValue(MusicRecorder.HAS_DISC)) {
                ItemStack itemstack = player.getHeldItem(hand);
                mr.insertRecord(worldIn,pos,iblockstate,itemstack,player.getUniqueID());
                itemstack.shrink(1);
            }
            return EnumActionResult.SUCCESS;
        } else if (iblockstate.getBlock() instanceof BlockJukebox && !(Boolean)iblockstate.getValue(BlockJukebox.HAS_RECORD)) {
            if (!worldIn.isRemote) {
                ItemStack stack = player.getHeldItem(hand);
                if(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("trackID") && stack.getTagCompound().hasKey("channelFrom")) {
                    ((BlockJukebox) Blocks.JUKEBOX).insertRecord(worldIn, pos, iblockstate, stack);
                    RegistryHandler.network.sendTo(new PacketJukeBoxCustom.PacketJukeBoxCustomMessage(pos,stack.getTagCompound().getString("channelFrom"),stack.getTagCompound().getString("trackID")),(EntityPlayerMP) player);
                    stack.shrink(1);
                    player.addStat(StatList.RECORD_PLAYED);
                }
            }
            return EnumActionResult.SUCCESS;
        }
        else return EnumActionResult.PASS;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if(stack.hasTagCompound() && Objects.requireNonNull(stack.getTagCompound()).hasKey("trackID"))
            return stack.getTagCompound().getString("trackID");
        return "Music Triggers Record";
    }

    @Override
    public net.minecraftforge.common.IRarity getForgeRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @SideOnly(Side.CLIENT)
    public float addTriggerPropertyGetter(String trigger, ItemStack stack) {
        if(stack.getTagCompound()!=null && stack.getTagCompound().hasKey("triggerID") && stack.getTagCompound().getString("triggerID").matches(trigger))
            return mapTriggerToFloat(trigger);
        return 1f;
    }

    private float mapTriggerToFloat(String trigger) {
        switch (trigger) {
            case "acidrain":
                return 0f;
            case "biome":
                return 0.01f;
            case "blizzard":
                return 0.02f;
            case "bloodmoon":
                return 0.03f;
            case "bluemoon":
                return 0.04f;
            case "cloudy":
                return 0.05f;
            case "command":
                return 0.06f;
            case "creative":
                return 0.07f;
            case "day":
                return 0.08f;
            case "dead":
                return 0.09f;
            case "difficulty":
                return 0.1f;
            case "dimension":
                return 0.11f;
            case "effect":
                return 0.12f;
            case "elytra":
                return 0.13f;
            case "fallingstars":
                return 0.14f;
            case "fishing":
                return 0.15f;
            case "gamestages":
                return 0.16f;
            case "generic":
                return 0.17f;
            case "gui":
                return 0.18f;
            case "harvestmoon":
                return 0.19f;
            case "height":
                return 0.2f;
            case "hurricane":
                return 0.21f;
            case "light":
                return 0.22f;
            case "loading":
                return 0.23f;
            case "lowhp":
                return 0.24f;
            case "menu":
                return 0.25f;
            case "mob":
                return 0.26f;
            case "night":
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
            case "sunrise":
                return 0.39f;
            case "sunset":
                return 0.4f;
            case "tornado":
                return 0.41f;
            case "underwater":
                return 0.42f;
            case "victory":
                return 0.43f;
            case "zones":
                return 0.44f;
            default:
                return 1f;
        }
    }


}