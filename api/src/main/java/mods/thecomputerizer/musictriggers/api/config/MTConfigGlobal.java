package mods.thecomputerizer.musictriggers.api.config;

public abstract class MTConfigGlobal extends ConfigRemapper {
    
    public abstract TableRemapper getChannelsMapper();
    public abstract TableRemapper getDebugMapper();
    public abstract TableRemapper getRegistrationMapper();
}