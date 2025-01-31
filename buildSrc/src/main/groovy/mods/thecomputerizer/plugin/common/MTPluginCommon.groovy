package mods.thecomputerizer.plugin.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.*
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.plugins.ide.idea.model.IdeaModel

import static org.gradle.api.file.DuplicatesStrategy.EXCLUDE

abstract class MTPluginCommon implements Plugin<Project> {

    static void applyFrom(Project project, String name) {
        project.apply from: rootFile(project,"gradle\\${name}.gradle")
    }

    static void applyLib(Project project) {
        if(project.extensions.findByType(MTPluginCommonConfig).lib)
            applyFrom(project,'til')
    }

    static void applyRuntime(Project project) {
        if(project.extensions.findByType(MTPluginCommonConfig).runtime)
            applyFrom(project,'runtime-module')
    }

    static void applyShadow(Project project) {
        if(project.extensions.findByType(MTPluginCommonConfig).shadow)
            applyFrom(project,'shadow-relocate')
    }

    static void configureTasks(Project project) {
        project.tasks.withType(Jar).configureEach {
            it.duplicatesStrategy = EXCLUDE
            it.archiveBaseName
        }
        project.tasks.withType(JavaCompile).configureEach {
            CompileOptions options = it.options
            options.deprecation = false
            options.encoding = 'UTF-8'
        }
        project.tasks.withType(ProcessResources).configureEach {
            it.duplicatesStrategy = EXCLUDE
        }
    }

    static String resourceFile(Project project, String path) {
        return project.file("src\\main\\resources\\$path")
    }

    static void idea(ExtensionContainer extensions) {
        extensions.findByType(IdeaModel).module {
            it.downloadJavadoc = true
            it.downloadSources = true
            it.inheritOutputDirs
        }
    }

    static <C extends MTPluginCommonConfig> void init(Project project, Class<C> configType) {
        ExtensionContainer extensions = project.extensions
        extensions.create 'musictriggers', configType, project
    }

    static void java(ExtensionContainer extensions) {
        def config = extensions.findByType(MTPluginCommonConfig)
        def version = JavaLanguageVersion.of(config.versions.java)
        extensions.findByType(JavaPluginExtension).toolchain.languageVersion.set(version)
    }

    static File rootFile(Project project, String path) {
        return project.rootProject.file(path)
    }

    void applyPlugins(PluginManager manager) {
        manager.apply JavaPlugin
        manager.apply JavaBasePlugin
        manager.apply JavaLibraryPlugin
        manager.apply IdeaPlugin
    }

    void buildArgs(MTPluginCommonConfig config, List<String> args) {
        if(config.dev) args.add '-Dtil.dev=true'
        if(!config.entrypoint.isEmpty()) args.add "-Dtil.classpath.mods=${config.entrypoint}".toString()
        args.add '-Dtil.classpath.coremods=mods.thecomputerizer.musictriggers.api.core.MTCoreEntryPoint'
    }

    List<String> buildArgs(MTPluginCommonConfig config) {
        List<String> args = new ArrayList<>()
        buildArgs config, args
        return args.asImmutable()
    }

    void configurePlugins(Project project) {
        ExtensionContainer extensions = project.extensions
        java extensions
        idea extensions
    }
}