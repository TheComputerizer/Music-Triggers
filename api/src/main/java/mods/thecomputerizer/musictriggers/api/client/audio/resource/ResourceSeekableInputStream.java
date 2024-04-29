package mods.thecomputerizer.musictriggers.api.client.audio.resource;

import com.sedmelluq.discord.lavaplayer.tools.io.ExtendedBufferedInputStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.info.AudioTrackInfoProvider;
import mods.thecomputerizer.musictriggers.api.data.log.LoggableAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ResourceSeekableInputStream extends SeekableInputStream {

    public static ResourceSeekableInputStream get(LoggableAPI logger, ResourceLocationAPI<?> location) {
        byte[] bytes = null;
        try(InputStream stream = ResourceHelper.getResourceStream(location)) {
            if(Objects.nonNull(stream)) bytes = IOUtils.toByteArray(stream);
        } catch(IOException ex) {
            logger.logError("Failed to get bytes of resource `{}`!",location.get(),ex);
        }
        return Objects.nonNull(bytes) ? new ResourceSeekableInputStream(logger,bytes) : null;
    }

    private final LoggableAPI logger;
    private final byte[] bytes;
    private final ExtendedBufferedInputStream stream;
    private long position;

    public ResourceSeekableInputStream(LoggableAPI logger, byte[] bytes) {
        super(bytes.length,0);
        this.logger = logger;
        this.bytes = bytes;
        this.stream = new ExtendedBufferedInputStream(new ByteArrayInputStream(bytes));
    }

    @Override
    public int available() throws IOException {
        return this.stream.available();
    }

    @Override
    public boolean canSeekHard() {
        return true;
    }

    @Override
    public void close() throws IOException {
        try {
            this.stream.close();
        } catch(IOException ex) {
            this.logger.logWarn("Failed to close stream",ex);
        }
    }

    @Override
    public long getPosition() {
        return this.position;
    }

    @Override
    public List<AudioTrackInfoProvider> getTrackInfoProviders() {
        return Collections.emptyList();
    }

    @Override
    public int read() throws IOException {
        int result = this.stream.read();
        if(result>=0) this.position++;
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = this.stream.read(b,off,len);
        this.position+=read;
        return read;
    }

    @Override
    protected void seekHard(long position) {
        this.position = Math.min(position,this.bytes.length);
        this.stream.discardBuffer();
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = this.stream.skip(n);
        this.position+=skipped;
        return skipped;
    }
}
