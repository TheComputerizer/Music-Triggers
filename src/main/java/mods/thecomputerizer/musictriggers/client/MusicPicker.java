package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc = Minecraft.getMinecraft();
    public static EntityPlayer player = mc.player;
    public static World world = mc.world;

    public static HashMap<String, String[]> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();

    public static String[] playThese() {
        if(player == null || world == null) {
            return config.menu.menuSongs;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res!=null) {
            List<String> finalList = new ArrayList<>();
            for(String r: res) {
                Collections.addAll(finalList, dynamicSongs.get(r));
            }
            dynamicSongs.clear();
            dynamicPriorities.clear();
            return finalList.toArray(new String[0]);
        }
        dynamicSongs.clear();
        dynamicPriorities.clear();
        return config.generic.genericSongs;
    }

    public static List<String> comboChecker(String st) {
        if(st==null) {
            return null;
        }
        List<String> playableEvents = new ArrayList<>();
        playableEvents.add(st);
        for(String these: dynamicSongs.get(st)) {
            if (these.startsWith("-")) {
                these = these.substring(1);
            }
            if (these.startsWith("+")) {
                these = these.substring(1);
                for (String songEvents : playableList) {
                    if (!songEvents.matches(st)) {
                        for (String song : dynamicSongs.get(songEvents)) {
                            if (song.contains(these)) {
                                playableEvents.add(songEvents);
                            }
                        }
                    }
                }
            }
        }
        return playableEvents;
    }

    public static String priorityHandler(List<String> sta) {
        if(sta==null) {
            return null;
        }
        int highest=-100;
        String trueHighest="";
        for(String list: sta) {
            if(dynamicPriorities.get(list)>highest) {
                highest = dynamicPriorities.get(list);
                trueHighest = list;
            }
        }
        if(trueHighest.startsWith("+")) {
            String temp = trueHighest.substring(1);
            if(temp.startsWith("-")) {
                dynamicSongs.put(trueHighest, null);
            }
        }
        if(trueHighest.startsWith("-")) {
            dynamicSongs.put(trueHighest, null);
        }
        while(dynamicSongs.get(trueHighest)==null) {
            sta.remove(trueHighest);
            if(sta.isEmpty()) {
                return null;
            }
            for(String list: sta) {
                if(dynamicPriorities.get(list)>highest) {
                    highest = dynamicPriorities.get(list);
                    trueHighest = list;
                }
            }
        }
        return trueHighest;
    }

    public static List<String> playableEvents() {
        List<String> events = new ArrayList<>();
        if(!world.isRemote) {
            if (world.isDaytime()) {
                events.add("day");
                dynamicSongs.put("day", config.day.daySongs);
                dynamicPriorities.put("day", config.day.dayPriority);
            }
            else {
                events.add("night");
                dynamicSongs.put("night", config.night.nightSongs);
                dynamicPriorities.put("night", config.night.nightPriority);
            }
            if(world.getWorldTime()<13000 && world.getWorldTime()>=12000) {
                events.add("sunset");
                dynamicSongs.put("sunset", config.sunset.sunsetSongs);
                dynamicPriorities.put("sunset", config.sunset.sunsetPriority);
            }
            else if(world.getWorldTime()>=23000) {
                events.add("sunrise");
                dynamicSongs.put("sunrise", config.sunrise.sunriseSongs);
                dynamicPriorities.put("sunrise", config.sunrise.sunrisePriority);
            }
            if (world.getLight(player.getPosition()) <= config.light.lightLevel) {
                events.add("light");
                dynamicSongs.put("light", config.light.lightSongs);
                dynamicPriorities.put("light", config.light.lightPriority);
            }
            if(player.posY<20 && !world.canSeeSky(player.getPosition())) {
                events.add("deepUnder");
                dynamicSongs.put("deepUnder", config.deepUnder.deepUnderSongs);
                dynamicPriorities.put("deepUnder", config.deepUnder.deepUnderPriority);
            }
            if(player.posY<55 && !world.canSeeSky(player.getPosition())) {
                events.add("underground");
                dynamicSongs.put("underground", config.underground.undergroundSongs);
                dynamicPriorities.put("underground", config.underground.undergroundPriority);
            }
            if(player.posY>=config.high.highLevel) {
                events.add("high");
                dynamicSongs.put("high", config.high.highSongs);
                dynamicPriorities.put("high", config.high.highPriority);
            }
            if(world.isRaining()) {
                events.add("raining");
                dynamicSongs.put("raining", config.raining.rainingSongs);
                dynamicPriorities.put("raining", config.raining.rainingPriority);
                if(world.canSnowAt(player.getPosition(),true)) {
                    events.add("snowing");
                    dynamicSongs.put("snowing", config.snowing.snowingSongs);
                    dynamicPriorities.put("snowing", config.snowing.snowingPriority);
                }
            }
            if(world.isThundering()) {
                events.add("storming");
                dynamicSongs.put("storming", config.storming.stormingSongs);
                dynamicPriorities.put("storming", config.storming.stormingPriority);
            }
            if(player.getHealth()<player.getMaxHealth()*(config.lowHP.lowHPLevel)) {
                events.add("lowHP");
                dynamicSongs.put("lowHP", config.lowHP.lowHPSongs);
                dynamicPriorities.put("lowHP", config.lowHP.lowHPPriority);
            }
            if(player.isDead) {
                events.add("dead");
                dynamicSongs.put("dead", config.dead.deadSongs);
                dynamicPriorities.put("dead", config.dead.deadPriority);
            }
            if(player.isSpectator()) {
                events.add("spectator");
                dynamicSongs.put("spectator", config.spectator.spectatorSongs);
                dynamicPriorities.put("spectator", config.spectator.spectatorPriority);
            }
            if(player.isCreative()) {
                events.add("creative");
                dynamicSongs.put("creative", config.creative.creativeSongs);
                dynamicPriorities.put("creative", config.creative.creativePriority);
            }
            if(player.isRiding()) {
                events.add("riding");
                dynamicSongs.put("riding", config.riding.ridingSongs);
                dynamicPriorities.put("riding", config.riding.ridingPriority);
            }
            for(EntityMob ent: world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16))) {
                if (ent.serializeNBT().getString("Owner").matches(player.getName())) {
                    events.add("pet");
                    dynamicSongs.put("pet", config.pet.petSongs);
                    dynamicPriorities.put("pet", config.pet.petPriority);
                    break;
                }
            }
            if(SoundHandler.dimensionSongs.get(player.dimension)!=null) {
                events.add("dimension"+player.dimension);
                String[] dimSongsArray = new String[SoundHandler.dimensionSongsString.get(player.dimension).size()];
                dynamicSongs.put("dimension"+player.dimension, SoundHandler.dimensionSongsString.get(player.dimension).toArray(dimSongsArray));
                dynamicPriorities.put("dimension"+player.dimension, SoundHandler.dimensionPriorities.get(player.dimension));
            }
            if(SoundHandler.biomeSongs.get(Objects.requireNonNull(player.world.getBiome(player.getPosition()).getRegistryName()).toString())!=null && !world.isRemote) {
                String biomeName = Objects.requireNonNull(player.world.getBiome(player.getPosition()).getRegistryName()).toString();
                events.add(biomeName);
                String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeName).size()];
                dynamicSongs.put(biomeName, SoundHandler.biomeSongsString.get(biomeName).toArray(biomeSongsArray));
                dynamicPriorities.put(biomeName, SoundHandler.biomePriorities.get(biomeName));
            }
        }
        playableList = events;
        System.out.print("Initial Events: ");
        for(String ev: events) {
            System.out.print(ev+" ");
        }
        System.out.print("\n");
        return events;
    }
}