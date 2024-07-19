package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.BasicLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataList;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink.ParameterElement;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.SelectionLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.SelectionLink.SelectionElement;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.WrapperLink.WrapperElement;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle.From;
import mods.thecomputerizer.musictriggers.api.data.global.Toggle.To;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.HolderTrigger;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.io.FileHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static mods.thecomputerizer.musictriggers.api.MTRef.CONFIG_PATH;
import static mods.thecomputerizer.musictriggers.api.MTRef.GLOBAL_CONFIG;
import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;

public class MTScreenInfo {
    
    public static MTScreenInfo get(String type) {
        return new MTScreenInfo(null,type);
    }
    
    public static MTScreenInfo get(@Nullable MTScreenInfo parent, String type) {
        return new MTScreenInfo(parent,type);
    }
    
    @Getter private final MTScreenInfo parent;
    @Getter private final String type;
    private final Map<ChannelAPI,Set<DataLink>> channelCache;
    private final Map<ChannelAPI,DataLink> channelLinks;
    private final Set<DataLink> globalCache;
    private final boolean global;
    private DataLink globalLink;
    @Getter private ChannelAPI channel;
    @Setter private Button applyButton;
    
    public MTScreenInfo(@Nullable MTScreenInfo parent, String type) {
        this.parent = parent;
        this.type = type;
        this.global = Misc.equalsAny(type,"debug","from","from_list","home","log","playback","to","to_list",
                                     "toggles","toggle");
        this.globalCache = new HashSet<>();
        this.channelCache = new HashMap<>();
        this.channelLinks = new HashMap<>();
        if(Objects.nonNull(this.parent)) {
            this.channel = parent.channel;
            for(ChannelAPI channel : parent.channelCache.keySet())
                this.channelCache.put(channel,new HashSet<>());
        } else {
            for(ChannelAPI channel : ChannelHelper.getClientHelper().getChannels().values()) {
                if(channel.isClientChannel() && !Misc.equalsAny(channel.getName(),"jukebox","preview")) {
                    this.channelCache.put(channel, new HashSet<>());
                    if(Objects.isNull(this.channel)) this.channel = channel;
                }
            }
        }
    }
    
    public void applyChanges() {
        if(Objects.nonNull(this.parent)) {
            this.parent.applyChanges();
            return;
        }
        if(!isModified()) return;
        Toml global = Toml.getEmpty();
        Toml debugTable = Toml.getEmpty();
        MTScreenInfo debug = next("debug",null);
        debug.globalLink.populateToml(debugTable);
        global.addTable("debug",debugTable);
        Toml channels = Toml.getEmpty();
        MTScreenInfo info = next("channels",null).next("channel_info",null);
        for(ChannelAPI channel : this.channelCache.keySet()) {
            Toml channelToml = Toml.getEmpty();
            info.channel = channel;
            if(!info.channelLinks.containsKey(channel)) info.setLink(channel.getInfo().getLink());
            info.getLink().populateToml(channelToml);
            buildChannelFiles(info.parent,channelToml);
            channels.addTable(channel.getName(),channelToml);
        }
        global.addTable("channels",channels);
        MTDataRef.writeToFile(global,GLOBAL_CONFIG);
        buildTogglesFile(ChannelHelper.getGlobalData().getTogglesPath());
        MTClientEvents.queueReload(ClientHelper.getMinecraft(),5);
        MTGUIScreen.isActive = false;
    }
    
    private void buildChannelFiles(MTScreenInfo info, Toml toml) {
        Set<DataLink> links = info.channelCache.get(info.channel);
        for(DataLink link : links) {
            if(link.getTypeName().equals("channel_info")) continue;
            String type = link.getTypeName();
            switch(type) {
                case "jukebox": info.writeChannelTxt(toml,buildRecords(info.channel,link),type);
                case "redirect": info.writeChannelTxt(toml,buildRedirect(info.channel,link),type);
                default: info.writeChannelToml(toml,buildChannelFile(info.channel,link),type);
            }
        }
    }
    
    private Toml buildChannelFile(ChannelAPI channel, DataLink link) {
        link.getType().setChannel(channel,true);
        Toml toml = Toml.getEmpty();
        link.populateToml(toml);
        return toml;
    }
    
