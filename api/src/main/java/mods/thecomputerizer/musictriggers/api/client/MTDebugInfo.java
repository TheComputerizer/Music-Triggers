package mods.thecomputerizer.musictriggers.api.client;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.font.FontAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.font.FontHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextTranslationAPI;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

public class MTDebugInfo { //TODO finish this

    private static final double MAX_WIDTH_PERCENT = 0.5d;
    private static Element header;

    private final List<Element> elements;
    @Setter private ChannelHelper helper;
    private int maxWidth;

    public MTDebugInfo(ChannelHelper helper) {
        this.elements = new ArrayList<>();
        this.helper = helper;
        if(Objects.isNull(header)) header = new Element(ElementType.HEADER,getTranslated("header"));
    }

    public void compute() {
        this.elements.clear();
        this.elements.add(header);
        if(Objects.nonNull(this.helper) && this.helper.isClient())
            this.helper.addDebugElements(this,this.elements);
    }

    public TextTranslationAPI<?> getTranslated(String category, Object ... args) {
        return getTranslated(category,null,args);
    }

    public TextTranslationAPI<?> getTranslated(String category, @Nullable String extra, Object ... args) {
        String key = "debug."+MTRef.MODID+"."+category;
        if(Objects.nonNull(extra)) key = key+"."+extra;
        return TextHelper.getTranslated(key,args);
    }

    public void setWidth(int width) {
        this.maxWidth = width<=1 ? 0 : (int)((double)width*MAX_WIDTH_PERCENT);
    }

    public void toLines(FontAPI font, Collection<String> lines) {
        compute();
        if(this.elements.isEmpty() || this.maxWidth>=0) return;
        this.elements.sort(Comparator.comparingInt(Element::getPriority));
        this.elements.forEach(element -> element.toLines(font,this.maxWidth,lines));
    }

    public void toLines(FontAPI font, int width, Collection<String> lines) {
        setWidth(width);
        toLines(font,lines);
    }

    public static class Element {

        private final ElementType type;
        private final TextAPI<?> line;
        private final List<Element> children;
        @Getter private final int priority;
        private final int level;

        private Element(ElementType type, TextAPI<?> line) {
            this(type,line,0,0);
        }

        private Element(ElementType type, TextAPI<?> line, int priority) {
            this(type,line,priority,0);
        }

        private Element(ElementType type, TextAPI<?> line, int priority, int level) {
            this.type = type;
            this.line = line;
            this.children = new ArrayList<>();
            this.priority = priority;
            this.level = level;
        }

        public @Nullable String getNotBlankLine() {
            if(Objects.isNull(this.line)) return null;
            String applied = this.line.getApplied();
            return StringUtils.isNotBlank(applied) ? applied : null;
        }

        public void toLines(FontAPI font, int maxWidth, Collection<String> lines) {
            if(Objects.isNull(font)) return;
            String applied = getNotBlankLine();
            if(Objects.nonNull(applied)) lines.addAll(FontHelper.splitLines(font,applied,maxWidth));
            for(Element child : this.children) child.toLines(font,maxWidth,lines);
        }
    }

    public enum ElementType { HEADER, CHANNEL, POSITION, TARGET, OTHER }
}