package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.Channel;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;

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
    public void init() {
        super.init();
        this.channels.clear();
        this.channels.addAll(ChannelManager.getAllChannels());
        if(!this.channels.isEmpty()) {
            this.currentChannel = this.channels.get(0);
            String displayName = Translate.guiGeneric(false, "button", "selected_channel");
            int width = this.font.width(displayName+" "+this.currentChannel.getChannelName()) + 8;
            addSuperButton(createBottomButton(displayName+" "+this.currentChannel.getChannelName(), width, this.channels.size(),
                    Translate.guiNumberedList(1, "button", "selected_channel", "desc"),
                    (screen, button, mode) -> {
                        this.currentChannel = this.channels.get(mode-1);
                        button.updateDisplay(displayName+" "+this.currentChannel.getChannelName(),this.font,this);
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
    public void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if(Objects.nonNull(this.radialBar)) {
            GuiUtil.drawColoredRing(new Vector3f((int)(((float)this.width)/2f),(int)(((float)this.height)/2f),0),
                    new Vector3f(110,112,0),new Vector4f(255,255,255,192),100,getBlitOffset());
            this.circleButton.render(matrix,getBlitOffset(),mouseX,mouseY);
            Vector3f center = new Vector3f((float)this.width / 2, (float)this.height / 2,0);
            String display = "Playback Unavailable";
            if(Objects.nonNull(this.currentChannel)) {
                updateProgressBar();
                display = this.currentChannel.formatPlayback();
            } else this.radialBar.setProgress(1f);
            drawCenteredString(matrix,this.font,display,(int)center.x(),(int)(center.y()-this.spacing-this.font.lineHeight-112),GuiUtil.WHITE);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
