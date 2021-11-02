package mods.thecomputerizer.musictriggers.common.objects;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import mods.thecomputerizer.musictriggers.util.packetCurSong;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

    public boolean has_record = false;

    public MusicRecorder() {
        super(Material.WOOD, MapColor.DIRT);
        setHardness(1F);
        setHarvestLevel("axe",3);
        setResistance(1F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (this.has_record)
        {
            this.dropRecord(worldIn, pos);
            this.has_record = false;
            return true;
        }
        else
        {
            return false;
        }
    }

    public void insertRecord(World worldIn, BlockPos pos, ItemStack recordStack, UUID uuid)
    {
        TileEntityMusicRecorder te = (TileEntityMusicRecorder)worldIn.getTileEntity(pos);
        assert te != null;
        te.setRecord(recordStack);
        te.setUUID(uuid);
        this.has_record = true;
    }

    private void dropRecord(World worldIn, BlockPos pos)
    {
        if (!worldIn.isRemote) {
            TileEntityMusicRecorder te = (TileEntityMusicRecorder)worldIn.getTileEntity(pos);
            assert te != null;
            ItemStack itemstack = te.getRecord();
            if (!itemstack.isEmpty()) {
                te.setRecord(ItemStack.EMPTY);
                double d0 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                double d1 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
                double d2 = (double) (worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
                EntityItem entityitem = new EntityItem(worldIn, (double) pos.getX() + d0, (double) pos.getY() + d1, (double) pos.getZ() + d2, itemstack);
                entityitem.setDefaultPickupDelay();
                worldIn.spawnEntity(entityitem);
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

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    public static class TileEntityMusicRecorder extends TileEntity implements ITickable {

        private ItemStack record = ItemStack.EMPTY;
        private int tickCounter = 0;
        private UUID playeruuid;

        @Override
        public void update() {
            if(!world.isRemote) {
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

        public ItemStack getRecord()
        {
            return this.record;
        }

        public void setRecord(ItemStack recordStack)
        {
            this.record = recordStack;
        }

        public UUID getUUID() {
            return this.playeruuid;
        }

        public void setUUID(UUID uuid) {
            this.playeruuid = uuid;
        }
    }
}
