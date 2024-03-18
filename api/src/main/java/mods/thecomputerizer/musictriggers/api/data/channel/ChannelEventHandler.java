package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventHandler {

    void activate(); //trigger becomes active
    void deactivate(); //trigger is no longer active
    void play(); //start playing audio
    void playable(); //trigger parameters are met
    void playing(); //while audio is playing
    void queue(); //queue audio to play
    void stop(); //stop playing audio
    void stopped(); //audio stopped playing
    void tickActive(); //trigger is active
    void tickPlayable(); //trigger is playable
    void unplayable(); //trigger is no longer playable
}