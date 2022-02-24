package mods.thecomputerizer.musictriggers.util.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class PNGMcMetaHandler {

    public static void splitPNG(File file, File folder, int split) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if(split==0) {
            split = image.getWidth();
        }
        int height = image.getHeight();
        File write;
        for(int i = split;i<height;i+=split) {
            BufferedImage splitImage = image.getSubimage(0,i-split,image.getWidth(),split);
            write = new File(folder, i+".png");
            if(!write.exists()) {
                write.createNewFile();
            }
            ImageIO.write(splitImage, "PNG", write);
        }
    }
}
