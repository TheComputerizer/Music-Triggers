package mods.thecomputerizer.musictriggers.client.gui;


import mods.thecomputerizer.musictriggers.client.MusicPlayer;
import mods.thecomputerizer.musictriggers.config.ConfigObject;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class CustomSlider extends AbstractSlider {

    public ConfigObject holder;
    public float max;

    public CustomSlider(int x, int y, int width, int height, ITextComponent component, double defaultValue, ConfigObject holder, float max) {
        super(x,y,width,height,component,defaultValue);
        this.holder=holder;
        this.max=max;
    }

    public double getValue() {
        return this.value*this.max;
    }

    public void set(double value) {
        this.value=value;
    }

    private String getText() {
        String track;
        track = this.holder.decode(MusicPlayer.curTrack);
        int minutes = (int)((this.value*this.max)/60);
        int seconds = (int)((this.value*this.max)%60);
        String formattedMinutes;
        if(minutes==0) formattedMinutes = "";
        else formattedMinutes = minutes+":";
        String formattedSeconds;
        if(seconds<10) formattedSeconds = "0"+seconds;
        else formattedSeconds  = ""+seconds;
        return  track+" "+formattedMinutes+formattedSeconds;
    }

    protected void updateMessage() {
        this.setMessage(new StringTextComponent(getText()));
    }

    public void applyValue() {}
}
