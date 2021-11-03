package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/"+MusicTriggers.MODID)
public class config {

    @Comment("Universal Delay - Amount of time (in ticks) between songs after a song ends")
    public static int universalDelay = 0;

    @Comment("Main Menu")
    public static Menu menu = new Menu(new String[] {});

    public static class Menu {
        @Comment("songs")
        public String[] menuSongs;

        public Menu(final String[] menuSongs) {
            this.menuSongs = menuSongs;
        }
    }

    @Comment("Generic")
    public static Generic generic = new Generic(0,new String[] {});

    public static class Generic {
        @Comment("Fade Time [in ticks, default: 0]")
        public int genericFade;
        @Comment("songs")
        public String[] genericSongs;

        public Generic(final int genericFade, final String[] genericSongs) {
            this.genericFade = genericFade;
            this.genericSongs = genericSongs;
        }
    }

    @Comment("Zones")
    public static Zones zones = new Zones(10000,0,new String[] {});

    public static class Zones {
        @Comment("Default Priority [min: -99, max: 2147483647 default: 10000]")
        public int zonesPriority;
        @Comment("Default Fade Time [in ticks, default: 0]")
        public int zonesFade;
        @Comment("Songs per zone\nFormat[min x,min y,min z,max x, max y,max z,songname,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]]\n" +
                "Example: 0,0,0,100,100,100,home3,33981,200")
        public String[] zonesSongs;

        public Zones(final int zonesPriority, final int zonesFade, final String[] zonesSongs) {
            this.zonesPriority = zonesPriority;
            this.zonesFade = zonesFade;
            this.zonesSongs = zonesSongs;
        }
    }

    @Comment("Day")
    public static Day day = new Day(1000,0,new String[] {});

    public static class Day {
        @Comment("Priority [min: -99, max: 2147483647 default: 1000]")
        public int dayPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int dayFade;
        @Comment("songs")
        public String[] daySongs;

        public Day(final int dayPriority, final int dayFade, final String[] daySongs) {
            this.dayPriority = dayPriority;
            this.dayFade = dayFade;
            this.daySongs = daySongs;
        }
    }

    @Comment("Night")
    public static Night night = new Night(900,new String[] {});

    public static class Night {
        @Comment("Priority [min: -99, max: 2147483647 default: 900]")
        public int nightPriority;
        @Comment("songs - There is a new format now!\n" +
                "[song name,moon phase,(Optional)fade time [in ticks, default: 0]]\n" +
                "Moon Phases: 1 - Full Moon, 2 - Waning Gibbous, 3 - Third Quarter, 4 - Waning Crescent\n" +
                "5 - New Moon, 6 - Waxing Crescent, 7 - First Quarter, 8 - Waxing Gibbous\n" +
                "You can put 0 to ignore moon phase, or put multiple numbers for a song to be active during multiple phases\n" +
                "Example 1: [nighttime,1] - This will only play during a full moon\n" +
                "Example 2: [nighttime,2,3,4,6,7,8] - This will play every night except for full moons and new moons\n" +
                "Example 3: [nighttime,0] - This will play whenever it is nighttime, just like the old version of this trigger\n" +
                "Note - If the fade is not the last number it will not work properly")
        public String[] nightSongs;

        public Night(final int nightPriority, final String[] nightSongs) {
            this.nightPriority = nightPriority;
            this.nightSongs = nightSongs;
        }
    }

    @Comment("Sunrise")
    public static Sunrise sunrise = new Sunrise(1111,0,new String[] {});

    public static class Sunrise {
        @Comment("Priority [min: -99, max: 2147483647 default: 1111]")
        public int sunrisePriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int sunriseFade;
        @Comment("songs")
        public String[] sunriseSongs;

        public Sunrise(final int sunrisePriority, final int sunriseFade, final String[] sunriseSongs) {
            this.sunrisePriority = sunrisePriority;
            this.sunriseFade = sunriseFade;
            this.sunriseSongs = sunriseSongs;
        }
    }

