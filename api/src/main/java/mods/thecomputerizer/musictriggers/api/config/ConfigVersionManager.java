package mods.thecomputerizer.musictriggers.api.config;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersion.Qualifier;
import mods.thecomputerizer.musictriggers.api.config.ConfigVersion.Version;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalData;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static mods.thecomputerizer.musictriggers.api.config.MTConfigV6.V6_3_1;
import static mods.thecomputerizer.musictriggers.api.config.MTConfigV7.LATEST;
import static mods.thecomputerizer.musictriggers.api.config.MTConfigV7.V7_0_0_BETA_1;
import static mods.thecomputerizer.musictriggers.api.config.MTConfigV7.V7_0_0_BETA_3;
import static mods.thecomputerizer.musictriggers.api.config.MTConfigV7.V7_0_0_BETA_4;

public class ConfigVersionManager {
    
    private static final Set<ConfigVersion> VERSIONS = collectVersions();
    public static final ConfigVersion CURRENT = findCurrent();
    
    private static Set<ConfigVersion> collectVersions() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                LATEST,V6_3_1,V7_0_0_BETA_1,V7_0_0_BETA_3,V7_0_0_BETA_4)));
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
            global.logInfo("Unable to find matching config mappings to version {}. Searching for a similar "+
                           "version...",version);
            for(ConfigVersion config : similarVersions) {
                int build = version.getQualifier().getBuild();
                int foundBuild = Objects.nonNull(found) ? found.getVersion().getQualifier().getBuild() : 0;
                int closest = foundBuild>0 && foundBuild<=build ? build-foundBuild : Integer.MAX_VALUE;
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
        index = qualifierStr.indexOf('-');
        return new Version(release,major,minor,new Qualifier(
                index>-1 ? qualifierStr.substring(0,index) : qualifierStr,
                index>-1 ? Integer.parseInt(qualifierStr.substring(index+1)) : 1));
    }
    
    public static void queryRemap() {
        String versionPath = MTRef.CONFIG_PATH+"/version";
        File version = FileHelper.get(versionPath+".txt",false); //Ensures the file exists before trying to read it
        List<String> versionLines = ChannelHelper.openTxt(versionPath,CURRENT);
        ConfigVersion fileVersion;
        if(versionLines.isEmpty()) {
            CURRENT.logWarn("No version info present! Attempting to remap from 6.3.1");
            fileVersion = MTConfigV6.V6_3_1;
        } else fileVersion = findVersion(versionLines.get(0).trim());
        if(Objects.isNull(fileVersion)) CURRENT.logFatal("Unable to remap missing config version!");
        else fileVersion.remap();
        FileHelper.writeLine(version,MTRef.VERSION,false);
    }
    
    public static void writeDefaults(Toml toml, String name, String path) {
        TableRef ref = MTDataRef.FILE_MAP.get(name);
        if(Objects.nonNull(ref) && ref.addMissingDefaults(toml,CURRENT)) {
            CURRENT.logInfo("Writing missing default {} values to {}",name,path);
            toml.clearComments();
            toml.addComments(CURRENT.getHeaderLines(name));
            MTDataRef.writeToFile(toml,path);
        }
    }
}