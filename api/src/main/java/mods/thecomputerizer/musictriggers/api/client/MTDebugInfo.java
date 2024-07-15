package mods.thecomputerizer.musictriggers.api.client;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.TableRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.musictriggers.api.data.global.Debug;
import mods.thecomputerizer.musictriggers.api.data.global.GlobalElement;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.font.FontAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.font.FontHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.biome.BiomeAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.effect.EffectAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.effect.EffectInstanceAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextTranslationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.DimensionAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.WorldAPI;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

import static mods.thecomputerizer.musictriggers.api.client.MTDebugInfo.ElementType.*;

public class MTDebugInfo extends GlobalElement {

    private static final double MAX_WIDTH_PERCENT = 2d/3d;
    private static final Comparator<Element> elementSorter = Collections.reverseOrder(Comparator.comparingInt(Element::getPriority));

    private final List<Element> elements;
    private final List<Element> visibleElements;
    @Setter private ChannelHelper helper;
    private int maxWidth;

    public MTDebugInfo(ChannelHelper helper) {
        super("Debug_Info");
        this.helper = helper;
        this.elements = new ArrayList<>();
        this.visibleElements = new ArrayList<>();
        addDefaultElements();
    }
    
    public void initChannelElements() {
        int priority = 10000;
        for(Entry<String,ChannelAPI> entry : this.helper.getChannels().entrySet()) {
            String name = entry.getKey();
            ChannelAPI channel = entry.getValue();
            addElement(CHANNEL,"song",true,priority)
                    .setVisibility(helper -> channel.showDebugSongInfo())
                    .setArgSetter(helper -> {
                        String song = channel.getPlayingSongName();
                        String time = channel.getFormattedSongTime();
                        if(Objects.isNull(song)) song = "?";
                        if(Objects.isNull(time)) time = "?";
                        return new Object[]{name,song,time};
                    });
            priority--;
            addElement(CHANNEL,"trigger",true, priority)
                    .setVisibility(helper -> channel.showDebugTriggerInfo())
                    .setArgSetter(helper -> {
                        String active = null;
                        String playable = null;
                        TriggerAPI trigger = channel.getActiveTrigger();
                        if(Objects.nonNull(trigger)) active = trigger.toString();
                        Collection<TriggerAPI> triggers = channel.getPlayableTriggers();
                        if(!triggers.isEmpty()) playable = triggers.toString();
                        if(Objects.isNull(active)) active = "?";
                        if(Objects.isNull(playable)) playable = "?";
                        return new Object[]{name, active, playable};
                    });
            priority--;
        }
    }
    
    private void addDefaultElements() {
        addElement(HEADER,"").setVisibility(helper -> true);
        addElement(POSITION,"dimension",true,1003)
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_position_info"))
                .setArgSetter(helper -> {
                    PlayerAPI<?,?> player = helper.getPlayer();
                    if(Objects.nonNull(player)) {
                        DimensionAPI<?> dimension = player.getDimension();
                        if(Objects.nonNull(dimension))
                            return new Object[]{dimension.getName(),dimension.getRegistryName()};
                    }
                    return new Object[]{"?","?"};
                });
        addElement(POSITION,"structure",true,1002)
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_position_info"))
                .setArgSetter(helper -> new Object[]{"?","?"});
        addElement(POSITION,"biome",true,1001)
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_position_info"))
                .setArgSetter(helper -> {
                    PlayerAPI<?,?> player = helper.getPlayer();
                    if(Objects.nonNull(player)) {
                        BiomeAPI<?> biome = player.getWorld().getBiomeAt(player.getPosRounded());
                        if(Objects.nonNull(biome))
                            return new Object[]{"?",biome.getRegistryName(),biome.getTagNames(player.getWorld())};
                    }
                    return new Object[]{"?","?","?"};
                });
        addElement(POSITION,"light")
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_position_info"))
                .setArgSetter(helper -> {
                    PlayerAPI<?,?> player = helper.getPlayer();
                    if(Objects.nonNull(player)) {
                        WorldAPI<?> world = player.getWorld();
                        if(Objects.nonNull(world)) {
                            BlockPosAPI<?> pos = player.getPosRounded();
                            return new Object[]{world.getLightBlock(pos),world.getLightSky(pos),world.getLightTotal(pos)};
                        }
                    }
                    return new Object[]{"?","?","?"};
                });
        addElement(STATUS, "effects")
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_status_info"))
                .setArgSetter(helper -> {
                    PlayerAPI<?,?> player = helper.getPlayer();
                    if(Objects.nonNull(player)) {
                        StringJoiner joiner = new StringJoiner(", ");
                        for(EffectInstanceAPI<?> instance : player.getActiveEffects()) {
                            EffectAPI<?> effect = instance.getEffect();
                            TextAPI<?> text = getTranslated("status","effect","?",effect.getRegistryName());
                            if(Objects.nonNull(text)) joiner.add(text.getApplied());
                        }
                        return new Object[]{joiner.toString()};
                    }
                    return new Object[]{"?"};
                });
        addElement(TARGET,"block_entity")
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_target_info"))
                .setArgSetter(helper -> new Object[]{"?","?"});
        addElement(TARGET,"entity")
                .setVisibility(helper -> ChannelHelper.getDebugBool("show_target_info"))
                .setArgSetter(helper -> new Object[]{"?","?"});
        addElement(OTHER,"blocked.mods",true,5000)
                .setVisibility(helper -> true)
                .setArgSetter(helper -> {
                    Debug debug = ChannelHelper.getDebug();
                    if(Objects.isNull(debug)) return new Object[]{"Unknown"};
                    StringJoiner joiner = new StringJoiner(", ");
                    for(Entry<String,List<String>> mod : ChannelHelper.getDebug().getFormattedBlockedMods().entrySet()) {
                        TextAPI<?> text = getTranslated("other","blocked.mod",mod.getKey(),mod.getValue());
                        if(Objects.nonNull(text)) joiner.add(text.getApplied());
                    }
                    return new Object[]{joiner.toString()};
                });
    }
    
