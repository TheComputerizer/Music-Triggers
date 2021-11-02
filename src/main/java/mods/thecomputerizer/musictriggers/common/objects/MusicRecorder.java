package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.util.packetCurSong;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("NullableProblems")
public class MusicRecorder extends BlockContainer {

    public static final PropertyBool HAS_RECORD = PropertyBool.create("has_record");

    public MusicRecorder() {
        super(Material.WOOD, MapColor.DIRT);
        setHardness(1F);
        this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_RECORD,false));
        setHarvestLevel("axe",3);
        setResistance(1F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (state.getValue(HAS_RECORD))
        {
            this.dropRecord(worldIn, pos);
            state = state.withProperty(HAS_RECORD,false);
            worldIn.setBlockState(pos, state, 2);
            return true;
        }
        else
        {
            return false;
        }
    }

    public void insertRecord(World worldIn, BlockPos pos, IBlockState state, ItemStack recordStack, UUID uuid)
    {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof MusicRecorder.TileEntityMusicRecorder) {
            ((MusicRecorder.TileEntityMusicRecorder)te).setRecord(recordStack.copy());
            ((MusicRecorder.TileEntityMusicRecorder)te).setUUID(uuid);
            worldIn.setBlockState(pos, state.withProperty(HAS_RECORD, Boolean.TRUE), 2);
        }
    }

    private void dropRecord(World worldIn, BlockPos pos)
    {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof MusicRecorder.TileEntityMusicRecorder) {
                MusicRecorder.TileEntityMusicRecorder temr = (MusicRecorder.TileEntityMusicRecorder)te;
                ItemStack itemstack = temr.getRecord();
                if (!itemstack.isEmpty()) {
                    temr.setRecord(ItemStack.EMPTY);
                    double d0 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                    double d1 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                    double d2 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                    ItemStack itemstack1 = itemstack.copy();
                    EntityItem entityitem = new EntityItem(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack1);
                    entityitem.setDefaultPickupDelay();
                    worldIn.spawnEntity(entityitem);
                }
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        this.dropRecord(worldIn, pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune)
    {
        if (!worldIn.isRemote)
        {
            super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new MusicRecorder.TileEntityMusicRecorder();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(IBlockState state)
    {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos)
    {
        TileEntity tileentity = worldIn.getTileEntity(pos);

        if (tileentity instanceof MusicRecorder.TileEntityMusicRecorder)
        {
            ItemStack itemstack = ((MusicRecorder.TileEntityMusicRecorder)tileentity).getRecord();

            if (!itemstack.isEmpty())
            {
                return 15;
            }
        }
        return 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(HAS_RECORD, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(HAS_RECORD) ? 1 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, HAS_RECORD);
    }

    public static class TileEntityMusicRecorder extends TileEntity implements ITickable {

        private ItemStack record = ItemStack.EMPTY;
        private int tickCounter = 0;
        private UUID playeruuid;

        @Override
        public void update() {
            if(!world.isRemote) {
                MusicTriggers.logger.info(this.getRecord().getDisplayName());
                if(!this.getRecord().isEmpty()) {
                    this.tickCounter++;
                    int randomNum = ThreadLocalRandom.current().nextInt(0, 5600);
                    if(randomNum+tickCounter>=6000) {
                        //this.world.playSound(null, pos, Objects.requireNonNull(SoundEvent.REGISTRY.getObject(new ResourceLocation("minecraft", "block.enderchest.open"))), SoundCategory.MASTER, 1F, 1F);
                        EntityLightningBolt lightning = new EntityLightningBolt(this.world,this.pos.getX(),this.pos.getY(),this.pos.getZ(),true);
                        this.world.spawnEntity(lightning);
                        this.tickCounter=0;
                        for (Item i : MusicTriggersItems.allItems) {
                            String itemName = Objects.requireNonNull(i.getRegistryName()).toString().replaceAll("musictriggers:","");
                            if(itemName.matches(packetCurSong.curSong.get(getUUID()))) {
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

        @Override
        public void readFromNBT(NBTTagCompound compound)
        {
            super.readFromNBT(compound);
            if (compound.hasKey("BlankRecord"))
            {
                this.setRecord(new ItemStack(compound.getCompoundTag("BlankRecord")));
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound compound)
        {
            super.writeToNBT(compound);
            if (!this.getRecord().isEmpty())
            {
                compound.setTag("BlankRecord", this.getRecord().writeToNBT(new NBTTagCompound()));
            }
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

        public UUID getUUID() {
            return this.playeruuid;
        }

        public void setUUID(UUID uuid) {
            this.playeruuid = uuid;
        }
    }
}
