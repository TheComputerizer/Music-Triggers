package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.common.CommonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.text.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class MTClient {

    public static @Nullable TextStyleAPI<?> getStyleAPI() {
        TextHelperAPI<?> api = TILRef.getCommonSubAPI("TextHelperAPI",CommonAPI::getTextHelperAPI);
        return Objects.nonNull(api) ? api.getStyleAPI() : null;
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
}