package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventHandler {

    void activate();
    void play();
    void playing();
    void queue();
    void stop();
    void stopped();
}