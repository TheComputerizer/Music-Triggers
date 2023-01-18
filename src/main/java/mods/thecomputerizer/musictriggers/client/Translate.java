package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.client.data.Trigger;
import mods.thecomputerizer.theimpossiblelibrary.util.client.AssetUtil;
import net.minecraft.client.resources.I18n;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Translate {

    public static String triggerOptionalID(Trigger trigger, boolean includeID) {
        if(trigger.hasID() && includeID) return triggerWithID(trigger.getNameWithID());
        return triggerName(trigger.getName());
    }

    public static String triggerName(String triggerName) {
        return guiGeneric(false,"trigger",triggerName);
    }

    public static String triggerWithID(String triggerIdentifier) {
        String[] split = triggerIdentifier.split("-", 2);
        return guiGeneric(false,"trigger",split[0])+"-"+ split[1];
    }

    public static List<String> triggersOptionalID(List<Trigger> triggers, boolean includeID) {
        return triggers.stream().map(trigger -> triggerOptionalID(trigger,includeID)).collect(Collectors.toList());
    }

    private static String condenseList(List<String> things) {
        StringBuilder builder = new StringBuilder();
        int checkMax = 0;
        for(String element : things) {
            builder.append("[").append(element).append("]");
            checkMax++;
            if(checkMax>=things.size()) builder.append(" ");
        }
        return builder.toString();
    }

    public static String condenseIdentifiers(List<String> ids, String ... stuff) {
        return guiGeneric(false,stuff)+" "+condenseList(ids);
    }

    public static String toggleTriggers(List<String> triggers) {
        return condenseList(triggers.stream().map(Translate::triggerWithID).collect(Collectors.toList()));
    }

    public static String parameter(String ... stuff) {
        String key = guiGeneric(true,stuff);
        return I18n.hasKey(key) ? libHook(key,null) :
        guiGeneric(false,ArrayUtils.remove(stuff,3));
    }

    public static List<String> triggerElementHover(Trigger trigger) {
        if(trigger.getName().matches("menu") || trigger.getName().matches("generic"))
            return new ArrayList<>();
        if(trigger.hasID())
            return Arrays.asList(guiGeneric(false,"trigger","identifier")+ ": "+trigger.getRegID(),
                    guiGeneric(false,"trigger","priority")+ ": "+trigger.getParameterInt("priority"));
        return Collections.singletonList(guiGeneric(false,"trigger","priority")+": "+
                trigger.getParameterInt("priority"));
    }

    public static String triggerElement(Trigger trigger) {
        return guiGeneric(false,"trigger")+"["+triggerOptionalID(trigger,false)+"]";
    }

    /*
        Allows for custom song names to display or uses the registered name if no key is set
    */
    public static String songInstance(String name) {
        return libHook(buildLangKey("audio",name,"name"),name);
    }

    /*
        Does not accept a fallback input.
    */
    public static String selectionTitle(String group, String channel) {
        return guiGeneric(false,"selection","group",group)+" "+channel;
    }

    /*
        Does not accept a fallback input.
    */
    public static List<String> songHover(char loadOrder, List<Trigger> triggers) {
        return Arrays.asList(guiGeneric(false,"selection","song","load")+" "+loadOrder,
                guiGeneric(false,"selection","song","triggers")+" "+
                        condenseList(triggersOptionalID(triggers,true)));
    }

    /*
        Does not accept a fallback input.
    */
    public static List<String> guiNumberedList(int numLines, String ... elements) {
        return IntStream.range(0, numLines).mapToObj(i -> libHook(guiGeneric(true,elements)+(i+1),null))
                .collect(Collectors.toList());
    }

    public static String selectionSong(String song, String ... elements) {
        String key = buildLangKey("gui",elements);
        return I18n.hasKey(key) ? libHook(key,null) : song;
    }

    /*
        Returns the key if asKey is true or otherwise the actual translation. Does not accept a fallback input.
    */
    public static String guiGeneric(boolean asKey, String ... elements) {
        String key = buildLangKey("gui",elements);
        return asKey ? key : libHook(key, null);
    }

    /*
        Builds a lang key from a variable of elements assuming a base of a category and the modid.
    */
    @SuppressWarnings("SameParameterValue")
    private static String buildLangKey(String category, String ... extras) {
        StringBuilder builder = new StringBuilder();
        builder.append(category).append(".").append(Constants.MODID);
        List<String> fixed = Arrays.stream(extras).filter(Objects::nonNull).collect(Collectors.toList());
        if(fixed.isEmpty()) return builder.toString();
        builder.append(".");
        int checkMax = 1;
        for(String extra : fixed) {
            builder.append(extra);
            if(checkMax<fixed.size()) {
                builder.append(".");
                checkMax++;
            }
        }
        return builder.toString();
    }

    /*
        This will assume the name ending has already been handled. The lang key will only get checked for validity if
        the fallback value is nonnull. In that case the fallback is returned if the key is not found.
    */
    private static String libHook(String key, String fallback) {
        if(I18n.hasKey(key) || fallback==null)
            return AssetUtil.customLang(key,false);
        return fallback;
    }
}
