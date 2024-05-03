package mods.thecomputerizer.musictriggers.api.config;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class MTConfigV6 extends ConfigVersion {
    
    protected MTConfigV6(int major, int minor) {
        super(6,major,minor);
    }
    
    protected MTConfigV6(int major, int minor, String qualifierName, int qualifierBuild) {
        super(6,major,minor,qualifierName,qualifierBuild);
    }
    
    @Override
    public ConfigVersion getTarget() {
        return ConfigVersionManager.findLatestQualified(7,0,0);
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
            return Collections.emptyMap();
        }
        
        @Override
        protected Map<String,Function<Object,Object>> initTransformers(Map<String,Function<Object,Object>> map) {
            return Collections.emptyMap();
        }
    }
    
    private static class Debug extends TableRemapper {
        
        @Override protected Map<String,Collection<String>> initMappings(Map<String,Collection<String>> map) {
            return Collections.emptyMap();
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
