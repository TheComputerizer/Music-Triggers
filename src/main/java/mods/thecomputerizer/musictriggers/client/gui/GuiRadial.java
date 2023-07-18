package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialElement;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.Vec2f;

import javax.annotation.Nullable;
import javax.vecmath.Point4f;

public class GuiRadial extends GuiSuperType {

    protected RadialElement circleButton;

    public GuiRadial(@Nullable GuiScreen mainParent, GuiSuperType parent, GuiType type, Instance configInstance) {
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
    public void initGui() {
        super.initGui();
        this.circleButton = this.type.getCircleForType(this, setCenterCircle(), createRadialProgressBar());
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.circleButton.mousePressed(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawStuff(int mouseX, int mouseY, float partialTicks) {
        GuiUtil.drawColoredRing(new Vec2f(((float)this.width)/2f,((float)this.height)/2f),
                new Vec2f(35,37),new Point4f(255,255,255,192),100,this.zLevel);
        GuiUtil.drawColoredRing(new Vec2f(((float)this.width)/2f,((float)this.height)/2f),
                new Vec2f(110,112),new Point4f(255,255,255,192),100,this.zLevel);
        this.circleButton.render(this.zLevel,mouseX,mouseY);
    }

    @Override
    protected void save() {

    }
}
