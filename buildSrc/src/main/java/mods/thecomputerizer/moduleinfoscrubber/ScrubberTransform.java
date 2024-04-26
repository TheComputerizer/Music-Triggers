package mods.thecomputerizer.moduleinfoscrubber;

import org.gradle.api.Project;
import org.gradle.api.artifacts.transform.*;
import org.gradle.api.file.CopySpec;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.CompileClasspath;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;

import static mods.thecomputerizer.moduleinfoscrubber.ScrubberPlugin.*;

/**
 * TransformAction implementation
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public abstract class ScrubberTransform implements TransformAction<TransformParameters.None> {

   public static Project project;

    @Classpath
    @InputArtifact
    public abstract Provider<FileSystemLocation> getPrimaryInput();

    @CompileClasspath
    @InputArtifactDependencies
    public abstract FileCollection getDependencies();

    private void supplyCopySpec(Project root, CopySpec spec, File inFile, File outFile) {
        spec.from(inFile.isDirectory() ? inFile : root.zipTree(inFile));
        spec.exclude("**/module-info.class");
        spec.into(outFile);
    }

    private void copyWithFilter(Project project, File inFile, File outFile) {
        logInfo(project,"Attempting to copy from input `{}` to output `{}`",inFile,outFile);
        project.copy(spec -> supplyCopySpec(project.getRootProject(),spec,inFile,outFile));
    }

    @Override
    public void transform(@NotNull TransformOutputs outputs) {
        File inFile = getPrimaryInput().get().getAsFile();
        File outFile = outputs.file(inFile);
        outFile.getParentFile().mkdirs();
        if(!inFile.getName().endsWith(".jar")) return;
        logInfo(project,"Attempting to transform input file {}",inFile);
        if(Objects.isNull(project)) logError(null,"Project reference is null!");
        if(inFile.getPath().equals(outFile.getPath())) {
            String name = inFile.getName();
            name = name.endsWith(".jar") ? name.substring(0,name.length()-4) : name;
            try {
                File temp = File.createTempFile(name,"");
                copyWithFilter(project,inFile,temp);
                inFile = temp;
            } catch(IOException ex) {
                logError(project,"Failed to create temporary file ",ex);
            }
        }
        copyWithFilter(project,inFile,outFile);
        logInfo(project,"Writing transform result to {}",outFile);
    }
}