    @Comment("Sunset")
    public static Sunset sunset = new Sunset(1111,0,new String[] {});

    public static class Sunset {
        @Comment("Priority [min: -99, max: 2147483647 default: 1111]")
        public int sunsetPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int sunsetFade;
        @Comment("songs")
        public String[] sunsetSongs;

        public Sunset(final int sunsetPriority, final int sunsetFade, final String[] sunsetSongs) {
            this.sunsetPriority = sunsetPriority;
            this.sunsetFade = sunsetFade;
            this.sunsetSongs = sunsetSongs;
        }
    }

    @Comment("Light Level")
    public static Light light = new Light(500,new String[] {});

    public static class Light {
        @Comment("Priority [min: -99, max: 2147483647 default: 500]")
        public int lightPriority;
        @Comment("Songs - [Format: \"SongName,Identifier(string name to tie a group of songs to a pool),Light Level(maximum light level),(Optional)Sky Light[default: true],(Optional)Light Time[default: 20],(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: Sky light will let you choose whether or not to take that into account\n" +
                "Additional Note: The light time is how long it will take before the trigger is deactivated after the conditions are no longer met" +
                "Example: spookydark,5,3,false,20,9945,50\n" +
                "Tip - Setting sky light to false would work best when combining it with the night trigger")
        public String[] lightSongs;

        public Light(final int lightPriority,final String[] lightSongs) {
            this.lightPriority = lightPriority;
            this.lightSongs = lightSongs;
        }
    }

    @Comment("Underground - underground with no sky visible")
    public static Underground underground = new Underground(1500,0,55,new String[] {});

    public static class Underground {
        @Comment("Priority [min: -99, max: 2147483647 default: 1500]")
        public int undergroundPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int undergroundFade;
        @Comment("The Y level that is considered underground [default: 55]")
        public int undergroundLevel;
        @Comment("songs")
        public String[] undergroundSongs;

        public Underground(final int undergroundPriority, final int undergroundFade, final int undergroundLevel, final String[] undergroundSongs) {
            this.undergroundPriority = undergroundPriority;
            this.undergroundFade = undergroundFade;
            this.undergroundLevel = undergroundLevel;
            this.undergroundSongs = undergroundSongs;
        }
    }

    @Comment("Deep Under - deep below the surface with no sky visible")
    public static DeepUnder deepUnder = new DeepUnder(2000,0,20,new String[] {});

    public static class DeepUnder {
        @Comment("Priority [min: -99, max: 2147483647 default: 2000]")
        public int deepUnderPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int deepUnderFade;
        @Comment("The Y level that is considered deep underground [default: 20]")
        public int deepUnderLevel;
        @Comment("songs")
        public String[] deepUnderSongs;

        public DeepUnder(final int deepUnderPriority, final int deepUnderFade, final int deepUnderLevel, final String[] deepUnderSongs) {
            this.deepUnderPriority = deepUnderPriority;
            this.deepUnderFade = deepUnderFade;
            this.deepUnderLevel = deepUnderLevel;
            this.deepUnderSongs = deepUnderSongs;
        }
    }

    @Comment("Raining")
    public static Raining raining = new Raining(1300,0,new String[] {});

    public static class Raining {
        @Comment("Priority [min: -99, max: 2147483647 default: 1300]")
        public int rainingPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int rainingFade;
        @Comment("songs")
        public String[] rainingSongs;

        public Raining(final int rainingPriority, final int rainingFade, final String[] rainingSongs) {
            this.rainingPriority = rainingPriority;
            this.rainingFade = rainingFade;
            this.rainingSongs = rainingSongs;
        }
    }

    @Comment("Storming")
    public static Storming storming = new Storming(1350,0,new String[] {});

    public static class Storming {
        @Comment("Priority [min: -99, max: 2147483647 default: 1350]")
        public int stormingPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int stormingFade;
        @Comment("songs")
        public String[] stormingSongs;

