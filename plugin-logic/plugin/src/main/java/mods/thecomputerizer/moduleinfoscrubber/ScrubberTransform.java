import org.apache.commons.io.IOUtils;
import org.gradle.api.artifacts.transform.*;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.CompileClasspath;
import org.gradle.internal.IoActions;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.println;

/**
 * TransformAction implementation
 */
public abstract class ModuleInfoScrubber implements TransformAction<TransformParameters.None> {

    @Classpath
    @InputArtifact
    abstract Provider<FileSystemLocation> getPrimaryInput();

    @CompileClasspath
    @InputArtifactDependencies
    abstract FileCollection getDependencies();

    @Override
    public void transform(TransformOutputs outputs) {
        File inFile = getPrimaryInput().get().getAsFile();
        if(inFile.getName().endsWith(".jar")) {
            File tempFile = null;
            try {
                tempFile = File.createTempFile(inFile.getName().replace(".jar",""),".jar");
                if (extractZipEntry(inFile,"module-info",tempFile))
                    Files.copy(tempFile.toPath(),inFile.toPath(),COPY_ATTRIBUTES,REPLACE_EXISTING);
                else println("No module-info files found in "+inFile);
            } catch(IOException ex) {
                println("Failed to copy file "+inFile+" due to: "+ex.getMessage());
            } finally {
                if(Objects.nonNull(tempFile)) tempFile.deleteOnExit();
            }
        } else println("Ignoring non jar file "+inFile);
        File outFile = outputs.file(inFile);
        outFile.getParentFile().mkdirs();
    }

    public static boolean extractZipEntry(File inFile, String entryName, File outFile) throws IOException {
        ZipInputStream zipStream = null;
        BufferedOutputStream targetStream = null;
        boolean foundModuleInfo = false;
        try {
            zipStream = new ZipInputStream(new FileInputStream(inFile));
            targetStream = new BufferedOutputStream(new FileOutputStream(outFile));
            boolean streamEnded = false;
            while(!streamEnded) {
                ZipEntry entry = zipStream.getNextEntry();
                if(Objects.isNull(entry)) streamEnded = true;
                else if(Objects.isNull(entryName) || !entry.getName().contains(entryName))
                    IOUtils.copy(zipStream,targetStream);
                else {
                    println("Skipped module-info entry from "+inFile);
                    foundModuleInfo = true;
                }
            }
        } finally {
            IoActions.closeQuietly(zipStream);
            IoActions.closeQuietly(targetStream);
        }
        return foundModuleInfo;
    }
}
