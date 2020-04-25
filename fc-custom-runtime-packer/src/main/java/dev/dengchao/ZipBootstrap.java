package dev.dengchao;

import org.apache.commons.compress.archivers.zip.AsiExtraField;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Zip build result together with bootstrap into a zip file.
 */
public class ZipBootstrap extends DefaultTask {

    /**
     * The profile which this task is focusing on.
     */
    @Nullable
    private String profile;
    /**
     * The bootstrap file.
     */
    @Nullable
    private File bootstrap;
    /**
     * The bootJar archive file.
     */
    @Nullable
    private File bootJarArchive;
    private File output;

    void setProfile(@NotNull String profile) {
        this.profile = profile;
    }

    void setBootstrap(@NotNull File bootstrap) {
        this.bootstrap = bootstrap;
    }

    void setBootJarArchive(@NotNull File bootJarArchive) {
        this.bootJarArchive = bootJarArchive;
    }

    @OutputFile
    public File getOutput() {
        Objects.requireNonNull(bootJarArchive);

        File dir = new File(getProject().getBuildDir(), "libs");
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }

        output = new File(dir, bootJarArchive.getName().replaceFirst("\\.jar", "-" + profile + "\\.zip"));

        return output;
    }

    @TaskAction
    public void taskAction() throws IOException {
        Objects.requireNonNull(profile);
        Objects.requireNonNull(bootstrap);
        Objects.requireNonNull(bootJarArchive);

        getLogger().debug("Output into {}", output);

        ZipArchiveOutputStream out = new ZipArchiveOutputStream(new FileOutputStream(output));

        ZipArchiveEntry bootstrapEntry = new ZipArchiveEntry("bootstrap");
        bootstrapEntry.setUnixMode(0x775);
        out.putArchiveEntry(bootstrapEntry);
        BufferedReader bootstrapReader = new BufferedReader(new FileReader(this.bootstrap));
        List<String> lines = bootstrapReader.lines().collect(Collectors.toList());
        byte[] newLine = "\n".getBytes();
        for (String line : lines) {
            out.write(line.replaceAll("archive|boot\\.jar", bootJarArchive.getName()).getBytes());
            out.write(newLine);
        }
        out.closeArchiveEntry();
        getLogger().debug("Bootstrap file zipped");

        out.putArchiveEntry(new ZipArchiveEntry(bootJarArchive.getName()));
        FileInputStream bootJarInputStream = new FileInputStream(bootJarArchive);
        byte[] bytes = new byte[bootJarInputStream.available()];
        out.write(bytes);
        out.closeArchiveEntry();
        getLogger().debug("Boot jar file zipped");

        out.flush();
        out.close();
    }
}
