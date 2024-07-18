package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
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
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerRegistry;
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

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
    private final Map<ChannelAPI,Map<String,MTScreenInfo>> channelCache;
    private final Map<ChannelAPI,DataLink> channelLinks;
    private final Map<String,MTScreenInfo> globalCache;
    private final boolean global;
    private DataLink globalLink;
    @Getter private ChannelAPI channel;
    @Setter private Button applyButton;
    
    public MTScreenInfo(@Nullable MTScreenInfo parent, String type) {
        this.parent = parent;
        this.type = type;
        this.global = Misc.equalsAny(type,"debug","from","home","log","playback","to","toggles","toggle");
        this.globalCache = new HashMap<>();
        this.channelCache = new HashMap<>();
        this.channelLinks = new HashMap<>();
        if(Objects.nonNull(this.parent)) {
            this.channel = parent.channel;
            for(ChannelAPI channel : parent.channelCache.keySet())
                this.channelCache.put(channel,new HashMap<>());
            if(parent.global) parent.globalCache.put(type,this);
            else parent.channelCache.get(parent.channel).put(type,this);
        } else {
            for(ChannelAPI channel : ChannelHelper.getClientHelper().getChannels().values()) {
                if(channel.isClientChannel() && !Misc.equalsAny(channel.getName(),"jukebox","preview")) {
                    this.channelCache.put(channel, new HashMap<>());
                    if(Objects.isNull(this.channel)) this.channel = channel;
                }
            }
        }
    }
    
    @SuppressWarnings("DataFlowIssue")
    public void applyChanges() {
        if(Objects.nonNull(this.parent)) {
            this.parent.applyChanges();
            return;
        }
        if(!isModified()) return;
        Toml global = Toml.getEmpty();
        Toml debugTable = Toml.getEmpty();
        MTScreenInfo debug = next("debug");
        if(Objects.isNull(debug.globalLink)) debug.globalLink = MTGUIScreen.findLink(debug);
        debug.globalLink.populateToml(debugTable);
        global.addTable("debug",debugTable);
        Toml channels = Toml.getEmpty();
        MTScreenInfo info = next("channels").next("channel_info");
        for(ChannelAPI channel : this.channelCache.keySet()) {
            Toml channelToml = Toml.getEmpty();
            info.channel = channel;
            if(!info.channelLinks.containsKey(channel)) info.setLink(channel.getInfo().getLink(info));
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
        Map<String,MTScreenInfo> infoMap = info.channelCache.get(info.channel);
        info.writeChannelToml(toml,buildChannelFile(info.channel,infoMap.get("commands")),"commands");
        info.writeChannelToml(toml,buildChannelFile(info.channel,infoMap.get("main")),"main");
        info.writeChannelToml(toml,buildChannelFile(info.channel,infoMap.get("renders")),"renders");
        info.writeChannelTxt(toml,buildRecords(info.channel,infoMap.get("jukebox")),"jukebox");
        info.writeChannelTxt(toml,buildRedirect(info.channel,infoMap.get("redirect")),"redirect");
    }
    
    private @Nullable Toml buildChannelFile(ChannelAPI channel, @Nullable MTScreenInfo info) {
        if(Objects.isNull(info) || !info.isModifiedOnChannel(channel)) return null;
        if(Objects.nonNull(info.getLink())) {
            Toml toml = Toml.getEmpty();
            info.getLink().populateToml(toml);
            return toml;
        }
        return null;
    }
    
    private @Nullable List<String> buildRecords(ChannelAPI channel, @Nullable MTScreenInfo info) {
        if(Objects.isNull(info) || !info.isModifiedOnChannel(channel)) return null;
        if(info.getLink() instanceof WrapperLink) {
            List<String> lines = new ArrayList<>();
            for(WrapperElement element : ((WrapperLink)info.getLink()).getWrappers()) {
                ParameterLink link = element.getAsParameter();
                if(Objects.nonNull(link) && link.getWrapper() instanceof RecordElement) {
                    RecordElement redirect = (RecordElement)link.getWrapper();
                    lines.add(redirect.getKey()+" = "+redirect.getValue());
                }
            }
            return lines;
        }
        return null;
    }
    
    private @Nullable List<String> buildRedirect(ChannelAPI channel, @Nullable MTScreenInfo info) {
        if(Objects.isNull(info) || !info.isModifiedOnChannel(channel)) return null;
        if(info.getLink() instanceof WrapperLink) {
            List<String> lines = new ArrayList<>();
            for(WrapperElement element : ((WrapperLink)info.getLink()).getWrappers()) {
                ParameterLink link = element.getAsParameter();
                if(Objects.nonNull(link) && link.getWrapper() instanceof RedirectElement) {
                    RedirectElement redirect = (RedirectElement)link.getWrapper();
                    String separator = redirect.isRemote() ? " = " : " == ";
                    lines.add(redirect.getKey()+separator+redirect.getValue());
                }
            }
            return lines;
        }
        return null;
    }
    
    private void buildTogglesFile(String path) {
        MTScreenInfo toggles = next("toggles");
        if(toggles.isModified()) {
            Toml toml = Toml.getEmpty();
            toggles.getLink().populateToml(toml);
            MTDataRef.writeToFile(toml,path);
        }
    }
    
    public void enableApplyButton() {
        if(Objects.nonNull(this.applyButton)) this.applyButton.setVisible(true);
        if(Objects.nonNull(this.parent)) this.parent.enableApplyButton();
    }
    
    public @Nullable DataLink findChannelLink() {
        if(Objects.isNull(this.channel)) return null;
        switch(this.type) {
            case "channel_info": return this.channel.getInfo().getLink(this);
            case "commands": return this.channel.getCommandsLink(this);
            case "jukebox": return this.channel.getRecordsLink(this);
            case "main": return this.channel.getMainLink(this);
            case "redirect": return this.channel.getRedirectLink(this);
            case "renders": return this.channel.getRendersLink(this);
            default: return null;
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
        info = info.next("channels").next("channel_info");
        info.setLink(info.findChannelLink());
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
    
    public Set<RedirectElement> findRedirects() {
        Set<RedirectElement> redirects = new HashSet<>();
        MTScreenInfo info = this;
        while(Objects.nonNull(info.parent)) info = info.parent;
        info = info.next("channels").next("redirect");
        info.setLink(info.findChannelLink());
        DataLink link = info.getLink();
        if(link instanceof WrapperLink) {
            for(WrapperElement wrapper : ((WrapperLink)link).getWrappers()) {
                ParameterLink parameter = wrapper.getAsParameter();
                if(Objects.nonNull(parameter) && parameter.getWrapper() instanceof RedirectElement)
                    redirects.add((RedirectElement)parameter.getWrapper());
            }
        }
        return redirects;
    }
    
    public Set<TriggerAPI> findRegisteredTriggers() {
        Set<TriggerAPI> triggers = new HashSet<>();
        MTScreenInfo info = this;
        while(Objects.nonNull(info.parent)) info = info.parent;
        info = info.next("channels").next("main");
        info.setLink(info.findChannelLink());
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
            for(MTScreenInfo info : this.channelCache.get(channel).values())
                if(info.isModifiedOnChannel(channel)) return true;
        return false;
    }
    
    public boolean isModified() {
        if(this.global) {
            if(Objects.nonNull(this.globalLink) && this.globalLink.isModified()) return true;
            for(MTScreenInfo info : this.globalCache.values())
                if(info.isModified()) return true;
        } else {
            for(ChannelAPI channel : this.channelCache.keySet()) {
                if(this.channelLinks.containsKey(channel) && this.channelLinks.get(channel).isModified()) return true;
                for(MTScreenInfo info : this.channelCache.get(channel).values())
                    if(info.isModified()) return true;
            }
        }
        return false;
    }
    
    public MTScreenInfo next(String type) {
        return this.global ? (this.globalCache.containsKey(type) ? this.globalCache.get(type) : get(this,type)) :
                (this.channelCache.get(this.channel).containsKey(type) ?
                        this.channelCache.get(this.channel).get(type) : get(this,type));
    }
    
    public void openAudioSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_audio", next -> {
            SelectionLink link = SelectionLink.dualSingle(next,wrapper ->
                    ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper));
            for(String file : findLocalAudio())
                link.addElementMaker(element -> file,d -> new SelectionElement(link,e ->
                    d.makeButton(TextHelper.getLiteral(file),b -> e.onLeftClick(screen)),() -> AudioRef.addToGui(
                        screen.getTypeInfo(),file.substring(0,file.lastIndexOf(".")),file,true)));
            for(RedirectElement redirect : findRedirects()) {
                String key = redirect.getKey();
                String value = redirect.getValue();
                link.addOtherElementMaker(element -> key,d -> new SelectionElement(link,e ->
                        d.makeButton(TextHelper.getLiteral(key+" = "+value),b -> e.onLeftClick(screen)),
                        () -> AudioRef.addToGui(screen.getTypeInfo(),key,value,true)));
            }
            return link;
        });
    }
    
    public void openImageSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_images",next -> SelectionLink.singleSingle(next,
                        wrapper -> ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper)));
    }
    
    protected void openSelectionScreen(MTGUIScreen screen, String nextType,
            Function<MTScreenInfo,SelectionLink> linkMaker) {
        MTScreenInfo type = screen.typeInfo;
        MTScreenInfo next = type.next(nextType);
        next.setLink(linkMaker.apply(next));
        MTGUIScreen.open(MTGUIScreen.constructScreen(screen,next,ClientHelper.getWindow(),ClientHelper.getGuiScale()));
    }
    
    public void openTriggerSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_triggers", next -> {
            SelectionLink link = SelectionLink.singleSingle(next,wrapper -> ((WrapperLink)screen.typeInfo.getLink())
                                             .addNewWrapper(screen,list,wrappers,wrapper));
            for(String trigger : TriggerRegistry.getTriggerNames()) {
                TextAPI<?> displayName = MTGUIScreen.triggerName(trigger,"not_set");
                link.addElementMaker(element -> displayName.toString(),d -> new SelectionElement(link,e ->
                        d.makeButton(displayName,b -> e.onLeftClick(screen)),
                        () -> TriggerRegistry.getTriggerInstance(this.channel,trigger)));
            }
            return link;
        });
    }
    
    public void openTriggerMultiSelectionScreen(MTGUIScreen screen, DataList list, Collection<WrapperElement> wrappers) {
        openSelectionScreen(screen,"potential_triggers",next -> SelectionLink.dualMulti(next,
                           selected -> {
                               for(ParameterWrapper wrapper : selected)
                                   ((WrapperLink)screen.typeInfo.getLink()).addNewWrapper(screen,list,wrappers,wrapper);
        }));
    }
    
    public void populateNext(Toml parent, boolean forced) {
        for(Entry<String,MTScreenInfo> entry : this.channelCache.get(this.channel).entrySet()) {
            MTScreenInfo next = entry.getValue();
            if(forced || next.isModified()) {
                DataLink link = next.getLink();
                if(link instanceof ParameterLink) {
                    Toml toml = Toml.getEmpty();
                    link.populateToml(toml);
                    parent.addTable(entry.getKey(),toml);
                } else if(Objects.nonNull(link)) link.populateToml(parent);
            }
        }
    }
    
    public void setChannel(ChannelAPI channel, boolean force) {
        if(force || Objects.isNull(this.channel)) this.channel = channel;
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