package mods.thecomputerizer.musictriggers.client.gui;

import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.client.EventsClient;
import mods.thecomputerizer.musictriggers.common.MusicTriggersItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Vector4f;
import java.util.List;

public class GuiSuperType extends GuiScreen {
    private static final float PRECISION = 5.0f;
    protected static final ResourceLocation icon = new ResourceLocation(MusicTriggers.MODID,"textures/gui/logo.png");
    private boolean closing;
    private boolean doneClosing;
    private double startAnimation;
    private int selectedItem;
    private List<RadialButton> radialButtons;


    public GuiSuperType(List<RadialButton> radialButtons) {
        this.closing = false;
        this.doneClosing = false;
        this.startAnimation = Minecraft.getMinecraft().world.getTotalWorldTime() + (double)Minecraft.getMinecraft().getRenderPartialTicks();
        this.selectedItem = -1;
        this.radialButtons = radialButtons;
    }

    @Override
    public void initGui() {
        EventsClient.renderDebug = false;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    private void animateClose() {
        closing = true;
        doneClosing = false;
        startAnimation = Minecraft.getMinecraft().world.getTotalWorldTime() + (double) Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        final float OPEN_ANIMATION_LENGTH = 2.5f;
        long worldTime = Minecraft.getMinecraft().world.getTotalWorldTime();
        float animationTime = (float) (worldTime + partialTicks - startAnimation);
        float openAnimation = closing ? 1.0f - animationTime / OPEN_ANIMATION_LENGTH : animationTime / OPEN_ANIMATION_LENGTH;
        //if (this.closing && openAnimation <= 0.0f) this.doneClosing = true;
        float animProgress = 1;//MathHelper.clamp(openAnimation, 0, 1);
        float radiusIn = Math.max(0.1f, 30 * animProgress);
        float radiusOut = radiusIn * 2;
        float itemRadius = (radiusIn + radiusOut) * 0.5f;
        float animTop = (1 - animProgress) * height / 2.0f;
        int x = this.width / 2;
        int y = this.height / 2;
        double a = Math.toDegrees(Math.atan2(mouseY - y, mouseX - x));
        double d = Math.sqrt(Math.pow(mouseX - x, 2) + Math.pow(mouseY - y, 2));
        int numberOfSlices = this.radialButtons.size();
        float s0 = (((0 - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
        if (a < s0) a += 360;
        GlStateManager.pushMatrix();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(0, animTop, 0);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        boolean hasMouseOver = false;
        if (!this.closing) {
            this.selectedItem = -1;
            for (int i = 0; i < numberOfSlices; i++) {
                float s = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
                float e = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
                if (a >= s && a < e && d >= radiusIn && d < radiusOut) {
                    this.selectedItem = i;
                    break;
                }
            }
        }
        for (int i = 0; i < numberOfSlices; i++) {
            float s = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
            float e = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
            if (selectedItem == i) {
                drawSlice(buffer, x, y, zLevel, radiusIn/2, radiusOut, s, e, 255, 255, 255, 64);
                hasMouseOver = true;
            } else drawSlice(buffer, x, y, zLevel, radiusIn/2, radiusOut, s, e, 0, 0, 0, 64);
        }
        tessellator.draw();
        drawCenter(buffer,x,y,zLevel,radiusIn);
        tessellator.draw();
        GlStateManager.enableTexture2D();
        if (hasMouseOver)
            drawCenteredString(fontRenderer, I18n.format("gui.musictriggers.test"), width / 2, (height - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);
        /*
        if (selectedCategory != null) {
            int[] slotIndices = selectedCategory.slotIndices();
            for (int i = 0; i < slotIndices.length; i++) {
                ItemStack inSlot = toolbox.getStackInSlot(slotIndices[i]);
                if (inSlot.getCount() <= 0) {
                    float angle1 =
                            ((i / (float) selectedCategory.numberOfIndices()) + 0.25f) * 2 * (float) Math.PI;
                    float posX = x + itemRadius * (float) Math.cos(angle1);
                    float posY = y + itemRadius * (float) Math.sin(angle1);
                    drawCenteredString(
                            fontRenderer,
                            I18n.format("text.immersiveradialmenu.empty"),
                            (int) posX,
                            (int) posY - fontRenderer.FONT_HEIGHT / 2,
                            0x7FFFFFFF
                    );
                }
            }

            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < slotIndices.length; i++) {
                float angle1 = ((i / (float) selectedCategory.numberOfIndices()) + 0.25f) * 2 * (float) Math.PI;
                float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
                float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
                ItemStack inSlot = toolbox.getStackInSlot(slotIndices[i]);
                if (inSlot.getCount() > 0) {
                    this.itemRender.renderItemAndEffectIntoGUI(
                            inSlot,
                            (int) posX,
                            (int) posY
                    );
                    this.itemRender.renderItemOverlayIntoGUI(
                            this.fontRenderer,
                            inSlot,
                            (int) posX,
                            (int) posY,
                            ""
                    );
                } else {
                    posX = x + itemRadius * (float) Math.cos(angle1);
                    posY = y + itemRadius * (float) Math.sin(angle1);
                    drawCenteredString(
                            fontRenderer,
                            I18n.format("text.immersiveradialmenu.empty"),
                            (int) posX,
                            (int) posY - fontRenderer.FONT_HEIGHT / 2,
                            0x7FFFFFFF
                    );
                }
            }
            RenderHelper.disableStandardItemLighting();
        }
        else {

         */
        RenderHelper.enableGUIStandardItemLighting();
        for (int i = 0; i < numberOfSlices; i++) {
            float angle1 = ((i / (float) numberOfSlices) + 0.25f) * 2 * (float) Math.PI;
            float posX = x - 8 + itemRadius * (float) Math.cos(angle1);
            float posY = y - 8 + itemRadius * (float) Math.sin(angle1);
            ItemStack icon = MusicTriggersItems.BLANK_RECORD.getDefaultInstance();
            this.itemRender.renderItemAndEffectIntoGUI(icon, (int) posX, (int) posY);
            this.itemRender.renderItemOverlayIntoGUI(this.fontRenderer, icon, (int) posX, (int) posY, "");
            /*
            ResourceLocation icon = this.radialButtons.get(i).getMenuIcon();
            GlStateManager.enableBlend();
            GlStateManager.pushMatrix();
            Vector4f color = new Vector4f(1, 1, 1, 1);
            GlStateManager.color(color.getX(), color.getY(), color.getZ(), 1f);
            this.mc.getTextureManager().bindTexture(icon);
            drawModalRectWithCustomSizedTexture((int) posX, (int) posY,this.width,this.height,this.width,this.height,itemRadius,itemRadius);
             */
        }
        RenderHelper.disableStandardItemLighting();
        //}
        GlStateManager.popMatrix();
        if(selectedItem!=-1) drawHoveringText(this.radialButtons.get(selectedItem).getTooltipLines(),mouseX,mouseY);
    }

    private void drawSlice(BufferBuilder buffer, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, MathHelper.ceil(angle / PRECISION));

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;
            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);
            buffer.pos(pos1OutX, pos1OutY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos1InX, pos1InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2InX, pos2InY, z).color(r, g, b, a).endVertex();
            buffer.pos(pos2OutX, pos2OutY, z).color(r, g, b, a).endVertex();
        }
    }

    private void drawCenter(BufferBuilder buffer, float x, float y, float z, float radiusIn) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(icon);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int startX = (int)(x-radiusIn);
        int endX = (int)(x+radiusIn);
        int startY = (int)(y-radiusIn);
        int endY = (int)(y+radiusIn);
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.pos(startX, endY, 0.0D).tex(startX, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.pos(endX, endY, 0.0D).tex((float)endX / 32.0F, (float)endY / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.pos(endX, startY, 0.0D).tex((float)endX / 32.0F, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
        buffer.pos(startX, startY, 0.0D).tex(startX, (float)startY / 32.0F).color(64, 64, 64, 255).endVertex();
    }

    @Override
    public void onGuiClosed() {
        EventsClient.renderDebug = true;
    }
}
