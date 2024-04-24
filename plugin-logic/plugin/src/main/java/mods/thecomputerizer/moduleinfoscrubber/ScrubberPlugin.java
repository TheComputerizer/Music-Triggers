import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.plugins.PluginContainer;

/**
 * Main plugin class
 */
public abstract class ModuleInfoScrubberPlugin implements Plugin<Project> {

    private final Attribute<String> artifactType;
    private final Attribute<Boolean> java8ified;

    public ModuleInfoScrubberPlugin() {
        this.artifactType = Attribute.of("artifactType",String.class);
        this.java8ified = Attribute.of("java8ified",Boolean.class);
    }

    @Override
    public void apply(Project project) {
        applyPlugins(project.getPlugins());
        registerAttributes(project.getDependencies());
        setupConfiguration(project,project.getConfigurations());
        registerTransforms(project.getDependencies());
    }

    private void applyPlugins(PluginContainer plugins) {
        plugins.apply("groovy");
        plugins.apply("idea");
        plugins.apply("java");
    }

    private void registerAttributes(DependencyHandler dependencies) {
        dependencies.attributesSchema(schema -> schema.attribute(this.artifactType));
        dependencies.getArtifactTypes().named("jar",def -> def.getAttributes().attribute(this.java8ified,false));
    }

    private void registerTransforms(DependencyHandler dependencies) {
        dependencies.registerTransform(ScrubberTransform.class, spec -> { //TODO Can the zip attribute be checked for here?
            AttributeContainer from = spec.getFrom();
            from.attribute(this.java8ified,false).attribute(this.artifactType,"jar");
            AttributeContainer to = spec.getTo();
            to.attribute(this.java8ified,false).attribute(this.artifactType,"jar");
        });
    }

    private void setupConfiguration(Project project, ConfigurationContainer configurations) {
        configurations.getByName("runtimeClasspath", config -> project.afterEvaluate(p -> {
            if(config.isCanBeResolved()) config.attributes(container -> container.attribute(java8ified,true));
        }));
    }
}