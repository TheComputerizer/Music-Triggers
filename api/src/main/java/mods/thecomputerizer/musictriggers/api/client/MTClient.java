package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelJukebox;
import mods.thecomputerizer.musictriggers.api.client.channel.ChannelPreview;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.text.*;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Toml;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

import static mods.thecomputerizer.musictriggers.api.MTRef.MODID;

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
    
    @SuppressWarnings("unchecked")
    public static <S> @Nullable TextStyleAPI<S> getStyleAPI() {
        TextHelperAPI<?> api = TextHelper.getHelper();
        return Objects.nonNull(api) ? (TextStyleAPI<S>)api.getStyle() : null;
    }
    
    @SafeVarargs
    public static <S> TextAPI<S> getStyledLiteral(String text, Function<TextStyleAPI<S>,S> ... styleFuncs) {
        return getStyledText(TextHelper.getLiteral(text),styleFuncs);
    }

    @SafeVarargs
    public static <S> TextAPI<S> getStyledText(TextAPI<S> text, Function<TextStyleAPI<S>,S>... styleFuncs) {
        TextStyleAPI<S> styler = getStyleAPI();
        if(Objects.nonNull(styler))
            for(Function<TextStyleAPI<S>,S> styleFunc : styleFuncs)
                text = text.withStyle(styleFunc.apply(styler));
        return text;
    }

    @SafeVarargs
    public static <S> TextAPI<?> getStyledTranslated(String category, String extra, @Nullable Object[] args,
            Function<TextStyleAPI<S>,S> ... styleFuncs) {
        if(Objects.isNull(args)) args = new Object[]{};
        return getStyledText(getTranslated(category,extra,args),styleFuncs);
    }
    
    public static <S> TextTranslationAPI<S> getTranslated(String category, String extra, Object ... args) {
        return TextHelper.getTranslated(category+"."+MODID+"."+extra,args);
    }
    
    public static boolean isFocused() {
        return ClientHelper.isLoading() || ClientHelper.isDisplayFocused();
    }
    
    public static boolean isUnpaused() {
        return ClientHelper.isLoading() || !ClientHelper.isPaused();
    }
    
    public static void runNBTCheck() { //TODO Add translation keys for the messages
        EntityAPI<?,?> target = ClientHelper.getTargetEntity();
        if(Objects.isNull(target)) {
            ClientHelper.sendMessage(getStyledLiteral("No targeted entity to get NBT data for",TextStyleAPI::gray));
            return;
        }
        ClientHelper.sendMessage(getStyledLiteral("Target NBT data => \n"+target.getData().toPrettyString(),TextStyleAPI::aqua));
    }
}