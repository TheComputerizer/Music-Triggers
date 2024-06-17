package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelJukebox;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelPreview;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.*;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class MTClient {
    
    private static final Toml SPECIAL_CHANNELS = buildSpecialChannels();
    
    private static Toml buildSpecialChannels() {
        Toml toml = Toml.getEmpty();
        toml.addTable("jukebox",buildSpecialChannel("record"));
        toml.addTable("preview",buildSpecialChannel("master"));
        return toml;
    }
    
    private static Toml buildSpecialChannel(String category) {
        Toml info = Toml.getEmpty();
        info.addEntry("sound_category",category);
        info.addEntry("play_normal_music",true);
        info.addEntry("pauses_overrides",true);
        return info;
    }
    
    public static ChannelJukebox getJukeboxChannel(ChannelHelper helper) {
        return new ChannelJukebox(helper,SPECIAL_CHANNELS.getTable("jukebox"));
    }
    
    public static ResourceLocationAPI<?> getLogoTexture() {
        return MTRef.res("textures/logo.png");
    }
    
    public static ChannelPreview getPreviewChannel(ChannelHelper helper) {
        return new ChannelPreview(helper,SPECIAL_CHANNELS.getTable("preview"));
    }

    public static @Nullable TextStyleAPI<?> getStyleAPI() {
        TextHelperAPI<?> api = TILRef.getCommonSubAPI(CommonAPI::getTextHelper);
        return Objects.nonNull(api) ? api.getStyle() : null;
    }

    @SuppressWarnings("unchecked")
    public static <S> TextAPI<?> getStyledLiteral(String text, Function<TextStyleAPI<S>,S> ... styleFuncs) {
        return getStyledText((TextStringAPI<S>)TextHelper.getLiteral(text),styleFuncs);
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <S> TextAPI<?> getStyledText(TextAPI<S> text, Function<TextStyleAPI<S>,S>... styleFuncs) {
        TextStyleAPI<S> styler = (TextStyleAPI<S>)getStyleAPI();
        if(Objects.nonNull(styler))
            for(Function<TextStyleAPI<S>,S> styleFunc : styleFuncs)
                text = text.withStyle(styleFunc.apply(styler));
        return text;
    }

    @SuppressWarnings("unchecked")
    public static <S> TextAPI<?> getStyledTranslated(
            String category, String extra, @Nullable Object[] args, Function<TextStyleAPI<S>,S> ... styleFuncs) {
        if(Objects.isNull(args)) args = new Object[]{};
        return getStyledText((TextTranslationAPI<S>)getTranslated(category,extra,args),styleFuncs);
    }

    public static TextTranslationAPI<?> getTranslated(String category, String extra, Object ... args) {
        return TextHelper.getTranslated(category+"."+MTRef.MODID+"."+extra,args);
    }
    
    public static boolean isFocused() {
        MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
        return Objects.isNull(mc) || mc.isLoading()|| mc.isDisplayFocused();
    }
    
    public static boolean isUnpaused() {
        MinecraftAPI mc = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
        return Objects.isNull(mc) || mc.isLoading() || !mc.isPaused();
    }
}