package mods.thecomputerizer.musictriggers.client;

import corgitaco.enhancedcelestials.EnhancedCelestialsWorldData;
import corgitaco.enhancedcelestials.LunarContext;
import corgitaco.enhancedcelestials.lunarevent.BloodMoon;
import corgitaco.enhancedcelestials.lunarevent.BlueMoon;
import corgitaco.enhancedcelestials.lunarevent.HarvestMoon;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.musictriggers.common.SoundHandler;
import mods.thecomputerizer.musictriggers.common.eventsCommon;
import mods.thecomputerizer.musictriggers.config;
import mods.thecomputerizer.musictriggers.configDebug;
import net.darkhax.gamestages.GameStageHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static PlayerEntity player;
    public static World world;

    public static HashMap<String, List<String>> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();

    public static int curFade = 0;
    public static boolean shouldChange = false;

    public static List<String> playThese() {
        if (!MusicPlayer.fading) {
            titleCardEvents = new ArrayList<>();
        }
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player != null) {
            world = player.getCommandSenderWorld();
        }
        if (player == null) {
            return config.menuSongs;
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFade = new HashMap<>();
            return res;
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        curFade = config.genericFade;
        return config.genericSongs;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> comboChecker(String st) {
        if (st == null) {
            return null;
        }
        List<String> playableSongs = new ArrayList<>();
        for (String s : dynamicSongs.get(st)) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.songCombos.entrySet()) {
                String checkThis = ((Map.Entry) stringListEntry).getKey().toString();
                if (s.matches(checkThis)) {
                    if (playableList.containsAll(SoundHandler.songCombos.get(s)) && SoundHandler.songCombos.get(s).size() != 1) {
                        playableSongs.add(s.substring(1));
                        if (!titleCardEvents.contains(st)) {
                            titleCardEvents.addAll(SoundHandler.songCombos.get(s));
                        }
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
            for (String s : dynamicSongs.get(st)) {
                if (!s.startsWith("@")) {
                    playableSongs.add(s);
                    if (!titleCardEvents.contains(st)) {
                        titleCardEvents.add(st);
                    }
                }
            }
        }
        if (playableSongs.isEmpty()) {
            List<String> tryAgain = playableList;
            tryAgain.remove(st);
            playableList = tryAgain;
            if (playableList.isEmpty()) {
                return null;
            }
            playableSongs = comboChecker(priorityHandler(playableList));
        }
        return playableSongs;
    }

    public static String priorityHandler(List<String> sta) {
        if (sta == null) {
            return null;
        }
        int highest = -100;
        String trueHighest = "";
        for (String list : sta) {
            if (dynamicPriorities.get(list) > highest && !dynamicSongs.get(list).isEmpty()) {
                highest = dynamicPriorities.get(list);
                trueHighest = list;
            }
        }
        while (dynamicSongs.get(trueHighest) == null) {
            sta.remove(trueHighest);
            if (sta.isEmpty()) {
                return null;
            }
            for (String list : sta) {
                if (dynamicPriorities.get(list) > highest) {
                    highest = dynamicPriorities.get(list);
                    trueHighest = list;
                }
            }
        }
        if (dynamicFade != null && !dynamicFade.isEmpty()) {
            if (dynamicFade.get(trueHighest) != null) {
                curFade = dynamicFade.get(trueHighest);
            } else {
                curFade = 0;
            }
        }
        return trueHighest;
    }

    @SuppressWarnings("rawtypes")
    public static List<String> playableEvents() {
        List<String> events = new ArrayList<>();
        double time = (double) world.dayTime() / 24000.0;
        if (time > 1) {
            time = time - (long) time;
        }
        if (time < 0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", config.daySongs);
            dynamicPriorities.put("day", config.dayPriority);
            dynamicFade.put("day", config.dayFade);
        } else {
            MusicTriggers.logger.info("Current Moon Phase: " + (world.getMoonPhase() + 1));
            if (SoundHandler.nightSongs.get(0) != null) {
                events.add("night" + 0);
                List<String> tempNight = new ArrayList<>();
                if (SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)) != null && !SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)).isEmpty()) {
                    tempNight.addAll(SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)));
                }
                tempNight.addAll(SoundHandler.nightSongsString.get(0));
                dynamicSongs.put("night" + 0, tempNight);
                dynamicPriorities.put("night" + 0, config.nightPriority);
                dynamicFade.put("night" + 0, SoundHandler.nightFade.get(0));
            } else {
                if (SoundHandler.nightSongs.get((world.getMoonPhase() + 1)) != null) {
                    events.add("night" + (world.getMoonPhase() + 1));
                    dynamicSongs.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightSongsString.get((world.getMoonPhase() + 1)));
                    dynamicPriorities.put("night" + (world.getMoonPhase() + 1), config.nightPriority);
                    dynamicFade.put("night" + (world.getMoonPhase() + 1), SoundHandler.nightFade.get((world.getMoonPhase() + 1)));
                }
            }
        }
        if (time < 0.54166666666 && time >= 0.5) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunsetSongs);
            dynamicPriorities.put("sunset", config.sunsetPriority);
            dynamicFade.put("sunset", config.sunsetFade);
        } else if (time >= 0.95833333333 && time < 1) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", config.sunriseSongs);
            dynamicPriorities.put("sunrise", config.sunrisePriority);
            dynamicFade.put("sunrise", config.sunriseFade);
        }
        if (world.getRawBrightness(player.blockPosition().below(), 0) <= config.lightLevel) {
            events.add("light");
            dynamicSongs.put("light", config.lightSongs);
            dynamicPriorities.put("light", config.lightPriority);
            dynamicFade.put("light", config.lightFade);
        }
        if (player.getY() < config.deepUnderLevel && !world.canSeeSky(player.blockPosition().below())) {
            events.add("deepUnder");
            dynamicSongs.put("deepUnder", config.deepUnderSongs);
            dynamicPriorities.put("deepUnder", config.deepUnderPriority);
            dynamicFade.put("deepUnder", config.deepUnderFade);
        }
        if (player.getY() < config.undergroundLevel && !world.canSeeSky(player.blockPosition().below())) {
            events.add("underground");
            dynamicSongs.put("underground", config.undergroundSongs);
            dynamicPriorities.put("underground", config.undergroundPriority);
            dynamicFade.put("underground", config.undergroundFade);
        }
        if (player.getY() < config.inVoidLevel) {
            events.add("inVoid");
            dynamicSongs.put("inVoid", config.inVoidSongs);
            dynamicPriorities.put("inVoid", config.inVoidPriority);
            dynamicFade.put("inVoid", config.inVoidFade);
        }
        if (player.getY() >= config.highLevel) {
            events.add("high");
            dynamicSongs.put("high", config.highSongs);
            dynamicPriorities.put("high", config.highPriority);
            dynamicFade.put("high", config.highFade);
        }
        if (world.isRaining()) {
            events.add("raining");
            dynamicSongs.put("raining", config.rainingSongs);
            dynamicPriorities.put("raining", config.rainingPriority);
            dynamicFade.put("raining", config.rainingFade);
            if (world.getBiome(player.blockPosition()).shouldSnow(world, player.blockPosition())) {
                events.add("snowing");
                dynamicSongs.put("snowing", config.snowingSongs);
                dynamicPriorities.put("snowing", config.snowingPriority);
                dynamicFade.put("snowing", config.snowingFade);
            }
        }
        if (world.isThundering()) {
            events.add("storming");
            dynamicSongs.put("storming", config.stormingSongs);
            dynamicPriorities.put("storming", config.stormingPriority);
            dynamicFade.put("storming", config.stormingFade);
        }
        if (player.getHealth() < player.getMaxHealth() * ((float) config.lowHPLevel / 100F)) {
            events.add("lowHP");
            dynamicSongs.put("lowHP", config.lowHPSongs);
            dynamicPriorities.put("lowHP", config.lowHPPriority);
            dynamicFade.put("lowHP", config.lowHPFade);
        }
        if (player.isDeadOrDying()) {
            events.add("dead");
            dynamicSongs.put("dead", config.deadSongs);
            dynamicPriorities.put("dead", config.deadPriority);
            dynamicFade.put("dead", config.deadFade);
        }
        if (player.isSpectator()) {
            events.add("spectator");
            dynamicSongs.put("spectator", config.spectatorSongs);
            dynamicPriorities.put("spectator", config.spectatorPriority);
            dynamicFade.put("spectator", config.spectatorFade);
        }
        if (player.isCreative()) {
            events.add("creative");
            dynamicSongs.put("creative", config.creativeSongs);
            dynamicPriorities.put("creative", config.creativePriority);
            dynamicFade.put("creative", config.creativeFade);
        }
        if (player.isPassenger()) {
            events.add("riding");
            dynamicSongs.put("riding", config.ridingSongs);
            dynamicPriorities.put("riding", config.ridingPriority);
            dynamicFade.put("riding", config.ridingFade);
        }
        for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16))) {
            if (ent.serializeNBT().getString("Owner").matches(player.getStringUUID())) {
                events.add("pet");
                dynamicSongs.put("pet", config.petSongs);
                dynamicPriorities.put("pet", config.petPriority);
                dynamicFade.put("pet", config.petFade);
                break;
            }
        }
        if (configDebug.DimensionChecker.get() && eventsCommon.isWorldRendered) {
            player.sendMessage(new TranslationTextComponent(player.level.dimension().location().toString()), MusicPicker.player.getUUID());
        }
        if (SoundHandler.dimensionSongs.get(player.level.dimension().location().toString()) != null) {
            events.add("dimension" + player.level.dimension().location());
            String[] dimSongsArray = new String[SoundHandler.dimensionSongsString.get(player.level.dimension().location().toString()).size()];
            dynamicSongs.put("dimension" + player.level.dimension().location(), Arrays.asList(SoundHandler.dimensionSongsString.get(player.level.dimension().location().toString()).toArray(dimSongsArray)));
            dynamicPriorities.put("dimension" + player.level.dimension().location(), SoundHandler.dimensionPriorities.get(player.level.dimension().location().toString()));
            dynamicFade.put("dimension" + player.level.dimension().location(), SoundHandler.dimensionFade.get(player.level.dimension().location().toString()));
        }
        if (configDebug.BiomeChecker.get() && eventsCommon.isWorldRendered) {
            player.sendMessage(new TranslationTextComponent(Objects.requireNonNull(player.getCommandSenderWorld().getBiome(player.blockPosition()).getRegistryName()).toString()), MusicPicker.player.getUUID());
        }
        if(FMLEnvironment.dist == Dist.CLIENT) {
            if (SoundHandler.biomeSongs.get(Objects.requireNonNull(world.getBiome(player.blockPosition()).getRegistryName()).toString()) != null) {
                String biomeName = Objects.requireNonNull(world.getBiome(player.blockPosition()).getRegistryName()).toString();
                events.add(biomeName);
                String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeName).size()];
                dynamicSongs.put(biomeName, Arrays.asList(SoundHandler.biomeSongsString.get(biomeName).toArray(biomeSongsArray)));
                dynamicPriorities.put(biomeName, SoundHandler.biomePriorities.get(biomeName));
                dynamicFade.put(biomeName, SoundHandler.biomeFade.get(biomeName));
            }
        }
        /*
        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
            String structName = ((Map.Entry) stringListEntry).getKey().toString();
            RegistryHandler.network.sendToServer(new packet.packetMessage(structName, player.getPosition(), player.level.dimension().location().toString()), player.getUniqueID()));
            if (fromServer.inStructure.containsKey(structName)) {
                if (fromServer.inStructure.get(structName)) {
                    events.add("structure:" + structName);
                    String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                    dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                    dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                    dynamicFade.put("structure:" + structName, SoundHandler.structureFade.get(structName));
                }
            }
        }

         */

        for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.mobSongsString.entrySet()) {
            String mobName = ((Map.Entry) stringListEntry).getKey().toString();
            double range = SoundHandler.mobRange.get(mobName);
            List<LivingEntity> mobTempList = world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX() - range, player.getY() - (range / 2), player.getZ() - range, player.getX() + range, player.getY() + (range / 2), player.getZ() + range));
            List<LivingEntity> mobList = new ArrayList<>();
            for (LivingEntity e : mobTempList) {
                if (e instanceof MobEntity) {
                    mobList.add(e);
                }
            }
            if (mobName.matches("MOB")) {
                if (mobList.size() >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, Arrays.asList(SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray)));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                }
            } else {
                int mobCounter = 0;
                for (LivingEntity e : mobTempList) {
                    if (e.getName().getString().matches(mobName)) {
                        mobCounter++;
                    }
                }
                if (mobCounter >= SoundHandler.mobNumber.get(mobName)) {
                    events.add(mobName);
                    String[] mobSongsArray = new String[SoundHandler.mobSongsString.get(mobName).size()];
                    dynamicSongs.put(mobName, Arrays.asList(SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray)));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                }
            }
        }
        if (!SoundHandler.zonesSongs.isEmpty()) {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.zonesSongsString.entrySet()) {
                String zoneRange = ((Map.Entry) stringListEntry).getKey().toString();
                String[] broken = SoundHandler.stringBreaker(zoneRange);
                BlockPos bp = player.blockPosition();
                int x1 = Integer.parseInt(broken[0]);
                int y1 = Integer.parseInt(broken[1]);
                int z1 = Integer.parseInt(broken[2]);
                int x2 = Integer.parseInt(broken[3]);
                int y2 = Integer.parseInt(broken[4]);
                int z2 = Integer.parseInt(broken[5]);
                if (bp.getX() > x1 && bp.getX() < x2 && bp.getY() > y1 && bp.getY() < y2 && bp.getZ() > z1 && bp.getZ() < z2) {
                    events.add(zoneRange);
                    String[] zonesSongsArray = new String[SoundHandler.zonesSongsString.get(zoneRange).size()];
                    dynamicSongs.put(zoneRange, Arrays.asList(SoundHandler.zonesSongsString.get(zoneRange).toArray(zonesSongsArray)));
                    dynamicPriorities.put(zoneRange, SoundHandler.zonesPriorities.get(zoneRange));
                    dynamicFade.put(zoneRange, SoundHandler.zonesFade.get(zoneRange));
                }
            }
        }
        List<String> whitelist = stageWhitelistChecker();
        List<String> blacklist = stageBlacklistChecker();
        if (whitelist != null && !whitelist.isEmpty()) {
            events.addAll(whitelist);
        }
        if (blacklist!=null && !blacklist.isEmpty()) {
            events.addAll(blacklist);
        }
        boolean bloodmoon = bloodmoon();
        if (bloodmoon) {
            events.add("bloodmoon");
        }
        boolean harvestmoon = harvestmoon();
        if (harvestmoon) {
            events.add("harvestmoon");
        }
        boolean bluemoon = bluemoon();
        if (bluemoon) {
            events.add("harvestmoon");
        }

        playableList = events;

        if (events.size() >= 1 && configDebug.PlayableEvents.get() && eventsCommon.isWorldRendered) {
            for (String ev : events) {
                player.sendMessage(new TranslationTextComponent(ev), MusicPicker.player.getUUID());
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageWhitelistChecker() {
        if (ModList.get().isLoaded("gamestages")) {
            List<String> events = new ArrayList<>();
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringWhitelist.entrySet()) {
                String stageName = ((Map.Entry) stringListEntry).getKey().toString();
                String temp = stageName;
                if (temp.startsWith("@")) {
                    temp = temp.substring(1);
                }
                if (GameStageHelper.hasStage(player, temp)) {
                    events.add(stageName + "true");
                    String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringWhitelist.get(stageName).size()];
                    dynamicSongs.put(stageName + "true", Arrays.asList(SoundHandler.gamestageSongsStringWhitelist.get(stageName).toArray(gamestageSongsArray)));
                    dynamicPriorities.put(stageName + "true", SoundHandler.gamestagePrioritiesWhitelist.get(stageName));
                    dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeWhitelist.get(stageName));
                }
            }
            return events;
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageBlacklistChecker() {
        if (ModList.get().isLoaded("gamestages")) {
            List<String> events = new ArrayList<>();
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.gamestageSongsStringBlacklist.entrySet()) {
                String stageName = ((Map.Entry) stringListEntry).getKey().toString();
                String temp = stageName;
                if (temp.startsWith("@")) {
                    temp = temp.substring(1);
                }
                if (!GameStageHelper.hasStage(player, temp)) {
                    events.add(stageName + "false");
                    String[] gamestageSongsArray = new String[SoundHandler.gamestageSongsStringBlacklist.get(stageName).size()];
                    dynamicSongs.put(stageName + "false", Arrays.asList(SoundHandler.gamestageSongsStringBlacklist.get(stageName).toArray(gamestageSongsArray)));
                    dynamicPriorities.put(stageName + "false", SoundHandler.gamestagePrioritiesBlacklist.get(stageName));
                    dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeBlacklist.get(stageName));
                }
            }
            return events;
        } else {
            return null;
        }
    }

    private static boolean bloodmoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BloodMoon) {
                dynamicSongs.put("bloodmoon", config.bloodmoonSongs);
                dynamicPriorities.put("bloodmoon", config.bloodmoonPriority);
                dynamicFade.put("bloodmoon", config.bloodmoonFade);
                return true;
            }
        }
        return false;
    }

    private static boolean harvestmoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof HarvestMoon) {
                dynamicSongs.put("bloodmoon", config.harvestmoonSongs);
                dynamicPriorities.put("bloodmoon", config.harvestmoonPriority);
                dynamicFade.put("bloodmoon", config.harvestmoonFade);
                return true;
            }
        }
        return false;
    }

    private static boolean bluemoon() {
        if (ModList.get().isLoaded("enhancedcelestials")) {
            LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
            if (lunarContext != null && lunarContext.getCurrentEvent() instanceof BlueMoon) {
                dynamicSongs.put("harvestmoon", config.bluemoonSongs);
                dynamicPriorities.put("harvestmoon", config.bluemoonPriority);
                dynamicFade.put("harvestmoon", config.bluemoonFade);
                return true;
            }
        }
        return false;
    }
}