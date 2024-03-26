package mods.thecomputerizer.musictriggers.api.server;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContextAPI;

import java.util.List;

public abstract class TriggerContextServer<PLAYER,WORLD> extends TriggerContextAPI<PLAYER,WORLD> {

    protected TriggerContextServer(ChannelAPI channel) {
        super(channel);
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
    public boolean isActiveEffect(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveElytra() {
        return false;
    }

    @Override
    public boolean isActiveFallingStars() {
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
    public boolean isActiveGUI(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActiveHarvestMoon() {
        return false;
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
    public boolean isActiveMoon(ResourceContext ctx) {
        return false;
    }

    @Override
    public boolean isActivePet(int range, float yRatio) {
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
    public boolean isActiveStatistic(ResourceContext ctx, int level) {
        return false;
    }

    @Override
    public boolean isActiveStorming() {
        return false;
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
    public boolean isClient() {
        return false;
    }
}