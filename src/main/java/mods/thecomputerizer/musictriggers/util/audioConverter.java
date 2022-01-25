package mods.thecomputerizer.musictriggers.util;

import javazoom.jl.converter.Converter;
import mods.thecomputerizer.musictriggers.MusicTriggers;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class audioConverter {

    public static void mp3ToOgg(File source, File folder, String name){
        File target = new File(folder,name);
        if(target.exists()) {
            target.delete();
        }
        try {
            new Converter().convert(source.getPath(),target.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        WavToOgg(target.getPath(), target.getPath().replaceAll(".wav",".ogg"), false);
    }

    public static void WavToOgg(String sourcePath, String targetPath, boolean temp) {
        try {
            AudioInputStream wav = AudioSystem.getAudioInputStream(new File(sourcePath));
            int numChannels = wav.getFormat().getChannels();
            double[][] stream = to2DDoubleArray(wav, wav.getFormat().getSampleSizeInBits()/8);
            wav.close();
            if(stream!=null) {
                try {
                    VorbisEncoder encoder = new VorbisEncoder(stream, numChannels, 48000, 1F);
                    MusicTriggers.logger.info("size: "+encoder.stream[0].length);
                    File target = new File(targetPath);
                    if (target.exists()) target.delete();
                    if (!temp) {
                        target = new File(targetPath);
                        encoder.encode(new FileOutputStream(target));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                MusicTriggers.logger.error("Unable to convert null stream!");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double[][] to2DDoubleArray(AudioInputStream audio, int bytesPerSample){
        try {
            ByteArrayOutputStream leftbaos = new ByteArrayOutputStream();
            ByteArrayOutputStream rightbaos = new ByteArrayOutputStream();
            byte[] bytes = new byte[bytesPerSample*2];
            final int channels = 2;
            while (true) {
                int readsize = 0;
                try {
                    readsize = audio.read(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (readsize==-1){
                    break;
                }
                for (int sample=0; sample<readsize/channels/bytesPerSample;sample++) {
                    final int offset = sample * bytesPerSample * channels;
                    leftbaos.write(bytes, offset, bytesPerSample);
                    rightbaos.write(bytes, offset + bytesPerSample, bytesPerSample);
                }
            }
            byte[] left = leftbaos.toByteArray();
            byte[] right = rightbaos.toByteArray();
            double[][] ret = new double[2][left.length];
            ret[0] = byteToDouble(left);
            ret[1] = byteToDouble(right);
            return ret;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static double[] byteToDouble(byte[] bytes) {
        int times = Double.SIZE / Byte.SIZE;
        double[] doubles = new double[bytes.length / times];
        for(int i=0;i<doubles.length;i++){
            doubles[i] = ByteBuffer.wrap(bytes, i*times, times).getDouble();
        }
        return doubles;
    }
}
