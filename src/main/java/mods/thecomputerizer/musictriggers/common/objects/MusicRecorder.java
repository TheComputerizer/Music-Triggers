package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("NullableProblems")
public class MusicRecorder extends BlockContainer {

    public boolean has_record = false;

    public TileEntityMusicRecorder temr;

    public MusicRecorder() {
        super(Material.WOOD, MapColor.DIRT);
        setHardness(1F);
        setHarvestLevel("axe",3);
        setResistance(1F);
    }

    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (this.has_record)
        {
            MusicTriggers.logger.info(temr.getPos().getX()+" "+temr.getPos().getY()+" "+temr.getPos().getZ());
            this.dropRecord(worldIn, pos);
            this.has_record = false;
            playerIn.sendMessage(new TextComponentString("ejecting disc"));
            return true;
        }
        else
        {
            return false;
        }
    }

    public void insertRecord(ItemStack recordStack)
    {
        this.temr.setRecord(recordStack);
        this.has_record = true;
        MusicTriggers.logger.info("disc inserted");
    }

    private void dropRecord(World worldIn, BlockPos pos)
    {
        if (!worldIn.isRemote) {
            ItemStack itemstack = this.temr.getRecord();
            if (!itemstack.isEmpty()) {
                MusicTriggers.logger.info("is this activating?");
                this.temr.setRecord(ItemStack.EMPTY);
                double d0 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                EntityItem entityitem = new EntityItem(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntity(entityitem);
            }
        }
    }

    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        this.dropRecord(worldIn, pos);
        super.breakBlock(worldIn, pos, state);
    }

    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (!worldIn.isRemote)
        {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
        }
    }

    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        this.temr = new MusicRecorder.TileEntityMusicRecorder();
        return this.temr;
    }

    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    public static class TileEntityMusicRecorder extends TileEntity implements ITickable {

        public ItemStack record = ItemStack.EMPTY;
        private int tickCounter = 0;

        @Override
        public void update() {
            if(!world.isRemote) {
                if(!getRecord().isEmpty()) {
                    MusicTriggers.logger.info("empty tester");
                    this.tickCounter++;
                    int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
                    if(randomNum+tickCounter>=6000) {
                        EntityPig pig = new EntityPig(this.world);
                        pig.setPosition(this.getPos().getX(),this.getPos().getY()+1,this.getPos().getZ());
                        this.world.spawnEntity(pig);
                        this.tickCounter=0;
                        for (Item i : MusicTriggersItems.allItems) {
                            String itemName = Objects.requireNonNull(i.getRegistryName()).toString().replaceAll("musictriggers:","");
                            if(itemName.matches(MusicPlayer.curTrack)) {
                                this.setRecord(i.getDefaultInstance());
                            }
                        }
                    }
                }
                else {
                    this.tickCounter = 0;
                }
            }
        }

        public void readFromNBT(NBTTagCompound compound)
        {
            super.readFromNBT(compound);
        }

        public NBTTagCompound writeToNBT(NBTTagCompound compound)
        {
            super.writeToNBT(compound);
            return compound;
        }

        public ItemStack getRecord()
        {
            return this.record;
        }

        public void setRecord(ItemStack recordStack)
        {
            this.record = recordStack;
            this.markDirty();
        }
    }
}