    private @Nullable List<String> buildRecords(ChannelAPI channel, DataLink data) {
        if(data instanceof WrapperLink) {
            data.getType().setChannel(channel,true);
            List<String> lines = new ArrayList<>();
            for(WrapperElement element : ((WrapperLink)data).getWrappers()) {
                ParameterLink link = element.getAsParameter();
                if(Objects.nonNull(link) && link.getWrapper() instanceof RecordElement) {
                    String key = String.valueOf(link.getModifiedValue("jukebox_key"));
                    String value = String.valueOf(link.getModifiedValue("jukebox_value"));
                    lines.add(key+" = "+value);
                }
            }
            return lines;
        }
        return null;
    }
    
    private @Nullable List<String> buildRedirect(ChannelAPI channel, DataLink data) {
        if(data instanceof WrapperLink) {
            data.getType().setChannel(channel,true);
            List<String> lines = new ArrayList<>();
            for(WrapperElement element : ((WrapperLink)data).getWrappers()) {
                ParameterLink link = element.getAsParameter();
                if(Objects.nonNull(link) && link.getWrapper() instanceof RedirectElement) {
                    RedirectElement redirect = (RedirectElement)link.getWrapper();
                    String key = String.valueOf(link.getModifiedValue("redirect_key"));
                    String value = String.valueOf(link.getModifiedValue("redirect_value"));
                    lines.add(key+(redirect.isRemote() ? " = " : " == ")+value);
                }
            }
            return lines;
        }
        return null;
    }
    
    private void buildTogglesFile(String path) {
        for(DataLink data : getCache()) {
            if(data.getTypeName().equals("toggles")) {
                if(data.isModified()) {
                    Toml toml = Toml.getEmpty();
                    data.populateToml(toml);
                    MTDataRef.writeToFile(toml,path);
                }
                return;
            }
        }
    }
    
    public void distributeIdentifierChange(ParameterLink link, String id, String previous) {
        TriggerAPI trigger = (TriggerAPI)link.getWrapper();
        setDisplayName(link,MTGUIScreen.triggerName(trigger.getName(),id,trigger instanceof HolderTrigger));
        MTScreenInfo type = this;
        while(Objects.nonNull(type.parent)) type = type.parent;
        for(DataLink data : this.globalCache)
            if(data.getTypeName().equals("toggles"))
                distributeIdentifierChangeToggles(data,trigger,id,previous);
    }
    
    public void distributeIdentifierChangeToggles(DataLink data, TriggerAPI trigger, String id, String previous) {
        if(data instanceof WrapperLink) {
            for(WrapperElement toggle : ((WrapperLink)data).getWrappers()) {
                ParameterLink link = toggle.getAsParameter();
                if(Objects.nonNull(link))
                    for(DataLink child : link.getChildren()) distributeIdentifierChangeToggle(child,trigger,id,previous);
            }
        }
    }
    
    //TODO This is stupid and can be abstracted
    public void distributeIdentifierChangeToggle(DataLink child, TriggerAPI trigger, String id, String previous) {
        if(child instanceof WrapperLink) {
            for(WrapperElement element : ((WrapperLink)child).getWrappers()) {
                ParameterLink parameters = element.getAsParameter();
                if(Objects.nonNull(parameters)) {
                    ParameterWrapper wrapper = parameters.getWrapper();
                    if(wrapper instanceof From || wrapper instanceof To) {
                        String channel = String.valueOf(parameters.getModifiedValue("channel"));
                        Object triggers = parameters.getModifiedValue("triggers");
                        if(channel.equals(trigger.getChannel().getName()) && triggers instanceof List<?>) {
                            List<String> newTriggers = new ArrayList<>();
                            boolean changed = false;
                            for(Object value : (List<?>)triggers) {
                                String asString = String.valueOf(value);
                                String[] split = asString.split("-",2);
                                if(split.length==1) {
                                    newTriggers.add(asString);
                                    continue;
                                }
                                String triggerName = trigger.getName();
                                if(split[0].equals(triggerName) && split[1].equals(previous)) {
                                    asString = triggerName+"-"+id;
                                    changed = true;
                                }
                                newTriggers.add(asString);
                            }
                            if(changed) parameters.distributeExternalChange("triggers",newTriggers);
                        }
                    }
                }
            }
        }
    }
    
