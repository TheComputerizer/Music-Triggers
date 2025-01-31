package mods.thecomputerizer.plugin.util

import mods.thecomputerizer.plugin.fabric.MTFabricVersions
import mods.thecomputerizer.plugin.forge.MTForgeVersions

import static mods.thecomputerizer.plugin.MTRef.*

class VersionHelper {

    static MTFabricVersions getFabricVersions(int major, int minor) {
        switch(major) {
            case 16: return initFabricVersions(FABRIC_16_5,FABRIC_MAPPINGS_16_5,MC_16_5,PARCHMENT_16_5)
            case 18: {
                def versions = initFabricVersions(FABRIC_18_2,FABRIC_MAPPINGS_18_2,MC_18_2,PARCHMENT_18_2)
                versions.java = 17
                return versions
            } case 19: {
                def versions = minor==2 ?
                        initFabricVersions(FABRIC_19_2,FABRIC_MAPPINGS_19_2,MC_19_2,PARCHMENT_19_2) :
                        initFabricVersions(FABRIC_19_4,FABRIC_MAPPINGS_19_4,MC_19_4,PARCHMENT_19_4)
                versions.java = 17
                return versions
            } case 20: {
                def versions
                switch(minor) {
                    case 1: {
                        versions = initFabricVersions(FABRIC_20_1,FABRIC_MAPPINGS_20_1,MC_20_1,PARCHMENT_20_1)
                        versions.java = 17
                        break
                    }
                    case 4: {
                        versions = initFabricVersions(FABRIC_20_4,FABRIC_MAPPINGS_20_4,MC_20_4,PARCHMENT_20_4)
                        versions.java = 17
                        break
                    }
                    default: {
                        versions = initFabricVersions(FABRIC_20_6,FABRIC_MAPPINGS_20_6,MC_20_6,PARCHMENT_20_6)
                        versions.java = 21
                        break
                    }
                }
                return versions
            } case 21: {
                def versions = initFabricVersions(FABRIC_21_1,FABRIC_MAPPINGS_21_1,MC_21_1,PARCHMENT_21_1)
                versions.java = 21
                return versions
            }
            default: return null
        }
    }

    static MTForgeVersions getForgeVersions(int major, int minor) {
        switch(major) {
            case 12: return initForgeVersions(FORGE_12_2,FORGE_MAPPINGS_12_2,MC_12_2,'')
            case 16: return initForgeVersions(FORGE_16_5,FORGE_MAPPINGS_16_5,MC_16_5,PARCHMENT_16_5)
            case 18: {
                def versions = initForgeVersions(FORGE_18_2,FORGE_MAPPINGS_18_2,MC_18_2,PARCHMENT_18_2)
                versions.java = 17
                return versions
            } case 19: {
                def versions = minor==2 ?
                        initForgeVersions(FORGE_19_2,FORGE_MAPPINGS_19_2,MC_19_2,PARCHMENT_19_2) :
                        initForgeVersions(FORGE_19_4,FORGE_MAPPINGS_19_4,MC_19_4,PARCHMENT_19_4)
                versions.java = 17
                return versions
            } case 20: {
                def versions
                switch(minor) {
                    case 1: {
                        versions = initForgeVersions(FORGE_20_1,FORGE_MAPPINGS_20_1,MC_20_1,PARCHMENT_20_1)
                        versions.java = 17
                        break
                    }
                    case 4: {
                        versions = initForgeVersions(FORGE_20_4,FORGE_MAPPINGS_20_4,MC_20_4,PARCHMENT_20_4)
                        versions.java = 17
                        break
                    }
                    default: {
                        versions = initForgeVersions(FORGE_20_6,FORGE_MAPPINGS_20_6,MC_20_6,PARCHMENT_20_6)
                        versions.java = 21
                        break
                    }
                }
                return versions
            } case 21: {
                def versions = initForgeVersions(FORGE_21_1,FORGE_MAPPINGS_21_1,MC_21_1,PARCHMENT_21_1)
                versions.java = 21
                return versions
            }
            default: return null
        }
    }

    private static MTFabricVersions initFabricVersions(String fabric, String mappings, String minecraft, String parchment) {
        def versions = new MTFabricVersions()
        versions.fabric_api = fabric
        versions.mappings = mappings
        versions.minecraft = minecraft
        versions.parchment = parchment
        return versions
    }

    private static MTForgeVersions initForgeVersions(String forge, String mappings, String minecraft, String parchment) {
        def versions = new MTForgeVersions()
        versions.forge = forge
        versions.mappings = mappings
        versions.minecraft = minecraft
        versions.parchment = parchment
        return versions
    }
}