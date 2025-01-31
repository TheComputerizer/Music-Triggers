package mods.thecomputerizer.plugin.util

import org.gradle.api.artifacts.Dependency

import java.util.function.Function

class EasyDep extends GroovyObjectSupport {

    String pkg
    String name
    String version

    Dependency apply(Function<String,Dependency> apply) {
        return apply.apply(get())
    }

    String get() {
        return "${this.pkg}:${this.name}:${this.version}"
    }

    static EasyDep get(String pkg, String name, String version) {
        EasyDep dep = new EasyDep()
        dep.pkg = pkg
        dep.name = name
        dep.version = version
        return dep
    }

    static EasyDep curse(String name, String version) {
        return get("curse.maven",name,version)
    }
}