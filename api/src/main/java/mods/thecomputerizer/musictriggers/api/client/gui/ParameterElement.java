package mods.thecomputerizer.musictriggers.api.client.gui;

import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;

import javax.annotation.Nullable;
import java.util.Objects;

public interface ParameterElement {
    
    default void save(@Nullable Widget parent, Object value) {
        ParameterScreen parameters = null;
        while(Objects.nonNull(parent)) {
            if(parent instanceof ParameterScreen) {
                parameters = (ParameterScreen)parent;
                break;
            }
            parent = parent.getParent();
        }
        if(Objects.nonNull(parameters)) parameters.saveActiveEntryAs(value);
    }
}