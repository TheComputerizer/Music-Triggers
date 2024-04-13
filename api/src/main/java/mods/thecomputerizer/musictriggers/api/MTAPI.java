package mods.thecomputerizer.musictriggers.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class MTAPI {

    public void init() throws IOException {
        File configDir = new File(MTRef.CONFIG_PATH);
        if(!configDir.exists() && !configDir.mkdirs())
            throw new FileNotFoundException("Unable to create file directory at "+MTRef.CONFIG_PATH+"! Music Triggers "+
                    "is unable to load any further.");
    }
}