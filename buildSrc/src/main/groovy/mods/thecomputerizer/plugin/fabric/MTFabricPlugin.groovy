package mods.thecomputerizer.plugin.fabric

import mods.thecomputerizer.plugin.common.MTPluginCommon
import mods.thecomputerizer.plugin.common.MTPluginCommonConfig
import org.gradle.api.Project
import org.gradle.api.plugins.PluginManager

class MTFabricPlugin extends MTPluginCommon {

    @Override
    void apply(Project project) {
        init project, MTFabricPluginConfig
        applyPlugins project.pluginManager
        configurePlugins project
        configureTasks project
    }

    @Override
    void applyPlugins(PluginManager manager) {
        super.applyPlugins manager
    }

    @Override
    void buildArgs(MTPluginCommonConfig config, List<String> args) {
        args.add '-Dfabric.log.level=debug'
        super.buildArgs config, args
    }

    @Override
    void configurePlugins(Project project) {
        super.configurePlugins project
    }
}
