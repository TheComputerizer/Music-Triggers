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
    public static Menu menu = new Menu(0,new String[] {});

    public static class Menu {
        @Comment("Priority [min: -99, max: 2147483647 default: 0]")
        public int menuPriority;
        @Comment("songs")
        public String[] menuSongs;

        public Menu(final int menuPriority, final String[] menuSongs) {
            this.menuPriority = menuPriority;
            this.menuSongs = menuSongs;
        }
    }

    @Comment("Generic")
    public static Generic generic = new Generic(0,new String[] {});

    public static class Generic {
        @Comment("Priority [min: -99, max: 2147483647 default: 0]")
        public int genericPriority;
        @Comment("songs")
        public String[] genericSongs;

        public Generic(final int genericPriority, final String[] genericSongs) {
            this.genericPriority = genericPriority;
            this.genericSongs = genericSongs;
        }
    }

    @Comment("Day")
    public static Day day = new Day(1000,new String[] {});

    public static class Day {
        @Comment("Priority [min: -99, max: 2147483647 default: 1000]")
        public int dayPriority;
        @Comment("songs")
        public String[] daySongs;

        public Day(final int dayPriority, final String[] daySongs) {
            this.dayPriority = dayPriority;
            this.daySongs = daySongs;
        }
    }

    @Comment("Night")
    public static Night night = new Night(900,new String[] {});

    public static class Night {
        @Comment("Priority [min: -99, max: 2147483647 default: 900]")
        public int nightPriority;
        @Comment("songs")
        public String[] nightSongs;

        public Night(final int nightPriority, final String[] nightSongs) {
            this.nightPriority = nightPriority;
            this.nightSongs = nightSongs;
        }
    }

    @Comment("Sunrise")
    public static Sunrise sunrise = new Sunrise(1111,new String[] {});

    public static class Sunrise {
        @Comment("Priority [min: -99, max: 2147483647 default: 1111]")
        public int sunrisePriority;
        @Comment("songs")
        public String[] sunriseSongs;

        public Sunrise(final int sunrisePriority, final String[] sunriseSongs) {
            this.sunrisePriority = sunrisePriority;
            this.sunriseSongs = sunriseSongs;
        }
    }

    @Comment("Sunset")
    public static Sunset sunset = new Sunset(1111,new String[] {});

    public static class Sunset {
        @Comment("Priority [min: -99, max: 2147483647 default: 1111]")
        public int sunsetPriority;
        @Comment("songs")
        public String[] sunsetSongs;

        public Sunset(final int sunsetPriority, final String[] sunsetSongs) {
            this.sunsetPriority = sunsetPriority;
            this.sunsetSongs = sunsetSongs;
        }
    }

    @Comment("Light Level")
    public static Light light = new Light(500,7,new String[] {});

    public static class Light {
        @Comment("Priority [min: -99, max: 2147483647 default: 500]")
        public int lightPriority;
        @Comment("light level - This indicates the maximum light level")
        public int lightLevel;
        @Comment("songs")
        public String[] lightSongs;

        public Light(final int lightPriority, final int lightLevel, final String[] lightSongs) {
            this.lightPriority = lightPriority;
            this.lightLevel = lightLevel;
            this.lightSongs = lightSongs;
        }
    }

    @Comment("Underground - below Y 55 with no sky visible")
    public static Underground underground = new Underground(1500,new String[] {});

    public static class Underground {
        @Comment("Priority [min: -99, max: 2147483647 default: 1500]")
        public int undergroundPriority;
        @Comment("songs")
        public String[] undergroundSongs;

        public Underground(final int undergroundPriority, final String[] undergroundSongs) {
            this.undergroundPriority = undergroundPriority;
            this.undergroundSongs = undergroundSongs;
        }
    }

    @Comment("Deep Under - below Y 20 with no sky visible")
    public static DeepUnder deepUnder = new DeepUnder(2000,new String[] {});

    public static class DeepUnder {
        @Comment("Priority [min: -99, max: 2147483647 default: 2000]")
        public int deepUnderPriority;
        @Comment("songs")
        public String[] deepUnderSongs;

        public DeepUnder(final int deepUnderPriority, final String[] deepUnderSongs) {
            this.deepUnderPriority = deepUnderPriority;
            this.deepUnderSongs = deepUnderSongs;
        }
    }

    @Comment("Raining")
    public static Raining raining = new Raining(1300,new String[] {});

    public static class Raining {
        @Comment("Priority [min: -99, max: 2147483647 default: 1300]")
        public int rainingPriority;
        @Comment("songs")
        public String[] rainingSongs;

        public Raining(final int rainingPriority, final String[] rainingSongs) {
            this.rainingPriority = rainingPriority;
            this.rainingSongs = rainingSongs;
        }
    }

    @Comment("Storming")
    public static Storming storming = new Storming(1350,new String[] {});

    public static class Storming {
        @Comment("Priority [min: -99, max: 2147483647 default: 1350]")
        public int stormingPriority;
        @Comment("songs")
        public String[] stormingSongs;

        public Storming(final int stormingPriority, final String[] stormingSongs) {
            this.stormingPriority = stormingPriority;
            this.stormingSongs = stormingSongs;
        }
    }

    @Comment("Snowing")
    public static Snowing snowing = new Snowing(1333,new String[] {});

    public static class Snowing {
        @Comment("Priority [min: -99, max: 2147483647 default: 1333]")
        public int snowingPriority;
        @Comment("songs")
        public String[] snowingSongs;

        public Snowing(final int snowingPriority, final String[] snowingSongs) {
            this.snowingPriority = snowingPriority;
            this.snowingSongs = snowingSongs;
        }
    }

    @Comment("Low HP")
    public static LowHP lowHP = new LowHP(3000, 0.3, new String[] {});

    public static class LowHP {
        @Comment("Priority [min: -99, max: 2147483647 default: 3000]")
        public int lowHPPriority;
        @Comment("HP decimal percentage to activate [min: 0, max: 1, default:0.3]")
        public double lowHPLevel;
        @Comment("songs")
        public String[] lowHPSongs;

        public LowHP(final int lowHPPriority, final double lowHPLevel, final String[] lowHPSongs) {
            this.lowHPPriority = lowHPPriority;
            this.lowHPLevel = lowHPLevel;
            this.lowHPSongs = lowHPSongs;
        }
    }

    @Comment("Dead")
    public static Dead dead = new Dead(10000,new String[] {});

    public static class Dead {
        @Comment("Priority [min: -99, max: 2147483647 default: 10000]")
        public int deadPriority;
        @Comment("songs")
        public String[] deadSongs;

        public Dead(final int deadPriority, final String[] deadSongs) {
            this.deadPriority = deadPriority;
            this.deadSongs = deadSongs;
        }
    }

    @Comment("Void")
    public static InVoid inVoid = new InVoid(7777,new String[] {});

    public static class InVoid {
        @Comment("Priority [min: -99, max: 2147483647 default: 7777]")
        public int inVoidPriority;
        @Comment("songs")
        public String[] inVoidSongs;

        public InVoid(final int inVoidPriority, final String[] inVoidSongs) {
            this.inVoidPriority = inVoidPriority;
            this.inVoidSongs = inVoidSongs;
        }
    }

    @Comment("Spectator")
    public static Spectator spectator = new Spectator(5000,new String[] {});

    public static class Spectator {
        @Comment("Priority [min: -99, max: 2147483647 default: 5000]")
        public int spectatorPriority;
        @Comment("songs")
        public String[] spectatorSongs;

        public Spectator(final int spectatorPriority, final String[] spectatorSongs) {
            this.spectatorPriority = spectatorPriority;
            this.spectatorSongs = spectatorSongs;
        }
    }

    @Comment("Creative")
    public static Creative creative = new Creative(5000,new String[] {});

    public static class Creative {
        @Comment("Priority [min: -99, max: 2147483647 default: 5000]")
        public int creativePriority;
        @Comment("songs")
        public String[] creativeSongs;

        public Creative(final int creativePriority, final String[] creativeSongs) {
            this.creativePriority = creativePriority;
            this.creativeSongs = creativeSongs;
        }
    }

    @Comment("Riding")
    public static Riding riding = new Riding(2222,new String[] {});

    public static class Riding {
        @Comment("Priority [min: -99, max: 2147483647 default: 2222]")
        public int ridingPriority;
        @Comment("songs")
        public String[] ridingSongs;

        public Riding(final int ridingPriority, final String[] ridingSongs) {
            this.ridingPriority = ridingPriority;
            this.ridingSongs = ridingSongs;
        }
    }

    @Comment("Pet")
    public static Pet pet = new Pet(1200,new String[] {});

    public static class Pet {
        @Comment("Priority [min: -99, max: 2147483647 default: 1200]")
        public int petPriority;
        @Comment("songs")
        public String[] petSongs;

        public Pet(final int petPriority, final String[] petSongs) {
            this.petPriority = petPriority;
            this.petSongs = petSongs;
        }
    }

    @Comment("High")
    public static High high = new High(1200,150,new String[] {});

    public static class High {
        @Comment("Priority [min: -99, max: 2147483647 default: 1200]")
        public int highPriority;
        @Comment("Minimum Y level to activate [default: 150]")
        public int highLevel;
        @Comment("songs")
        public String[] highSongs;

        public High(final int highPriority, final int highLevel, final String[] highSongs) {
            this.highPriority = highPriority;
            this.highLevel = highLevel;
            this.highSongs = highSongs;
        }
    }

    @Comment("Dimension")
    public static Dimension dimension = new Dimension(1150,new String[] {});

    public static class Dimension {
        @Comment("General Priority [min: -99, max: 2147483647 default: 1150]\nNote: Priorities specified for individual dimensions will override this")
        public int dimensionPriority;
        @Comment("Songs Per Dimension [Format: DimensionID,SongName,(Optional)Priority:[min: -99, max: 2147483647 ]]\nNote: You only have to set the priority per dimension ID for 1 song\nExample: -1,(songname),11111")
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
        @Comment("Songs Per Biome [Format: \"BiomeResourceName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ]\"]\nNote: You only have to set the priority per biome name for 1 song\nExample: minecraft:swampland,(songname),11111")
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
        @Comment("Songs Per Structure [Format: \"StructureName,SongName,(Optional)Priority:[min: -99, max: 2147483647 ]\"]\nNote: You only have to set the priority per structure name for 1 song\nExample: Fortress,(songname),11111")
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
        @Comment("Songs Per Mob [Format: \"MobName,number of mobs,SongName,(Optional)Priority:[min: -99, max: 2147483647 ]\"]\nNote: You only have to set the priority per mob name for 1 song\nExample: Zombie,8,(songname),11111\nSpecial case - If you put \"MOB\" as the mob ID, it will default to any hostile mob")
        public String[] mobSongs;

        public Mob(final int mobPriority, final String[] mobSongs) {
            this.mobPriority = mobPriority;
            this.mobSongs = mobSongs;
        }
    }

    @Mod.EventBusSubscriber(modid = MusicTriggers.MODID)
    private static class EventHandler {

        /**
         * Inject the new values and save to the config file when the config has been changed from the GUI.
         *
         * @param event The event
         */
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MusicTriggers.MODID)) {
                ConfigManager.sync(MusicTriggers.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
