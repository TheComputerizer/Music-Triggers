package mods.thecomputerizer.musictriggers.api.registry;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.shadow.org.joml.Vector3d;
import mods.thecomputerizer.theimpossiblelibrary.api.common.block.BlockStateAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.blockentity.BlockEntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.registry.RegistryHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.util.RandomHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.WorldAPI;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static mods.thecomputerizer.musictriggers.api.registry.MTBlockRegistry.MUSIC_RECORDER;
import static mods.thecomputerizer.musictriggers.api.registry.MTItemRegistry.ENHANCED_MUSIC_DISC;

public class MTBlockEntityRegistry {
    
    public static final BlockEntityAPI<?,?> MUSIC_RECORDER_ENTITY = RegistryHelper.makeBlockEntityBuilder()
            .setRegistryName(MTRef.res("music_recorder_entity"))
            .setValidBlocks(() -> Collections.singleton(MUSIC_RECORDER))
            .setOnTick(entity -> {
                BlockStateAPI<?> state = entity.getState();
                if(Objects.nonNull(state)) {
                    boolean recording = state.getPropertyBool("recording");
                    boolean recordingSpecial = state.getPropertyBool("recording_special");
                    if((recording || recordingSpecial) && RandomHelper.randomDouble(1d)<=0.005d) {
                        List<? extends PlayerAPI<?,?>> players = ChannelHelper.getPlayers(false);
                        if(players.isEmpty()) {
                            MTRef.logError("Unable to record music when there are no players online!");
                            return;
                        }
                        WorldAPI<?> world = entity.getWorld();
                        BlockPosAPI<?> pos = entity.getPos();
                        if(spawnRecordedDisc(world,pos,recordingSpecial)) {
                            MTRef.logInfo("Successfully spawned {}music disc",recordingSpecial ? "special " : "");
                            world.setState(pos,state.withProperty("recording",false)
                                    .withProperty("recording_special",false));
                        }
                    }
                }
            }).build();
    
    public static BlockEntityAPI<?,?> createRecorderEntity(WorldAPI<?> ignored1, BlockStateAPI<?> ignored2) {
        return MUSIC_RECORDER_ENTITY.createFromReference();
    }
    
    public static @Nullable PlayerAPI<?,?> findClosestPlayerInWorld(
            WorldAPI<?> world, BlockPosAPI<?> pos, Collection<? extends PlayerAPI<?,?>> players) {
        String dimName = String.valueOf(world.getDimension().getRegistryName().unwrap());
        PlayerAPI<?,?> closest = null;
        double minDist = Double.MAX_VALUE;
        for(PlayerAPI<?,?> player : players) {
            if(dimName.equals(String.valueOf(player.getDimension().getRegistryName().unwrap()))) {
                double distance = pos.distanceTo(player.getPosRounded());
                if(distance<minDist) {
                    closest = player;
                    minDist = distance;
                }
            }
        }
        return closest;
    }
    
    private static boolean spawnRecordedDisc(WorldAPI<?> world, BlockPosAPI<?> pos, boolean special) {
        if(!world.isClient()) {
            MinecraftServerAPI<?> server = ServerHelper.getAPI();
            if(Objects.isNull(server)) {
                MTRef.logError("Cannot spawn disc in nonexistant server!");
                return false;
            }
            PlayerAPI<?,?> closestPlayer = findClosestPlayerInWorld(world,pos,server.getPlayers());
            if(Objects.isNull(closestPlayer)) {
                MTRef.logWarn("Cannot record disc without any players in the same dimension!");
                return false;
            }
            ChannelHelper helper = ChannelHelper.getHelper(String.valueOf(closestPlayer.getUUID()),false);
            if(Objects.isNull(helper)) {
                MTRef.logError("Unable to find ChannelHelper for player with UUID {}",closestPlayer.getUUID());
                return false;
            }
            Vector3d spawnPos = new Vector3d(RandomHelper.randomDouble(pos.x()+0.7d,pos.x()+0.85d),
                                             RandomHelper.randomDouble(pos.y()+0.7d,pos.y()+1.36d),
                                             RandomHelper.randomDouble(pos.z()+0.7d,pos.z()+0.85d));
            world.spawnItem(ENHANCED_MUSIC_DISC,spawnPos,stack -> helper.writeDisc(stack, special),
                            entity -> entity.setPosition(pos.x(),pos.y()+0.5d,pos.z()));
            return true;
        }
        return false;
    }
}