        public Storming(final int stormingPriority, final int stormingFade, final String[] stormingSongs) {
            this.stormingPriority = stormingPriority;
            this.stormingFade = stormingFade;
            this.stormingSongs = stormingSongs;
        }
    }

    @Comment("Snowing")
    public static Snowing snowing = new Snowing(1333,0,new String[] {});

    public static class Snowing {
        @Comment("Priority [min: -99, max: 2147483647 default: 1333]")
        public int snowingPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int snowingFade;
        @Comment("songs")
        public String[] snowingSongs;

        public Snowing(final int snowingPriority, final int snowingFade, final String[] snowingSongs) {
            this.snowingPriority = snowingPriority;
            this.snowingFade = snowingFade;
            this.snowingSongs = snowingSongs;
        }
    }

    @Comment("Low HP")
    public static LowHP lowHP = new LowHP(3000,0,0.3, new String[] {});

    public static class LowHP {
        @Comment("Priority [min: -99, max: 2147483647 default: 3000]")
        public int lowHPPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int lowHPFade;
        @Comment("HP decimal percentage to activate [min: 0, max: 1, default:0.3]")
        public double lowHPLevel;
        @Comment("songs")
        public String[] lowHPSongs;

        public LowHP(final int lowHPPriority, final int lowHPFade, final double lowHPLevel, final String[] lowHPSongs) {
            this.lowHPPriority = lowHPPriority;
            this.lowHPFade = lowHPFade;
            this.lowHPLevel = lowHPLevel;
            this.lowHPSongs = lowHPSongs;
        }
    }

    @Comment("Dead")
    public static Dead dead = new Dead(10000,0,new String[] {});

    public static class Dead {
        @Comment("Priority [min: -99, max: 2147483647 default: 10000]")
        public int deadPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int deadFade;
        @Comment("songs")
        public String[] deadSongs;

        public Dead(final int deadPriority, final int deadFade, final String[] deadSongs) {
            this.deadPriority = deadPriority;
            this.deadFade = deadFade;
            this.deadSongs = deadSongs;
        }
    }

    @Comment("Void")
    public static InVoid inVoid = new InVoid(7777,0,0,new String[] {});

    public static class InVoid {
        @Comment("Priority [min: -99, max: 2147483647 default: 7777]")
        public int inVoidPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int inVoidFade;
        @Comment("Below what y level would consider to be actually in the void? [default: 0]")
        public int inVoidLevel;
        @Comment("songs")
        public String[] inVoidSongs;

        public InVoid(final int inVoidPriority, final int inVoidFade, final int inVoidLevel, final String[] inVoidSongs) {
            this.inVoidPriority = inVoidPriority;
            this.inVoidFade = inVoidFade;
            this.inVoidLevel = inVoidLevel;
            this.inVoidSongs = inVoidSongs;
        }
    }

    @Comment("Spectator")
    public static Spectator spectator = new Spectator(5000,0,new String[] {});

    public static class Spectator {
        @Comment("Priority [min: -99, max: 2147483647 default: 5000]")
        public int spectatorPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int spectatorFade;
        @Comment("songs")
        public String[] spectatorSongs;

        public Spectator(final int spectatorPriority, final int spectatorFade, final String[] spectatorSongs) {
            this.spectatorPriority = spectatorPriority;
            this.spectatorFade = spectatorFade;
            this.spectatorSongs = spectatorSongs;
        }
    }

    @Comment("Creative")
    public static Creative creative = new Creative(5000,0,new String[] {});

    public static class Creative {
        @Comment("Priority [min: -99, max: 2147483647 default: 5000]")
        public int creativePriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int creativeFade;
        @Comment("songs")
        public String[] creativeSongs;

        public Creative(final int creativePriority, final int creativeFade, final String[] creativeSongs) {
            this.creativePriority = creativePriority;
            this.creativeFade = creativeFade;
            this.creativeSongs = creativeSongs;
        }
    }

    @Comment("Riding")
    public static Riding riding = new Riding(2222,0,new String[] {});

