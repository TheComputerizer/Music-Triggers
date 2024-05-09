package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventRunner extends ChannelEventHandler {
    
    @Override default void activate() {
        if(checkSide() && canRun("activate")) run();
    }
    
    boolean canRun(String event);
    boolean checkSide();
    
    @Override default void deactivate() {
        if(checkSide() && canRun("deactivate")) run();
    }
    
    boolean isClient();
    boolean isServer();
    
    @Override default void play() {
        if(checkSide() && canRun("play")) run();
    }
    
    @Override default void playable() {
        if(checkSide() && canRun("playable")) run();
    }
    
    @Override default void playing() {
        if(checkSide() && canRun("playing") && tick()) run();
    }
    
    @Override default void queue() {
        if(checkSide() && canRun("queue")) run();
    }
    
    void run();
    
    @Override default void stop() {
        if(checkSide() && canRun("stop")) run();
    }
    
    @Override default void stopped() {
        if(checkSide() && canRun("stopped")) run();
    }
    
    boolean tick();
    
    @Override default void tickActive() {
        if(checkSide() && canRun("tick_active") && tick()) run();
    }
    
    @Override default void tickPlayable() {
        if(checkSide() && canRun("tick_playable") && tick()) run();
    }
    
    @Override default void unplayable() {
        if(checkSide() && canRun("unplayable")) run();
    }
}