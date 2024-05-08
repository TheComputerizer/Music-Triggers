package mods.thecomputerizer.musictriggers.api.client;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.ResourceContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerBiome;
import mods.thecomputerizer.musictriggers.api.data.trigger.holder.TriggerMob;
import mods.thecomputerizer.theimpossiblelibrary.api.client.ClientAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.client.MinecraftAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.blockentity.BlockEntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.container.PlayerInventoryAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.EntityAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.common.item.ItemStackAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.core.TILRef;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.BloodmoonAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ModAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.ModHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.integration.Weather2API.WeatherData;
import mods.thecomputerizer.theimpossiblelibrary.api.resource.ResourceLocationAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.util.Box;
import mods.thecomputerizer.theimpossiblelibrary.api.world.BlockPosAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.world.DimensionAPI;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class TriggerContextClient extends TriggerContext {

    private MinecraftAPI minecraft;
    private BlockPosAPI<?> pos;

    public TriggerContextClient(ChannelAPI channel) {
        super(channel,"client_context");
    }

    @Override
    public void cache() {
        this.minecraft = TILRef.getClientSubAPI(ClientAPI::getMinecraft);
        this.player = Objects.nonNull(this.minecraft) ? this.minecraft.getPlayer() : null;
        this.world = Objects.nonNull(this.minecraft) ? this.minecraft.getWorld() : null;
        this.pos = hasPlayer() ? this.player.getPosRounded() : null;
    }

    private <M extends ModAPI> boolean checkMod(Supplier<M> modSupplier, Function<M,Boolean> checker) {
        M mod = modSupplier.get();
        return Objects.nonNull(mod) && checker.apply(mod);
    }

    @Override
    public void close() {
        super.close();
        this.minecraft = null;
    }
    
    @Override
    public PlayerAPI<?,?> getPlayer() {
        return Objects.nonNull(this.minecraft) ? this.minecraft.getPlayer() : null;
    }

    private Collection<ItemStackAPI<?>> getStacksFromSlotMatcher(String slotMatcher) {
        PlayerInventoryAPI<?> inventory = this.player.getInventory();
        switch(slotMatcher.toLowerCase()) {
            case "mainhand": return Collections.singleton(this.player.getMainHandStack());
            case "offhand": return Collections.singleton(this.player.getOffHandStack());
            case "hotbar": return inventory.getHotbarStacks();
            case "armor": return inventory.getArmorStacks();
            case "any": {
                List<ItemStackAPI<?>> stacks = new ArrayList<>();
                for(int i=0;i<inventory.getSlots();i++) {
                    ItemStackAPI<?> stack = inventory.getStack(i);
                    if(stack.isNotEmpty()) stacks.add(stack);
                }
                return stacks;
            }
            default: {
                int slot = MTRef.randomInt(this.channel,"inventory_slot_number",slotMatcher,-1);
                return slot>=0 ? Collections.singleton(inventory.getStack(slot)) : Collections.emptyList();
            }
        }
    }

    @Override
    public boolean isActiveAcidRain() {
        return hasWorld() && checkMod(ModHelper::betterWeather,mod -> mod.isAcidRaining(this.world));
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
    public boolean isActiveBlizzard() {
        return hasWorld() && checkMod(ModHelper::betterWeather,mod -> mod.isBlizzard(this.world));
    }

    @Override
    public boolean isActiveBlockEntity(ResourceContext ctx, int range, float yRatio) {
        for(BlockEntityAPI<?,?> block : getBlockEntitiesAround(getBox(range,yRatio))) {
            ResourceLocationAPI<?> registryName = block.getRegistryName();
            if(Objects.nonNull(registryName) && ctx.checkMatch(registryName.toString(),null))
                return true;
        }
        return false;
    }

    @Override
    public boolean isActiveBloodMoon() {
        return hasWorld() && (checkMod(ModHelper::bloodmoon,BloodmoonAPI::isBloodMoon) ||
                checkMod(ModHelper::nyx,mod -> mod.isBloodMoon(this.world)) ||
                checkMod(ModHelper::enhancedCelestials,mod -> mod.isBloodMoon(this.world)));
    }

    @Override
    public boolean isActiveBlueMoon() {
        return hasWorld() && checkMod(ModHelper::enhancedCelestials,mod -> mod.isBlueMoon(this.world));
    }

    @Override
    public boolean isActiveCloudy() {
        return hasWorld() && checkMod(ModHelper::betterWeather,mod -> mod.isCloudy(this.world));
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
        return ctx.checkMatch(dimension.getRegistryName().toString(),dimension.getName());
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
    public boolean isActiveStarShower() {
        return hasWorld() && checkMod(ModHelper::nyx,mod -> mod.isStarShower(this.world));
    }

    @Override
    public boolean isActiveFishing() {
        return hasPlayer() && this.player.isFishing();
    }

    @Override
    public boolean isActiveGamestage(ResourceContext ctx, boolean whitelist) {
        return hasPlayer() && checkMod(ModHelper::gameStages,mod -> {
            for(String stage : mod.getStages(this.player))
                if(ctx.checkMatch(stage,null)) return true;
            return false;
        });
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
    public boolean isActiveHarvestMoon() {
        return hasWorld() && (checkMod(ModHelper::nyx,mod -> mod.isHarvestMoon(this.world)) ||
                checkMod(ModHelper::enhancedCelestials,mod -> mod.isHarvestMoon(this.world)));
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
    public boolean isActiveHurricane(int range) {
        return Objects.nonNull(this.pos) && checkMod(ModHelper::weather2,mod ->
                Objects.nonNull(mod.getClosestHurricane(this.world,this.pos,range)));
    }

    @Override
    public boolean isActiveInventory(List<String> items, List<String> slots) {
        if(items.isEmpty() || slots.isEmpty() || !hasPlayer()) return false;
        for(String slot : slots)
            for(ItemStackAPI<?> stack : getStacksFromSlotMatcher(slot))
                for(String item : items)
                    if(isActiveItem(stack,item)) return true;
        return false;
    }

    private boolean isActiveItem(ItemStackAPI<?> stack, String itemString) {
        String[] parts = itemString.split(":");
        if(parts.length==0) return false;
        if(parts.length==1) return parts[0].equals("empty") && stack.isEmpty();
        ResourceLocationAPI<?> itemName = stack.getItem().getRegistryName();
        if(parts[0].equals(itemName.getNamespace()) && parts[1].equals(itemName.getPath())) {
            if(parts.length==2) return true;
            if(stack.getCount()==MTRef.randomInt(this.channel,"parsed_item_count",parts[2],-1))
                return parts.length==3 || checkNBT(stack.getTag(),itemString);
        }
        return false;
    }

    @Override
    public boolean isActiveLight(int level, String type) {
        if(!hasBoth()) return false;
        switch(type.toLowerCase()) {
            case "block": return this.world.getLightBlock(this.pos)<=level;
            case "sky": return this.world.getLightSky(this.pos)<=level;
            default: return this.world.getLightTotal(this.pos)<=level;
        }
    }

    @Override
    public boolean isActiveLightRain() {
        return hasWorld() && checkMod(ModHelper::betterWeather,mod -> mod.isRaining(this.world));
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
    public boolean isActiveMoon(ResourceContext ctx) {
        return hasWorld() && checkMod(ModHelper::enhancedCelestials,mod -> mod.isMoon(this.world));
    }

    @Override
    public boolean isActivePet(int range, float yRatio) {
        for(EntityAPI<?,?> entity : getEntitiesAround(getBox(range,yRatio)))
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
    public boolean isActiveRainIntensity(float level) {
        return hasWorld() && checkMod(ModHelper::dynamicSurroundings,mod -> mod.getRainStrength(this.world)>level);
    }

    @Override
    public boolean isActiveRiding(ResourceContext ctx) {
        if(!hasPlayer()) return false;
        EntityAPI<?,?> entity = this.player.getVehicle();
        return Objects.nonNull(entity) && ctx.checkMatch(entity.getRegistryName().toString(),entity.getName());
    }

    @Override
    public boolean isActiveSandstorm(int range) {
        return Objects.nonNull(this.pos) && checkMod(ModHelper::weather2,mod ->
                Objects.nonNull(mod.getClosestSandStorm(this.world,this.pos,range)));
    }

    @Override
    public boolean isActiveSeason(int level) {
        return hasWorld() && checkMod(ModHelper::sereneSeasons,mod -> {
            switch(level) {
                case 0: return mod.isSpring(this.world);
                case 1: return mod.isSummer(this.world);
                case 2: return mod.isAutumn(this.world);
                case 3: return mod.isWinter(this.world);
                default: return false;
            }
        });
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
        switch(bundle.toLowerCase()) {
            case "day": return this.world.isDaytime();
            case "night": return this.world.isNighttime();
            case "sunrise": return this.world.isSunrise();
            case "sunset": return this.world.isSunset();
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
    public boolean isActiveTornado(int range, int level) {
        return Objects.nonNull(this.pos) && checkMod(ModHelper::weather2,mod -> {
            WeatherData data = mod.getClosestTornado(this.world,this.pos,range);
            return Objects.nonNull(data) && data.getLevel()>=level;
        });
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
