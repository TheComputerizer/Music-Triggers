package mods.thecomputerizer.musictriggers.util.image;


import mods.thecomputerizer.musictriggers.MusicTriggersCommon;
import org.jcodec.api.FrameGrab;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;

import javax.imageio.ImageIO;
import java.io.File;

public class MP4Handler {

    public static void splitMP4(File file, File folder, int time) throws Exception {
        int i = 0;
        FileChannelWrapper FCW = NIOUtils.readableChannel(file);
        FrameGrab grab = FrameGrab.createFrameGrab(FCW);
        int total = grab.getVideoTrack().getMeta().getTotalFrames();
        Picture picture;
        while (null != (picture = grab.getNativeFrame())) {
            MusicTriggersCommon.logger.info(i);
            ImageIO.write(AWTUtil.toBufferedImage(picture), "png", new File(folder,i+".png"));
            i+=time;
            if(1>=total) break;
            if(grab.seekToFramePrecise(i)!=null) {
                grab = grab.seekToFramePrecise(i);
            }
        }
        FCW.close();
    }
}
