package mods.thecomputerizer.musictriggers.config;


import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.util.NetworkUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.*;
import java.util.*;

public class ConfigJukebox {

    private File file;
    private ResourceLocation fileSource;
    public final Map<String, String> recordMap;

    public ConfigJukebox(File file) {
        boolean exists = file.exists();
        this.file = exists ? file : FileUtil.generateNestedFile(file,false);
        if(!exists) FileUtil.writeLinesToFile(this.file,headerLines(),false);
        this.recordMap = new HashMap<>();
    }

    public ConfigJukebox(ResourceLocation fileSource) {
        this.fileSource = fileSource;
        this.recordMap = new HashMap<>();
    }

    private List<String> headerLines() {
        return Arrays.asList("Format this like name = key",
                "The key refers to a lang key in the format of record.musictriggers.key which ",
                "determines the description of the registered disc",
                "Any lines with Format in the name or = not present will not be read in",
                "Make sure each new entry is on a new line",
                "Here is an example",
                "song1 = dragon");
    }

    private Reader makeReader() throws IOException {
        return Objects.nonNull(this.file) ? new FileReader(this.file) : new InputStreamReader(Minecraft.getMinecraft()
                .getResourceManager().getResource(this.fileSource).getInputStream());
    }

    public void parse() {
        if(Objects.isNull(this.file) && Objects.isNull(this.fileSource)) return;
        try {
            BufferedReader br = new BufferedReader(makeReader());
            String line = br.readLine();
            while(Objects.nonNull(line)) {
                if(!line.contains("Format") && line.contains("=") && !line.contains("==")) {
                    String[] broken = MusicTriggers.stringBreaker(line,"=");
                    recordMap.put(broken[0].trim(),broken[1].trim());
                }
                line = br.readLine();
            }
            br.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    public ConfigJukebox(ByteBuf buf) {
        this.recordMap = NetworkUtil.readGenericMap(buf,NetworkUtil::readString,NetworkUtil::readString);
    }
}
