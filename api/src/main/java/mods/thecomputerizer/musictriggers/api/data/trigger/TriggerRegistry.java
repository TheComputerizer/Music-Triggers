package mods.thecomputerizer.musictriggers.api.data.trigger;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.TriggerGeneric;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.TriggerLoading;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.TriggerMenu;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.*;
import mods.thecomputerizer.musictriggers.api.data.trigger.simple.*;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TriggerRegistry {

    private static final Map<String,Class<? extends TriggerAPI>> DEFAULT_TRIGGERS = loadDefaultTriggers();
    private static final Map<String,Class<? extends TriggerAPI>> REGISTERED_TRIGGERS = new HashMap<>(DEFAULT_TRIGGERS);

    public static @Nullable TriggerAPI getTriggerInstance(ChannelAPI channel, String name) {
        Class<? extends TriggerAPI> clazz = REGISTERED_TRIGGERS.get(name);
        if(Objects.nonNull(clazz)) {
            Constructor<?> constructor = ParameterHelper.findConstructor(clazz,ChannelAPI.class);
            if(Objects.nonNull(constructor)) {
                try {
                    return (TriggerAPI)constructor.newInstance(channel);
                } catch(ReflectiveOperationException ex) {
                    channel.logError("Unable to create new instance of trigger class `{}`!",clazz);
                }
            }
        } else channel.logError("Unable to locate trigger class of type `{}`!",name);
        return null;
    }

    private static Map<String,Class<? extends TriggerAPI>> loadDefaultTriggers() {
        Map<String,Class<? extends TriggerAPI>> map = new HashMap<>();
        map.put("acidrain",TriggerAcidRain.class);
        map.put("advancement",TriggerAdvancement.class);
        map.put("adventure",TriggerAdventure.class);
        map.put("biome",TriggerBiome.class);
        map.put("blizzard",TriggerBlizzard.class);
        map.put("blockentity",TriggerBlockEntity.class);
        map.put("bloodmoon",TriggerBloodmoon.class);
        map.put("bluemoon",TriggerBluemoon.class);
        map.put("cloudy",TriggerCloudy.class);
        map.put("command",TriggerCommand.class);
        map.put("creative",TriggerCreative.class);
        map.put("dead",TriggerDead.class);
        map.put("difficulty",TriggerDifficulty.class);
        map.put("dimension",TriggerDimension.class);
        map.put("drowning",TriggerDrowning.class);
        map.put("effect",TriggerEffect.class);
        map.put("elytra",TriggerElytra.class);
        map.put("fallingstars",TriggerFallingStars.class);
        map.put("fishing",TriggerFishing.class);
        map.put("gamestage",TriggerGamestage.class);
        map.put("generic",TriggerGeneric.class);
        map.put("gui",TriggerGUI.class);
        map.put("harvestmoon",TriggerHarvestmoon.class);
        map.put("height",TriggerHeight.class);
        map.put("home",TriggerHome.class);
        map.put("hurricane",TriggerHurricane.class);
        map.put("inventory",TriggerInventory.class);
        map.put("light",TriggerLight.class);
        map.put("lightrain",TriggerLightRain.class);
        map.put("loading",TriggerLoading.class);
        map.put("lowhp",TriggerLowHP.class);
        map.put("menu",TriggerMenu.class);
        map.put("mob",TriggerMob.class);
        map.put("moon",TriggerMoon.class);
        map.put("pet",TriggerPet.class);
        map.put("pvp",TriggerPVP.class);
        map.put("raid",TriggerRaid.class);
        map.put("raining",TriggerRaining.class);
        map.put("rainintensity",TriggerRainIntensity.class);
        map.put("riding",TriggerRiding.class);
        map.put("sandstorm",TriggerSandstorm.class);
        map.put("season",TriggerSeason.class);
        map.put("snowing",TriggerSnowing.class);
        map.put("spectator",TriggerSpectator.class);
        map.put("statistic",TriggerStatistic.class);
        map.put("storming",TriggerStorming.class);
        map.put("structure",TriggerStructure.class);
        map.put("time",TriggerTime.class);
        map.put("tornado",TriggerTornado.class);
        map.put("underwater",TriggerUnderwater.class);
        map.put("victory",TriggerVictory.class);
        map.put("zones",TriggerZones.class);
        return map;
    }

    public static void registerTrigger(String name, Class<? extends TriggerAPI> clazz) {
        registerTrigger(name,clazz,false);
    }

    public static void registerTrigger(String name, Class<? extends TriggerAPI> clazz, boolean overrideDefault) {
        if(REGISTERED_TRIGGERS.containsKey(name)) {
            if(overrideDefault) REGISTERED_TRIGGERS.put(name,clazz);
            else MTRef.logWarn("There is already a trigger with the name `{}` registered to class `{}`! "+
                    "If you know what you are doing and want to override it anyways make sure to call" +
                    "TriggerRegistry#registerTrigger with overrideDefault set to true",name,REGISTERED_TRIGGERS.get(name));
        } else REGISTERED_TRIGGERS.put(name,clazz);
    }
}
