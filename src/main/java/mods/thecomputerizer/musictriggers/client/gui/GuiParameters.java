package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.audio.ChannelManager;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class GuiParameters extends GuiSuperType {

    private final List<Parameter> parameters;
    private final List<Parameter> searchedParameters;
    private final String id;
    private final String title;
    private int longestParameterName;
    private int rightSide;
    private int scrollPos;
    private boolean canScrollDown;
    private int numElements;
    private boolean mouseHoverRight;
    private boolean mouseHoverLeft;

    public GuiParameters(GuiSuperType parent, GuiType type, Instance configInstance, String guiID, String guiTitle,
                         List<Parameter> parameters) {
        this(parent,type,configInstance,guiID,guiTitle,parameters,null);
    }

    public GuiParameters(GuiSuperType parent, GuiType type, Instance configInstance, String guiID, String guiTitle,
                         List<Parameter> parameters, @Nullable String channel) {
        super(parent, type, configInstance, channel);
        this.id = guiID;
        this.title = guiTitle;
        this.parameters = parameters;
        this.searchedParameters = new ArrayList<>();
        this.scrollPos = 0;
        this.numElements = 0;
    }

    private int calculateLongestParameter(FontRenderer fontRenderer) {
        int ret = fontRenderer.width(this.title)*2;
        for(Parameter parameter : this.searchedParameters) {
            int temp = parameter.displayNameLength(fontRenderer);
            if(temp>ret) ret = temp;
        }
        this.rightSide = this.width-(ret+(this.spacing*3));
        return ret;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        this.longestParameterName = calculateLongestParameter(Minecraft.getInstance().font);
        int textSlot = Minecraft.getInstance().font.lineHeight+this.spacing;
        int totalHeight = this.height-((this.spacing*3)+Minecraft.getInstance().font.lineHeight+48);
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScrollDown = this.numElements < this.searchedParameters.size();
    }

    @Override
    protected void updateSearch() {
        this.searchedParameters.clear();
        for(Parameter parameter : this.parameters)
            if (checkSearch(parameter.getDisplayName()))
                this.searchedParameters.add(parameter);
        calculateScrollSize();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(scroll!=0) {
            if(this.mouseHoverRight) {
                if (scroll < 1) {
                    if (this.canScrollDown) {
                        this.scrollPos++;
                        this.canScrollDown = this.numElements + this.scrollPos + 1 < this.searchedParameters.size();
                        return true;
                    }
                } else if (this.scrollPos > 0) {
                    this.scrollPos--;
                    this.canScrollDown = true;
                    return true;
                }
            } else if(this.mouseHoverLeft) {
                for (Parameter parameter : this.searchedParameters) {
                    if (parameter.isSelected()) {
                        parameter.scroll((int) scroll);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (Parameter parameter : this.searchedParameters)
                if(parameter.onClick(mouseX>this.rightSide && mouseX<=(this.width-this.spacing),this,
                        this.spacing+(this.spacing/2),this.width/2,(spacing*4)+24+this.font.lineHeight,
                        this.font.lineHeight+(this.spacing/2)))
                    return true;
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if(super.keyPressed(keyCode, x, y)) return true;
        if(Minecraft.getInstance().screen==this && keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            for (Parameter parameter : this.searchedParameters)
                parameter.onType(true, ' ');
            return true;
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if(super.charTyped(c, mod)) return true;
        if(Minecraft.getInstance().screen==this && SharedConstants.isAllowedChatCharacter(c)) {
            for (Parameter parameter : this.searchedParameters)
                parameter.onType(false, c);
            return true;
        }
        return false;
    }

    @Override
    public void init() {
        super.init();
        enableSearch();
        updateSearch();
    }

    @Override
    protected void drawStuff(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawRightSide(matrix, mouseX, mouseY, this.font.lineHeight);
        drawLeftSide(matrix, mouseX, mouseY, this.font);
    }

    private void drawLeftSide(MatrixStack matrix, int mouseX, int mouseY, FontRenderer fontRenderer) {
        int textHeight = fontRenderer.lineHeight;
        int centerX = this.width/2;
        int left = this.spacing;
        int top = this.spacing+24;
        int textX = left+(this.spacing/2);
        for(Parameter parameter : this.searchedParameters) {
            if(parameter.isSelected()) {
                GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                top += this.spacing;
                drawString(matrix,fontRenderer, parameter.getDisplayName(), textX, top, GuiUtil.WHITE);
                top += (textHeight + this.spacing);
                GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                top += this.spacing;
                top = GuiUtil.drawMultiLineString(matrix,fontRenderer,parameter.getDescription(),textX,centerX,top,
                        textHeight+(this.spacing/2),10,parameter.getScrollPos(),GuiUtil.WHITE)+(this.spacing/2);
                GuiUtil.drawLine(new Vector2f(left, top), new Vector2f(centerX, top), white(128), 1f, this.getBlitOffset());
                int topScroll = (this.spacing*3)+24+this.font.lineHeight;
                int bottomScroll = top;
                top += this.spacing;
                if(Minecraft.getInstance().screen==this && parameter.canScroll()) {
                    parameter.drawScrollBar(topScroll,bottomScroll,left,this.getBlitOffset());
                    this.mouseHoverLeft = this.mouseHover(new Vector2f(left,topScroll),mouseX,mouseY,
                            (width/2)-left,bottomScroll-topScroll);
                }
                List<ITextComponent> hoverLines = parameter.drawVariableElement(matrix,new Vector2f(left,top),this.spacing,mouseX,mouseY,
                        fontRenderer,this.getBlitOffset(), Minecraft.getInstance().screen==this);
                if(Minecraft.getInstance().screen==this && !hoverLines.isEmpty())
                    renderComponentTooltip(matrix,hoverLines,mouseX,mouseY);
                break;
            }
        }
    }

    private void drawRightSide(MatrixStack matrix, int mouseX, int mouseY, int textHeight) {
        int top = this.spacing+24;
        int sideWidth = this.longestParameterName+(this.spacing*3);
        GuiUtil.drawBox(new Vector2f(this.rightSide,0),sideWidth,this.height,
                new Vector4f(0,0,0,128),this.getBlitOffset());
        int lineX = this.width-this.spacing;
        GuiUtil.drawLine(new Vector2f(lineX,0),new Vector2f(lineX, this.height),
                new Vector4f(255,255,255,194),1f,this.getBlitOffset());
        int textX = this.rightSide+this.spacing;
        drawString(matrix,this.font,this.title,textX,top,GuiUtil.WHITE);
        this.mouseHoverRight = mouseX>this.rightSide && mouseX<=lineX;
        int startY = top+textHeight+(this.spacing*2);
        int index = 0;
        for(Parameter parameter: this.searchedParameters) {
            if (index >= this.scrollPos) {
                boolean mouseHover = Minecraft.getInstance().screen==this && this.mouseHoverRight &&
                        mouseY>=startY && mouseY<(startY+textHeight+this.spacing);
                parameter.setDisplayHover(mouseHover);
                if(mouseHover) {
                    GuiUtil.drawBox(new Vector2f(this.rightSide,startY),this.width-this.rightSide-this.spacing,textHeight+this.spacing,
                            new Vector4f(255,255,255,194),this.getBlitOffset());
                    drawString(matrix,this.font,parameter.getDisplayName(),textX,startY+(this.spacing/2),
                            GuiUtil.makeRGBAInt(50,50,50,255));
                } else drawString(matrix,this.font,parameter.getDisplayName(),textX,startY+(this.spacing/2),GuiUtil.WHITE);
                startY+=(textHeight+this.spacing);
                if ((this.height-(this.spacing+24) - startY) < (this.spacing + this.font.lineHeight)) break;
            }
            index++;
        }
        if(this.searchedParameters.size()>this.numElements) drawScrollBar();
    }

    private void drawScrollBar() {
        float height = (float)this.height;
        float indices = this.searchedParameters.size()-this.numElements;
        float perIndex = height/indices;
        int top = (int)(perIndex*this.scrollPos);
        int x = this.width-1;
        Vector2f start = new Vector2f(x, top);
        if(perIndex<1) perIndex = 1;
        Vector2f end = new Vector2f(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.getBlitOffset());
    }

    @Override
    protected void save() {
        for(Parameter parameter : this.parameters) {
            if(parameter.hasEdits()) {
                madeChange(!(this.id.matches("debug") || this.id.matches("registration")));
                break;
            }
        }
    }

    public static class Parameter {
        private final String display;
        private final String description;
        private final Variable var;
        private boolean displayHover;
        private boolean selected;
        private boolean varHover;
        private boolean hasEdits;
        private int descLines;
        private int scrollPos;

        public Parameter(String id, String trigger, Variable var) {
            this(id,trigger,null,var);
        }

        public Parameter(String id, String trigger, String extra, Variable var) {
            this.display = Translate.parameter("parameter",id,trigger,extra,"name");
            this.description = Translate.parameter("parameter",id,trigger,extra,"desc");
            this.var = var;
            this.hasEdits = false;
            this.descLines = 0;
            this.scrollPos = 0;
        }

        public int displayNameLength(FontRenderer fontRenderer) {
            return fontRenderer.width(this.display);
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

        public void scroll(int scroll) {
            if(scroll<1) {
                if(this.descLines-10>this.scrollPos) this.scrollPos++;
            } else if(this.scrollPos>0) this.scrollPos--;
        }

        public int getScrollPos() {
            return this.scrollPos;
        }

        private void toggleCheckBox() {
            this.hasEdits = true;
            this.var.invert();
            this.saveScreen();
        }

        public boolean onClick(boolean wasRightSide, GuiSuperType parent, int left, int right, int top, int spacing) {
            if(Minecraft.getInstance().screen==parent) {
                if (wasRightSide) {
                    this.selected = this.displayHover;
                    if (this.selected)
                        this.descLines = GuiUtil.howManyLinesWillThisBe(Minecraft.getInstance().font, this.description, left,
                                right, top, spacing);
                    return this.selected;
                } else if(this.selected) {
                    if(this.varHover) {
                        if (this.var.getAsBool(true).isPresent()) {
                            toggleCheckBox();
                            return true;
                        }
                        else if (this.var.get() instanceof List<?>) {
                            GuiSuperType next = this.var.getName().matches("triggers") ?
                                    parent.getInstance().createMultiSelectTriggerScreen(parent, parent.getChannel(),
                                            (elements) -> {
                                                this.setList(elements.stream().map(this::makeTriggerString)
                                                        .filter(Objects::nonNull).collect(Collectors.toList()));
                                                this.hasEdits = true;
                                                parent.save();
                                            }) : new GuiParameterList(parent, GuiType.PARAMETER_GENERIC,
                                    parent.getInstance(), this);
                            parent.playGenericClickSound();
                            Minecraft.getInstance().setScreen(next);
                            return true;
                        }
                    }
                }
            }
            return false;
        }

        private String makeTriggerString(GuiSelection.Element element) {
            Table parent = this.var.getParent();
            if(Objects.isNull(parent)) return null;
            if(element instanceof GuiSelection.MonoElement)
                return ((GuiSelection.MonoElement) element).getID();
            return null;
        }

        public void onType(boolean backspace, char c) {
            if(this.selected) {
                Optional<String> optional = this.var.getAsString(true);
                if(optional.isPresent()) {
                    String val = optional.get();
                    if (backspace) {
                        if (!val.isEmpty()) val = val.substring(0, val.length() - 1);
                    } else val += c;
                    this.var.set(val);
                    this.hasEdits = true;
                    this.saveScreen();
                }
            }
        }

        public List<ITextComponent> drawVariableElement(MatrixStack matrix, Vector2f topLeft, int spacing, int mouseX, int mouseY,
                                                FontRenderer fontRenderer, float zLevel, boolean isCurrent) {
            boolean varHover1 = isCurrent && mouseX >= topLeft.x && mouseX < topLeft.x + (spacing * 5) && mouseY >= topLeft.y
                    && mouseY < topLeft.y + (spacing * 5);
            if(this.var.getAsBool(true).isPresent()) {
                float center = ((float)spacing)*2.5f;
                this.varHover = varHover1;
                GuiUtil.bufferSquareTexture(matrix,new Vector2f((int)(topLeft.x+center),(int)(topLeft.y+center)),center,
                        getCheckboxTexture());
                return new ArrayList<>();
            } else if(this.var.get() instanceof List<?>) {
                float center = ((float)spacing)*2.5f;
                this.varHover = varHover1;
                GuiUtil.bufferSquareTexture(matrix,new Vector2f((int)(topLeft.x+center),(int)(topLeft.y+center)),center,
                        ButtonType.getIcons("edit",this.varHover));
                if(!this.varHover) return new ArrayList<>();
                List<String> ret = ((List<?>)this.var.get()).isEmpty() ? Translate.singletonHoverExtra(this.display,"parameter_list","empty") :
                        Collections.singletonList(Translate.condenseList((List<?>)this.var.get()));
                return ret.stream().map(StringTextComponent::new).collect(Collectors.toList());
            } else {
                int width = fontRenderer.width(this.var.get().toString())+6;
                int height = fontRenderer.lineHeight+4;
                this.varHover = isCurrent && mouseX>topLeft.x && mouseX<topLeft.x+(width*2) && mouseY>topLeft.y &&
                        mouseY<topLeft.y+(height*2);
                //topLeft = new Vector2f(topLeft.x/2,topLeft.y/2);
                Vector2f textCorner = new Vector2f(topLeft.x+2,topLeft.y+2);
                matrix.pushPose();
                matrix.scale(2f, 2f, 2f);
                char extra = this.selected ? ChannelManager.blinker : ' ';
                if(this.varHover) {
                    GuiUtil.drawBoxWithOutline(topLeft, width*2, height*2, new Vector4f(255, 255, 255, 192),
                            new Vector4f(0, 0, 0, 192), 1f, zLevel);
                    fontRenderer.drawShadow(matrix,this.var.get().toString()+extra, textCorner.x/2, textCorner.y/2,
                            GuiUtil.makeRGBAInt(new Vector4f(0, 0, 0, 192)));
                } else {
                    GuiUtil.drawBoxWithOutline(topLeft, width*2, height*2, new Vector4f(0, 0, 0, 192),
                            new Vector4f(255, 255, 255, 192), 1f, zLevel);
                    fontRenderer.drawShadow(matrix,this.var.get().toString()+extra, textCorner.x/2, textCorner.y/2, GuiUtil.WHITE);
                }
                matrix.popPose();
                return new ArrayList<>();
            }
        }

        public boolean canScroll() {
            return this.descLines>10;
        }

        public void drawScrollBar(int scrollTop, int bottom, int left, float zLevel) {
            float height = bottom-scrollTop;
            float indices = this.descLines-9;
            float perIndex = height/indices;
            int top = (int)(scrollTop+(perIndex*this.scrollPos));
            Vector2f start = new Vector2f(left, top);
            if(perIndex<1) perIndex = 1;
            Vector2f end = new Vector2f(left,(int)(top+perIndex));
            GuiUtil.drawLine(start,end,new Vector4f(255,255,255,128), 2f, zLevel);
        }

        private ResourceLocation getCheckboxTexture() {
            String checked = "unchecked";
            Optional<Boolean> optional= this.var.getAsBool(true);
            if(optional.isPresent() && optional.get()) checked = "checked";
            String path = "white_icons";
            if(this.varHover) path = "black_icons";
            return new ResourceLocation(Constants.MODID,LogUtil.injectParameters("textures/gui/{}/{}.png",
                    path,checked));
        }

        public boolean hasEdits() {
            return this.hasEdits;
        }

        private void saveScreen() {
            if(Minecraft.getInstance().screen instanceof GuiSuperType)
                ((GuiSuperType)Minecraft.getInstance().screen).save();
        }

        public void setList(List<String> list) {
            this.var.set(list);
        }

        public List<String> getList() {
            return ((List<?>)this.var.get()).stream().map(Objects::toString).collect(Collectors.toList());
        }
    }
}
