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
        for(int i=index;i< byteArray.length;i++) {
            ret[i-index] = byteArray[i];
        }
        return byteArray;
    }

    /*
    @Override
    public boolean stream() {
        if(channel==null) return false;
        if(preLoad) {
            if(rawDataStream) preLoad = false;
            else return preLoad();
        }

        if(rawDataStream) {
            if(stopped() || paused()) return true;
            if(channel.buffersProcessed() > 0) channel.processBuffer();
            return true;
        }
        else {
            if(codec==null) return false;
            if(stopped()) return false;
            if(paused()) return true;

            int processed = channel.buffersProcessed();

            SoundBuffer buffer;
            for(int i=0;i < processed;i++) {
                buffer = codec.read();
                if(buffer!=null) {
                    if(buffer.audioData!=null) channel.queueBuffer(buffer.audioData);
                    buffer.cleanup();
                    buffer = null;
                    return true;
                }
                else if(codec.endOfStream()) {
                    synchronized(soundSequenceLock) {
                        if(SoundSystemConfig.getStreamQueueFormatsMatch()) {
                            if(soundSequenceQueue!=null && soundSequenceQueue.size() > 0) {
                                if( codec != null ) codec.cleanup();
                                filenameURL = soundSequenceQueue.remove(0);
                                codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
                                codec.initialize(filenameURL.getURL());
                                buffer = codec.read();
                                if(buffer != null) {
                                    if(buffer.audioData!=null) channel.queueBuffer(buffer.audioData);
                                    buffer.cleanup();
                                    buffer = null;
                                    return true;
                                }
                            }
                            else if(toLoop) {
                                codec.initialize(filenameURL.getURL());
                                buffer = codec.read();
                                if(buffer!=null) {
                                    if(buffer.audioData!=null) channel.queueBuffer(buffer.audioData);
                                    buffer.cleanup();
                                    buffer = null;
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
     */
}
