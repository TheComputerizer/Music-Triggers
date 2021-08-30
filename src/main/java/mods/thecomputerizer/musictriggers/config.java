package mods.thecomputerizer.musictriggers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = MusicTriggers.MODID, name = "MusicTriggers/"+MusicTriggers.MODID)
public class config {

    @Comment("Main Menu")
    public static Menu menu = new Menu(0,0,new String[] {});

    public static class Menu {
        @Comment("Priority [min: -99, max: 2147483647 default: 0]")
        public int menuPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int menuFade;
        @Comment("songs")
        public String[] menuSongs;

        public Menu(final int menuPriority, final int menuFade, final String[] menuSongs) {
            this.menuPriority = menuPriority;
            this.menuFade = menuFade;
            this.menuSongs = menuSongs;
        }
    }

    @Comment("Generic")
    public static Generic generic = new Generic(0,0,new String[] {});

    public static class Generic {
        @Comment("Priority [min: -99, max: 2147483647 default: 0]")
        public int genericPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int genericFade;
        @Comment("songs")
        public String[] genericSongs;

        public Generic(final int genericPriority, final int genericFade, final String[] genericSongs) {
            this.genericPriority = genericPriority;
            this.genericFade = genericFade;
            this.genericSongs = genericSongs;
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
    public static Night night = new Night(900,0,new String[] {});

    public static class Night {
        @Comment("Priority [min: -99, max: 2147483647 default: 900]")
        public int nightPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int nightFade;
        @Comment("songs")
        public String[] nightSongs;

        public Night(final int nightPriority, final int nightFade, final String[] nightSongs) {
            this.nightPriority = nightPriority;
            this.nightFade = nightFade;
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
    public static Light light = new Light(500,0,7,new String[] {});

    public static class Light {
        @Comment("Priority [min: -99, max: 2147483647 default: 500]")
        public int lightPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int lightFade;
        @Comment("light level - This indicates the maximum light level")
        public int lightLevel;
        @Comment("songs")
        public String[] lightSongs;

        public Light(final int lightPriority, final int lightFade, final int lightLevel, final String[] lightSongs) {
            this.lightPriority = lightPriority;
            this.lightFade = lightFade;
            this.lightLevel = lightLevel;
            this.lightSongs = lightSongs;
        }
    }

    @Comment("Underground - below Y 55 with no sky visible")
    public static Underground underground = new Underground(1500,0,new String[] {});

    public static class Underground {
        @Comment("Priority [min: -99, max: 2147483647 default: 1500]")
        public int undergroundPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int undergroundFade;
        @Comment("songs")
        public String[] undergroundSongs;

        public Underground(final int undergroundPriority, final int undergroundFade, final String[] undergroundSongs) {
            this.undergroundPriority = undergroundPriority;
            this.undergroundFade = undergroundFade;
            this.undergroundSongs = undergroundSongs;
        }
    }

    @Comment("Deep Under - below Y 20 with no sky visible")
    public static DeepUnder deepUnder = new DeepUnder(2000,0,new String[] {});

    public static class DeepUnder {
        @Comment("Priority [min: -99, max: 2147483647 default: 2000]")
        public int deepUnderPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int deepUnderFade;
        @Comment("songs")
        public String[] deepUnderSongs;

        public DeepUnder(final int deepUnderPriority, final int deepUnderFade, final String[] deepUnderSongs) {
            this.deepUnderPriority = deepUnderPriority;
            this.deepUnderFade = deepUnderFade;
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
    public static InVoid inVoid = new InVoid(7777,0,new String[] {});

    public static class InVoid {
        @Comment("Priority [min: -99, max: 2147483647 default: 7777]")
        public int inVoidPriority;
        @Comment("Fade Time [in ticks, default: 0]")
        public int inVoidFade;
        @Comment("songs")
        public String[] inVoidSongs;

        public InVoid(final int inVoidPriority, final int inVoidFade, final String[] inVoidSongs) {
            this.inVoidPriority = inVoidPriority;
            this.inVoidFade = inVoidFade;
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
        @Comment("Songs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ],(Optional)Fade Time:[in ticks, default: 0]\"]\nNote: You only have to set the priority per biome name for 1 song\nExample: minecraft:swampland,(songname),11111")
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
        @Comment("Songs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)detection range[min: 1, max: 1000, default: 16],(Optional)Priority:[min: -99, max: 2147483647],(Optional)Fade Time:[in ticks, default: 0]\"]\nNote: You only have to set the priority per mob name for 1 song\nAdditional Note: Putting high numbers for the mob range will cause lag! The higher it is, the more noticable that will be. Only use higher numbers for a boss that could be far away, like the Ender Dragon\nExample: Zombie,8,(songname),16,11111\nSpecial case - If you put \"MOB\" as the mob ID, it will default to any hostile mob")
        public String[] mobSongs;

        public Mob(final int mobPriority, final String[] mobSongs) {
            this.mobPriority = mobPriority;
            this.mobSongs = mobSongs;
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
