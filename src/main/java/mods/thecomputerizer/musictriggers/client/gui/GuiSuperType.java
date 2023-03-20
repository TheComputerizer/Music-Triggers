package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.ClientEvents;
import mods.thecomputerizer.musictriggers.client.gui.instance.Instance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.config.GuiUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.util.TriConsumer;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Point2i;
import javax.vecmath.Point4i;
import java.util.*;

public abstract class GuiSuperType extends GuiScreen {

    protected final GuiSuperType parent;
    protected final GuiType type;
    private final Instance configInstance;
    private final List<ButtonSuperType> superButtons;
    private final String channel;
    private ButtonSuperType applyButton;
    protected int spacing;
    private int buttonIDCounter;
    protected GuiTextField searchBar;
    protected boolean isInitialized;

    public GuiSuperType(GuiSuperType parent, GuiType type, Instance configInstance) {
        this(parent,type,configInstance,null);
    }

    public GuiSuperType(GuiSuperType parent, GuiType type, Instance configInstance, @Nullable String channel) {
        this.parent = parent;
        this.type = type;
        this.configInstance = configInstance;
        this.channel = channel;
        this.superButtons = new ArrayList<>();
        this.spacing = 16;
        this.isInitialized = false;
    }

    public Instance getInstance() {
        return this.configInstance;
    }

    public GuiSuperType getParent() {
        return this.parent;
    }

    public String getChannel() {
        return this.channel;
    }

    public Point4i white(int alpha) {
        return new Point4i(255,255,255,alpha);
    }

