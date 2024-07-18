package mods.thecomputerizer.musictriggers.api.client.gui.parameters;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Button;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.ShapeWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.TextWidget;
import mods.thecomputerizer.theimpossiblelibrary.api.client.gui.widget.Widget;

import javax.annotation.Nullable;

@Getter @Setter
public class SpecialButton extends Button {
    
    private boolean deleting;
    
    public SpecialButton(@Nullable ShapeWidget shape, @Nullable TextWidget text, @Nullable Widget hover) {
        super(shape,text,hover);
    }
}
