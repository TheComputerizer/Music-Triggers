package mods.thecomputerizer.musictriggers.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.Translate;
import mods.thecomputerizer.musictriggers.client.channels.ChannelManager;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Variable;
import mods.thecomputerizer.theimpossiblelibrary.util.client.GuiUtil;
import mods.thecomputerizer.theimpossiblelibrary.util.file.LogUtil;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
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

    private int calculateLongestParameter(Font Font) {
        int ret = Font.width(this.title)*2;
        for(Parameter parameter : this.searchedParameters) {
            int temp = parameter.displayNameLength(Font);
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
        this.canScroll = this.numElements < this.searchedParameters.size();
        this.canScrollDown = this.canScroll;
    }

    @Override
    protected void updateSearch() {
        int prevScroll = 0;
        this.searchedParameters.clear();
        for(Parameter parameter : this.parameters) {
            parameter.setDisplayHover(false);
            if (checkSearch(parameter.getDisplayName()))
                this.searchedParameters.add(parameter);
        }
        calculateScrollSize();
        this.searchedParameters.sort((p1,p2) -> {
            if(p1.varName.matches("loops") || p1.varName.matches("links")) return -1;
            return p1.getDisplayName().compareToIgnoreCase(p2.getDisplayName());
        });
        this.scrollPos = Math.min(prevScroll,this.canScroll ? this.searchedParameters.size()-this.numElements : 0);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(this.canScroll) {
            if (scroll != 0) {
                if (this.mouseHoverRight) {
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
                } else if (this.mouseHoverLeft) {
                    for (Parameter parameter : this.searchedParameters)
                        if (parameter.isSelected()) {
                            parameter.scroll((int)scroll);
                            return true;
                        }
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if(super.mouseClicked(mouseX, mouseY, mouseButton)) return true;
        boolean clickSuccess = false;
        if (mouseButton == 0) {
            for (Parameter parameter : this.searchedParameters)
                if (parameter.onClick(mouseX > this.rightSide && mouseX <= (this.width - this.spacing), this,
                    this.spacing+(this.spacing/2),this.width/2))
                    clickSuccess = true;
        }
        return clickSuccess;
    }

    @Override
    public boolean keyPressed(int keyCode, int x, int y) {
        if(super.keyPressed(keyCode, x, y)) return true;
        if(isActive(this)) {
            for(Parameter parameter : this.searchedParameters) {
                if(parameter.isSelected()) {
                    if(checkCopy(keyCode, parameter.onCopy())) return true;
                    String paste = checkPaste(keyCode).replaceAll("\"", "");
                    if (!paste.isEmpty()) {
                        parameter.onPaste(paste);
                        return true;
                    }
                    if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                        parameter.onType(true, ' ');

                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char c, int mod) {
        if(super.charTyped(c, mod)) return true;
        if(isActive(this) && SharedConstants.isAllowedChatCharacter(c)) {
            for (Parameter parameter : this.searchedParameters)
                if(parameter.isSelected())
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
    protected void drawStuff(PoseStack matrix, int mouseX, int mouseY, float partialTicks) {
        drawRightSide(matrix, mouseX, mouseY, this.font.lineHeight);
        drawLeftSide(matrix, mouseX, mouseY, this.font);
    }

    private void drawLeftSide(PoseStack matrix, int mouseX, int mouseY, Font Font) {
        int textHeight = Font.lineHeight;
        int centerX = this.width/2;
        int left = this.spacing;
        int top = this.spacing+24;
        int textX = left+(this.spacing/2);
        for(Parameter parameter : this.searchedParameters) {
            if(parameter.isSelected()) {
                GuiUtil.drawLine(new Vector3f(left, top,0), new Vector3f(centerX, top,0), white(128), 1f, this.getBlitOffset());
                top += this.spacing;
                drawString(matrix,Font, parameter.getDisplayName(), textX, top, GuiUtil.WHITE);
                top += (textHeight + this.spacing);
                GuiUtil.drawLine(new Vector3f(left, top,0), new Vector3f(centerX, top,0), white(128), 1f, this.getBlitOffset());
                top += this.spacing;
                top = GuiUtil.drawMultiLineString(matrix,Font,parameter.getDescription(),textX,centerX,top,
                        textHeight+(this.spacing/2),10,parameter.getScrollPos(),GuiUtil.WHITE)+(this.spacing/2);
                GuiUtil.drawLine(new Vector3f(left, top,0), new Vector3f(centerX, top,0), white(128), 1f, this.getBlitOffset());
                int topScroll = (this.spacing*3)+24+this.font.lineHeight;
                int bottomScroll = top;
                top += this.spacing;
                if(Minecraft.getInstance().screen==this && parameter.canScroll()) {
                    parameter.drawScrollBar(topScroll,bottomScroll,left,this.getBlitOffset());
                    this.mouseHoverLeft = this.mouseHover(new Vector3f(left,topScroll,0),mouseX,mouseY,
                            (width/2)-left,bottomScroll-topScroll);
                }
                List<Component> hoverLines = parameter.drawVariableElement(matrix,new Vector3f(left,top,0),this.spacing,mouseX,mouseY,
                        Font,this.getBlitOffset(), Minecraft.getInstance().screen==this);
                if(Minecraft.getInstance().screen==this && !hoverLines.isEmpty())
                    renderComponentTooltip(matrix,hoverLines,mouseX,mouseY);
                break;
            }
        }
    }

    private void drawRightSide(PoseStack matrix, int mouseX, int mouseY, int textHeight) {
        int top = this.spacing+24;
        int sideWidth = this.longestParameterName+(this.spacing*3);
        GuiUtil.drawBox(new Vector3f(this.rightSide,0,0),sideWidth,this.height,
                new Vector4f(0,0,0,128),this.getBlitOffset());
        int lineX = this.width-this.spacing;
        GuiUtil.drawLine(new Vector3f(lineX,0,0),new Vector3f(lineX, this.height,0),
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
                    GuiUtil.drawBox(new Vector3f(this.rightSide,startY,0),this.width-this.rightSide-this.spacing,textHeight+this.spacing,
                            new Vector4f(255,255,255,194),this.getBlitOffset());
                    drawString(matrix,this.font,parameter.getDisplayName(),textX,startY+(this.spacing/2),
                            GuiUtil.makeRGBAInt(50,50,50,255));
                    if(Objects.nonNull(parameter.var)) renderComponentTooltip(matrix,parameter.varNameHover(),mouseX,mouseY);
                } else drawString(matrix,this.font,parameter.getDisplayName(),textX,startY+(this.spacing/2),GuiUtil.WHITE);
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
        Vector3f start = new Vector3f(x, top,0);
        if(perIndex<1) perIndex = 1;
        Vector3f end = new Vector3f(x, (int)(top+perIndex),0);
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


        private List<Component> varNameHover() {
            return Collections.singletonList(new TextComponent(this.varName));
        }

        public int displayNameLength(Font Font) {
            return Font.width(this.display);
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

        public boolean onClick(boolean wasRightSide, GuiSuperType parent, int left, int right) {
            if(parent.isActive(parent)) {
                if (wasRightSide) {
                    if(Objects.isNull(this.var) && Objects.nonNull(this.specialClickFunc) && this.displayHover)
                        this.specialClickFunc.accept(parent);
                    else {
                        this.selected = this.displayHover;
                        if (this.selected)
                            this.descLines = GuiUtil.howManyLinesWillThisBe(parent.getMinecraft().font, this.description, left, right);
                    }
                    return this.selected;
                } else if(this.selected) {
                    if(this.varHover) {
                        if (this.var.getAsBool(true).isPresent()) {
                            toggleCheckBox();
                            return true;
                        }
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
                            Minecraft.getInstance().setScreen(next);
                            return true;
                        } else if (this.var.getName().matches("channel")) {
                            GuiSuperType next = parent.getInstance().createChannelSelectParameterScreen(parent,this);
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

        public List<Component> drawVariableElement(PoseStack matrix, Vector3f topLeft, int spacing, int mouseX, int mouseY,
                                                Font Font, float zLevel, boolean isCurrent) {
            boolean varHover1 = isCurrent && mouseX >= topLeft.x() && mouseX < topLeft.x() + (spacing * 5) && mouseY >= topLeft.y()
                    && mouseY < topLeft.y() + (spacing * 5);
            if(this.var.getAsBool(true).isPresent()) {
                float center = ((float)spacing)*2.5f;
                this.varHover = varHover1;
                GuiUtil.bufferSquareTexture(matrix,new Vector3f((int)(topLeft.x()+center),(int)(topLeft.y()+center),0),center,
                        getCheckboxTexture());
                return new ArrayList<>();
            } else if(this.var.get() instanceof List<?>) {
                float center = ((float)spacing)*2.5f;
                this.varHover = varHover1;
                GuiUtil.bufferSquareTexture(matrix,new Vector3f((int)(topLeft.x()+center),(int)(topLeft.y()+center),0),center,
                        ButtonType.getIcons("edit",this.varHover));
                if(!this.varHover) return new ArrayList<>();
                if(this.var.getName().matches("channel")) return Collections.singletonList(new TextComponent(this.var.get().toString()));
                List<String> ret = ((List<?>)this.var.get()).isEmpty() ? Translate.singletonHoverExtra(this.display,"parameter_list","empty") :
                        Collections.singletonList(Translate.condenseList((List<?>)this.var.get()));
                return ret.stream().map(TextComponent::new).collect(Collectors.toList());
            } else {
                int width = Font.width(this.var.get().toString())+6;
                int height = Font.lineHeight+4;
                this.varHover = isCurrent && mouseX>topLeft.x() && mouseX<topLeft.x()+(width*2) && mouseY>topLeft.y() &&
                        mouseY<topLeft.y()+(height*2);
                Vector3f textCorner = new Vector3f(topLeft.x()+2,topLeft.y()+2,0);
                matrix.pushPose();
                matrix.scale(2f, 2f, 2f);
                char extra = this.selected ? ChannelManager.blinkerChar : ' ';
                if(this.varHover) {
                    GuiUtil.drawBoxWithOutline(topLeft, width*2, height*2, new Vector4f(255, 255, 255, 192),
                            new Vector4f(0, 0, 0, 192), 1f, zLevel);
                    Font.drawShadow(matrix,this.var.get().toString()+extra, textCorner.x()/2, textCorner.y()/2,
                            GuiUtil.makeRGBAInt(new Vector4f(0, 0, 0, 192)));
                } else {
                    GuiUtil.drawBoxWithOutline(topLeft, width*2, height*2, new Vector4f(0, 0, 0, 192),
                            new Vector4f(255, 255, 255, 192), 1f, zLevel);
                    Font.drawShadow(matrix,this.var.get().toString()+extra, textCorner.x()/2, textCorner.y()/2, GuiUtil.WHITE);
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
            Vector3f start = new Vector3f(left, top,0);
            if(perIndex<1) perIndex = 1;
            Vector3f end = new Vector3f(left,(int)(top+perIndex),0);
            GuiUtil.drawLine(start,end,new Vector4f(255,255,255,128), 2f, zLevel);
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
            if(Minecraft.getInstance().screen instanceof GuiSuperType)
                ((GuiSuperType)Minecraft.getInstance().screen).save();
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
