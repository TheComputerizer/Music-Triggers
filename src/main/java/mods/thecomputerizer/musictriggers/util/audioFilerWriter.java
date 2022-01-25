package mods.thecomputerizer.musictriggers.util;

import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import java.lang.reflect.Type;
import java.util.Collection;

public class audioFilerWriter extends TAudioFileWriter {

    public audioFilerWriter(Collection<AudioFileFormat.Type> fileTypes, Collection<AudioFormat> audioFormats) {
        super(fileTypes,audioFormats);
    }

    public AudioOutputStream getAudioOutputStream(
            AudioFormat audioFormat,
            long lLengthInBytes,
            AudioFileFormat.Type fileType,
            TDataOutputStream dataOutputStream) {
        return null;
    }
}
