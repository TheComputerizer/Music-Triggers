package mods.thecomputerizer.musictriggers.api.data.global;

import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterList;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterString;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.TomlWritingException;

import java.util.Collections;
import java.util.Map;

public class Debug extends GlobalElement { //TODO Implement log_level and max_hover_elements in the gui

    @Override
    public String getTypeName() {
        return "Debug";
    }

    @Override
    protected void supplyParameters(Map<String,Parameter<?>> map) {
        map.put("allow_timestamps",new ParameterBoolean(false));
        map.put("block_sound_effects",new ParameterBoolean(false));
        map.put("blocked_sound_categories",new ParameterList<>(String.class,Collections.singletonList("minecraft;music"))); //TODO Allow the modid to be omitted
        map.put("client_only",new ParameterBoolean(false));
        map.put("enable_debug_info",new ParameterBoolean(false));
        map.put("enable_discs",new ParameterBoolean(true));
        map.put("encoding_quality",new ParameterInt(10));
        map.put("independent_audio_pools",new ParameterBoolean(false));
        map.put("interrupted_sound_categories",new ParameterList<>(String.class,Collections.singletonList("music")));
        map.put("pause_unless_focused",new ParameterBoolean(true));
        map.put("play_normal_music",new ParameterBoolean(false));
        map.put("resampling_quality",new ParameterString("HIGH"));
        map.put("reverse_priority",new ParameterBoolean(false));
        map.put("show_channel_info",new ParameterBoolean(false));
        map.put("show_position_info",new ParameterBoolean(false));
        map.put("show_song_info",new ParameterBoolean(false));
        map.put("show_trigger_info",new ParameterBoolean(false));
        map.put("slow_tick_factor",new ParameterFloat(5f));
        map.put("tick_rate",new ParameterInt(20));
    }

    @Override
    public boolean verifyRequiredParameters() {
        return true;
    }

    @Override
    public void writeDefault(Toml holder) throws TomlWritingException {
        Toml table = holder.addTable("debug",false);
        appendToTable(table);
    }
}
