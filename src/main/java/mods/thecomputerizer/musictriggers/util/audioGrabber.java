package mods.thecomputerizer.musictriggers.util;


import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import de.jarnbjo.theora.OggpackBuffer;
import org.apache.commons.compress.utils.IOUtils;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.ogg.OggPacketReader;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;
import org.gagravarr.opus.OpusInfo;
import org.gagravarr.opus.OpusPacket;

import java.io.*;

public class audioGrabber {

    public static void dl(String url) {
        /*
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            InputStream audioStream = new BufferedInputStream(response.body().byteStream());
            OutputStream out = new FileOutputStream("config/MusicTriggers/songs/assets/musictriggers/sounds/music/output.opus");
            OpusFile opus = new OpusFile(new OggPacketReader(audioStream));
            opus.getInfo().setSampleRate(48000);
            opus.getInfo().setNumChannels(2);
            opus.getTags().addComment("title","music triggers links implementation");
            OpusFile file = new OpusFile(out,opus.getInfo(),opus.getTags());
            while (opus.getNextAudioPacket()!=null) {
                file.writeAudioData(opus.getNextAudioPacket());
            }
            IOUtils.closeQuietly(audioStream);
            IOUtils.closeQuietly(out);
        } catch(Exception e) {
            e.printStackTrace();
        }
         */
    }
}
