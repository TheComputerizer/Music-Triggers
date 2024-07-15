package mods.thecomputerizer.musictriggers.api.client.gui;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.MTClientEvents;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Misc;

import javax.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    
    private final MTScreenInfo parent;
    @Getter private final String type;
    private final Map<ChannelAPI,Map<String,MTScreenInfo>> channelCache;
    private final Map<ChannelAPI,DataLink> channelLinks;
    private final Map<String,MTScreenInfo> globalCache;
    private final boolean global;
    private DataLink globalLink;
    private ChannelAPI channel;
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
        info.channel.logInfo("Beginning build from GUI}");
        info.channel.logInfo("Info is {} and is global? {}",info,info.global);
        Map<String,MTScreenInfo> infoMap = info.channelCache.get(info.channel);
        info.writeChannelFile(toml,buildChannelFile(info.channel,infoMap.get("commands")),"commands");
        info.writeChannelFile(toml,buildChannelFile(info.channel,infoMap.get("main")),"main");
        info.writeChannelFile(toml,buildChannelFile(info.channel,infoMap.get("renders")),"renders");
    }
    
    private @Nullable Toml buildChannelFile(ChannelAPI channel, @Nullable MTScreenInfo info) {
        channel.logInfo("Building toml from GUI data for {}",info);
        if(Objects.isNull(info) || !info.isModifiedOnChannel(channel)) return null;
        channel.logInfo("Verfied modification have been made");
        if(Objects.nonNull(info.getLink())) {
            channel.logInfo("Populating toml data");
            Toml toml = Toml.getEmpty();
            info.getLink().populateToml(toml);
            return toml;
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
        channel.logInfo("Checking channel specific modification for {}",this.type);
        if(this.global) return false;
        if(Objects.nonNull(this.globalLink) && this.globalLink.isModified()) return true;
        if(this.channelLinks.containsKey(channel) && this.channelLinks.get(channel).isModified()) return true;
        channel.logInfo("Channel link isnt modified");
        if(this.channelCache.containsKey(channel)) {
            channel.logInfo("Cache has the channel");
            for(MTScreenInfo info : this.channelCache.get(channel).values()) {
                channel.logInfo("Checking child info");
                if(info.isModifiedOnChannel(channel)) return true;
            }
        }
        channel.logInfo("BAD!!!!!!!!!");
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
    
    private void writeChannelFile(Toml info, @Nullable Toml toml, String type) {
        MTRef.logInfo("Potentially writing file type {}",type);
        if(Objects.nonNull(toml)) {
            MTRef.logInfo("{} is being written to",CONFIG_PATH+"/"+this.channel.getName()+"/"+info.getValueString(type));
            MTDataRef.writeToFile(toml,CONFIG_PATH+"/"+this.channel.getName()+"/"+info.getValueString(type));
        }
    }
}
