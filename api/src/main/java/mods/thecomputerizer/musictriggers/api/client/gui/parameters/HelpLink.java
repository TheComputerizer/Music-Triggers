package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.client.gui.MTGUIScreen;
import mods.thecomputerizer.musictriggers.api.client.gui.ParameterScreen;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.BasicWidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetGroup;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.TextHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;
import static mods.thecomputerizer.theimpossiblelibrary.api.client.render.TextBuffer.Alignment.TOP_CENTER;

@Getter
public class HelpLink extends DataLink {
    
    static final Set<String> TYPES = Collections.unmodifiableSet(new HashSet<>(buildTypes()));
    static HelpLink instance;
    
    static List<String> buildTypes() {
        return Arrays.asList("alpha_warning","channel_interaction","channels","identifiers","removing_triggers");
    }
    
    public static HelpLink getInstance() {
        if(Objects.isNull(instance)) instance = new HelpLink();
        return instance;
    }
    
    static String typeKeyDesc(String type) {
        return "parameter."+MODID+".help."+type+".desc";
    }
    
    static String typeKeyName(String type) {
        return "parameter."+MODID+".help."+type+".name";
    }
    
    private final Set<HelpElement> elements;
    
    private HelpLink() {
        super(false);
        this.elements = TYPES.stream().map(HelpElement::new).collect(Collectors.toSet());
    }
    
    @Override public TextAPI<?> getDescription() {
        return Objects.nonNull(this.type) ? MTGUIScreen.selectionDesc(this.type.getType()) : null;
    }
    
    @Override public TextAPI<?> getDisplayName() {
        return Objects.nonNull(this.type) ? MTGUIScreen.selectionName(this.type.getType()) : null;
    }
    
    @Override public void populateToml(Toml toml) {}
    
    public static final class HelpElement {
        
        @Getter final String name;
        Widget widget;
        
        HelpElement(String name) {
            this.name = name;
        }
        
        public TextAPI<?> getDescription() {
            return TextHelper.getTranslated(typeKeyDesc(this.name));
        }
        
        public TextAPI<?> getDisplayName() {
            return TextHelper.getTranslated(typeKeyName(this.name));
        }
        
        public TextAPI<?> getHover() {
            return TextHelper.getLiteral(this.name);
        }
        
        public Widget toWidget(ParameterScreen screen) {
            if(Objects.isNull(this.widget)) {
                WidgetGroup group = BasicWidgetGroup.from(-0.5d,0d,0.95d,2d);
                group.addWidget(TextWidget.from(getDisplayName(),0d,0.75d));
                group.addWidget(ShapeWidget.outlineFrom(0.95d,0.25d,0d,0.75d));
                DataList list = new DataList(0d,-0.125d,0.95d,1.5d,0.05d);
                list.addWidget(TextWidget.from(TextBuffer.getBuilder(getDescription()).setAlignment(TOP_CENTER).build()));
                group.addWidget(list);
                group.addWidget(ShapeWidget.outlineFrom(0.95d,1.5d,0d,-0.125d));
                this.widget = group;
            }
            if(!screen.getWidgets().contains(this.widget)) screen.addWidget(this.widget);
            return this.widget;
        }
    }
}