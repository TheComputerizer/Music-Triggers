package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;

import java.util.Collections;
import java.util.Map;

public class Debug extends GlobalElement {

    @Override
    public String getTypeName() {
        return "Debug";
    }

    @Override
    protected void supplyParameters(Map<String,Parameter<?>> map) {
        map.put("ALLOW_TIMESTAMPS",new ParameterBoolean(false));
        map.put("BLOCK_STREAMING_ONLY",new ParameterBoolean(true));
        map.put("BLOCKED_MOD_CATEGORIES",new ParameterList<>(String.class, Collections.singletonList("minecraft;music")));
        map.put("COMBINE_EQUAL_PRIORITY",new ParameterBoolean(false));
        map.put("CURRENT_SONG_ONLY",new ParameterBoolean(false));
        map.put("ENABLE_DEBUG_INFO",new ParameterBoolean(false));
        map.put("ENCODING_QUALITY",new ParameterInt(10));
        map.put("INTERRUPTED_AUDIO_CATEGORIES",new ParameterList<>(String.class, Collections.singletonList("music")));
        map.put("LOG_LEVEL",new ParameterString("INFO"));
        map.put("MAX_HOVER_ELEMENTS",new ParameterInt(15));
        map.put("PAUSE_WHEN_TABBED",new ParameterBoolean(true));
        map.put("PLAY_NORMAL_MUSIC",new ParameterBoolean(false));
        map.put("RESAMPLING_QUALITY",new ParameterString("HIGH"));
        map.put("REVERSE_PRIORITY",new ParameterBoolean(false));
        map.put("SLOW_TICK_FACTOR",new ParameterInt(5));
        map.put("TICK_RATE",new ParameterInt(20));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }
}
