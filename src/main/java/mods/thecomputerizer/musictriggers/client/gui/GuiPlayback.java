package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import net.minecraft.util.math.Vec2f;

import javax.vecmath.Point4f;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiPlayback extends GuiRadial {

    private final List<Channel> channels;
    private RadialProgressBar radialBar;
    private Channel currentChannel = null;

    public GuiPlayback(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.channels = new ArrayList<>();
    }

    @Override
    protected RadialProgressBar createRadialProgressBar() {
        this.radialBar = this.type.getBarForType(0, 25,0f);
        return this.radialBar;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.channels.clear();
        this.channels.addAll(ChannelManager.getAllChannels());
        if(!this.channels.isEmpty()) {
            this.currentChannel = this.channels.get(0);
            String displayName = Translate.guiGeneric(false, "button", "selected_channel");
            int width = this.fontRenderer.getStringWidth(displayName+" "+this.currentChannel.getChannelName()) + 8;
            addSuperButton(createBottomButton(displayName+" "+this.currentChannel.getChannelName(), width, this.channels.size(),
                    Translate.guiNumberedList(1, "button", "selected_channel", "desc"),
                    (screen, button, mode) -> {
                        this.currentChannel = this.channels.get(mode-1);
                        button.updateDisplay(displayName+" "+this.currentChannel.getChannelName(),this.fontRenderer);
                    }), 16);
        }
    }

    public void skip() {
        if(Objects.nonNull(this.currentChannel))
            this.currentChannel.stopTrack(false);
    }

    public void reset() {
        if(Objects.nonNull(this.currentChannel))
            this.currentChannel.resetTrack();
    }

    public void click(float percent) {
        if(Objects.nonNull(this.currentChannel) && this.currentChannel.isPlayingSeekable())
            this.currentChannel.setMillis((long)(this.currentChannel.getTotalMillis()*percent));
    }

    private void updateProgressBar() {
        float percent = 1f;
        if(this.currentChannel.isPlayingSeekable())
            percent = ((float)this.currentChannel.getMillis())/((float)this.currentChannel.getTotalMillis());
        this.radialBar.setProgress(percent);
    }

    @Override
    public void drawStuff(int mouseX, int mouseY, float partialTicks) {
        if(Objects.nonNull(this.radialBar)) {
            GuiUtil.drawColoredRing(new Vec2f((int)(((float)this.width)/2f),(int)(((float)this.height)/2f)),
                    new Vec2f(110,112),new Point4f(255,255,255,192),100,this.zLevel);
            circleButton.render(this.zLevel,mouseX,mouseY);
            Vec2f center = new Vec2f((float)this.width / 2, (float)this.height / 2);
            String display = "Playback Unavailable";
            if(Objects.nonNull(this.currentChannel)) {
                updateProgressBar();
                display = this.currentChannel.formatPlayback();
            } else this.radialBar.setProgress(1f);
            drawCenteredString(this.fontRenderer, display, (int)center.x, (int)(center.y-this.spacing-this.fontRenderer.FONT_HEIGHT-112), GuiUtil.WHITE);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
