package mods.thecomputerizer.musictriggers.client.gui.instance;

import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.data.Audio;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.data.Universal;
import mods.thecomputerizer.musictriggers.client.gui.*;
import mods.thecomputerizer.musictriggers.config.TomlHolders;
import mods.thecomputerizer.theimpossiblelibrary.util.file.FileUtil;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends AbstractChannelConfig {
    private final Universal universalParameters;
    private final Map<TomlHolders.Type, Audio> parsedFile;
    private final List<Audio> newAudio;
    private final List<String> songNames;
    private int nextTotalIndex;

    public Main(File configFile, String channelName, Universal universal, Map<TomlHolders.Type, Audio> parsedFile) {
        super(configFile,channelName);
        this.universalParameters = universal;
        this.parsedFile = parsedFile;
        this.newAudio = new ArrayList<>();
        this.songNames = parsedFile.values().stream().map(Audio::getName).distinct().collect(Collectors.toList());
        setMaxIndices();
    }

    private void setMaxIndices() {
        this.nextTotalIndex = Collections.max(this.parsedFile.keySet(),
                Comparator.comparingInt(TomlHolders.Type::getActualIndex)).getActualIndex()+1;
    }

    @Override
    public List<GuiParameters.Parameter> getParameters(GuiType type) {
        return new ArrayList<>();
    }

    @Override
    public List<GuiPage.Icon> getPageIcons(String channel) {
        return Collections.singletonList(ButtonType.MAIN.getIconButton("main",false));
    }

    @Override
    protected List<String> headerLines() {
        return Arrays.asList("# Please refer to the wiki page located at https://github.com/TheComputerizer/Music-Triggers/wiki/The-Basics",
                "# or the discord server located at https://discord.gg/FZHXFYp8fc",
                "# for any specific questions you might have regarding the main config file","");
    }

    @Override
    protected void write(String newFilePath) {
        File file = FileUtil.generateNestedFile("config/MusicTriggers/"+newFilePath+".toml",true);
        List<TomlHolders.Type> orderedStuff = this.parsedFile.keySet().stream()
                .sorted(Comparator.comparingInt(TomlHolders.Type::getActualIndex)).collect(Collectors.toList());
        List<String> lines = new ArrayList<>(headerLines());
        for(TomlHolders.Type type : orderedStuff) {
            if(type instanceof TomlHolders.Blank)
                for(int i=0;i<((TomlHolders.Blank)type).getLines();i++)
                    lines.add("\n");
            else if (type instanceof TomlHolders.Comment)
                lines.add(((TomlHolders.Comment)type).getComment());
            else {
                if(((TomlHolders.Table)type).getName().matches("universal"))
                    lines.addAll(this.universalParameters.getAsTomlLines());
                else lines.addAll(this.parsedFile.get(type).getAsTomlLines());
            }
        }
        FileUtil.writeLinesToFile(file,lines,false);
    }

    public List<Trigger> getAllTriggers() {
        return this.parsedFile.values().stream()
                .map(Audio::getTriggers)
                .flatMap(Collection::stream)
                .distinct().collect(Collectors.toList());
    }

    public List<String> allIdentifiersOfTrigger(String name) {
        return getAllTriggers().stream().map(trigger -> {
            if(!trigger.getName().matches(name)) return null;
            return trigger.getRegID();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public boolean hasTrigger(String name) {
        for(Trigger trigger : getAllTriggers())
            if(trigger.getName().matches(name))
                return true;
        return false;
    }

    public List<GuiSelection.Element> getSongInstances(GuiSelection selectionScreen, String channelName) {
        List<GuiSelection.Element> elements = new ArrayList<>();
        elements.add(new GuiSelection.Element(selectionScreen, channelName, "universal",
                Translate.guiGeneric(false,"selection","universal"), universalHover(),false,
                0, (channel,songID) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiParameters(selectionScreen,GuiType.SONG_INFO, selectionScreen.getInstance(),
                        "song_info",Translate.guiGeneric(false,"selection","universal"),
                        universalParameters())), null));
        elements.addAll(this.parsedFile.keySet().stream().map(type -> {
            if(type instanceof TomlHolders.Table) {
                TomlHolders.Table song = (TomlHolders.Table)type;
                return new GuiSelection.Element(selectionScreen, channelName, ""+song.getIndex(),
                        Translate.songInstance(song.getName()),null,true,
                        song.getActualIndex()+1, (channel,songID) -> Minecraft.getMinecraft().displayGuiScreen(
                        new GuiSelection(selectionScreen,GuiType.SONG_TRIGGERS, selectionScreen.getInstance(),
                                channelName,"triggers",""+song.getIndex(),
                                songInfoTitle(song.getName()),selectionScreen.getInstance().
                                getChannel(channelName).clickAddButton(channelName,""+song.getIndex()))),
                        (channel, songID) -> removeSong(this.parsedFile.get(type)));
            } else if(type instanceof TomlHolders.Comment) {
                TomlHolders.Comment comment = (TomlHolders.Comment)type;
                return new GuiSelection.Element(selectionScreen, channelName, "comment",
                        Translate.guiGeneric(false,"selection","comment"),
                        Collections.singletonList(comment.getComment()), false, comment.getActualIndex()+1,
                        null, (channel,songID) -> removeComment(type.getActualIndex()));
            } else return null;
        }).filter((Objects::nonNull)).sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList()));
        return elements;
    }

    private List<GuiParameters.Parameter> universalParameters() {
        return Arrays.asList(new GuiParameters.Parameter("universal","fade_in", null,
                this.universalParameters.getFadeIn(), this.universalParameters::setFadeIn),
                new GuiParameters.Parameter("universal","fade_out", null,
                        this.universalParameters.getFadeOut(), this.universalParameters::setFadeOut),
                new GuiParameters.Parameter("universal","persistence", null,
                        this.universalParameters.getPersistence(), this.universalParameters::setPersistence),
                new GuiParameters.Parameter("universal","trigger_delay", null,
                        this.universalParameters.getTriggerDelay(), this.universalParameters::setTriggerDelay),
                new GuiParameters.Parameter("universal","song_delay", null,
                        this.universalParameters.getSongDelay(), this.universalParameters::setSongDelay),
                new GuiParameters.Parameter("universal","start_delay", null,
                        this.universalParameters.getStartDelay(), this.universalParameters::setStartDelay));
    }

    private String songInfoTitle(String name) {
        return Translate.guiGeneric(false,"selection","group","triggers")+" "+
                Translate.songInstance(name);
    }

    public List<GuiSelection.Element> getTriggerInstances(GuiSelection selectionScreen, String channelName, String extra) {
        List<GuiSelection.Element> ret = new ArrayList<>();
        Audio audio = getAudioFromIndex(extra);
        ret.add(new GuiSelection.Element(selectionScreen, channelName, "song_info",
                Translate.guiGeneric(false,"selection","song_info"), songInfoHover(audio),false,
                0, (channel,songID) -> Minecraft.getMinecraft().displayGuiScreen(
                new GuiParameters(selectionScreen,GuiType.SONG_INFO, selectionScreen.getInstance(),
                        "song_info",audio.getName(),songInfoParameters(audio))), null));
        for(Trigger trigger : audio.getTriggers()) {
            if(trigger.getName().matches("menu") || trigger.getName().matches("generic"))
                ret.add(new GuiSelection.Element(selectionScreen, channelName, trigger.getNameWithID(),
                        Translate.triggerElement(trigger), Translate.triggerElementHover(trigger), false, 0,
            null,(channel, triggerIdentifier) -> audio.removeTrigger(trigger)));
            else ret.add(new GuiSelection.Element(selectionScreen, channelName, trigger.getNameWithID(),
                    Translate.triggerElement(trigger), Translate.triggerElementHover(trigger), false, 0,
                    (channel,triggerIdentifier) -> Minecraft.getMinecraft().displayGuiScreen(
                            new GuiParameters(selectionScreen,GuiType.TRIGGER_INFO, selectionScreen.getInstance(),
                                    "trigger_info",audio.getName(),
                                    triggerInfoParameters(trigger))),
                    (channel, triggerIdentifier) -> audio.removeTrigger(trigger)));
        }
        return ret.stream()
                .filter((Objects::nonNull))
                .sorted(Comparator.comparingInt(GuiSelection.Element::getIndex))
                .collect(Collectors.toList());
    }

    private List<String> universalHover() {
        return Collections.singletonList(Translate.guiGeneric(false, "selection", "universal"));
    }

    private List<String> songInfoHover(Audio audio) {
        return Arrays.asList(Translate.guiGeneric(false,"parameter","song_info","volume","name")+ ": "+
                        audio.getVolume(),
                Translate.guiGeneric(false,"parameter","song_info","pitch","name")+ ": "+
                        audio.getPitch());
    }

    public List<GuiParameters.Parameter> songInfoParameters(Audio audio) {
        return Arrays.asList(new GuiParameters.Parameter("song_info","load_order",null,
                        audio.getLoadOrder(), audio::setLoadOrder),
                new GuiParameters.Parameter("song_info","volume",null,""+audio.getVolume(),
                        (element) -> audio.setVolume(Float.parseFloat(element))),
                new GuiParameters.Parameter("song_info","pitch",null,""+audio.getPitch(),
                        (element) -> audio.setPitch(Float.parseFloat(element))),
                new GuiParameters.Parameter("song_info","chance",null,audio.getChance(),
                        audio::setChance),
                new GuiParameters.Parameter("song_info","play_once",null,audio.getPlayOnce(),
                        audio::setPlayOnce),
                new GuiParameters.Parameter("song_info","must_finish",null,audio.mustFinish(),
                        audio::setMustFinish));
    }

    public List<GuiParameters.Parameter> triggerInfoParameters(Trigger trigger) {
        return Trigger.getAcceptedParameters(trigger.getName())
                .stream().map(parameter -> new GuiParameters.Parameter("trigger_info",parameter,trigger.getName()
                        ,trigger.getParameter(parameter),(element) -> trigger.setParameter(parameter,element)))
                .collect(Collectors.toList());
    }

    private Audio getAudioFromIndex(String index) {
        int i = Integer.parseInt(index);
        for(Audio audio : this.parsedFile.values())
            if(audio.getLoadOrder()==i)
                return audio;
        return null;
    }

    public List<Trigger> getTriggers(String index) {
        return getAudioFromIndex(index).getTriggers();
    }

    public boolean isSongUsed(String song) {
        return this.songNames.contains(song);
    }

    public void addTrigger(String index, String triggerName) {
        for(Audio audio : this.parsedFile.values())
            if(audio.getLoadOrder()==Integer.parseInt(index))
                audio.addTrigger(Trigger.createEmptyForGui(triggerName,this.getChannelName()));
    }

    public void addSong(String song) {
        int size = (int) this.parsedFile.values().stream().filter(Objects::nonNull).count();
        int multi = -1;
        if(this.songNames.contains(song)) {
            multi = 0;
            for(Audio audio : this.parsedFile.values())
                if(audio.getName().matches(song))
                    multi++;
        } else this.songNames.add(song);
        Audio audio = new Audio(null,null,song,size,multi);
        this.parsedFile.put(new TomlHolders.Table(new String[]{""+size,song},this.nextTotalIndex),audio);
        this.newAudio.add(audio);
        this.nextTotalIndex++;
    }

    public void removeSong(Audio audio) {
        String name = audio.getName();
        this.newAudio.remove(audio);
        int id = audio.getLoadOrder();
        int tomlID = 0;
        Iterator<Map.Entry<TomlHolders.Type, Audio>> itr = this.parsedFile.entrySet().iterator();
        while (itr.hasNext()) {
            TomlHolders.Type type = itr.next().getKey();
            if (type instanceof TomlHolders.Table && ((TomlHolders.Table) type).getIndex() == id) {
                tomlID = type.getActualIndex();
                itr.remove();
                break;
            }
        }
        for(Map.Entry<TomlHolders.Type, Audio> entry : this.parsedFile.entrySet()) {
            if(entry.getKey().getActualIndex()>tomlID) {
                entry.getKey().updateIndex(entry.getKey().getActualIndex()-1);
                if(entry.getKey() instanceof TomlHolders.Table) {
                    TomlHolders.Table table = (TomlHolders.Table)entry.getKey();
                    table.updateSongIndex(table.getIndex()-1);
                    entry.getValue().setLoadOrder(table.getIndex()-1);
                }
            }
        }
        for(Audio audioVal : this.parsedFile.values())
            if(audioVal.getName().matches(name))
                return;
        this.songNames.remove(name);
        this.nextTotalIndex--;
    }

    public void removeComment(int tomlIndex) {
        Iterator<Map.Entry<TomlHolders.Type, Audio>> itr = this.parsedFile.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getKey().getActualIndex() == tomlIndex) {
                itr.remove();
                break;
            }
        }
        for (TomlHolders.Type type : this.parsedFile.keySet()) {
            if (type.getActualIndex() > tomlIndex) {
                type.updateIndex(type.getActualIndex() - 1);
                if (type instanceof TomlHolders.Table) {
                    TomlHolders.Table table = (TomlHolders.Table) type;
                    table.updateSongIndex(table.getIndex() - 1);
                }
            }
        }
        this.nextTotalIndex--;
    }
}
