package mods.thecomputerizer.moduleinfoscrubber;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

import static org.apache.log4j.Level.*;

/**
 * Main plugin class
 */
public abstract class ScrubberPlugin implements Plugin<Project> {

    private static final Logger LOGGER = LogManager.getLogger("Module-Info Scrubber");

    private static @Nullable Throwable findThrowable(Object ... args) {
        if(Objects.nonNull(args) && args.length>0) {
            Object last = args[args.length-1];
            return last instanceof Throwable ? (Throwable)last : null;
        }
        return null;
    }

    private static String injectArgs(String msg, Object ... args) {
        if(Objects.isNull(msg)) return null;
        if(Objects.nonNull(args) && args.length>0) {
            String nextMsg = injectNextArg(msg,args[0]);
            if(!msg.equals(nextMsg)) {
                msg = nextMsg;
                if(args.length>1) msg = injectArgs(msg,Arrays.copyOfRange(args,1,args.length));
            }
        }
        return msg;
    }

    private static String injectNextArg(String msg, @Nullable Object arg) {
        return msg.replaceFirst("\\{}",Objects.nonNull(arg) ? arg.toString() : "null");
    }

    public static void log(Level level, String msg, Object ... args) {
        Throwable throwable = findThrowable(args);
        if(Objects.nonNull(throwable)) args = args.length>1 ? Arrays.copyOfRange(args,0,args.length-1) : null;
        logInner(level,injectArgs(msg,args),throwable);
    }

    public static void logDebug(@Nullable Project project, String msg, Object ... args) {
        log(DEBUG,msg(true,msg),args);
        if(Objects.nonNull(project)) project.getLogger().debug(msg(false,msg),args);
    }

    public static void logError(@Nullable Project project, String msg, Object ... args) {
        log(ERROR,msg(true,msg),args);
        if(Objects.nonNull(project)) project.getLogger().error(msg(false,msg),args);
    }

    public static void logInfo(@Nullable Project project, String msg, Object ... args) {
        log(INFO,msg(true,msg),args);
        if(Objects.nonNull(project)) project.getLogger().info(msg(false,msg),args);
    }

    private static void logInner(Level level, @Nullable Object msg, @Nullable Throwable throwable) {
        msg = Objects.nonNull(msg) ? msg : "Null message";
        if(Objects.nonNull(throwable)) LOGGER.log(level,msg,throwable);
        else LOGGER.log(level,msg);
    }

    public static void logTrace(@Nullable Project project, String msg, Object ... args) {
        log(TRACE,msg(true,msg),args);
        if(Objects.nonNull(project)) project.getLogger().trace(msg(false,msg),args);
    }

    public static void logWarn(@Nullable Project project, String msg, Object ... args) {
        log(WARN,msg(true,msg),args);
        if(Objects.nonNull(project)) project.getLogger().warn(msg(false,msg),args);
    }

    public static String msg(boolean external, String msg) {
        return "["+(external ? "External" : "Module-Info Scrubber")+"] "+msg;
    }

    private final Attribute<String> artifactType;
    private final Attribute<Boolean> java8ified;

    public ScrubberPlugin() {
        logInfo(null,"Instantiated plugin");
        this.artifactType = Attribute.of("artifactType",String.class);
        this.java8ified = Attribute.of("java8ified",Boolean.class);
    }

    @Override
    public void apply(@NotNull Project project) {
        logInfo(project,"Applying plugin");
        registerAttributes(project);
        setupConfiguration(project);
        registerTransforms(project);
        logInfo(project,"Finished applying plugin");
    }

    private void registerAttributes(Project project) {
        logInfo(project,"Registering attributes");
        DependencyHandler dependencies = project.getDependencies();
        dependencies.attributesSchema(schema -> schema.attribute(this.artifactType));
        dependencies.getArtifactTypes().named("jar",def -> def.getAttributes().attribute(this.java8ified,false));
    }

    private void registerTransforms(Project project) {
        logInfo(project,"Registering transforms");
        project.getDependencies().registerTransform(ScrubberTransform.class,spec -> { //TODO Can the zip attribute be checked for here?
            ScrubberTransform.project = project;
            AttributeContainer from = spec.getFrom();
            from.attribute(this.java8ified,false).attribute(this.artifactType,"jar");
            AttributeContainer to = spec.getTo();
            to.attribute(this.java8ified,true).attribute(this.artifactType,"jar");
        });
    }

    private void setupConfiguration(Project project) {
        logInfo(project,"Initializing configurations");
        project.getConfigurations().all(config -> project.afterEvaluate(p -> {
            if(config.isCanBeResolved()) {
                try {
                    logInfo(project,"Resolvable configuration {}",config.getName());
                    config.attributes(container -> container.attribute(java8ified,true));
                } catch(Exception ignored) {}
            }}));
    }
}