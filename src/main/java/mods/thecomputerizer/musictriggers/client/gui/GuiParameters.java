package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiParameters extends GuiSuperType {

    private final List<Parameter> parameters;
    private final String id;
    private final String title;
    private int longestParameterName;
    private int rightSide;
    private int scrollPos;
    private boolean canScrollDown;


    public GuiParameters(GuiSuperType parent, GuiType type, Instance configInstance, String guiID, String guiTitle,
                         List<Parameter> parameters) {
        super(parent, type, configInstance);
        this.id = guiID;
        this.title = guiTitle;
        this.parameters = parameters;
        this.scrollPos = 0;
    }

    private int calculateLongestParameter(FontRenderer fontRenderer) {
        int ret = fontRenderer.getStringWidth(this.title)*2;
        for(Parameter parameter : this.parameters) {
            int temp = parameter.displayNameLength(fontRenderer);
            if(temp>ret) ret = temp;
        }
        this.rightSide = this.width-(ret+(this.spacing*3));
        return ret;
    }

    @Override
    public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height) {
        super.setWorldAndResolution(mc, width, height);
        this.longestParameterName = calculateLongestParameter(mc.fontRenderer);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if(scroll!=0) {
            if(scroll<1) {
                if(this.canScrollDown) {
                    this.scrollPos++;
                    this.canScrollDown = false;
                }
            } else if(this.scrollPos>0) this.scrollPos--;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (Parameter parameter : this.parameters)
                parameter.onClick(mouseX>this.rightSide && mouseX<=(this.width-this.spacing));
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        super.keyTyped(c, key);
        if(!blacklistedKeys.contains(key))
            for (Parameter parameter : this.parameters)
                parameter.onType(key == Keyboard.KEY_BACK, numberKeys.contains(key), c);
    }

    @Override
    protected void drawStuff(int mouseX, int mouseY, float partialTicks) {
        drawRightSide(mouseX, mouseY, this.fontRenderer.FONT_HEIGHT);
        drawLeftSide(mouseX, mouseY, this.fontRenderer);
    }

    private void drawLeftSide(int mouseX, int mouseY, FontRenderer fontRenderer) {
        int textHeight = fontRenderer.FONT_HEIGHT;
        int centerX = this.width/2;
        int left = this.spacing;
        int top = this.spacing+24;
        int textX = left+(this.spacing/2);
        for(Parameter parameter : this.parameters) {
            if(parameter.isSelected()) {
                GuiUtil.drawLine(new Point2i(left, top), new Point2i(centerX, top), white(128), 1f, this.zLevel);
                top += this.spacing;
                drawString(fontRenderer, parameter.getDisplayName(), textX, top, GuiUtil.WHITE);
                top += (textHeight + this.spacing);
                GuiUtil.drawLine(new Point2i(left, top), new Point2i(centerX, top), white(128), 1f, this.zLevel);
                top += this.spacing;
                top = GuiUtil.drawMultiLineString(this,parameter.getDescription(),textX,centerX,top,textHeight+(this.spacing/2)
                )+(this.spacing/2);
                GuiUtil.drawLine(new Point2i(left, top), new Point2i(centerX, top), white(128), 1f, this.zLevel);
                top += this.spacing;
                parameter.drawVariableElement(new Point2i(left,top),this.spacing,mouseX,mouseY,fontRenderer,this.zLevel);
                break;
            }
        }
    }

    private void drawRightSide(int mouseX, int mouseY, int textHeight) {
        int top = this.spacing+24;
        int sideWidth = this.longestParameterName+(this.spacing*3);
        GuiUtil.drawBox(new Point2i(this.rightSide,0),sideWidth,this.height,
                new Point4i(0,0,0,128),this.zLevel);
        int lineX = this.width-this.spacing;
        GuiUtil.drawLine(new Point2i(lineX,0),new Point2i(lineX, this.height),
                new Point4i(255,255,255,194),1f,this.zLevel);
        int textX = this.rightSide+this.spacing;
        drawString(this.fontRenderer,this.title,textX,top,GuiUtil.WHITE);
        boolean mouseHoverX = mouseX>this.rightSide && mouseX<=lineX;
        int numParameters = this.parameters.size();
        int startY = top+textHeight+(this.spacing*2);
        int index = this.scrollPos;
        this.canScrollDown = (startY+((numParameters-this.scrollPos)*(textHeight+this.spacing)))>this.height;
        while((startY+textHeight+this.spacing)<this.height) {
            Parameter parameter = this.parameters.get(index);
            boolean mouseHover = mouseHoverX && mouseY>=startY && mouseY<(startY+textHeight+this.spacing);
            parameter.setDisplayHover(mouseHover);
            if(mouseHover) {
                GuiUtil.drawBox(new Point2i(this.rightSide,startY),this.width-this.rightSide-this.spacing,textHeight+this.spacing,
                        new Point4i(255,255,255,194),this.zLevel);
                drawString(this.fontRenderer,parameter.getDisplayName(),textX,startY+(this.spacing/2),
                        GuiUtil.makeRGBAInt(0,0,0,255));
            } else drawString(this.fontRenderer,parameter.getDisplayName(),textX,startY+(this.spacing/2),GuiUtil.WHITE);
            index++;
            if(index>=(numParameters-this.scrollPos)) break;
            startY+=(textHeight+this.spacing);
        }
    }

    @Override
    protected void save() {
        for(Parameter parameter : this.parameters) {
            parameter.save();
            if(parameter.hasEdits())
                madeChange(!(this.id.matches("debug") || this.id.matches("registration")));
        }
    }

    public static class Parameter {
        private final String display;
        private final String description;
        private final boolean isCheckBox;
        private final List<String> listValues;
        private final Consumer<Boolean> boolSave;
        private final Consumer<String> stringSave;
        private final Consumer<Integer> intSave;
        private final Consumer<List<String>> listSave;
        private boolean isChecked;
        private String value;
        private int intVal;
        private boolean displayHover;
        private boolean selected;
        private boolean checkBoxHover;
        private boolean valueHover;
        private boolean valueSelected;
        private int listHover;
        private int listSelected;
        private boolean intHover;
        private boolean intSelected;
        private boolean hasEdits;

        public Parameter(String id, String trigger, String extra, int initialInt, Consumer<Integer> onSave) {
            this(id,trigger,extra,false,false,null,new ArrayList<>(),initialInt,
                    null,null,onSave,null);
        }

        public Parameter(String id, String trigger, String extra, Consumer<List<String>> onSave, String ... initialElements) {
            this(id,trigger,extra,Arrays.stream(initialElements).collect(Collectors.toList()),onSave);
        }

        public Parameter(String id, String trigger, String extra, List<String> initialList, Consumer<List<String>> onSave) {
            this(id,trigger,extra,false,false,null,initialList,-1,null,
                    null,null,onSave);
        }

        public Parameter(String id, String trigger, String extra, String initialValue, Consumer<String> onSave) {
            this(id,trigger,extra,false,false,initialValue,new ArrayList<>(),-1,
                    null,onSave,null,null);
        }

        public Parameter(String id, String trigger, String extra, boolean initialCheckBox, Consumer<Boolean> onSave) {
            this(id,trigger,extra,true,initialCheckBox,null,new ArrayList<>(),-1,onSave,
                    null,null,null);
        }

        private Parameter(String id, String trigger, String extra, boolean isCheckBox, boolean initialCheckBox,
                         String initialValue, List<String> initialList, int initialInt, Consumer<Boolean> boolSave,
                          Consumer<String> stringSave, Consumer<Integer> intSave, Consumer<List<String>> listSave) {
            this.display = Translate.parameter("parameter",id,trigger,extra,"name");
            this.description = Translate.parameter("parameter",id,trigger,extra,"desc");
            this.isCheckBox = isCheckBox;
            this.boolSave = boolSave;
            this.stringSave = stringSave;
            this.intSave = intSave;
            this.listSave = listSave;
            this.isChecked = initialCheckBox;
            this.value = initialValue;
            this.listValues = initialList;
            this.intVal = initialInt;
            this.displayHover = false;
            this.selected = false;
            this.checkBoxHover = false;
            this.valueHover = false;
            this.valueSelected = false;
            this.listHover = -1;
            this.listSelected = -1;
            this.hasEdits = false;
        }

        public int displayNameLength(FontRenderer fontRenderer) {
            return fontRenderer.getStringWidth(this.display);
        }

        public String getDisplayName() {
            return this.display;
        }

        public String getDescription() {
            return this.description;
        }

        public boolean isSelected() {
            return this.selected;
        }

        public void setDisplayHover(boolean hover) {
            this.displayHover = hover;
        }

        private void toggleCheckBox() {
            this.hasEdits = true;
            this.isChecked = !this.isChecked;
            this.saveScreen();
        }

        public void onClick(boolean wasRightSide) {
            if(wasRightSide) {
                this.selected = this.displayHover;
                this.valueSelected = false;
                this.listSelected = -1;
                this.intSelected = false;
            }
            else if(this.selected) {
                if(this.checkBoxHover) toggleCheckBox();
                this.valueSelected = this.valueHover;
                this.listSelected = listHover;
                this.intSelected = intHover;
            }
        }

        public void onType(boolean backspace, boolean isNumber, char c) {
            if(this.valueSelected) {
                if(backspace) {
                    if(!this.value.isEmpty()) this.value = this.value.substring(0,this.value.length()-1);
                } else this.value+=c;
                this.hasEdits = true;
                this.saveScreen();
            } else if(this.listSelected>=0) {
                String val = this.listValues.get(this.listSelected);
                if(backspace) {
                    if(!val.isEmpty()) val = val.substring(0,val.length()-1);
                } else val+=c;
                this.listValues.set(this.listSelected,val);
                this.hasEdits = true;
                this.saveScreen();
            } else if(this.intSelected && isNumber) {
                String asString = ""+this.intVal;
                if(backspace) {
                    if(!asString.isEmpty()) asString = asString.substring(0,asString.length()-1);
                } else asString+=c;
                if(asString.isEmpty() || asString.matches("-")) this.intVal = 0;
                else this.intVal = Integer.parseInt(asString);
                this.hasEdits = true;
                this.saveScreen();
            }
        }

        public void drawVariableElement(Point2i topLeft, int spacing, int mouseX, int mouseY, FontRenderer fontRenderer,
                                        float zLevel) {
            if(this.isCheckBox) {
                float center = ((float)spacing)*2.5f;
                this.checkBoxHover = mouseX>=topLeft.x && mouseX<topLeft.x+(spacing*5) && mouseY>=topLeft.y
                        && mouseY< topLeft.y+(spacing*5);
                GuiUtil.bufferSquareTexture(new Point2i((int)(topLeft.x+center),(int)(topLeft.y+center)),center,
                        getCheckboxTexture());
            } else if(Objects.nonNull(this.value)) {
                int width = fontRenderer.getStringWidth(this.value)+4;
                int height = fontRenderer.FONT_HEIGHT+4;
                this.valueHover = mouseX>topLeft.x && mouseX<topLeft.x+(width*2) && mouseY>topLeft.y && mouseY<topLeft.y+(height*2);
                topLeft.setX(topLeft.x/2);
                topLeft.setY(topLeft.y/2);
                Point2i textCorner = new Point2i(topLeft.x+2,topLeft.y+2);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 2f);
                if(this.valueHover) {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(255, 255, 255, 192),
                            new Point4i(0, 0, 0, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(this.value, textCorner.x, textCorner.y,
                            GuiUtil.makeRGBAInt(new Point4i(0, 0, 0, 192)));
                } else {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(0, 0, 0, 192),
                            new Point4i(255, 255, 255, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(this.value, textCorner.x, textCorner.y, GuiUtil.WHITE);
                }
                GlStateManager.popMatrix();
            } else if(Objects.nonNull(this.listSave)) {
                int width;
                int height = fontRenderer.FONT_HEIGHT+4;
                boolean listHover;
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 2f);
                int index = 0;
                topLeft.setX(topLeft.y/2);
                topLeft.setY(topLeft.y/2);
                Point2i textCorner = new Point2i(topLeft.x+2,topLeft.y+2);
                for(String value : this.listValues) {
                    width = fontRenderer.getStringWidth(value)+4;
                    listHover = mouseX>topLeft.x && mouseX<topLeft.x+width && mouseY>topLeft.y && mouseY<topLeft.y+height;
                    if(listHover) {
                        this.listHover = index;
                        GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(255, 255, 255, 192),
                                new Point4i(0, 0, 0, 192), 1f, zLevel);
                        fontRenderer.drawStringWithShadow(value, textCorner.x, textCorner.y,
                                GuiUtil.makeRGBAInt(new Point4i(0, 0, 0, 192)));
                    } else {
                        GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(0, 0, 0, 192),
                                new Point4i(255, 255, 255, 192), 1f, zLevel);
                        fontRenderer.drawStringWithShadow(value, textCorner.x, textCorner.y, GuiUtil.WHITE);
                    }
                    topLeft.setY(topLeft.y+height+16);
                    textCorner.setY(textCorner.y+height+16);
                    index++;
                }
                GlStateManager.popMatrix();
            } else {
                int width = fontRenderer.getStringWidth(""+this.intVal)+4;
                int height = fontRenderer.FONT_HEIGHT+4;
                this.intHover = mouseX>topLeft.x && mouseX<topLeft.x+(width*2) && mouseY>topLeft.y && mouseY<topLeft.y+(height*2);
                topLeft.setX(topLeft.x/2);
                topLeft.setY(topLeft.y/2);
                Point2i textCorner = new Point2i(topLeft.x+2,topLeft.y+2);
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 2f);
                if(this.intHover) {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(255, 255, 255, 192),
                            new Point4i(0, 0, 0, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(""+this.intVal, textCorner.x, textCorner.y,
                            GuiUtil.makeRGBAInt(new Point4i(0, 0, 0, 192)));
                } else {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4i(0, 0, 0, 192),
                            new Point4i(255, 255, 255, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(""+this.intVal, textCorner.x, textCorner.y, GuiUtil.WHITE);
                }
                GlStateManager.popMatrix();
            }
        }

        private ResourceLocation getCheckboxTexture() {
            String checked = "unchecked";
            if(this.isChecked) checked = "checked";
            String path = "black_icons";
            if(this.checkBoxHover) path = "white_icons";
            return new ResourceLocation(Constants.MODID,LogUtil.injectParameters("textures/gui/{}/{}.png",
                    path,checked));
        }

        public boolean hasEdits() {
            return this.hasEdits;
        }

        private void saveScreen() {
            if(Minecraft.getMinecraft().currentScreen instanceof GuiSuperType)
                ((GuiSuperType)Minecraft.getMinecraft().currentScreen).save();
        }

        public void save() {
            if(Objects.nonNull(this.boolSave)) this.boolSave.accept(this.isChecked);
            else if(Objects.nonNull(this.stringSave)) this.stringSave.accept(this.value);
            else if(Objects.nonNull(this.intSave)) this.intSave.accept(this.intVal);
            else this.listSave.accept(this.listValues);
        }
    }
}
