package mods.thecomputerizer.musictriggers.util.image;

import com.madgag.gif.fmsware.GifDecoder;
import mods.thecomputerizer.musictriggers.MusicTriggersCommon;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class GIFHandler {

    public static void splitGif(File file, File folder) throws IOException {
        GifDecoder g = new GifDecoder();
        FileInputStream FIS = new FileInputStream(file);
        g.read(FIS);
        for(int i=0;i<g.getFrameCount();i++) {
            BufferedImage image = g.getFrame(i);
            MusicTriggersCommon.logger.info(image.getWidth());
            ImageIO.write(image, "PNG", new File(folder,i+".png"));
        }
        FIS.close();
    }
}
