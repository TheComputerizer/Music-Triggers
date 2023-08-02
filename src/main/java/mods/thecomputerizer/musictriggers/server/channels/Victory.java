package mods.thecomputerizer.musictriggers.server.channels;

import mods.thecomputerizer.musictriggers.Constants;
import mods.thecomputerizer.musictriggers.MusicTriggers;
import mods.thecomputerizer.theimpossiblelibrary.common.toml.Table;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.Level;

import java.util.*;
import java.util.function.Function;

class Victory {

    private final int TIMEOUT;
    private final HashSet<Table> REFERNCES;
    private final Map<Table,Boolean> ACTIVE_REFERENCES;
    private final Map<Table,HashMap<LivingEntity,MutableInt>> ENTITY_CACHE;
    private final Map<Table,HashMap<ServerPlayerEntity,MutableInt>> PLAYER_CACHE;

    Victory(List<Table> references, int timeout) {
        this.TIMEOUT = timeout;
        this.REFERNCES = new HashSet<>(references);
        this.ACTIVE_REFERENCES = new HashMap<>();
        for(Table reference : this.REFERNCES)
            this.ACTIVE_REFERENCES.put(reference,false);
        this.ENTITY_CACHE = buildCache(reference -> reference.getName().matches("mob"));
        this.PLAYER_CACHE = buildCache(reference -> reference.getName().matches("pvp"));
    }

    private <T> Map<Table,HashMap<T,MutableInt>> buildCache(Function<Table,Boolean> canAdd) {
        Map<Table,HashMap<T,MutableInt>> ret = new HashMap<>();
        for(Table reference : this.REFERNCES)
            if(canAdd.apply(reference))
                ret.put(reference,new HashMap<>());
        return ret;
    }

    @SuppressWarnings("unchecked")
    protected <T> void add(Table trigger, boolean isMob, T obj) {
        try {
            if (isMob) innerAdd(trigger, obj, (Map<Table, HashMap<T, MutableInt>>) (Object) this.ENTITY_CACHE);
            else innerAdd(trigger, obj, (Map<Table, HashMap<T, MutableInt>>) (Object) this.PLAYER_CACHE);
        }
        catch (ClassCastException e) {
            MusicTriggers.logExternally(Level.ERROR,"Something went wrong when attempting to add to a victory " +
                    "trigger! See the main log for the full stacktrace");
            Constants.MAIN_LOG.error("Something went wrong when attempting to add to a victory trigger! ",e);
        }
    }

    private <T> void innerAdd(Table trigger, T obj, Map<Table, HashMap<T,MutableInt>> backendMap) {
        HashMap<T,MutableInt> map = backendMap.get(trigger);
        if(Objects.nonNull(map)) {
            if(map.containsKey(obj)) map.get(obj).setValue(TIMEOUT);
            else map.put(obj,new MutableInt(TIMEOUT));
        }
        else MusicTriggers.logExternally(Level.ERROR,"Tried to add to the wrong type of victory trigger for " +
                "trigger {}! This is an issue and should be reported!",trigger.getName()+"-"+
                trigger.getValOrDefault("identifier","not_set"));
    }

    protected void setActive(Table trigger, boolean status) {
        if(this.ACTIVE_REFERENCES.containsKey(trigger)) this.ACTIVE_REFERENCES.put(trigger,status);
        else MusicTriggers.logExternally(Level.ERROR,"Tried to set the status of a victory trigger from the wrong " +
                "trigger {}! This is an issue and should be reported!",trigger.getName()+"-"+
                trigger.getValOrDefault("identifier","not_set"));
    }

    protected boolean runCalculation(ServerTriggerStatus calculator) {
        for(Table reference : this.REFERNCES) {
            if(this.ACTIVE_REFERENCES.get(reference)) continue;
            if(this.ENTITY_CACHE.containsKey(reference)) {
                float num = (float)(calculator.getParameterInt(reference,"level"));
                if (num > 0) {
                    boolean pass = false;
                    HashMap<LivingEntity,MutableInt> entities = this.ENTITY_CACHE.get(reference);
                    if (entities.isEmpty()) continue;
                    if (entities.size() >= num) {
                        float percent = calculator.getParameterFloat(reference,"victory_percentage") / 100f;
                        float count = 0;
                        for (Map.Entry<LivingEntity,MutableInt> entityEntry : entities.entrySet())
                            if (entityEntry.getKey().isDeadOrDying() || entityEntry.getKey().getHealth() <= 0f)
                                count++;
                        if (count / num >= percent) pass = true;
                    }
                    entities.entrySet().removeIf(entry -> entry.getValue().addAndGet(-5)<=0);
                    if(pass) return true;
                }
            }
            else if(this.PLAYER_CACHE.containsKey(reference)) {
                float num = (float)(calculator.getParameterInt(reference,"level"));
                if (num > 0) {
                    HashMap<ServerPlayerEntity,MutableInt> players = this.PLAYER_CACHE.get(reference);
                    if (players.isEmpty()) continue;
                    float percent = calculator.getParameterFloat(reference,"victory_percentage") / 100f;
                    float count = 0;
                    for (Map.Entry<ServerPlayerEntity, MutableInt> playerEntry : players.entrySet())
                        if (playerEntry.getKey().isDeadOrDying() || playerEntry.getKey().getHealth() <= 0f)
                            count++;
                    players.entrySet().removeIf(entry -> entry.getValue().addAndGet(-5) <= 0);
                    if (count / num >= percent) return true;
                }
            }
        }
        return false;
    }
}