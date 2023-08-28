package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec2f;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import javax.vecmath.Point4f;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiParameters extends GuiSuperType {

    private final List<Parameter> parameters;
    private final List<Parameter> searchedParameters;
    private final String id;
    private final String title;
    private int longestParameterName;
    private int rightSide;
    private int scrollPos;
    private boolean canScroll;
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
        int ret = fontRenderer.getStringWidth(this.title)*2;
        for(Parameter parameter : this.searchedParameters) {
            int temp = parameter.displayNameLength(fontRenderer);
            if(temp>ret) ret = temp;
        }
        this.rightSide = this.width-(ret+(this.spacing*3));
        return ret;
    }

    private void calculateScrollSize() {
        this.scrollPos = 0;
        this.longestParameterName = calculateLongestParameter(mc.fontRenderer);
        int textSlot = mc.fontRenderer.FONT_HEIGHT+this.spacing;
        int totalHeight = this.height-((this.spacing*3)+mc.fontRenderer.FONT_HEIGHT+48);
        int runningHeight = textSlot;
        int runningTotal = 1;
        while(runningHeight+textSlot<totalHeight) {
            runningTotal++;
            runningHeight+=textSlot;
        }
        this.numElements = runningTotal;
        this.canScroll = this.numElements < this.searchedParameters.size();
        this.canScrollDown = this.canScroll;
    }

    @Override
    protected void updateSearch() {
        int prevScroll = 0;
        this.searchedParameters.clear();
        for(Parameter parameter : this.parameters)
            if (checkSearch(parameter.getDisplayName()))
                this.searchedParameters.add(parameter);
        calculateScrollSize();
        this.searchedParameters.sort((p1,p2) -> {
            if(p1.varName.matches("loops") || p1.varName.matches("links")) return -1;
            return p1.getDisplayName().compareToIgnoreCase(p2.getDisplayName());
        });
        this.scrollPos = Math.min(prevScroll,this.canScroll ? this.searchedParameters.size()-this.numElements : 0);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        if(this.canScroll) {
            int scroll = Mouse.getEventDWheel();
            if (scroll != 0) {
                if (this.mouseHoverRight) {
                    if (scroll < 1) {
                        if (this.canScrollDown) {
                            this.scrollPos++;
                            this.canScrollDown = this.numElements + this.scrollPos + 1 < this.searchedParameters.size();
                        }
                    } else if (this.scrollPos > 0) {
                        this.scrollPos--;
                        this.canScrollDown = true;
                    }
                } else if (this.mouseHoverLeft) {
                    for (Parameter parameter : this.searchedParameters)
                        if (parameter.isSelected()) {
                            parameter.scroll(scroll);
                            break;
                        }
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0) {
            for (Parameter parameter : this.searchedParameters)
                parameter.onClick(mouseX>this.rightSide && mouseX<=(this.width-this.spacing),this,
                        this.spacing+(this.spacing/2),this.width/2);
        }
    }

    @Override
    protected void keyTyped(char c, int key) {
        super.keyTyped(c, key);
        if(isActive(this)) {
            for(Parameter parameter : this.searchedParameters) {
                if(checkCopy(key,parameter.onCopy())) continue;
                String paste = checkPaste(key).replaceAll("\"","");
                if(!paste.isEmpty()) {
                    parameter.onPaste(paste);
                    continue;
                }
                if(isKeyValid(c, key) && c!='"') parameter.onType(key == Keyboard.KEY_BACK, c);
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        enableSearch();
        updateSearch();
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
        for(Parameter parameter : this.searchedParameters) {
            if(parameter.isSelected()) {
                GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                top += this.spacing;
                drawString(fontRenderer, parameter.getDisplayName(), textX, top, GuiUtil.WHITE);
                top += (textHeight + this.spacing);
                GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                top += this.spacing;
                top = GuiUtil.drawMultiLineString(fontRenderer,parameter.getDescription(),textX,centerX,top,
                        textHeight+(this.spacing/2),10,parameter.getScrollPos(),GuiUtil.WHITE)+(this.spacing/2);
                GuiUtil.drawLine(new Vec2f(left, top), new Vec2f(centerX, top), white(128), 1f, this.zLevel);
                int topScroll = (this.spacing*3)+24+this.fontRenderer.FONT_HEIGHT;
                int bottomScroll = top;
                top += this.spacing;
                if(Minecraft.getMinecraft().currentScreen==this && parameter.canScroll()) {
                    parameter.drawScrollBar(topScroll,bottomScroll,left,this.zLevel);
                    this.mouseHoverLeft = this.mouseHover(new Vec2f(left,topScroll),mouseX,mouseY,
                            (width/2)-left,bottomScroll-topScroll);
                }
                List<String> hoverLines = parameter.drawVariableElement(new Vec2f(left,top),this.spacing,mouseX,mouseY,
                        fontRenderer,this.zLevel, Minecraft.getMinecraft().currentScreen==this);
                if(Minecraft.getMinecraft().currentScreen==this && !hoverLines.isEmpty())
                    drawHoveringText(hoverLines,mouseX,mouseY);
                break;
            }
        }
    }

    private void drawRightSide(int mouseX, int mouseY, int textHeight) {
        int top = this.spacing+24;
        int sideWidth = this.longestParameterName+(this.spacing*3);
        GuiUtil.drawBox(new Vec2f(this.rightSide,0),sideWidth,this.height,
                new Point4f(0,0,0,128),this.zLevel);
        int lineX = this.width-this.spacing;
        GuiUtil.drawLine(new Vec2f(lineX,0),new Vec2f(lineX, this.height),
                new Point4f(255,255,255,194),1f,this.zLevel);
        int textX = this.rightSide+this.spacing;
        drawString(this.fontRenderer,this.title,textX,top,GuiUtil.WHITE);
        this.mouseHoverRight = mouseX>this.rightSide && mouseX<=lineX;
        int startY = top+textHeight+(this.spacing*2);
        int index = 0;
        for(Parameter parameter: this.searchedParameters) {
            if (index >= this.scrollPos) {
                boolean mouseHover = Minecraft.getMinecraft().currentScreen==this && this.mouseHoverRight &&
                        mouseY>=startY && mouseY<(startY+textHeight+this.spacing);
                parameter.setDisplayHover(mouseHover);
                if(mouseHover) {
                    GuiUtil.drawBox(new Vec2f(this.rightSide,startY),this.width-this.rightSide-this.spacing,textHeight+this.spacing,
                            new Point4f(255,255,255,194),this.zLevel);
                    drawString(this.fontRenderer,parameter.getDisplayName(),textX,startY+(this.spacing/2),
                            GuiUtil.makeRGBAInt(50,50,50,255));
                    if(Objects.nonNull(parameter.var)) drawHoveringText(parameter.var.getName(),mouseX,mouseY);
                } else drawString(this.fontRenderer,parameter.getDisplayName(),textX,startY+(this.spacing/2),GuiUtil.WHITE);
                startY+=(textHeight+this.spacing);
                if (this.height-startY<(textHeight+(this.spacing/2))) break;
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
        Vec2f start = new Vec2f(x, top);
        if(perIndex<1) perIndex = 1;
        Vec2f end = new Vec2f(x, (int)(top+perIndex));
        GuiUtil.drawLine(start,end,white(192), 2f, this.zLevel);
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
        private final Consumer<GuiSuperType> specialClickFunc;
        private final String varName;
        private final String display;
        private final String description;
        private final Variable var;
        private boolean displayHover;
        private boolean selected;
        private boolean varHover;
        private boolean hasEdits;
        private int descLines;
        private int scrollPos;

        public Parameter(String id, String trigger, Consumer<GuiSuperType> specialClickFunc) {
            this(id,trigger,null,null,false,specialClickFunc);
        }

        public Parameter(String id, String trigger, Variable var) {
            this(id,trigger,null,var,false,null);
        }

        public Parameter(String id, String trigger, Variable var, boolean forceList) {
            this(id,trigger,null,var,forceList,null);
        }

        public Parameter(String id, String trigger, String extra, Variable var, boolean forceList,
                         Consumer<GuiSuperType> specialClickFunc) {
            this.varName = trigger;
            this.display = Translate.parameter("parameter",id,trigger,extra,"name");
            this.description = Translate.parameter("parameter",id,trigger,extra,"desc");
            if(Objects.nonNull(var) && forceList && !(var.get() instanceof List<?>)) {
                if(var.get() instanceof String) var.set(Collections.singletonList((String)var.get()));
                else var.set(Trigger.getDefaultParameter(extra,trigger));
            }
            this.var = var;
            this.hasEdits = false;
            this.descLines = 0;
            this.scrollPos = 0;
            this.specialClickFunc = specialClickFunc;
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

        public void onClick(boolean wasRightSide, GuiSuperType parent, int left, int right) {
            if(parent.isActive(parent)) {
                if (wasRightSide) {
                    if(Objects.isNull(this.var) && Objects.nonNull(this.specialClickFunc) && this.displayHover)
                        this.specialClickFunc.accept(parent);
                    else {
                        this.selected = this.displayHover;
                        if (this.selected)
                            this.descLines = GuiUtil.howManyLinesWillThisBe(parent.mc.fontRenderer, this.description, left, right);
                    }
                } else if(this.selected) {
                    if(this.varHover) {
                        if (this.var.getAsBool(true).isPresent()) toggleCheckBox();
                        else if (this.var.get() instanceof List<?>) {
                            String name = this.var.getName();
                            String channel = name.matches("linked_triggers") ? this.var.getParent()
                                    .getValOrDefault("channel",parent.getChannel()) : parent.getChannel();
                            GuiSuperType next;
                            if(name.matches("triggers") || name.matches("required_triggers") ||
                            name.matches("linked_triggers"))
                                next = parent.getInstance().createMultiSelectTriggersScreen(parent, channel,
                                            (elements) -> {
                                                this.setList(elements.stream().map(this::makeTriggerString)
                                                        .filter(Objects::nonNull).collect(Collectors.toList()));
                                                this.hasEdits = true;
                                                parent.save();
                                            });
                            else next = new GuiParameterList(parent,GuiType.PARAMETER_GENERIC,parent.getInstance(),this);
                            parent.playGenericClickSound();
                            Minecraft.getMinecraft().displayGuiScreen(next);
                        } else if (this.var.getName().matches("channel")) {
                            GuiSuperType next = parent.getInstance().createChannelSelectParameterScreen(parent,this);
                            parent.playGenericClickSound();
                            Minecraft.getMinecraft().displayGuiScreen(next);
                        }
                    }
                }
            }
        }

        private String makeTriggerString(GuiSelection.Element element) {
            Table parent = this.var.getParent();
            if(Objects.isNull(parent)) return null;
            if(element instanceof GuiSelection.MonoElement)
                return ((GuiSelection.MonoElement) element).getID();
            return null;
        }

        public String onCopy() {
            if(this.selected) {
                Optional<String> optional = this.var.getAsString(true);
                if(optional.isPresent()) return optional.get();
            }
            return "";
        }

        public void onPaste(String pasted) {
            if(this.selected) {
                Optional<String> optional = this.var.getAsString(true);
                if(optional.isPresent()) {
                    this.var.set(optional.get()+pasted);
                    this.hasEdits = true;
                    this.saveScreen();
                }
            }
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

        @SuppressWarnings("DuplicateExpressions")
        public List<String> drawVariableElement(Vec2f topLeft, int spacing, int mouseX, int mouseY, FontRenderer fontRenderer,
                                                float zLevel, boolean isCurrent) {
            if(this.var.getAsBool(true).isPresent()) {
                float center = ((float)spacing)*2.5f;
                this.varHover = isCurrent && mouseX>=topLeft.x && mouseX<topLeft.x+(spacing*5) && mouseY>=topLeft.y
                        && mouseY< topLeft.y+(spacing*5);
                GuiUtil.bufferSquareTexture(new Vec2f((int)(topLeft.x+center),(int)(topLeft.y+center)),center,
                        getCheckboxTexture());
                return new ArrayList<>();
            } else if(this.var.get() instanceof List<?> || this.var.getName().matches("channel")) {
                float center = ((float)spacing)*2.5f;
                this.varHover = isCurrent && mouseX>=topLeft.x && mouseX<topLeft.x+(spacing*5) && mouseY>=topLeft.y
                        && mouseY< topLeft.y+(spacing*5);
                GuiUtil.bufferSquareTexture(new Vec2f((int)(topLeft.x+center),(int)(topLeft.y+center)),center,
                        ButtonType.getIcons("edit",this.varHover));
                if(!this.varHover) return new ArrayList<>();
                if(this.var.getName().matches("channel")) return Collections.singletonList(this.var.get().toString());
                return ((List<?>)this.var.get()).isEmpty() ? Translate.singletonHoverExtra(this.display,"parameter_list","empty") :
                        Collections.singletonList(Translate.condenseList((List<?>)this.var.get()));
            } else {
                int width = fontRenderer.getStringWidth(this.var.get().toString())+6;
                int height = fontRenderer.FONT_HEIGHT+4;
                this.varHover = isCurrent && mouseX>topLeft.x && mouseX<topLeft.x+(width*2) && mouseY>topLeft.y &&
                        mouseY<topLeft.y+(height*2);
                topLeft = new Vec2f(topLeft.x/2,topLeft.y/2);
                Vec2f textCorner = new Vec2f(topLeft.x+2,topLeft.y+2);
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 2f);
                char extra = this.selected ? ChannelManager.blinkerChar : ' ';
                if(this.varHover) {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4f(255, 255, 255, 192),
                            new Point4f(0, 0, 0, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(this.var.get().toString()+extra, textCorner.x, textCorner.y,
                            GuiUtil.makeRGBAInt(new Point4f(0, 0, 0, 192)));
                } else {
                    GuiUtil.drawBoxWithOutline(topLeft, width, height, new Point4f(0, 0, 0, 192),
                            new Point4f(255, 255, 255, 192), 1f, zLevel);
                    fontRenderer.drawStringWithShadow(this.var.get().toString()+extra, textCorner.x, textCorner.y, GuiUtil.WHITE);
                }
                GlStateManager.popMatrix();
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
            Vec2f start = new Vec2f(left, top);
            if(perIndex<1) perIndex = 1;
            Vec2f end = new Vec2f(left,(int)(top+perIndex));
            GuiUtil.drawLine(start,end,new Point4f(255,255,255,128), 2f, zLevel);
        }

        private ResourceLocation getCheckboxTexture() {
            String checked = "unchecked";
            Optional<Boolean> optional= this.var.getAsBool(true);
            if(optional.isPresent() && optional.get()) checked = "checked";
            String path = "white_icons";
            if(this.varHover) path = "black_icons";
            return Constants.res(LogUtil.injectParameters("textures/gui/{}/{}.png",path,checked));
        }

        public boolean hasEdits() {
            boolean hadEdits = this.hasEdits;
            this.hasEdits = false;
            return hadEdits;
        }

        public void saveScreen() {
            if(Minecraft.getMinecraft().currentScreen instanceof GuiSuperType)
                ((GuiSuperType)Minecraft.getMinecraft().currentScreen).save();
        }

        public void setList(List<String> list) {
            this.var.set(list);
        }

        public List<String> getList() {
            return ((List<?>)this.var.get()).stream().map(Objects::toString).collect(Collectors.toList());
        }

        public void setChannelParameter(String channel) {
            if(this.var.getName().matches("channel"))
                this.var.set(channel);
        }
    }
}
