package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialElement;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;

import javax.annotation.Nullable;

public class GuiRadial extends GuiSuperType {

    protected RadialElement circleButton;

    public GuiRadial(@Nullable Screen mainParent, GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        getInstance().setMainParent(mainParent);
    }

    public GuiRadial(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
    }

    private Integer[] setCenterCircle() {
        return new Integer[]{(int)(((float)this.width)/2f),(int)(((float)this.height)/2f),50,100,25};
    }

    protected RadialProgressBar createRadialProgressBar() {
        return null;
    }

    @Override
    public void init() {
        super.init();
        this.circleButton = this.type.getCircleForType(this, setCenterCircle(), createRadialProgressBar());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.circleButton.mousePressed((int)mouseX, (int)mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        GuiUtil.drawColoredRing(new Vector2f(((float)this.width)/2f,((float)this.height)/2f),
                new Vector2f(35,37),new Vector4f(255,255,255,192),100,getBlitOffset());
        GuiUtil.drawColoredRing(new Vector2f(((float)this.width)/2f,((float)this.height)/2f),
                new Vector2f(110,112),new Vector4f(255,255,255,192),100,getBlitOffset());
        this.circleButton.render(matrix,getBlitOffset(),mouseX,mouseY);
    }

    @Override
    protected void save() {

    }
}