    public Element addElement(ElementType type, String text) {
        return addElement(type,text,true,0);
    }
    
    public Element addElement(ElementType type, String text, boolean isTranlation, int priority) {
        Element element = new Element(type,text,isTranlation,priority);
        this.elements.add(element);
        return element;
    }

    public void compute() {
        this.visibleElements.clear();
        for(Element element : this.elements) {
            if(element.computeVisibility(this.helper).visible) {
                element.computeArgs(this.helper);
                this.visibleElements.add(element);
            }
        }
    }
    
    @Override public TableRef getReferenceData() {
        return null;
    }
    
    public TextTranslationAPI<?> getTranslated(String type, String key, Object ... args) {
        String built = "debug."+MTRef.MODID+"."+type;
        if(StringUtils.isNotEmpty(key)) built+=("."+key);
        return TextHelper.getTranslated(built,args);
    }

    public void setWidth(int width) {
        this.maxWidth = width<=1 ? 0 : (int)((double)width*MAX_WIDTH_PERCENT);
    }

    public void toLines(FontAPI font, Collection<String> lines) {
        compute();
        if(this.visibleElements.isEmpty() || this.maxWidth<=0) return;
        this.visibleElements.sort(elementSorter);
        for(Element element : this.visibleElements) element.toLines(font,this.maxWidth,lines);
    }

    public void toLines(FontAPI font, int width, Collection<String> lines) {
        setWidth(width);
        toLines(font,lines);
    }
    
    public static class Element {

        private final ElementType type;
        private final String text;
        private final boolean translation;
        @Getter private final int priority;
        private Function<ChannelHelper,Boolean> visibility;
        @Setter private Function<ChannelHelper,Object[]> argSetter;
        @Getter private boolean visible;
        @Getter private Object[] args;

        private Element(ElementType type, String text, boolean isTranlation, int priority) {
            this.type = type;
            this.text = text;
            this.translation = isTranlation;
            this.priority = priority!=0 ? priority : type.defaultPriority;
        }
        
        void computeArgs(ChannelHelper helper) {
            this.args = Objects.nonNull(this.argSetter) ? this.argSetter.apply(helper) : null;
        }
        
        Element computeVisibility(ChannelHelper helper) {
            this.visible = Objects.isNull(this.visibility) || this.visibility.apply(helper);
            return this;
        }

        public @Nullable String getNotBlankLine() {
            TextAPI<?> line = this.translation ?
                    getTranslated(this.text,Objects.nonNull(this.args) ? this.args : new Object[]{}) :
                    TextHelper.getLiteral(this.text);
            if(Objects.isNull(line)) return null;
            String applied = line.getApplied();
            return StringUtils.isNotBlank(applied) ? applied : null;
        }
        
        private TextTranslationAPI<?> getTranslated(String key, Object ... args) {
            String built = "debug."+MTRef.MODID+"."+this.type.getId();
            if(StringUtils.isNotEmpty(key)) built+=("."+key);
            return TextHelper.getTranslated(built,args);
        }
        
        private Element setVisibility(Function<ChannelHelper,Boolean> visibility) {
            this.visibility = visibility;
            return this;
        }

        public void toLines(FontAPI font, int maxWidth, Collection<String> lines) {
            if(Objects.isNull(font)) return;
            String applied = getNotBlankLine();
            if(Objects.nonNull(applied)) lines.addAll(FontHelper.splitLines(font,applied,maxWidth));
        }
    }

    @Getter
    public enum ElementType {
        HEADER(Integer.MAX_VALUE),
        CHANNEL(10000),
        POSITION(1000),
        STATUS(100),
        TARGET(10),
        OTHER(0);
        
        final String id;
        final int defaultPriority;
        
        ElementType(int priority) {
            this.id = name().toLowerCase();
            this.defaultPriority = priority;
        }
    }
}