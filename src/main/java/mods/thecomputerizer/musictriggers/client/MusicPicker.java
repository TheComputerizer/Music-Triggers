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

import java.util.*;

public class MusicPicker {
    public static Minecraft mc;
    public static PlayerEntity player;
    public static World world;

    public static HashMap<String, String[]> dynamicSongs = new HashMap<>();
    public static HashMap<String, Integer> dynamicPriorities = new HashMap<>();
    public static HashMap<String, Integer> dynamicFade = new HashMap<>();

    public static List<String> playableList = new ArrayList<>();
    public static List<String> titleCardEvents = new ArrayList<>();

    public static int curFade = 0;
    public static boolean shouldChange = false;

    public static String[] playThese() {
        if (!MusicPlayer.fading) {
            titleCardEvents = new ArrayList<>();
        }
        mc = Minecraft.getInstance();
        player = mc.player;
        if (player != null) {
            world = player.getCommandSenderWorld();
        }
        if (player == null) {
            return config.menuSongs.get();
        }
        List<String> res = comboChecker(priorityHandler(playableEvents()));
        if (res != null && !res.isEmpty()) {
            dynamicSongs = new HashMap<>();
            dynamicPriorities = new HashMap<>();
            dynamicFade = new HashMap<>();
            return res.toArray(new String[0]);
        }
        dynamicSongs = new HashMap<>();
        dynamicPriorities = new HashMap<>();
        dynamicFade = new HashMap<>();
        curFade = config.genericFade.get();
        return config.genericSongs.get();
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
            if (dynamicPriorities.get(list) > highest && !Arrays.asList(dynamicSongs.get(list)).isEmpty()) {
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
        double time = (double) world.getGameTime() / 24000.0;
        if (time > 1) {
            time = time - (long) time;
        }
        if (time < 0.54166666666) {
            events.add("day");
            dynamicSongs.put("day", config.daySongs.get());
            dynamicPriorities.put("day", config.dayPriority.get());
            dynamicFade.put("day", config.dayFade.get());
        } else {
            MusicTriggers.logger.info("Current Moon Phase: "+(world.getMoonPhase()+1));
            if (SoundHandler.nightSongs.get(0)!=null) {
                events.add("night" + 0);
                String[] dimSongsArray;
                if(SoundHandler.nightSongsString.get((world.getMoonPhase()+1))!=null && !SoundHandler.nightSongsString.get((world.getMoonPhase()+1)).isEmpty()) {
                    dimSongsArray = new String[(SoundHandler.nightSongsString.get(0).size()) + (SoundHandler.nightSongsString.get((world.getMoonPhase()+1)).size())];
                }
                else {
                    dimSongsArray = new String[(SoundHandler.nightSongsString.get(0).size())];
                }
                List<String> tempNight = new ArrayList<>();
                if(SoundHandler.nightSongsString.get((world.getMoonPhase()+1))!=null && !SoundHandler.nightSongsString.get((world.getMoonPhase()+1)).isEmpty()) {
                    tempNight.addAll(SoundHandler.nightSongsString.get((world.getMoonPhase()+1)));
                }
                tempNight.addAll(SoundHandler.nightSongsString.get(0));
                for(String ghgh : tempNight) {
                    MusicTriggers.logger.info(ghgh);
                }
                dynamicSongs.put("night" + 0, tempNight.toArray(dimSongsArray));
                dynamicPriorities.put("night" + 0, config.nightPriority.get());
                dynamicFade.put("night" + 0, SoundHandler.nightFade.get(0));
            }
            else {
                if (SoundHandler.nightSongs.get((world.getMoonPhase()+1))!=null) {
                    events.add("night" + (world.getMoonPhase()+1));
                    String[] dimSongsArray = new String[SoundHandler.nightSongsString.get((world.getMoonPhase()+1)).size()];
                    dynamicSongs.put("night" + (world.getMoonPhase()+1), SoundHandler.nightSongsString.get((world.getMoonPhase()+1)).toArray(dimSongsArray));
                    dynamicPriorities.put("night"+ (world.getMoonPhase()+1), config.nightPriority.get());
                    dynamicFade.put("night" + (world.getMoonPhase()+1), SoundHandler.nightFade.get((world.getMoonPhase()+1)));
                }
            }
        }
        if (time < 0.54166666666 && time >= 0.5) {
            events.add("sunset");
            dynamicSongs.put("sunset", config.sunsetSongs.get());
            dynamicPriorities.put("sunset", config.sunsetPriority.get());
            dynamicFade.put("sunset", config.sunsetFade.get());
        } else if (time >= 0.95833333333 && time < 1) {
            events.add("sunrise");
            dynamicSongs.put("sunrise", config.sunriseSongs.get());
            dynamicPriorities.put("sunrise", config.sunrisePriority.get());
            dynamicFade.put("sunrise", config.sunriseFade.get());
        }
        if (world.getRawBrightness(player.blockPosition().below(),0) <= config.lightLevel.get()) {
            events.add("light");
            dynamicSongs.put("light", config.lightSongs.get());
            dynamicPriorities.put("light", config.lightPriority.get());
            dynamicFade.put("light", config.lightFade.get());
        }
        if (player.getY() < config.deepUnderLevel.get() && !world.canSeeSky(player.blockPosition().below())) {
            events.add("deepUnder");
            dynamicSongs.put("deepUnder", config.deepUnderSongs.get());
            dynamicPriorities.put("deepUnder", config.deepUnderPriority.get());
            dynamicFade.put("deepUnder", config.deepUnderFade.get());
        }
        if (player.getY() < config.undergroundLevel.get() && !world.canSeeSky(player.blockPosition().below())) {
            events.add("underground");
            dynamicSongs.put("underground", config.undergroundSongs.get());
            dynamicPriorities.put("underground", config.undergroundPriority.get());
            dynamicFade.put("underground", config.undergroundFade.get());
        }
        if (player.getY() < config.inVoidLevel.get()) {
            events.add("inVoid");
            dynamicSongs.put("inVoid", config.inVoidSongs.get());
            dynamicPriorities.put("inVoid", config.inVoidPriority.get());
            dynamicFade.put("inVoid", config.inVoidFade.get());
        }
        if (player.getY() >= config.highLevel.get()) {
            events.add("high");
            dynamicSongs.put("high", config.highSongs.get());
            dynamicPriorities.put("high", config.highPriority.get());
            dynamicFade.put("high", config.highFade.get());
        }
        if (world.isRaining()) {
            events.add("raining");
            dynamicSongs.put("raining", config.rainingSongs.get());
            dynamicPriorities.put("raining", config.rainingPriority.get());
            dynamicFade.put("raining", config.rainingFade.get());
            if (world.getBiome(player.blockPosition()).shouldSnow(world, player.blockPosition())) {
                events.add("snowing");
                dynamicSongs.put("snowing", config.snowingSongs.get());
                dynamicPriorities.put("snowing", config.snowingPriority.get());
                dynamicFade.put("snowing", config.snowingFade.get());
            }
        }
        if (world.isThundering()) {
            events.add("storming");
            dynamicSongs.put("storming", config.stormingSongs.get());
            dynamicPriorities.put("storming", config.stormingPriority.get());
            dynamicFade.put("storming", config.stormingFade.get());
        }
        if (player.getHealth() < player.getMaxHealth() * (config.lowHPLevel.get())) {
            events.add("lowHP");
            dynamicSongs.put("lowHP", config.lowHPSongs.get());
            dynamicPriorities.put("lowHP", config.lowHPPriority.get());
            dynamicFade.put("lowHP", config.lowHPFade.get());
        }
        if (player.isDeadOrDying()) {
            events.add("dead");
            dynamicSongs.put("dead", config.deadSongs.get());
            dynamicPriorities.put("dead", config.deadPriority.get());
            dynamicFade.put("dead", config.deadFade.get());
        }
        if (player.isSpectator()) {
            events.add("spectator");
            dynamicSongs.put("spectator", config.spectatorSongs.get());
            dynamicPriorities.put("spectator", config.spectatorPriority.get());
            dynamicFade.put("spectator", config.spectatorFade.get());
        }
        if (player.isCreative()) {
            events.add("creative");
            dynamicSongs.put("creative", config.creativeSongs.get());
            dynamicPriorities.put("creative", config.creativePriority.get());
            dynamicFade.put("creative", config.creativeFade.get());
        }
        if (player.getMyRidingOffset()!=0) {
            events.add("riding");
            dynamicSongs.put("riding", config.ridingSongs.get());
            dynamicPriorities.put("riding", config.ridingPriority.get());
            dynamicFade.put("riding", config.ridingFade.get());
        }
        for (LivingEntity ent : world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(player.getX()- 16, player.getY() - 8, player.getZ() - 16, player.getX() + 16, player.getY() + 8, player.getZ() + 16))) {
            if (ent.serializeNBT().getString("Owner").matches(player.getName().getString())) {
                events.add("pet");
                dynamicSongs.put("pet", config.petSongs.get());
                dynamicPriorities.put("pet", config.petPriority.get());
                dynamicFade.put("pet", config.petFade.get());
                break;
            }
        }
        if (configDebug.DimensionChecker.get() && eventsCommon.isWorldRendered) {
            player.sendMessage(new TranslationTextComponent(player.getCommandSenderWorld().dimension().getRegistryName().toString()),MusicPicker.player.getUUID());
        }
        if (SoundHandler.dimensionSongs.get(player.getCommandSenderWorld().dimension().getRegistryName().toString()) != null) {
            events.add("dimension" + player.getCommandSenderWorld().dimension().getRegistryName());
            String[] dimSongsArray = new String[SoundHandler.dimensionSongsString.get(player.getCommandSenderWorld().dimension().getRegistryName().toString()).size()];
            dynamicSongs.put("dimension" + player.getCommandSenderWorld().dimension().getRegistryName(), SoundHandler.dimensionSongsString.get(player.getCommandSenderWorld().dimension().getRegistryName().toString()).toArray(dimSongsArray));
            dynamicPriorities.put("dimension" + player.getCommandSenderWorld().dimension().getRegistryName(), SoundHandler.dimensionPriorities.get(player.getCommandSenderWorld().dimension().getRegistryName().toString()));
            dynamicFade.put("dimension" + player.getCommandSenderWorld().dimension().getRegistryName(), SoundHandler.dimensionFade.get(player.getCommandSenderWorld().dimension().getRegistryName().toString()));
        }
        if (configDebug.BiomeChecker.get() && eventsCommon.isWorldRendered) {
            player.sendMessage(new TranslationTextComponent(Objects.requireNonNull(player.getCommandSenderWorld().getBiome(player.blockPosition()).getRegistryName()).toString()),MusicPicker.player.getUUID());
        }
        if (SoundHandler.biomeSongs.get(Objects.requireNonNull(world.getBiome(player.blockPosition()).getRegistryName()).toString()) != null) {
            String biomeName = Objects.requireNonNull(world.getBiome(player.blockPosition()).getRegistryName()).toString();
            events.add(biomeName);
            String[] biomeSongsArray = new String[SoundHandler.biomeSongsString.get(biomeName).size()];
            dynamicSongs.put(biomeName, SoundHandler.biomeSongsString.get(biomeName).toArray(biomeSongsArray));
            dynamicPriorities.put(biomeName, SoundHandler.biomePriorities.get(biomeName));
            dynamicFade.put(biomeName, SoundHandler.biomeFade.get(biomeName));
        }
        //if (FMLEnvironment.dist == Dist.CLIENT) {
            ServerWorld nWorld = Objects.requireNonNull(player.getServer()).getLevel(player.getCommandSenderWorld().dimension());
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                for (Structure<?> structureFeature : net.minecraftforge.registries.ForgeRegistries.STRUCTURE_FEATURES) {
                    if (structName.matches(structureFeature.getFeatureName())) {
                        assert nWorld != null;
                        if (nWorld.structureFeatureManager().getStructureAt(player.blockPosition(),true,structureFeature)!=null) {
                            events.add("structure:" + structName);
                            String[] structureSongsArray = new String[SoundHandler.structureSongsString.get(structName).size()];
                            dynamicSongs.put("structure:" + structName, SoundHandler.structureSongsString.get(structName).toArray(structureSongsArray));
                            dynamicPriorities.put("structure:" + structName, SoundHandler.structurePriorities.get(structName));
                            dynamicFade.put("structure:" + structName, SoundHandler.structureFade.get(structName));
                        }
                    }
                }
            }
        //}
        /*else {
            for (Map.Entry<String, List<String>> stringListEntry : SoundHandler.structureSongsString.entrySet()) {
                String structName = ((Map.Entry) stringListEntry).getKey().toString();
                RegistryHandler.network.sendToServer(new packet.packetMessage(structName, player.getPosition(), player.getCommandSenderWorld().dimension().getRegistryName().toString()), player.getUniqueID()));
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
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
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
                    dynamicSongs.put(mobName, SoundHandler.mobSongsString.get(mobName).toArray(mobSongsArray));
                    dynamicPriorities.put(mobName, SoundHandler.mobPriorities.get(mobName));
                    dynamicFade.put(mobName, SoundHandler.mobFade.get(mobName));
                }
            }
        }
        if(!SoundHandler.zonesSongs.isEmpty()) {
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
                if(bp.getX()>x1 && bp.getX()<x2 && bp.getY()>y1 && bp.getY()<y2 && bp.getZ()>z1 && bp.getZ()<z2) {
                    events.add(zoneRange);
                    String[] zonesSongsArray = new String[SoundHandler.zonesSongsString.get(zoneRange).size()];
                    dynamicSongs.put(zoneRange, SoundHandler.zonesSongsString.get(zoneRange).toArray(zonesSongsArray));
                    dynamicPriorities.put(zoneRange, SoundHandler.zonesPriorities.get(zoneRange));
                    dynamicFade.put(zoneRange, SoundHandler.zonesFade.get(zoneRange));
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
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean bloodmoon = bloodmoon();
            if (bloodmoon) {
                events.add("bloodmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean harvestmoon = harvestmoon();
            if (harvestmoon) {
                events.add("harvestmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }
        try {
            boolean bluemoon = bluemoon();
            if (bluemoon) {
                events.add("harvestmoon");
            }
        } catch (NoSuchMethodError ignored) {
        }

        playableList = events;

        if (events.size() >= 1 && configDebug.PlayableEvents.get() && eventsCommon.isWorldRendered) {
            for (String ev : events) {
                player.sendMessage(new TranslationTextComponent(ev),MusicPicker.player.getUUID());
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageWhitelistChecker() {
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
                dynamicSongs.put(stageName + "true", SoundHandler.gamestageSongsStringWhitelist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName + "true", SoundHandler.gamestagePrioritiesWhitelist.get(stageName));
                dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeWhitelist.get(stageName));
            }
        }
        return events;
    }

    @SuppressWarnings("rawtypes")
    private static List<String> stageBlacklistChecker() {
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
                dynamicSongs.put(stageName + "false", SoundHandler.gamestageSongsStringBlacklist.get(stageName).toArray(gamestageSongsArray));
                dynamicPriorities.put(stageName + "false", SoundHandler.gamestagePrioritiesBlacklist.get(stageName));
                dynamicFade.put(stageName + "true", SoundHandler.gamestageFadeBlacklist.get(stageName));
            }
        }
        return events;
    }

    private static boolean bloodmoon() {
        LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
        if (lunarContext!=null && lunarContext.getCurrentEvent() instanceof BloodMoon) {
            dynamicSongs.put("bloodmoon", config.bloodmoonSongs.get());
            dynamicPriorities.put("bloodmoon", config.bloodmoonPriority.get());
            dynamicFade.put("bloodmoon", config.bloodmoonFade.get());
            return true;
        }
        return false;
    }

    private static boolean harvestmoon() {
        LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
        if (lunarContext!=null && lunarContext.getCurrentEvent() instanceof HarvestMoon) {
            dynamicSongs.put("bloodmoon", config.harvestmoonSongs.get());
            dynamicPriorities.put("bloodmoon", config.harvestmoonPriority.get());
            dynamicFade.put("bloodmoon", config.harvestmoonFade.get());
            return true;
        }
        return false;
    }

    private static boolean bluemoon() {
        LunarContext lunarContext = ((EnhancedCelestialsWorldData) world).getLunarContext();
        if (lunarContext!=null && lunarContext.getCurrentEvent() instanceof BlueMoon) {
            dynamicSongs.put("harvestmoon", config.bluemoonSongs.get());
            dynamicPriorities.put("harvestmoon", config.bluemoonPriority.get());
            dynamicFade.put("harvestmoon", config.bluemoonFade.get());
            return true;
        }
        return false;
    }
}