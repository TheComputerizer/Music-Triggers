package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.*;

public class MusicPicker {
    public static MinecraftServer server;
    public static EntityPlayer player;
    public static World world;

    public static HashMap<String, String[]> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();

    public static String[] playThese() {
        server = FMLCommonHandler.instance().getMinecraftServerInstance();
        player = Minecraft.getMinecraft().player;
        if(player!=null && server!=null) {
            world = server.getWorld(player.dimension);
        }
        if(player == null || world == null) {
            return config.menu.menuSongs;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res!=null && !res.isEmpty()) {
            dynamicSongs.clear();
            dynamicPriorities.clear();
            return res.toArray(new String[0]);
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        return config.generic.genericSongs;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if(st==null) {
            return null;
        }
        List<String> playableSongs = new ArrayList<>();
        for(String s: dynamicSongs.get(st)) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.songCombos.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if(s.matches(checkThis)) {
                    if(playableList.containsAll(SoundHandler.songCombos.get(s)) && SoundHandler.songCombos.get(s).size()!=1) {
                        playableSongs.add(s.substring(1));
                    }
                }
            }
        }
        if(playableSongs.isEmpty()) {
            for(String s: dynamicSongs.get(st)) {
                if(!s.startsWith("@")) {
                    playableSongs.add(s);
                }
            }
        }
        if(playableSongs.isEmpty()) {
            List<String> tryAgain = playableEvents();
            tryAgain.remove(st);
            if(tryAgain.isEmpty()) {
                return null;
            }
            playableSongs = comboChecker(priorityHandler(tryAgain));
        }
        return playableSongs;
    }

    public static String priorityHandler(List<String> sta) {
        if(sta==null) {
            return null;
        }
        int highest=-100;
        String trueHighest="";
        for(String list: sta) {
            if(dynamicPriorities.get(list)>highest && !Arrays.asList(dynamicSongs.get(list)).isEmpty()) {
                highest = dynamicPriorities.get(list);
                trueHighest = list;
            }
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

    @SuppressWarnings("rawtypes")
    public static List<String> playableEvents() {
        List<String> events = new ArrayList<>();
        if (world.isDaytime()) {
            events.add("day");
            dynamicSongs.put("day", config.day.daySongs);
            dynamicPriorities.put("day", config.day.dayPriority);
        } else {
            events.add("night");
            dynamicSongs.put("night", config.night.nightSongs);
            dynamicPriorities.put("night", config.night.nightPriority);
        }
        if (world.getWorldTime() < 13000 && world.getWorldTime() >= 12000) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunset.sunsetSongs);
            dynamicPriorities.put("sunset", config.sunset.sunsetPriority);
        } else if (world.getWorldTime() >= 23000 && world.getWorldTime() < 24000) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", config.sunrise.sunriseSongs);
            dynamicPriorities.put("sunrise", config.sunrise.sunrisePriority);
        }
        if (world.getLight(player.getPosition()) <= config.light.lightLevel) {
            events.add("light");
            dynamicSongs.put("light", config.light.lightSongs);
            dynamicPriorities.put("light", config.light.lightPriority);
        }
        if (player.posY < 20 && !world.canSeeSky(player.getPosition())) {
            events.add("deepUnder");
            dynamicSongs.put("deepUnder", config.deepUnder.deepUnderSongs);
            dynamicPriorities.put("deepUnder", config.deepUnder.deepUnderPriority);
        }
        if (player.posY < 55 && !world.canSeeSky(player.getPosition())) {
            events.add("underground");
            dynamicSongs.put("underground", config.underground.undergroundSongs);
            dynamicPriorities.put("underground", config.underground.undergroundPriority);
        }
        if (player.posY < 0) {
            events.add("inVoid");
            dynamicSongs.put("inVoid", config.inVoid.inVoidSongs);
            dynamicPriorities.put("inVoid", config.inVoid.inVoidPriority);
        }
        if (player.posY >= config.high.highLevel) {
            events.add("high");
            dynamicSongs.put("high", config.high.highSongs);
            dynamicPriorities.put("high", config.high.highPriority);
        }
        if (world.isRaining()) {
            events.add("raining");
            dynamicSongs.put("raining", config.raining.rainingSongs);
            dynamicPriorities.put("raining", config.raining.rainingPriority);
            if (world.canSnowAt(player.getPosition(), true)) {
                events.add("snowing");
                dynamicSongs.put("snowing", config.snowing.snowingSongs);
                dynamicPriorities.put("snowing", config.snowing.snowingPriority);
            }
        }
        if (world.isThundering()) {
            events.add("storming");
            dynamicSongs.put("storming", config.storming.stormingSongs);
            dynamicPriorities.put("storming", config.storming.stormingPriority);
        }
        if (player.getHealth() < player.getMaxHealth() * (config.lowHP.lowHPLevel)) {
            events.add("lowHP");
            dynamicSongs.put("lowHP", config.lowHP.lowHPSongs);
            dynamicPriorities.put("lowHP", config.lowHP.lowHPPriority);
        }
        if (player.isDead) {
            events.add("dead");
            dynamicSongs.put("dead", config.dead.deadSongs);
            dynamicPriorities.put("dead", config.dead.deadPriority);
        }
        if (player.isSpectator()) {
            events.add("spectator");
            dynamicSongs.put("spectator", config.spectator.spectatorSongs);
            dynamicPriorities.put("spectator", config.spectator.spectatorPriority);
        }
        if (player.isCreative()) {
            events.add("creative");
            dynamicSongs.put("creative", config.creative.creativeSongs);
            dynamicPriorities.put("creative", config.creative.creativePriority);
        }
        if (player.isRiding()) {
            events.add("riding");
            dynamicSongs.put("riding", config.riding.ridingSongs);
            dynamicPriorities.put("riding", config.riding.ridingPriority);
        }
        for (EntityMob ent : world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16))) {
            if (ent.serializeNBT().getString("Owner").matches(player.getName())) {
                events.add("pet");
                dynamicSongs.put("pet", config.pet.petSongs);
                dynamicPriorities.put("pet", config.pet.petPriority);
                break;
            }
        }
        if (SoundHandler.dimensionSongs.get(player.dimension) != null) {
            events.add("dimension" + player.dimension);
            String[] dimSongsArray = new String[SoundHandler.dimensionSongsString.get(player.dimension).size()];
            dynamicSongs.put("dimension" + player.dimension, SoundHandler.dimensionSongsString.get(player.dimension).toArray(dimSongsArray));
            dynamicPriorities.put("dimension" + player.dimension, SoundHandler.dimensionPriorities.get(player.dimension));
        }
        if (SoundHandler.biomeSongs.get(Objects.requireNonNull(world.getBiome(player.getPosition()).getRegistryName()).toString()) != null && !world.isRemote) {
            String biomeName = Objects.requireNonNull(world.getBiome(player.getPosition()).getRegistryName()).toString();
            events.add(biomeName);
            String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeName).size()];
            dynamicSongs.put(biomeName, SoundHandler.biomeSongsString.get(biomeName).toArray(biomeSongsArray));
            dynamicPriorities.put(biomeName, SoundHandler.biomePriorities.get(biomeName));
        }
        WorldServer serv = server.getWorld(player.dimension);
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
            String structName = ((Map.Entry) stringListEntry).getKey().toString();
            if (serv.getChunkProvider().isInsideStructure(world, structName, player.getPosition())) {
                events.add("structure:" + structName);
                String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
            }
        }
        List<EntityMob> mobList = world.getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(player.posX - 16, player.posY - 8, player.posZ - 16, player.posX + 16, player.posY + 8, player.posZ + 16));
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            if (mobName.matches("MOB")) {
                if (mobList.size() >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                }
            } else {
                int mobCounter = 0;
                for (EntityMob e : mobList) {
                    if (e.getName().matches(mobName)) {
                        mobCounter++;
                    }
                }
                if (mobCounter >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                }
            }
        }
        playableList = events;
        /* This is for debugging
        System.out.print("Initial Events: ");
        for(String ev: events) {
            System.out.print(ev+" ");
        }
        System.out.print("\n");
         */
        return events;
    }
}