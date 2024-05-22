package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventRunner {
    
    boolean canRun(String event);
    boolean checkSide();
    boolean isClient();
    boolean isServer();
    void run();
    boolean tick();
}