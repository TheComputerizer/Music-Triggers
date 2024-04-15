package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.theimpossiblelibrary.api.common.biome.BiomeAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.structure.StructureAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ChampionsAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ChampionsAPI.ChampionData;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.InfernalMobsAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.InfernalMobsAPI.InfernalData;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ModHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TriggerContextServer extends TriggerContext {

    private BlockPosAPI<?> pos;
    private StructureAPI<?> structure;
    private BiomeAPI<?> biome;

    public TriggerContextServer(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public void cache() {
        this.pos = hasBoth() ? this.player.getPosRounded() : null;
        this.biome = Objects.nonNull(this.pos) ? this.world.getBiomeAt(this.pos) : null;
        this.structure = Objects.nonNull(this.pos) ? this.world.getStructureAt(this.pos) : null;
    }

    private boolean checkBiomeNameAndType(TriggerBiome trigger) {
        ResourceLocationAPI<?> regName = this.biome.getRegistryName();
        if(Objects.isNull(regName) || Objects.isNull(regName.get())) return false;
        ResourceContext ctx = trigger.getResourceCtx();
        if(ctx.checkMatch(regName.get().toString(),null)) return true; //TODO Sync biome names or check biomes on the client
        ctx = trigger.getTagCtx();
        for(String tag : this.biome.getTagNames(this.world))
            if(ctx.checkMatch(tag,null)) return true;
        return false;
    }

    private boolean checkBiomeRain(TriggerBiome trigger) {
        String rainType = trigger.getParameterAsString("rain_type").toUpperCase();
        if(!this.biome.canRain()) return rainType.equals("ANY") || rainType.equals("NONE");
        if(this.biome.canSnow() && !rainType.equals("SNOW") && !rainType.equals("ANY")) return false;
        float rainfall = trigger.getParameterAsFloat("biome_rainfall");
        return trigger.getParameterAsBoolean("rainfall_greater_than") ?
                this.biome.getRainfall()>=rainfall : this.biome.getRainfall()<=rainfall;
    }

    private boolean checkBiomeExtras(TriggerBiome trigger) {
        if(checkBiomeRain(trigger)) {
            float temperature = trigger.getParameterAsFloat("biome_temperature");
            return trigger.getParameterAsBoolean("temperature_greater_than") ?
                    this.biome.getTemperatureAt(this.pos)>=temperature :
                    this.biome.getTemperatureAt(this.pos)<=temperature;
        }
        return false;
    }

    private boolean checkEntity(TriggerMob trigger, EntityAPI<?,?> entity) {
        ResourceContext ctx = trigger.getResourceCtx();
        return Objects.nonNull(ctx) && checkEntityName(ctx,entity) && checkEntityMods(trigger,entity) ;
    }

    @SuppressWarnings("unchecked")
    private boolean checkEntityChampion(TriggerMob trigger, EntityAPI<?,?> entity) {
        ChampionsAPI champions = ModHelper.champions();
        if(Objects.nonNull(champions)) {
            List<String> championNames = (List<String>)trigger.getParameterAsList("champion");
            if(championNames.isEmpty() || (championNames.size()==1 && championNames.get(0).equals("ANY"))) return true;
            ChampionData data = champions.getChampionData(entity);
            if(Objects.nonNull(data))
                for(String name : championNames)
                    for(String affix : data.getAffixes())
                        if(affix.contains(name)) return true;
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private boolean checkEntityInfernal(TriggerMob trigger, EntityAPI<?,?> entity) {
        InfernalMobsAPI infernalMobs = ModHelper.infernalMobs();
        if(Objects.nonNull(infernalMobs)) {
            List<String> infernalNames = (List<String>)trigger.getParameterAsList("infernal");
            if(infernalNames.isEmpty() || (infernalNames.size()==1 && infernalNames.get(0).equals("ANY"))) return true;
            InfernalData<?> data = infernalMobs.getInfernalData(entity);
            if(Objects.nonNull(data))
                for(String name : infernalNames)
                    for(String display : data.getDisplayNames())
                        if(display.contains(name)) return true;
            return false;
        }
        return true;
    }

    private boolean checkEntityMods(TriggerMob trigger, EntityAPI<?,?> entity) {
        return checkEntityChampion(trigger,entity) && checkEntityInfernal(trigger,entity);
    }

    private boolean checkEntityName(ResourceContext ctx, EntityAPI<?,?> entity) {
        ResourceLocationAPI<?> regName = entity.getRegistryName();
        if(Objects.isNull(regName) || Objects.isNull(regName.get())) return false;
        String name = entity.getName();
        return ctx.checkMatch(regName.toString(),StringUtils.isNotBlank(name) ? name : null);
    }

    private Set<EntityAPI<?,?>> getEntitiesAround(TriggerMob trigger) {
        int range = trigger.getParameterAsInt("detection_range");
        float rangeRatioY = trigger.getParameterAsFloat("detection_y_ratio");
        return trigger.removeDuplicates(getEntitiesAround(getBox(range,rangeRatioY)));
    }

    @Override
    public boolean isActiveAcidRain() {
        return false;
    }

    @Override
    public boolean isActiveAdvancement(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveAdventure() {
        return false;
    }

    @Override
    public boolean isActiveBiome(TriggerBiome trigger) { //TODO Better caching
        return Objects.nonNull(this.biome) && checkBiomeNameAndType(trigger) && checkBiomeExtras(trigger);
    }

    @Override
    public boolean isActiveBlizzard() {
        return false;
    }

    @Override
    public boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio) {
        return false;
    }

    @Override
    public boolean isActiveBloodMoon() {
        return false;
    }

    @Override
    public boolean isActiveBlueMoon() {
        return false;
    }

    @Override
    public boolean isActiveCloudy() {
        return false;
    }

    @Override
    public boolean isActiveCommand() {
        return false;
    }

    @Override
    public boolean isActiveCreative() {
        return false;
    }

    @Override
    public boolean isActiveDead() {
        return false;
    }

    @Override
    public boolean isActiveDifficulty(int level) {
        return false;
    }

    @Override
    public boolean isActiveDimension(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveDrowning(int level) {
        return false;
    }

    @Override
    public boolean isActiveEffect(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveElytra() {
        return false;
    }

    @Override
    public boolean isActiveStarShower() {
        return false;
    }

    @Override
    public boolean isActiveFishing() {
        return false;
    }

    @Override
    public boolean isActiveGamestage(ResourceContext ctx, boolean whitelist) {
        return false;
    }

    @Override
    public boolean isActiveGeneric() {
        return false;
    }

    @Override
    public boolean isActiveGUI(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveHarvestMoon() {
        return false;
    }

    @Override
    public boolean isActiveHeight(int level, boolean checkSky, boolean checkAbove) {
        return false;
    }

    @Override
    public boolean isActiveHome(int range, float yRatio) {
        if(Objects.isNull(this.pos)) return false;
        BlockPosAPI<?> bed = this.player.getBedPos(this.player.getDimension());
        return Objects.nonNull(bed) && isCloseEnough(bed.x(),bed.y(),bed.z(),range,yRatio,
                this.pos.x(),this.pos.y(),this.pos.z());
    }

    @Override
    public boolean isActiveHurricane(int range) {
        return false;
    }

    @Override
    public boolean isActiveInventory(List<String> items, List<String> slots) {
        return false;
    }

    @Override
    public boolean isActiveLight(int level, String type) {
        return false;
    }

    @Override
    public boolean isActiveLightRain() {
        return false;
    }

    @Override
    public boolean isActiveLoading() {
        return false;
    }

    @Override
    public boolean isActiveLowHP(float percent) {
        return false;
    }

    @Override
    public boolean isActiveMenu() {
        return false;
    }

    @Override
    public boolean isActiveMob(TriggerMob trigger) { //TODO Finish this
        if(Objects.isNull(this.pos)) return false;
        validateEntities(trigger,getEntitiesAround(trigger));
        int min = trigger.getParameterAsInt("min_entities");
        int max = trigger.getParameterAsInt("max_entities");
        return trigger.hasCorrectSize(min,max);
    }

    @Override
    public boolean isActiveMoon(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActivePet(int range, float yRatio) {
        return false;
    }

    @Override
    public boolean isActivePVP() { //TODO
        return false;
    }

    @Override
    public boolean isActiveRaid(int wave) { //TODO
        return false;
    }

    @Override
    public boolean isActiveRaining() {
        return false;
    }

    @Override
    public boolean isActiveRainIntensity(float level) {
        return false;
    }

    @Override
    public boolean isActiveRiding(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveSandstorm(int range) {
        return false;
    }

    @Override
    public boolean isActiveSeason(int level) {
        return false;
    }

    @Override
    public boolean isActiveSnowing() { //TODO
        return Objects.nonNull(this.pos) && this.world.canSnowAt(this.pos);
    }

    @Override
    public boolean isActiveSpectator() {
        return false;
    }

    @Override
    public boolean isActiveStatistic(ResourceContext ctx, int level) {
        return false;
    }

    @Override
    public boolean isActiveStorming() {
        return false;
    }

    @Override
    public boolean isActiveStructure(ResourceContext ctx) {
        return Objects.nonNull(this.structure) && ctx.checkMatch(
                this.structure.getRegistryName().get().toString(),this.structure.getName());
    }

    @Override
    public boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase) {
        return false;
    }

    @Override
    public boolean isActiveTornado(int range, int level) {
        return false;
    }

    @Override
    public boolean isActiveUnderwater() {
        return false;
    }

    @Override
    public boolean isActiveVictory(int timeout) { //TODO
        return false;
    }

    @Override
    public boolean isActiveZones(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return false;
    }

    @Override
    public boolean isClient() {
        return false;
    }

    private void validateEntities(TriggerMob trigger, Collection<EntityAPI<?,?>> entitiesAround) {
        Set<EntityAPI<?,?>> entities = trigger.getValidEntities();
        entities.addAll(entitiesAround);
        trigger.getValidEntities().removeIf(entity -> !checkEntity(trigger,entity));
    }
}