    public void distributeJukeboxChange(ParameterLink link) {
        setDisplayName(link,MTGUIScreen.jukeboxName(link));
    }
    
    public void distributeRedirectChange(ParameterLink link, String previous) {
        setDisplayName(link,MTGUIScreen.redirectName(link));
        MTScreenInfo mainType = this.parent.next("main",null);
        DataLink data = mainType.getLink();
        if(data instanceof WrapperLink) {
            for(WrapperElement wrapper : ((WrapperLink)data).getWrappers()) {
                ParameterLink audioLink = wrapper.getAsParameter();
                if(Objects.nonNull(audioLink)) {
                    ParameterWrapper parameters = audioLink.getWrapper();
                    String newKey = String.valueOf(link.getModifiedValue("redirect_key"));
                    String location = String.valueOf(audioLink.getModifiedValue("location"));
                    if(!Misc.equalsAny(location,"null","_")) {
                        if(location.equals(previous)) audioLink.distributeExternalChange("location",newKey);
                    } else if(parameters.getName().equals(previous) && !((AudioRef)parameters).isFile()) {
                        String newValue = String.valueOf(link.getModifiedValue("redirect_value"));
                        audioLink.setWrapper(AudioRef.addToGui(mainType,newKey,newValue,false));
                    }
                }
            }
        }
    }
    
    public void enableApplyButton() {
        if(Objects.nonNull(this.applyButton)) this.applyButton.setVisible(true);
        if(Objects.nonNull(this.parent)) this.parent.enableApplyButton();
    }
    
    public @Nullable DataLink findChannelLink(String type, @Nullable ParameterWrapper wrapper) {
        if(Objects.isNull(this.channel)) return null;
        switch(type) {
            case "channel_info": return this.channel.getInfo().getLink();
            case "commands": return this.channel.getCommandsLink();
            case "jukebox": return this.channel.getRecordsLink();
            case "main": return this.channel.getMainLink();
            case "redirect": return this.channel.getRedirectLink();
            case "renders": return this.channel.getRendersLink();
            default: return Objects.nonNull(wrapper) ? wrapper.getLink() : null;
        }
    }
    
    public Collection<String> findLocalAudio() {
        File folder = findLocalFolder();
        File[] files = folder.exists() && folder.isDirectory() ? folder.listFiles(((dir,name) -> name.length()>4 &&
                Misc.equalsAny(name.substring(name.length()-4),".aac",".m3u",".mp3",".mp4",".ogg",".wav"))) : null;
        Set<String> names = new HashSet<>();
        if(Objects.nonNull(files))
            for(File file : files)
                if(!file.isDirectory()) names.add(file.getName());
        return names;
    }
    
    public File findLocalFolder() {
        MTScreenInfo info = this;
        while(Objects.nonNull(info.parent)) info = info.parent;
        info = info.next("channels",null).next("channel_info",null);
        String path = CONFIG_PATH+"/songs";
        ParameterLink link = (ParameterLink)info.getLink();
        if(Objects.nonNull(link)) {
            for(ParameterElement parameter : link.getParameters()) {
                if(parameter.getName().equals("local_folder")) {
                    path = parameter.getLiteralValue();
                    break;
                }
            }
        }
        return new File(path);
    }
    
    public Set<ParameterLink> findRedirects() {
        Set<ParameterLink> redirects = new HashSet<>();
        MTScreenInfo info = this;
        while(Objects.nonNull(info.parent)) info = info.parent;
        info = info.next("channels",null).next("redirect",null);
        DataLink link = info.getLink();
        if(link instanceof WrapperLink) {
            for(WrapperElement wrapper : ((WrapperLink)link).getWrappers()) {
                ParameterLink parameter = wrapper.getAsParameter();
                if(Objects.nonNull(parameter) && parameter.getWrapper() instanceof RedirectElement)
                    redirects.add(parameter);
            }
        }
        return redirects;
    }
    
    public Set<TriggerAPI> findRegisteredTriggers() {
        Set<TriggerAPI> triggers = new HashSet<>();
        MTScreenInfo info = this;
        while(Objects.nonNull(info.parent)) info = info.parent;
        info = info.next("channels",null).next("main",null);
        DataLink link = info.getLink();
        if(link instanceof WrapperLink) {
            for(WrapperElement wrapper : ((WrapperLink)link).getWrappers()) {
                ParameterLink parameter = wrapper.getAsParameter();
                if(Objects.nonNull(parameter) && parameter.getWrapper() instanceof TriggerAPI)
                    triggers.add((TriggerAPI)parameter.getWrapper());
            }
        }
        return triggers;
    }
    
