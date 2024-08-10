package mods.thecomputerizer.musictriggers.api.data.channel;

public interface ChannelEventHandler {

    void activate(); //trigger becomes active
    void close(); //the channel is getting closed and unregistered
    void deactivate(); //trigger is no longer active
    void play(boolean unpaused); //audio has started playing
    void playable(); //trigger parameters are met
    void playing(boolean unpaused); //while audio is playing
    void queue(); //queue audio to play
    void stop(); //stop playing audio
    void stopped(); //audio stopped playing
    void tickActive(boolean unpaused); //trigger is active
    void tickPlayable(boolean unpaused); //trigger is playable
    void unplayable(); //trigger is no longer playable
}