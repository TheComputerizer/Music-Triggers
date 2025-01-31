package mods.thecomputerizer.plugin.fabric

import mods.thecomputerizer.plugin.common.MTPluginCommonConfig
import mods.thecomputerizer.plugin.util.VersionHelper
import org.gradle.api.Project

import javax.annotation.Nonnull

class MTFabricPluginConfig extends MTPluginCommonConfig {

    String log_level = "debug"
    MTFabricVersions versions

    MTFabricPluginConfig(@Nonnull final Project project) {
        super(project)
    }

    @Override
    void setVersions(int major, int minor) {
        this.versions = VersionHelper.getFabricVersions(major,minor)
    }
}
