package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.DataList;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink;
import mods.thecomputerizer.musictriggers.api.client.gui.parameters.ParameterLink.ParameterElement;
import mods.thecomputerizer.musictriggers.api.data.MTDataRef.ParameterRef;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.MinecraftWindow;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.ScreenAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.WidgetList;
import mods.thecomputerizer.theimpossiblelibrary.api.client.render.RenderContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ParameterScreen extends MTGUIScreen {
    
    private ParameterElement activeParameter;
    private Widget activeWidget;
    private Widget activeDropDown;
    
    public ParameterScreen(ScreenAPI parent, MTScreenInfo info, MinecraftWindow window, int guiScale) {
        super(parent,info,window,guiScale);
        addTextBackground(0d,0d,2d,1.8d,v -> {
            DataList list = new DataList(0.5d, 0d, 1d, 1.8d, 0.05d);
            DataLink link = this.typeInfo.getLink();
            if(link instanceof ParameterLink) {
                for(ParameterElement parameter : ((ParameterLink)link).getParameters()) {
                    list.addButton(parameter.getDisplayName(),b -> {
                        this.activeParameter = parameter;
                        if(Objects.nonNull(this.activeWidget)) this.activeWidget.setVisible(false);
                        this.activeWidget = parameter.toWidget(this);
                        this.activeWidget.setVisible(true);
                    });
                }
            }
            addWidget(list);
            addTypeTexture(-list.getScrollBar().getWidth(),0d);
        });
    }
    
    public float defaultBackgroundDarkness() {
        return 0.6f;
    }
    
    public void draw(RenderContext ctx, Vector3d center, double mouseX, double mouseY) {
        if(Objects.nonNull(this.parentScreen))
            this.parentScreen.draw(ctx,center,mouseX+9999d,mouseY+9999d); //Add a large offset to prevent hover behavior
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
    
    private void setActiveParameter(ParameterElement parameter) {
        this.activeParameter = parameter;
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