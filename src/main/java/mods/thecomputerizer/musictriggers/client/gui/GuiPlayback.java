package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mods.thecomputerizer.musictriggers.client.audio.Channel;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.client.gui.RadialProgressBar;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;

import java.util.List;
import java.util.Objects;

public class GuiPlayback extends GuiRadial {

    private RadialProgressBar radialBar;
    private final List<String> channels;
    private String currentChannel;
    private int channelPos;

    public GuiPlayback(GuiSuperType parent, GuiType type, Instance configInstance) {
        super(parent, type, configInstance);
        this.channels = ChannelManager.getChannelNames();
        this.currentChannel = this.channels.get(0);
        this.channelPos = 0;
    }

    @Override
    protected RadialProgressBar createRadialProgressBar() {
        this.radialBar = this.type.getBarForType(0, 25,0f);
        return this.radialBar;
    }

    public void click(float percent) {
        Channel channel = ChannelManager.getChannel(this.currentChannel);
        channel.setMillis((long)(channel.getTotalMillis()*percent));
    }

    private void updateProgressBar(Channel channel) {
        float percent = 1f;
        if(channel.isPlaying())
            percent = ((float)channel.getMillis())/((float)channel.getTotalMillis());
        this.radialBar.setProgress(percent);
    }

    @Override
    public void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        if(Objects.nonNull(this.radialBar)) {
            super.drawStuff(matrix, mouseX, mouseY, partialTicks);
            Vector3f center = new Vector3f(((float)this.width) / 2, ((float)this.height) / 2, 0);
            String display = "Playback unavailable";
            if (ChannelManager.channelExists(this.currentChannel)) {
                Channel channel = ChannelManager.getChannel(this.currentChannel);
                updateProgressBar(channel);
                display = channel.formatPlayback();
            } else this.radialBar.setProgress(1f);
            drawCenteredString(matrix,this.font, display, (int)center.x(), (int)center.y(), GuiUtil.WHITE);
        }
    }
}
