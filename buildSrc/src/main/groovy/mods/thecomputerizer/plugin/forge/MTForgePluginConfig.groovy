package mods.thecomputerizer.plugin.forge

import mods.thecomputerizer.plugin.common.MTPluginCommonConfig
import mods.thecomputerizer.plugin.util.VersionHelper
import org.gradle.api.Project

import javax.annotation.Nonnull

class MTForgePluginConfig extends MTPluginCommonConfig {

    String at = ''
    boolean fancy_gradle = false
    String mappings_channel = 'parchment'
    MTForgeVersions versions

    MTForgePluginConfig(@Nonnull final Project project) {
        super(project)
    }

    @Override
    void setVersions(int major, int minor) {
        this.versions = VersionHelper.getForgeVersions(major,minor)
    }
}
