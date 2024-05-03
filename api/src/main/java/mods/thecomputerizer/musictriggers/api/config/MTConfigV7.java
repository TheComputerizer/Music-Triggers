package mods.thecomputerizer.musictriggers.api.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public abstract class MTConfigV7 extends ConfigVersion {
    
    public static final MTConfigV7 V7_0_0_BETA_1 = new MTConfigV7(0,0,"beta",1){};
    
    protected MTConfigV7(int major, int minor) {
        super(7,major,minor);
    }
    
    protected MTConfigV7(int major, int minor, String qualifierName, int qualifierBuild) {
        super(7,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override
    public ConfigVersion getTarget() {
        return ConfigVersionManager.CURRENT;
    }
    
    @Override public MTConfigGlobal initGlobalMappers() {
        return new Global();
    }
    
    private static class Global extends MTConfigGlobal {
        
        @Override public TableRemapper getChannelsMapper() {
            return new Channels();
        }
        
        @Override public TableRemapper getDebugMapper() {
            return new Debug();
        }
        
        @Override public TableRemapper getRegistrationMapper() {
            return new Registration();
        }
    }
    
    private static class Channels extends TableRemapper {
        
        @Override protected Map<String,Collection<String>> initMappings(Map<String,Collection<String>> map) {
            map.put("explicitly_overrides",Collections.singleton("explicit_overrides"));
            map.put("overrides_music",Collections.singleton("overrides_normal_music"));
            map.put("pauses_overrides",Collections.singleton("pause_overrides"));
            map.put("local_folder",Collections.singleton("songs_folder"));
            map.put("renders",Collections.singleton("transitions"));
            return map;
        }
        
        @Override
        protected Map<String,Function<Object,Object>> initTransformers(Map<String,Function<Object,Object>> map) {
            map.put("renders",val -> val.toString().replace("transitions","renders"));
            return map;
        }
    }
    
    private static class Debug extends TableRemapper {
        
        @Override protected Map<String,Collection<String>> initMappings(Map<String,Collection<String>> map) {
            map.put("ENABLE_DEBUG_INFO",Collections.singleton("SHOW_DEBUG"));
            map.put("SHOW_SONG_INFO",Collections.singleton("CURRENT_SONG_ONLY"));
            return map;
        }
        
        @Override
        protected Map<String,Function<Object,Object>> initTransformers(Map<String,Function<Object,Object>> map) {
            return Collections.emptyMap();
        }
    }
    
    private static class Registration extends TableRemapper {
        
        @Override protected Map<String,Collection<String>> initMappings(Map<String,Collection<String>> map) {
            return Collections.emptyMap();
        }
        
        @Override
        protected Map<String,Function<Object,Object>> initTransformers(Map<String,Function<Object,Object>> map) {
            return Collections.emptyMap();
        }
    }
}
