package mods.thecomputerizer.musictriggers.client;

import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.config;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Optional;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static EntityPlayer player;
    public static World world;

    public static HashMap<String, String[]> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();

    public static String[] playThese() {
        mc = Minecraft.getMinecraft();
        player = mc.player;
        if(player !=null) {
            world = player.getEntityWorld();
        }
        if(player == null) {
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
            List<String> tryAgain = playableList;
            tryAgain.remove(st);
            playableList = tryAgain;
            if(playableList.isEmpty()) {
                return null;
            }
            playableSongs = comboChecker(priorityHandler(playableList));
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
        double time = (double)world.getWorldTime()/24000.0;
        if(time>1) {
            time = time-(long)time;
        }
        if (time<0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", config.day.daySongs);
            dynamicPriorities.put("day", config.day.dayPriority);
        } else {
            events.add("night");
            dynamicSongs.put("night", config.night.nightSongs);
            dynamicPriorities.put("night", config.night.nightPriority);
        }
        if (time<0.54166666666 && time>=0.5) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunset.sunsetSongs);
            dynamicPriorities.put("sunset", config.sunset.sunsetPriority);
        } else if (time>=0.95833333333 && time<1) {
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
        if(mc.isSingleplayer()) {
            WorldServer nworld = Objects.requireNonNull(mc.getIntegratedServer()).getWorld(player.dimension);
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                if (nworld.getChunkProvider().isInsideStructure(world, structName, player.getPosition())) {
                    events.add("structure:" + structName);
                    String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                    dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                    dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                }
            }
        }
        else {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if(server!=null) {
                System.out.print(server.getName() + "\n");
                UUID uuid = player.getUniqueID();
                WorldServer nworld = server.getWorld(server.getPlayerList().getPlayerByUUID(uuid).dimension);
                for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                    String structName = ((Map.Entry) stringListEntry).getKey().toString();
                    if (nworld.getChunkProvider().isInsideStructure(world, structName, player.getPosition())) {
                        events.add("structure:" + structName);
                        String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                        dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                        dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                    }
                }
            }
        }
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            double range = SoundHandler.mobRange.get(mobName);
            List<EntityLiving> mobTempList = world.getEntitiesWithinAABB(EntityLiving.class, new AxisAlignedBB(player.posX - range, player.posY - (range/2), player.posZ - range, player.posX + range, player.posY + (range/2), player.posZ + range));
            List<EntityLiving> mobList = new ArrayList<>();
            for(EntityLiving e:mobTempList) {
                if(e instanceof EntityMob || e instanceof EntityDragon) {
                    mobList.add(e);
                }
            }
            if (mobName.matches("MOB")) {
                if (mobList.size() >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                }
            } else {
                int mobCounter = 0;
                for (EntityLiving e : mobList) {
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
        try {
            List<String> whitelist = stageWhitelistChecker();
            List<String> blacklist = stageBlacklistChecker();
            if (!whitelist.isEmpty()) {
                events.addAll(whitelist);
            }
            if (!blacklist.isEmpty()) {
                events.addAll(blacklist);
            }
        }
        catch(NoSuchMethodError ignored) {}

        playableList = events;

        /*
        if(events.size()>=1) {
            for (String ev : events) {
                player.sendMessage(new TextComponentString(ev+" "+time));
            }
        }

         */
        return events;
    }
    @SuppressWarnings("rawtypes")
    @Optional.Method(modid="gamestages")
    private static List<String> stageWhitelistChecker() {
        List<String> events = new ArrayList<>();
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringWhitelist.entrySet()) {
            String stageName = ((Map.Entry) stringListEntry).getKey().toString();
            String temp = stageName;
            if(temp.startsWith("@")) {
                temp = temp.substring(1);
            }
            if(GameStageHelper.clientHasStage(player,temp)) {
                events.add(stageName+"true");
                String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringWhitelist.get(stageName).size()];
                dynamicSongs.put(stageName+"true", SoundHandler.gamestageSongsStringWhitelist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName+"true", SoundHandler.gamestagePrioritiesWhitelist.get(stageName));
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    @Optional.Method(modid="gamestages")
    private static List<String> stageBlacklistChecker() {
        List<String> events = new ArrayList<>();
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringBlacklist.entrySet()) {
            String stageName = ((Map.Entry) stringListEntry).getKey().toString();
            String temp = stageName;
            if(temp.startsWith("@")) {
                temp = temp.substring(1);
            }
            if(!GameStageHelper.clientHasStage(player,temp)) {
                events.add(stageName+"false");
                String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringBlacklist.get(stageName).size()];
                dynamicSongs.put(stageName+"false", SoundHandler.gamestageSongsStringBlacklist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName+"false", SoundHandler.gamestagePrioritiesBlacklist.get(stageName));
            }
        }
        return events;
    }
}