    public static class Riding {
        @Comment("Priority [min: -99, max: 2147483647 default: 2222]")
        public int ridingPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int ridingFade;
        @Comment("songs")
        public String[] ridingSongs;

        public Riding(final int ridingPriority, final int ridingFade, final String[] ridingSongs) {
            this.ridingPriority = ridingPriority;
            this.ridingFade = ridingFade;
            this.ridingSongs = ridingSongs;
        }
    }

    @Comment("Pet")
    public static Pet pet = new Pet(1200,0,new String[] {});

    public static class Pet {
        @Comment("Priority [min: -99, max: 2147483647 default: 1200]")
        public int petPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int petFade;
        @Comment("songs")
        public String[] petSongs;

        public Pet(final int petPriority, final int petFade, final String[] petSongs) {
            this.petPriority = petPriority;
            this.petFade = petFade;
            this.petSongs = petSongs;
        }
    }

    @Comment("High")
    public static High high = new High(1200,0,150,new String[] {});

    public static class High {
        @Comment("Priority [min: -99, max: 2147483647 default: 1200]")
        public int highPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int highFade;
        @Comment("Minimum Y level to activate [default: 150]")
        public int highLevel;
        @Comment("songs")
        public String[] highSongs;

        public High(final int highPriority, final int highFade, final int highLevel, final String[] highSongs) {
            this.highPriority = highPriority;
            this.highFade = highFade;
            this.highLevel = highLevel;
            this.highSongs = highSongs;
        }
    }

    @Comment("Underwater")
    public static Underwater underwater = new Underwater(1999,0,new String[] {});

    public static class Underwater {
        @Comment("Priority [min: -99, max: 2147483647 default: 1999]")
        public int underwaterPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int underwaterFade;
        @Comment("songs")
        public String[] underwaterSongs;

        public Underwater(final int underwaterPriority, final int underwaterFade, final String[] underwaterSongs) {
            this.underwaterPriority = underwaterPriority;
            this.underwaterFade = underwaterFade;
            this.underwaterSongs = underwaterSongs;
        }
    }

    @Comment("Dimension")
    public static Dimension dimension = new Dimension(1150,new String[] {});

    public static class Dimension {
        @Comment("General Priority [min: -99, max: 2147483647 default: 1150]\nNote: Priorities specified for individual dimensions will override this")
        public int dimensionPriority;
        @Comment("Songs Per Dimension [Format: DimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\nNote: You only have to set the priority per dimension ID for 1 song\nExample: -1,(songname),11111")
        public String[] dimensionSongs;

        public Dimension(final int dimensionPriority, final String[] dimensionSongs) {
            this.dimensionPriority = dimensionPriority;
            this.dimensionSongs = dimensionSongs;
        }
    }

    @Comment("Biome")
    public static Biome biome = new Biome(1160,new String[] {});

    public static class Biome {
        @Comment("General Priority [min: -99, max: 2147483647 default: 1160]\nNote: Priorities specified for individual biomes will override this")
        public int biomePriority;
        @Comment("Songs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0],(Optional)Biome Time[default: 20]\"]\n" +
                "Note: You only have to set the priority per biome name for 1 song\n" +
                "Example: minecraft:swampland,(songname),11111\n" +
                "Additional Note: You can also specify multiple biomes at once through regexing! You can use this feature for both mod ids and biome names\n" +
                "Example 2: minecraft,(songname),11111 (all minecraft biomes will have (songname))\n" +
                "Example 3: forest,(songname),11111 (all biomes with \"forest\" in the name will have (songname))\n" +
                "Final Note: The biome time will allow the trigger to persist after leaving the specified biome for that amount of time\n" +
                "Full Scale Example: swamp,(songname),11111,50,30")
        public String[] biomeSongs;

        public Biome(final int biomePriority, final String[] biomeSongs) {
            this.biomePriority = biomePriority;
            this.biomeSongs = biomeSongs;
        }
    }

