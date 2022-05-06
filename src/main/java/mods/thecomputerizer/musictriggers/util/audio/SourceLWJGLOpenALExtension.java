package mods.thecomputerizer.musictriggers.util.audio;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;

public class SourceLWJGLOpenALExtension extends SourceLWJGLOpenAL {

    public float milliseconds;

    public SourceLWJGLOpenALExtension(FloatBuffer listenerPosition, IntBuffer myBuffer, boolean priority, boolean toStream, boolean toLoop, String sourcename,
                             FilenameURL filenameURL, SoundBuffer soundBuffer, float x, float y, float z, int attModel, float distOrRoll, boolean temporary,
                                      float millisecondOffset, float volume, float pitch) {
        super(listenerPosition,myBuffer,priority,toStream,toLoop,sourcename,filenameURL,soundBuffer,x,y,z,attModel,distOrRoll,temporary);
        this.milliseconds=millisecondOffset;
        this.sourceVolume = volume;
        this.pitch = pitch;
    }


    @Override
    public boolean preLoad() {
        float read = 0f;
        if(codec==null) return false;
        codec.initialize(filenameURL.getURL());
        LinkedList<byte[]> preLoadBuffers = new LinkedList<>();
        for( int i = 0; i < SoundSystemConfig.getNumberStreamingBuffers(); i++) {
            soundBuffer = codec.read();
            if(soundBuffer == null || soundBuffer.audioData == null) break;
            if(read<this.milliseconds) {
                float bytesPerMilli = ((soundBuffer.audioFormat.getSampleRate()*soundBuffer.audioFormat.getSampleSizeInBits()*soundBuffer.audioFormat.getChannels())/1000f);
                read+=((soundBuffer.audioData.length/bytesPerMilli)*8f);
                if(read<this.milliseconds) {
                    i-=1;
                    MusicTriggers.logger.info("Skipping packet: Read "+read+" milliseconds of packet data");
                }
                else {
                    int index = soundBuffer.audioData.length-(int)(soundBuffer.audioData.length*((read-this.milliseconds)/(soundBuffer.audioData.length)));
                    MusicTriggers.logger.info("Original byte array length: "+soundBuffer.audioData.length+" Index: "+index);
                    preLoadBuffers.add(copyByteArrayAfterIndex(soundBuffer.audioData, index));
                }
            }
            else preLoadBuffers.add(soundBuffer.audioData);
        }
        positionChanged();
        channel.preLoadBuffers( preLoadBuffers );
        preLoad = false;
        return true;
    }

    private byte[] copyByteArrayAfterIndex(byte[] byteArray, int index) {
        byte[] ret = new byte[byteArray.length-index];
        if (byteArray.length - index >= 0)
            System.arraycopy(byteArray, index, ret, 0, byteArray.length - index);
        return byteArray;
    }
}