    public Set<DataLink> getCache() {
        return this.global ? this.globalCache : this.channelCache.get(this.channel);
    }
    
    public TextAPI<?> getDisplayName() {
        return getDisplayName(this.type);
    }
    
    public TextAPI<?> getDisplayName(String type) {
        return TextHelper.getTranslated(String.format("gui.%1$s.screen.%2$s",MODID,type));
    }
    
    public ResourceLocationAPI<?> getIconTexture(boolean hover) {
        return getIconTexture(this.type,hover);
    }
    
    public ResourceLocationAPI<?> getIconTexture(String type, boolean hover) {
        return MTRef.res(String.format("textures/gui%1$sicon/%2$s.png",hover ? "/hover/" : "/",type));
    }
    
    public DataLink getLink() {
        return this.global ? this.globalLink : this.channelLinks.get(this.channel);
    }
    
    public TextAPI<?> getSpecialDisplayName() {
        if(this.global || Objects.isNull(this.channel)) return getDisplayName();
        DataLink link = this.channelLinks.get(this.channel);
        if(!(link instanceof ParameterLink)) return getDisplayName();
        ParameterWrapper wrapper = ((ParameterLink)link).getWrapper();
        if(wrapper instanceof TriggerAPI) return getSpecialLang("trigger",wrapper.getName());
        return TextHelper.getLiteral(wrapper.getName());
    }
    
    public TextAPI<?> getSpecialLang(String category, String key) {
        return TextHelper.getTranslated(String.format("%1$s.%2$s.%3$s",category,MODID,key));
    }
    
    public boolean is(String type) {
        return this.type.equals(type);
    }
    
    public boolean isGloballyModified() {
        return Objects.nonNull(this.parent) ? this.parent.isModified() : isModified();
    }
    
    public boolean isModifiedOnChannel(ChannelAPI channel) {
        if(this.global) return false;
        if(Objects.nonNull(this.globalLink) && this.globalLink.isModified()) return true;
        if(this.channelLinks.containsKey(channel) && this.channelLinks.get(channel).isModified()) return true;
        if(this.channelCache.containsKey(channel))
            for(DataLink data : this.channelCache.get(channel))
                if(data.getType().isModifiedOnChannel(channel)) return true;
        return false;
    }
    
    public boolean isModified() {
        if(this.global) {
            if(Objects.nonNull(this.globalLink) && this.globalLink.isModified()) return true;
            for(DataLink data : this.globalCache)
                if(data.getType().isModified()) return true;
        } else {
            for(ChannelAPI channel : this.channelCache.keySet()) {
                if(this.channelLinks.containsKey(channel) && this.channelLinks.get(channel).isModified()) return true;
                for(DataLink data : this.channelCache.get(channel))
                    if(data.getType().isModifiedOnChannel(channel)) return true;
            }
        }
        return false;
    }
    
    public MTScreenInfo next(String type, @Nullable DataLink next) {
        if(this.global) {
            for(DataLink data : this.globalCache)
                if(data.getTypeName().equals(type) && (Objects.isNull(next) || data.equals(next)))
                    return data.getType();
            if(Objects.isNull(next)) next = MTGUIScreen.findLink(this,type,null);
            if(Objects.isNull(next)) next = new BasicLink();
            this.globalCache.add(next);
        } else {
            for(DataLink data : this.channelCache.get(this.channel))
                if(data.getTypeName().equals(type) && (Objects.isNull(next) || data.equals(next)))
                    return data.getType();
            if(Objects.isNull(next)) next = MTGUIScreen.findLink(this,type,null);
            if(Objects.isNull(next)) next = new BasicLink();
            this.channelCache.get(this.channel).add(next);
        }
        MTScreenInfo info = new MTScreenInfo(this,type);
        next.setType(info);
        info.setLink(next);
        return info;
    }
    
