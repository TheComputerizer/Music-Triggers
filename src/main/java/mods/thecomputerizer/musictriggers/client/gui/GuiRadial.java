package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialElement;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;

public class GuiRadial extends GuiSuperType {

    protected RadialElement circleButton;

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
        this.circleButton = type.getCircleForType(this, setCenterCircle(), createRadialProgressBar());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        circleButton.mousePressed((int)mouseX, (int)mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        GuiUtil.drawColoredRing(new Vector3f((int)(((float)this.width)/2f),(int)(((float)this.height)/2f),0),
                new Vector3f(35,37,0),new Vector4f(255,255,255,192),100,this.getBlitOffset());
        GuiUtil.drawColoredRing(new Vector3f((int)(((float)this.width)/2f),(int)(((float)this.height)/2f),0),
                new Vector3f(110,112,0),new Vector4f(255,255,255,192),100,this.getBlitOffset());
        circleButton.render(matrix, this.getBlitOffset(),mouseX,mouseY);
    }

    @Override
    protected void save() {

    }


}
