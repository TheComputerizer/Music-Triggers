package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Box;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.DimensionAPI;

import java.util.List;
import java.util.Objects;

public class TriggerContextClient extends TriggerContext {

    private MinecraftAPI minecraft;
    private BlockPosAPI<?> pos;

    public TriggerContextClient(ChannelAPI channel) {
        super(channel);
    }

    @Override
    public void cache() {
        this.minecraft = TILRef.getClientSubAPI("MinecraftAPI",ClientAPI::getMinecraft);
        this.player = Objects.nonNull(this.minecraft) ? this.minecraft.getPlayer() : null;
        this.world = Objects.nonNull(this.minecraft) ? this.minecraft.getWorld() : null;
        this.pos = hasPlayer() ? this.player.getPosRounded() : null;
    }

    @Override
    public void close() {
        super.close();
        this.minecraft = null;
    }

    @Override
    public boolean isActiveAcidRain() { //TODO
        return false;
    }

    @Override
    public boolean isActiveAdvancement(ResourceContext ctx) { //TODO
        return false;
    }

    @Override
    public boolean isActiveAdventure() {
        return hasPlayer() && this.player.isGamemodeAdventure();
    }

    @Override
    public boolean isActiveBiome(TriggerBiome trigger) {
        return false;
    }

    @Override
    public boolean isActiveBlizzard() { //TODO
        return false;
    }

