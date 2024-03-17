package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventHandler {

    void activate(); //trigger becomes active
    void play(); //start playing audio
    void playable(); //trigger parameters are met
    void playing(); //while audio is playing
    void queue(); //queue audio to play
    void stop(); //stop playing audio
    void stopped(); //audio stopped playing
}