package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataList;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.HelpLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.HelpLink.HelpElement;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink.ParameterElement;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;
import mods.thecomputerizer.theimpossiblelibrary.api.shapes.vectors.Vector3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ParameterScreen extends MTGUIScreen {
    
    private Widget activeWidget;
    private Widget activeDropDown;
    
    public ParameterScreen(ScreenAPI parent, MTScreenInfo info, MinecraftWindow window, int guiScale) {
        super(parent,info,window,guiScale);
        addTextBackground(v -> {
            DataList list = new DataList(0.5d, 0d, 1d, 1.8d, 0.05d);
            DataLink typeLink = this.typeInfo.getLink();
            if(typeLink instanceof ParameterLink) addParameterLink(list,(ParameterLink)typeLink);
            else if(typeLink instanceof HelpLink) addHelpLink(list,(HelpLink)typeLink);
            addWidget(list);
            autoAddTypeTexture(-list.getScrollBar().getWidth());
        });
    }
    
    private void addHelpLink(DataList list, HelpLink link) {
        List<HelpElement> elements = new ArrayList<>(link.getElements());
        elements.sort(Comparator.comparing(e -> e.getDisplayName().toString()));
        for(HelpElement parameter : elements) {
            list.addButton(parameter.getDisplayName(),b -> {
                if(Objects.nonNull(this.activeWidget)) this.activeWidget.setVisible(false);
                this.activeWidget = parameter.toWidget(this);
                this.activeWidget.setVisible(true);
            },parameter.getHover());
        }
    }
    
    private void addParameterLink(DataList list, ParameterLink link) {
        link.addChildren(this,list);
        List<ParameterElement> parameters = new ArrayList<>(link.getParameters());
        parameters.sort(Comparator.comparing(e -> e.getDisplayName().toString()));
        for(ParameterElement parameter : parameters) {
            list.addButton(parameter.getDisplayName(),b -> {
                if(Objects.nonNull(this.activeWidget)) this.activeWidget.setVisible(false);
                this.activeWidget = parameter.toWidget(this);
                this.activeWidget.setVisible(true);
            },parameter.getHover());
        }
    }
    
    @Override public float defaultBackgroundDarkness() {
        return 0.6f;
    }
    
    @Override public void draw(RenderContext ctx, Vector3 center, double mouseX, double mouseY) {
        RenderAPI renderer = ctx.getRenderer();
        renderer.translate(0d,0d,-200d);
        if(Objects.nonNull(this.parentScreen))
            this.parentScreen.draw(ctx,center,mouseX+9999d,mouseY+9999d); //Add a large offset to prevent hover behavior
        renderer.translate(0d,0d,200d);
        super.draw(ctx,center,mouseX,mouseY);
    }
    
    private WidgetList potentialValues(Button template, ParameterRef<?> ref) {
        List<Widget> potentials = new ArrayList<>();
        for(Object potentialValue : ref.getPotentialValues()) {
            Button potential = template.copy();
            potential.setText(String.valueOf(potentialValue));
            potential.setClickFunc(b -> saveActiveEntryAs(potentialValue));
            potentials.add(potential);
        }
        return WidgetHelper.dropDownFrom(template,-0.9d,potentials);
    }
    
    public void saveActiveEntryAs(Object value) {
    
    }
    
    public enum ParameterConstraints {
        CHARACTER_BLACKLIST,
        DECIMAL,
        INTEGER,
        NUMBER_RANGE,
        RELOAD_REQUIRED,
        RESTART_REQUIRED
    }
    
    public enum ParameterType {
        BOOLEAN,
        DROPDOWN,
        LIST,
        NUMBER,
        STRING
    }
}