    @Override
    public boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio) { //TODO
        return false;
    }

    @Override
    public boolean isActiveBloodMoon() { //TODO
        return false;
    }

    @Override
    public boolean isActiveBlueMoon() { //TODO
        return false;
    }

    @Override
    public boolean isActiveCloudy() { //TODO
        return false;
    }

    @Override
    public boolean isActiveCommand() { //TODO
        return false;
    }

    @Override
    public boolean isActiveCreative() {
        return hasPlayer() && this.player.isGamemodeCreative();
    }

    @Override
    public boolean isActiveDead() {
        return hasPlayer() && !this.player.isAlive();
    }

    @Override
    public boolean isActiveDifficulty(int level) {
        return hasWorld() && this.world.getDifficultyOrdinal()==level;
    }

    @Override
    public boolean isActiveDimension(ResourceContext ctx) {
        if(!hasBoth()) return false;
        DimensionAPI<?> dimension = this.player.getDimension();
        return ctx.checkMatch(dimension.getRegistryName().get().toString(),dimension.getName());
    }

    @Override
    public boolean isActiveDrowning(int level) {
        return hasPlayer() && this.player.getAir()<level;
    }

    @Override
    public boolean isActiveEffect(ResourceContext ctx) { //TODO
        return false;
    }

    @Override
    public boolean isActiveElytra() {
        return hasPlayer() && this.player.isFlying();
    }

    @Override
    public boolean isActiveFallingStars() { //TODO
        return false;
    }

    @Override
    public boolean isActiveFishing() {
        return hasPlayer() && this.player.isFishing();
    }

    @Override
    public boolean isActiveGamestage(ResourceContext ctx, boolean whitelist) { //TODO
        return false;
    }

    @Override
    public boolean isActiveGeneric() {
        return true;
    }

    @Override
    public boolean isActiveGUI(ResourceContext ctx) { //TODO
        return false;
    }

    @Override
    public boolean isActiveHarvestMoon() { //TODO
        return false;
    }

    @Override
    public boolean isActiveHeight(int level, boolean checkSky, boolean checkAbove) {
        BlockPosAPI<?> pos = hasBoth() ? this.player.getPosRounded() : null;
        return Objects.nonNull(pos) && (checkAbove ? pos.y()>level : pos.y()<level &&
                (!checkSky || this.world.isSkyVisible(pos)));
    }

    @Override
    public boolean isActiveHome(int range, float yRatio) {
        return false;
    }

    @Override
    public boolean isActiveHurricane(int range) { //TODO
        return false;
    }

    @Override
    public boolean isActiveInventory(List<String> items, List<String> slots) { //TODO
        return false;
    }

    @Override
    public boolean isActiveLight(int level, String type) {
        if(!hasBoth()) return false;
        switch(type.toUpperCase()) {
            case "BLOCK": return this.world.getLightBlock(this.pos)<=level;
            case "SKY": return this.world.getLightSky(this.pos)<=level;
            default: return this.world.getLightTotal(this.pos)<=level;
        }
    }

    @Override
    public boolean isActiveLightRain() { //TODO
        return false;
    }

    @Override
    public boolean isActiveLoading() {
        return Objects.nonNull(this.minecraft) && this.minecraft.isLoading();
    }

    @Override
    public boolean isActiveLowHP(float percent) {
        return hasPlayer() && (percent/100f)<this.player.getHealthPercent();
    }

    @Override
    public boolean isActiveMenu() {
        return Objects.nonNull(this.minecraft) && this.minecraft.isFinishedLoading() && !hasWorld();
    }

    @Override
    public boolean isActiveMob(TriggerMob trigger) {
        return false;
    }

    @Override
    public boolean isActiveMoon(ResourceContext ctx) { //TODO
        return false;
    }

    @Override
    public boolean isActivePet(int range, float yRatio) {
        for(EntityAPI<?,?> entity : getEntitiesAround(range,yRatio))
            if(entity.isOwnedBy(this.player)) return true;
        return false;
    }

    @Override
    public boolean isActivePVP() {
        return false;
    }

    @Override
    public boolean isActiveRaid(int wave) {
        return false;
    }

    @Override
    public boolean isActiveRaining() {
        return hasWorld() && this.world.isRaining();
    }

    @Override
    public boolean isActiveRainIntensity(float level) { //TODO
        return false;
    }

    @Override
    public boolean isActiveRiding(ResourceContext ctx) { //TODO
        return false;
    }

    @Override
    public boolean isActiveSandstorm(int range) { //TODO
        return false;
    }

    @Override
    public boolean isActiveSeason(int level) { //TODO
        return false;
    }

    @Override
    public boolean isActiveSnowing() {
        return false;
    }

    @Override
    public boolean isActiveSpectator() {
        return hasPlayer() && this.player.isGamemodeSpectator();
    }

    @Override
    public boolean isActiveStatistic(ResourceContext ctx, int level) { //TODO
        return false;
    }

    @Override
    public boolean isActiveStorming() {
        return hasWorld() && this.world.isStorming();
    }

    @Override
    public boolean isActiveStructure(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveTime(String bundle, float startHour, float endHour, int startDay, int endDay, int moonPhase) {
        if(!hasWorld() || !isActiveTimeExtras(startDay,endDay,moonPhase)) return false;
        switch(bundle.toUpperCase()) {
            case "DAY": return this.world.isDaytime();
            case "NIGHT": return this.world.isNighttime();
            case "SUNRISE": return this.world.isSunrise();
            case "SUNSET": return this.world.isSunset();
            default: {
                if(startHour==endHour) return true;
                float time = (float)this.world.getTimeDay()/1000f;
                return startHour<endHour ? (time>=startHour && time<endHour) : (time>=startHour || time<endHour);
            }
        }
    }

    private boolean isActiveTimeExtras(int startDay, int endDay, int moonPhase) {
        int day = this.world.getDayNumber();
        return day>=startDay && day<=endDay && (moonPhase==0 || moonPhase==this.world.getMoonPhase()+1);
    }

    @Override
    public boolean isActiveTornado(int range, int level) { //TODO
        return false;
    }

    @Override
    public boolean isActiveUnderwater() {
        return Objects.nonNull(this.pos) && this.world.isUnderwater(this.pos);
    }

    @Override
    public boolean isActiveVictory(int timeout) {
        return false;
    }

    @Override
    public boolean isActiveZones(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return hasPlayer() && new Box(minX,minY,minZ,maxX,maxY,maxZ).isInside(this.player.getPosRounded());
    }

    @Override
    public boolean isClient() {
        return true;
    }
}
