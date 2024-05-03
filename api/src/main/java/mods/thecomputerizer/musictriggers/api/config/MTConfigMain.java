package mods.thecomputerizer.musictriggers.api.config;

public abstract class MTConfigMain extends ConfigRemapper {
    
    public static abstract class Songs {
        
        public abstract TableRemapper getSongRemapper();
        public abstract TableRemapper getParameterRemapper(String element);
    }
    
    public static abstract class Triggers extends FileRemapper {
        
        public abstract TableRemapper getTriggerRemapper();
        public abstract TableRemapper getParameterRemapper();
    }
}