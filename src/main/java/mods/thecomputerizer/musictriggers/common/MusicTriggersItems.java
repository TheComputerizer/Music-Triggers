package mods.thecomputerizer.musictriggers.common;

import com.google.common.collect.Lists;
import net.minecraft.item.*;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class MusicTriggersItems {
    public static List<Item> allItems;
    public static final MusicTriggersItems INSTANCE = new MusicTriggersItems();

    public void init() {
        SoundHandler.registerSounds();
        allItems = Lists.newArrayList();
        for (SoundEvent s : SoundHandler.allSoundEvents) {
            Item i = (new MusicDiscItem(15,s,new Item.Properties().rarity(Rarity.EPIC).stacksTo(1).tab(new ItemGroup("Music Triggers") {
                @Override
                public ItemStack makeIcon() {
                    return null;
                }
                @OnlyIn(Dist.CLIENT)
                public ITextComponent getDisplayName() {
                    return new ITextComponent() {
                        @Override
                        public Style getStyle() {
                            return null;
                        }

                        @Override
                        public String getContents() {
                            return "Music Triggers";
                        }

                        @Override
                        public List<ITextComponent> getSiblings() {
                            return null;
                        }

                        @Override
                        public IFormattableTextComponent plainCopy() {
                            return null;
                        }

                        @Override
                        public IFormattableTextComponent copy() {
                            return null;
                        }

                        @Override
                        public IReorderingProcessor getVisualOrderText() {
                            return null;
                        }
                    };
                }
            }).fireResistant()));
            if(!allItems.contains(i)) {
                allItems.add(i);
            }
        }
    }

    public List<Item> getItems(){
        return allItems;
    }
}