    public void openAudioSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_audio",() -> {
            SelectionLink link = SelectionLink.dualSingle(wrapper ->
                    ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper));
            for(String file : findLocalAudio())
                link.addElementMaker(element -> file,d -> new SelectionElement(link,e ->
                    d.makeButton(TextHelper.getLiteral(file),b -> e.onLeftClick(screen)),() -> AudioRef.addToGui(
                        screen.getTypeInfo(),file.substring(0,file.lastIndexOf(".")),file,true)));
            for(ParameterLink parameter : findRedirects()) {
                String key = String.valueOf(parameter.getModifiedValue("redirect_key"));
                String value = String.valueOf(parameter.getModifiedValue("redirect_value"));
                link.addOtherElementMaker(element -> key,d -> new SelectionElement(link,e ->
                        d.makeButton(TextHelper.getLiteral(key+" = "+value),b -> e.onLeftClick(screen)),
                        () -> AudioRef.addToGui(screen.getTypeInfo(),key,value,true)));
            }
            return link;
        });
    }
    
    public void openImageSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_images",() -> SelectionLink.singleSingle(
                        wrapper -> ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper)));
    }
    
    protected void openSelectionScreen(MTGUIScreen screen, String nextType, Supplier<SelectionLink> linkSupplier) {
        SelectionLink link = linkSupplier.get();
        link.setType(screen.typeInfo.next(nextType,link));
        MTGUIScreen.open(MTGUIScreen.constructScreen(screen,link.getType(),ClientHelper.getWindow(),ClientHelper.getGuiScale()));
    }
    
    public void openTriggerSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_triggers",() -> {
            SelectionLink link = SelectionLink.singleSingle(wrapper -> ((WrapperLink)screen.typeInfo.getLink())
                                             .addNewWrapper(screen,list,wrappers,wrapper));
            for(String trigger : TriggerRegistry.getTriggerNames()) {
                TextAPI<?> displayName = MTGUIScreen.triggerName(trigger,"not_set",false);
                link.addElementMaker(element -> displayName.toString(),d -> new SelectionElement(link,e ->
                        d.makeButton(displayName,b -> e.onLeftClick(screen)),
                        () -> TriggerRegistry.getTriggerInstance(this.channel,trigger)));
            }
            return link;
        });
    }
    
    public void openTriggerMultiSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_triggers",() -> SelectionLink.dualMulti(selected -> {
                               for(ParameterWrapper wrapper : selected)
                                   ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper);
        }));
    }
    
    public void populateNext(Toml parent, boolean forced) {
        for(DataLink data : this.channelCache.get(this.channel)) {
            if(forced || data.isModified()) {
                if(data instanceof ParameterLink) {
                    Toml toml = Toml.getEmpty();
                    data.populateToml(toml);
                    if(!toml.getEntryValuesAsMap().isEmpty()) parent.addTable(data.getTypeName(),toml);
                } else data.populateToml(parent);
            }
        }
    }
    
    public void setChannel(ChannelAPI channel, boolean force) {
        if(force || Objects.isNull(this.channel)) this.channel = channel;
    }
    
    public void setDisplayName(ParameterLink link, TextAPI<?> name) {
        if(Objects.nonNull(this.parent)) {
            DataLink data = this.parent.getLink();
            if(data instanceof WrapperLink) ((WrapperLink)data).setElementDisplayName(link,name);
        }
    }
    
    public void setLink(@Nullable DataLink link) {
        if(Objects.nonNull(link)) {
            if(this.global && Objects.isNull(this.globalLink)) this.globalLink = link;
            else if(!this.global) this.channelLinks.putIfAbsent(this.channel,link);
        }
    }
    
    @Override public String toString() {
        return this.type;
    }
    
    private void writeChannelToml(Toml info, @Nullable Toml toml, String type) {
        if(Objects.nonNull(toml)) {
            String path = CONFIG_PATH+"/"+this.channel.getName()+"/"+info.getValueString(type);
            MTRef.logInfo("{} is being written to",path);
            MTDataRef.writeToFile(toml,path);
        }
    }
    
    private void writeChannelTxt(Toml info, List<String> lines, String type) {
        if(Objects.nonNull(lines)) {
            String path = CONFIG_PATH+"/"+this.channel.getName()+"/"+info.getValueString(type);
            MTRef.logInfo("{} is being written to",path);
            FileHelper.writeLines(path+".txt",lines,false);
        }
    }
}