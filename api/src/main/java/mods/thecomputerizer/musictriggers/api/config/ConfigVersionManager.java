package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersion.Qualifier;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersion.Version;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class ConfigVersionManager {
    
    private static final Set<ConfigVersion> VERSIONS = collectVersions();
    public static final ConfigVersion CURRENT = findCurrent();
    
    private static Set<ConfigVersion> collectVersions() {
        Set<ConfigVersion> versions = new HashSet<>();
        return versions;
    }
    
    private static ConfigVersion findCurrent() {
        return findVersion(MTRef.VERSION);
    }
    
    public static ConfigVersion findLatestQualified(int release, int major, int minor) {
        ConfigVersion closest = null;
        int build = 0;
        for(ConfigVersion config : VERSIONS) {
            Version version = config.version;
            if(version.getRelease()==release && version.getMajor()==major && version.getMinor()==minor) {
                Qualifier qualifier = version.getQualifier();
                if(Objects.isNull(qualifier)) return config; //Target release over beta
                else if(qualifier.getBuild()>build) {
                    closest = config;
                    build = qualifier.getBuild();
                }
            }
        }
        if(Objects.isNull(closest)) {
            ChannelHelper.getGlobalData().logError("Unable to find latest build for {}.{}.{}! Substituting with "+
                                                   "current version {}",release,major,minor,CURRENT);
            closest = CURRENT;
        }
        return closest;
    }
    
    public static ConfigVersion findVersion(String versionStr) {
        Version version = parseVersion(versionStr);
        ConfigVersion found = null;
        Set<ConfigVersion> similarVersions = new HashSet<>();
        for(ConfigVersion config : VERSIONS) {
            if(config.version.equals(version)) {
                found = config;
                break;
            } else if(config.similar(version)) similarVersions.add(config);
        }
        GlobalData global = ChannelHelper.getGlobalData();
        if(Objects.isNull(found)) {
            global.logInfo("Unable to find match config mappings to version {}. Searching for a similar "+
                           "version...",version);
            for(ConfigVersion config : similarVersions) {
                int closest = Objects.nonNull(found) ?
                        version.getQualifier().getBuild()-found.getVersion().getQualifier().getBuild() : Integer.MAX_VALUE;
                if(config.hasCloserQualiferThan(config,closest)) found = config;
            }
            if(Objects.isNull(found)) global.logError("Unable to find any config mappings similar to {}",version);
        }
        return found;
    }
    
    private static Version parseVersion(String versionStr) {
        String[] numbers;
        String qualifierStr = null;
        int index = versionStr.indexOf('+');
        if(index>-1) {
            qualifierStr = versionStr.substring(index+1);
            numbers = versionStr.substring(0,index).split("\\.");
        } else numbers = versionStr.split("\\.");
        int release = numbers.length>0 ? Integer.parseInt(numbers[0]) : 0;
        int major = numbers.length>1 ? Integer.parseInt(numbers[1]) : 0;
        int minor = numbers.length>2 ? Integer.parseInt(numbers[2]) : 0;
        if(Objects.isNull(qualifierStr)) return new Version(release,major,minor);
        index = qualifierStr.indexOf('.');
        return new Version(release,major,minor,new Qualifier(
                index>-1 ? qualifierStr.substring(0,index) : qualifierStr,
                index>-1 ? Integer.parseInt(qualifierStr.substring(index+1)) : 1));
    }
    
    public static void remapValues(Map<String,Object> map, Map<String,String> keyMap) {
        Map<String,Object> copy = new HashMap<>();
        for(Entry<String,Object> entry : map.entrySet()) {
            String original = entry.getKey();
            String mapped = keyMap.getOrDefault(original,original);
            copy.put(mapped,entry.getValue());
        }
        map.clear();
        map.putAll(copy);
    }
}