    public Point4i black(int alpha) {
        return new Point4i(0,0,0,alpha);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if(!getInstance().hasEdits()) {
                this.mc.displayGuiScreen(null);
                if (this.mc.currentScreen == null) this.mc.setIngameFocus();
            } else this.mc.displayGuiScreen(new GuiPopUp(this,GuiType.POPUP,getInstance(),"confirm"));
        }
        this.searchBar.textboxKeyTyped(typedChar, keyCode);
        updateSearch();
    }

    protected boolean isKeyValid(char c, int keyCode) {
        return ChatAllowedCharacters.isAllowedCharacter(c) || keyCode == Keyboard.KEY_BACK;
    }

    protected String backspace(String value) {
        if(!value.isEmpty()) return value.substring(0,value.length()-1);
        return value;
    }

    @Override
    public void initGui() {
        this.superButtons.clear();
        switch (Minecraft.getMinecraft().gameSettings.guiScale) {
            case 0: {
                this.spacing = 10;
                break;
            }
            case 1: {
                this.spacing = 24;
                break;
            }
            case 2: {
                this.spacing = 16;
                break;
            }
            case 3: {
                this.spacing = 12;
                break;
            }
        }
        ClientEvents.SHOULD_RENDER_DEBUG = false;
        for (ButtonType buttonHolder : this.type.getButtonHolders()) {
            if (buttonHolder.isNormal()) {
                ButtonSuperType button = buttonHolder.getNormalButton(this.buttonIDCounter++, this);
                if(buttonHolder.getID().contains("apply"))
                    this.applyButton = button;
                addSuperButton(button);
            }
        }
        if(this.configInstance.hasEdits() && Objects.nonNull(this.applyButton)) this.applyButton.setEnable(true);
        this.searchBar = new GuiTextField(this.buttonIDCounter++, this.fontRenderer, this.width / 4, 8,
                this.width / 2, 16);
        this.searchBar.setMaxStringLength(32500);
        this.searchBar.setVisible(false);
        this.searchBar.setEnabled(false);
        this.searchBar.setText("");
    }

    protected void enableSearch() {
        this.searchBar.setEnabled(true);
        this.searchBar.setVisible(true);
    }

    protected boolean checkSearch(String toCheck) {
        return toCheck.toLowerCase().contains(this.searchBar.getText().toLowerCase());
    }

    protected void updateSearch() {

    }

    public ButtonSuperType createBottomButton(String name, int width, int modes, List<String> hoverText,
                                                 TriConsumer<GuiSuperType, ButtonSuperType, Integer> handler) {
        return new ButtonSuperType(this.buttonIDCounter++,0, this.height-24,width,16, modes,name,
                hoverText, handler,true);
    }

    private void addSuperButton(ButtonSuperType button) {
        this.superButtons.add(button);
    }

    protected void addSuperButton(ButtonSuperType button, int x) {
        if(x>=0) button.x=x;
        this.superButtons.add(button);
    }

    public void madeChange(boolean needReload) {
        if(!this.applyButton.enabled) {
            recursivelySetApply(this);
            getInstance().madeChanges(needReload);
        }
    }

    private void recursivelySetApply(GuiSuperType superScreen) {
        this.applyButton.setEnable(true);
        if(Objects.nonNull(superScreen.parent)) recursivelySetApply(superScreen.parent);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    protected abstract void drawStuff(int mouseX, int mouseY, float partialTicks);

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawStuff(mouseX, mouseY, partialTicks);
        for(ButtonSuperType superButton : this.superButtons)
            superButton.drawButton(this.mc,mouseX,mouseY,partialTicks);
        this.searchBar.drawTextBox();
        for(ButtonSuperType superButton : this.superButtons) {
            if(Minecraft.getMinecraft().currentScreen == this) {
                List<String> hoverText = superButton.getHoverText(mouseX,mouseY);
                if(!hoverText.isEmpty()) drawHoveringText(hoverText,mouseX,mouseY);
            }
        }
    }

    public boolean mouseHover(Point2i topLeft, int mouseX, int mouseY, int width, int height) {
        return Minecraft.getMinecraft().currentScreen == this &&
                mouseX>=topLeft.x && mouseX<topLeft.x+width && mouseY>=topLeft.y && mouseY<topLeft.y+height;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0) {
            for (ButtonSuperType superButton : this.superButtons) {
                if (superButton.mousePressed(this.mc, mouseX, mouseY)) {
                    this.selectedButton = superButton;
                    superButton.playPressSound(this.mc.getSoundHandler());
                    superButton.handle(this);
                }
            }
        }
        this.searchBar.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void saveAndDisplay(GuiSuperType next) {
        save();
        this.mc.displayGuiScreen(next);
    }

    public void parentUpdate() {
        madeChange(true);
    }

    protected abstract void save();

    public void saveAndClose(boolean reload) {
        save();
        if(reload) {
            if(this.configInstance.hasEdits())
                applyButton();
            MusicTriggers.logExternally(Level.INFO,"No in-game changes were detected - Loading file changes");
            ClientEvents.initReload();
            Minecraft.getMinecraft().setIngameFocus();
        } else this.mc.displayGuiScreen(this.parent);
    }

    public void applyButton() {
        save();
        if(Objects.nonNull(this.parent)) {
            Minecraft.getMinecraft().displayGuiScreen(this.parent);
            this.parent.applyButton();
        }
        else this.getInstance().writeAndReload();
    }

    @Override
    public void onGuiClosed() {
        ClientEvents.SHOULD_RENDER_DEBUG = true;
    }

    public void playGenericClickSound() {
        Minecraft.getMinecraft().getSoundHandler().playSound(
                PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    //Forges drawHoveringText implementation but without disabling lighting
    @Override
    public void drawHoveringText(List<String> textLines, int x, int y, @Nonnull FontRenderer font) {
        if (!textLines.isEmpty()) {
            RenderTooltipEvent.Pre event = new RenderTooltipEvent.Pre(ItemStack.EMPTY, textLines, x, y, this.width,
                    this.height, -1, font);
            if (MinecraftForge.EVENT_BUS.post(event)) return;
            x = event.getX();
            y = event.getY();
            font = event.getFontRenderer();

            GlStateManager.disableRescaleNormal();
            GlStateManager.disableDepth();
            int tooltipTextWidth = 0;
            for (String textLine : textLines) {
                int textLineWidth = font.getStringWidth(textLine);
                if (textLineWidth > tooltipTextWidth) tooltipTextWidth = textLineWidth;
            }
            boolean needsWrap = false;
            int titleLinesCount = 1;
            int tooltipX = x + 12;
            if (tooltipX + tooltipTextWidth + 4 > this.width) {
                tooltipX = x - 16 - tooltipTextWidth;
                if (tooltipX < 4) {// if the tooltip doesn't fit on the screen
                    if (x > this.width / 2) tooltipTextWidth = x - 12 - 8;
                    else tooltipTextWidth = this.width - 16 - x;
                    needsWrap = true;
                }
            }
            if (needsWrap) {
                int wrappedTooltipWidth = 0;
                List<String> wrappedTextLines = new ArrayList<>();
                for (int i = 0; i < textLines.size(); i++) {
                    String textLine = textLines.get(i);
                    List<String> wrappedLine = font.listFormattedStringToWidth(textLine, tooltipTextWidth);
                    if (i == 0) titleLinesCount = wrappedLine.size();
                    for (String line : wrappedLine) {
                        int lineWidth = font.getStringWidth(line);
                        if (lineWidth > wrappedTooltipWidth) wrappedTooltipWidth = lineWidth;
                        wrappedTextLines.add(line);
                    }
                }
                tooltipTextWidth = wrappedTooltipWidth;
                textLines = wrappedTextLines;
                if (x > this.width / 2) tooltipX = x - 16 - tooltipTextWidth;
                else tooltipX = x + 12;
            }
            int tooltipY = y - 12;
            int tooltipHeight = 8;
            if (textLines.size() > 1) {
                tooltipHeight += (textLines.size() - 1) * 10;
                if (textLines.size() > titleLinesCount)
                    tooltipHeight += 2; // gap between title lines and next lines
            }
            if (tooltipY < 4) tooltipY = 4;
            else if (tooltipY + tooltipHeight + 4 > this.height)
                tooltipY = this.height - tooltipHeight - 4;
            final int zLevel = 300;
            int backgroundColor = 0xF0100010;
            int borderColorStart = 0x505000FF;
            int borderColorEnd = (borderColorStart & 0xFEFEFE) >> 1 | borderColorStart & 0xFF000000;
            RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(ItemStack.EMPTY, textLines, tooltipX, tooltipY,
                    font, backgroundColor, borderColorStart, borderColorEnd);
            MinecraftForge.EVENT_BUS.post(colorEvent);
            backgroundColor = colorEvent.getBackground();
            borderColorStart = colorEvent.getBorderStart();
            borderColorEnd = colorEvent.getBorderEnd();
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 4, tooltipX + tooltipTextWidth + 3,
                    tooltipY - 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipTextWidth + 3,
                    tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                    tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 4, tooltipY - 3, tooltipX - 3,
                    tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 3, tooltipY - 3, tooltipX + tooltipTextWidth + 4,
                    tooltipY + tooltipHeight + 3, backgroundColor, backgroundColor);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3 + 1, tooltipX - 3 + 1,
                    tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX + tooltipTextWidth + 2, tooltipY - 3 + 1, tooltipX + tooltipTextWidth + 3,
                    tooltipY + tooltipHeight + 3 - 1, borderColorStart, borderColorEnd);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY - 3, tooltipX + tooltipTextWidth + 3,
                    tooltipY - 3 + 1, borderColorStart, borderColorStart);
            GuiUtils.drawGradientRect(zLevel, tooltipX - 3, tooltipY + tooltipHeight + 2, tooltipX + tooltipTextWidth + 3,
                    tooltipY + tooltipHeight + 3, borderColorEnd, borderColorEnd);
            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(ItemStack.EMPTY, textLines, tooltipX, tooltipY,
                    font, tooltipTextWidth, tooltipHeight));
            int tooltipTop = tooltipY;
            for (int lineNumber = 0; lineNumber < textLines.size(); ++lineNumber) {
                String line = textLines.get(lineNumber);
                font.drawStringWithShadow(line, (float)tooltipX, (float)tooltipY, -1);
                if (lineNumber + 1 == titleLinesCount) tooltipY += 2;
                tooltipY += 10;
            }
            MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(ItemStack.EMPTY, textLines, tooltipX, tooltipTop,
                    font, tooltipTextWidth, tooltipHeight));
            GlStateManager.enableDepth();
            GlStateManager.enableRescaleNormal();
        }
    }
}