    @Comment("Structure")
    public static Structure structure = new Structure(3333,new String[] {});

    public static class Structure {
        @Comment("General Priority [min: -99, max: 2147483647 default: 3333]\nNote: Priorities specified for individual structures will override this")
        public int structurePriority;
        @Comment("Songs Per Structure [Format: \"StructureName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\nNote: You only have to set the priority per structure name for 1 song\nExample: Fortress,(songname),11111")
        public String[] structureSongs;

        public Structure(final int structurePriority, final String[] structureSongs) {
            this.structurePriority = structurePriority;
            this.structureSongs = structureSongs;
        }
    }

    @Comment("Mob (This works for both bosses and hordes!)")
    public static Mob mob = new Mob(3500,new String[] {});

    public static class Mob {
        @Comment("General Priority [min: -99, max: 2147483647 default: 3500]\nNote: Priorities specified for individual mobs will override this")
        public int mobPriority;
        @Comment("Songs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0],(Optional)Targetting[default: false],(Optional)Horde Targetting percentage[default: 100], (Optional)Health[default: 100],(Optional)Horde Health Percentage[default: 100],(Optional)Battle Time[in ticks, default: 0],(Optional)Trigger Victory[default: false],(Optional)Victory ID[min:0, max:2147483647, default: 0](Optional)Infernal[only works with the mod infernal mobs active]\"]\n" +
                "Note: You only have to set the priority per mob name for 1 song\n" +
                "Additional Note: Putting high numbers (over 100) for the mob range may cause lag! The higher it is, the more noticable that lag will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon\n" +
                "Additional Note: Targetting requires the mob(s) to be looking at you while horde targetting percentage is the total percentage of the number of mobs you set that have to be looking at you\n"+
                "Additional Note: Health requires the mob(s) to be below the set percentage threshold of health while horde health percentage is the total percentage of the number of mobs you set that have to be below the set percentage threshold of health\n"+
                "Additional Note: Battle time is how long the trigger will persist after the conditions are no longer met. Due to possible conflicts it may to better to leave this at 0\n"+
                "Additional Note: The victory trigger is special in that it can only activated after escaping the set trigger. The ID exists so there can multiple different victory scenarios\n"+
                "Final Note: The infernal trigger goes of of the mod name, which can be obtained via the debug info set by the debug config. Number of mobs will not affect this\n"+
                "Example: Zombie,8,(songname),16,11111\n" +
                "Full-Scale example: Skeleton,4,123486,50,true,50,80,25,0,Withering\n"+
                "Special case - If you put \"MOB\" as the mob ID, it will default to any hostile mob")
        public String[] mobSongs;

        public Mob(final int mobPriority, final String[] mobSongs) {
            this.mobPriority = mobPriority;
            this.mobSongs = mobSongs;
        }
    }

    @Comment("Effect (Trigger based on potion effects)")
    public static Effect effect = new Effect(500,new String[] {});

    public static class Effect {
        @Comment("Priority [min: -99, max: 2147483647 default: 500]")
        public int effectPriority;
        @Comment("Songs Per Effect [Format: \"EffectName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note: You only have to set the priority per effect name for 1 song\n" +
                "Example: effect.regeneration,(songname),11111")
        public String[] effectSongs;

        public Effect(final int effectPriority, final String[] effectSongs) {
            this.effectPriority = effectPriority;
            this.effectSongs = effectSongs;
        }
    }

    @Comment("PVP")
    public static PVP pvp = new PVP(20000,0,16,200,false,0,new String[] {});

    public static class PVP {
        @Comment("Priority [min: -99, max: 2147483647 default: 20000]")
        public int pvpPriority;
        @Comment("Fade Time:[in ticks, default: 0]")
        public int pvpFade;
        @Comment("Detection Range[default: 16]")
        public int pvpRange;
        @Comment("Battle Time[in ticks, default: 200]")
        public int pvpTime;
        @Comment("Victory - whether to activate the victory trigger [default: false]")
        public boolean pvpVictory;
        @Comment("Victory ID - ID of the victory to activate [default: 0]")
        public int pvpVictoryID;
        @Comment("songs")
        public String[] pvpSongs;

        public PVP(final int pvpPriority, final int pvpFade, final int pvpRange, final int pvpTime, final boolean pvpVictory, final int pvpVictoryID, final String[] pvpSongs) {
            this.pvpPriority = pvpPriority;
            this.pvpFade = pvpFade;
            this.pvpRange = pvpRange;
            this.pvpTime = pvpTime;
            this.pvpSongs = pvpSongs;
            this.pvpVictory = pvpVictory;
            this.pvpVictoryID = pvpVictoryID;
        }
    }

    @Comment("Victory - This can only be called after the pvp or mob trigger")
    public static Victory victory = new Victory(20000,0,16,200,new String[] {});

    public static class Victory {
        @Comment("Priority [min: -99, max: 2147483647 default: 20000]")
        public int victoryPriority;
        @Comment("Songs - [Format: \"SongName,Victory ID,(Optional)Victory Time[default: 200],(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\n" +
                "Note - The victory time is how long the victory trigger will last for\n" +
                "Additional Note: Dying will cut the trigger short\n" +
                "Example: enderdragonwin,11,300,9999999,20")
        public String[] victorySongs;

        public Victory(final int victoryPriority, final int victoryFade, final int victoryRange, final int victoryTime, final String[] victorySongs) {
            this.victoryPriority = victoryPriority;
            this.victorySongs = victorySongs;
        }
    }

    @Comment("Gamestages (Only fires if the mod Game Stages is active)")
    public static Gamestage gamestage = new Gamestage(500,new String[] {});

    public static class Gamestage {
        @Comment("General Priority [min: -99, max: 2147483647 default: 500]\nNote: Priorities specified for individual gamestages will override this")
        public int gamestagePriority;
        @Comment("Songs Per Gamestage [Format: \"StageName,whitelist,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\nNote: You only have to set the priority per gamestage name for 1 song\nExample: StageOne,true,(songname),11111 - This will play when the player has the stage. If it were false it would play whenever the player does not have it.")
        public String[] gamestageSongs;

        public Gamestage(final int gamestagePriority, final String[] gamestageSongs) {
            this.gamestagePriority = gamestagePriority;
            this.gamestageSongs = gamestageSongs;
        }
    }

    @Comment("Blood Moon (Only fires if the mod Blood Moon or nyx is active)")
    public static BloodMoon bloodmoon = new BloodMoon(1200,0,new String[] {});

    public static class BloodMoon {
        @Comment("Priority [min: -99, max: 2147483647 default: 1200]")
        public int bloodmoonPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int bloodmoonFade;
        @Comment("songs")
        public String[] bloodmoonSongs;

        public BloodMoon(final int bloodmoonPriority, final int bloodmoonFade, final String[] bloodmoonSongs) {
            this.bloodmoonPriority = bloodmoonPriority;
            this.bloodmoonFade = bloodmoonFade;
            this.bloodmoonSongs = bloodmoonSongs;
        }
    }

    @Comment("Harvest Moon (Only fires if the mod nyx is active)")
    public static HarvestMoon harvestmoon = new HarvestMoon(1400,0,new String[] {});

    public static class HarvestMoon {
        @Comment("Priority [min: -99, max: 2147483647 default: 1400]")
        public int harvestmoonPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int harvestmoonFade;
        @Comment("songs")
        public String[] harvestmoonSongs;

        public HarvestMoon(final int harvestmoonPriority, final int harvestmoonFade, final String[] harvestmoonSongs) {
            this.harvestmoonPriority = harvestmoonPriority;
            this.harvestmoonFade = harvestmoonFade;
            this.harvestmoonSongs = harvestmoonSongs;
        }
    }

    @Comment("Falling Stars (Only fires if the mod nyx is active)")
    public static FallingStars fallingstars = new FallingStars(1400,0,new String[] {});

    public static class FallingStars {
        @Comment("Priority [min: -99, max: 2147483647 default: 1400]")
        public int fallingstarsPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int fallingstarsFade;
        @Comment("songs")
        public String[] fallingstarsSongs;

        public FallingStars(final int fallingstarsPriority, final int fallingstarsFade, final String[] fallingstarsSongs) {
            this.fallingstarsPriority = fallingstarsPriority;
            this.fallingstarsFade = fallingstarsFade;
            this.fallingstarsSongs = fallingstarsSongs;
        }
    }

    @Comment("Rain Intensity (Only fires if the mod dynamic surroundings is active)")
    public static RainIntensity rainintensity = new RainIntensity(1349,0,new String[] {});

    public static class RainIntensity {
        @Comment("Priority [min: -99, max: 2147483647 default: 1349]")
        public int rainintensityPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int rainintensityFade;
        @Comment("Songs [Format: songname,Intensity Level (min: 0, max: 100)]\n" +
                "Note - This trigger will play when the rain has a higher intensity than you put in\n" +
                "Example: intenserain,70")
        public String[] rainintensitySongs;

        public RainIntensity(final int rainintensityPriority, final int rainintensityFade, final String[] rainintensitySongs) {
            this.rainintensityPriority = rainintensityPriority;
            this.rainintensityFade = rainintensityFade;
            this.rainintensitySongs = rainintensitySongs;
        }
    }

    @Comment("Tornado (Only fires if the mod weather2 is active)")
    public static Tornado tornado = new Tornado(9999,200,new String[] {});

    public static class Tornado {
        @Comment("Priority [min: -99, max: 2147483647 default: 9999]")
        public int tornadoPriority;
        @Comment("Detection Radius [default: 200]")
        public int tornadoRange;
        @Comment("Songs [Format: songname,Intensity Level (min: 1, max: 5),(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\n" +
                "Example: tornado3,3")
        public String[] tornadoSongs;

        public Tornado(final int tornadoPriority, final int tornadoRange, final String[] tornadoSongs) {
            this.tornadoPriority = tornadoPriority;
            this.tornadoRange = tornadoRange;
            this.tornadoSongs = tornadoSongs;
        }
    }

    @Comment("Hurricane (Only fires if the mod weather2 is active)")
    public static Hurricane hurricane = new Hurricane(9999,0,200,new String[] {});

    public static class Hurricane {
        @Comment("Priority [min: -99, max: 2147483647 default: 9999]")
        public int hurricanePriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int hurricaneFade;
        @Comment("Detection Radius [default: 200]")
        public int hurricaneRange;
        @Comment("songs")
        public String[] hurricaneSongs;

        public Hurricane(final int hurricanePriority, final int hurricaneFade, final int hurricaneRange, final String[] hurricaneSongs) {
            this.hurricanePriority = hurricanePriority;
            this.hurricaneFade = hurricaneFade;
            this.hurricaneRange = hurricaneRange;
            this.hurricaneSongs = hurricaneSongs;
        }
    }

    @Comment("Sandstorm (Only fires if the mod weather2 is active)")
    public static Sandstorm sandstorm = new Sandstorm(9999,0,200,new String[] {});

    public static class Sandstorm {
        @Comment("Priority [min: -99, max: 2147483647 default: 9999]")
        public int sandstormPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int sandstormFade;
        @Comment("Detection Radius [default: 200]")
        public int sandstormRange;
        @Comment("songs")
        public String[] sandstormSongs;

        public Sandstorm(final int sandstormPriority, final int sandstormFade, final int sandstormRange, final String[] sandstormSongs) {
            this.sandstormPriority = sandstormPriority;
            this.sandstormFade = sandstormFade;
            this.sandstormRange = sandstormRange;
            this.sandstormSongs = sandstormSongs;
        }
    }

    @Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MusicTriggers.MODID)) {
                ConfigManager.sync(MusicTriggers.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
