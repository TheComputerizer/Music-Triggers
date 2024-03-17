package mods.thecomputerizer.musictriggers.api.data.channel;

import lombok.Getter;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioHelper;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioPool;
import mods.thecomputerizer.musictriggers.api.data.audio.AudioRef;
import mods.thecomputerizer.musictriggers.api.data.command.CommandElement;
import mods.thecomputerizer.musictriggers.api.data.jukebox.RecordElement;
import mods.thecomputerizer.musictriggers.api.data.parameter.UniversalParameters;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterFloat;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.musictriggers.api.data.redirect.RedirectElement;
import mods.thecomputerizer.musictriggers.api.data.render.CardAPI;
import mods.thecomputerizer.musictriggers.api.data.render.CardHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerHelper;
import mods.thecomputerizer.musictriggers.api.data.trigger.basic.BasicTrigger;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Holder;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import javax.annotation.Nullable;
import java.util.*;

@Getter
public class ChannelData extends ChannelElement {

    private final Set<AudioRef> audio;
    private final Set<AudioPool> audioPools;
    private final Set<CardAPI> cards;
    private final Set<CommandElement> commands;
    private final Set<RecordElement> records;
    private final Set<RedirectElement> redirects;
    private final Set<TriggerAPI> triggers;
    private final Map<TriggerAPI,List<ChannelEventHandler>> triggerEventMap;
    private final Map<Class<? extends ChannelElement>,UniversalParameters> universalMap;
    private BasicTrigger genericTrigger;
    private BasicTrigger loadingTrigger;
    private BasicTrigger menuTrigger;

    public ChannelData(ChannelAPI channel) {
        super(channel);
        this.audio = new HashSet<>();
        this.audioPools = new HashSet<>();
        this.cards = new HashSet<>();
        this.commands = new HashSet<>();
        this.records = new HashSet<>();
        this.redirects = new HashSet<>();
        this.triggers = new HashSet<>();
        this.triggerEventMap = new HashMap<>();
        this.universalMap = initUniversals();
    }

    @Override
    public void activate() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.activate();
    }

    protected void addUniversals(Map<Class<? extends ChannelElement>,UniversalParameters> map) {
        map.put(AudioRef.class,universalAudio());
        map.put(TriggerAPI.class,universalTriggers());
    }

    public void clear() {
        this.audio.clear();
        this.audioPools.clear();
        this.cards.clear();
        this.commands.clear();
        this.records.clear();
        this.redirects.clear();
        this.triggers.clear();
        this.triggerEventMap.clear();
        this.universalMap.clear();
    }

    protected void extractTriggerCombinations() { //TODO Implement this

    }

    public List<ChannelEventHandler> getActiveEventHandlers() {
        TriggerAPI activeTrigger = this.channel.getActiveTrigger();
        if(Objects.nonNull(activeTrigger)) {
            List<ChannelEventHandler> handlers = this.triggerEventMap.get(activeTrigger);
            if(Objects.nonNull(handlers)) return handlers;
            logWarn("There are no registered event handlers for the active trigger `{}`!");
        }
        return Collections.emptyList();
    }

    public @Nullable AudioPool getActivePool() {
        for(ChannelEventHandler handler : getActiveEventHandlers())
            if(handler instanceof AudioPool) return (AudioPool)handler;
        return null;
    }

    public @Nullable AudioPool getPool(Collection<TriggerAPI> triggers) {
        for(AudioPool pool : this.audioPools)
            if(pool.matchingTriggers(triggers)) return pool;
        return null;
    }

    public @Nullable UniversalParameters getUniversals(Class<? extends ChannelElement> clazz) {
        return this.universalMap.get(clazz);
    }

    public boolean hasPool(Collection<TriggerAPI> triggers) {
        return Objects.nonNull(getPool(triggers));
    }

    private Map<Class<? extends ChannelElement>,UniversalParameters> initUniversals() {
        Map<Class<? extends ChannelElement>,UniversalParameters> map = new HashMap<>();
        addUniversals(map);
        return map;
    }

    public void organize() {
        setAudioPools();
        extractTriggerCombinations();
    }

    public void parse() {
        readRedirect(ChannelHelper.openTxt(getChannel().getInfo().getRedirectPath(),getChannel()));
        readMain(ChannelHelper.openToml(getChannel().getInfo().getMainPath(),getChannel()));
        readRenders(ChannelHelper.openToml(getChannel().getInfo().getRendersPath(),getChannel()));
        readCommands(ChannelHelper.openToml(getChannel().getInfo().getCommandsPath(),getChannel()));
        readJukebox(ChannelHelper.openTxt(getChannel().getInfo().getJukeboxPath(),getChannel()));
        organize();
    }

    @Override
    public void play() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.play();
    }

    @Override
    public void playing() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.playing();
    }

    @Override
    public void queue() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.queue();
    }

    public void readCommands(@Nullable Holder commands) {
        if(Objects.isNull(commands)) return;
        for(Table table : commands.getTables().values()) {
            CommandElement command = new CommandElement(getChannel(),table);
            if(command.isValid()) this.commands.add(command);
        }
    }

    public void readJukebox(Collection<String> lines) {
        for(String line : lines) {
            RecordElement record = new RecordElement(getChannel(),line);
            if(record.isValid()) this.records.add(record);
        }
    }

    public void readMain(@Nullable Holder main) {
        if(Objects.isNull(main)) return;
        TriggerHelper.parseTriggers(getChannel(),this.triggers,main.getTableByName("triggers"));
        AudioHelper.parseAudio(getChannel(),this.audio,main.getTableByName("songs"));
    }

    public void readRedirect(Collection<String> lines) {
        for(String line : lines) {
            RedirectElement redirect = new RedirectElement(getChannel(),line);
            if(redirect.isValid()) this.redirects.add(redirect);
        }
    }

    public void readRenders(@Nullable Holder renders) {
        if(Objects.isNull(renders)) return;
        CardHelper.parseImageCards(getChannel(),this.cards,renders.getTablesByName("image"));
        CardHelper.parseTitleCards(getChannel(),this.cards,renders.getTablesByName("title"));
    }

    protected void setAudioPools() {
        this.audioPools.clear();
        for(AudioRef ref : this.audio) {
            AudioPool pool = getPool(ref.getTriggers());
            if(Objects.nonNull(pool)) pool.addAudio(ref);
            else {
                pool = new AudioPool("pool_"+ref.getName(),ref);
                if(pool.isValid()) this.audioPools.add(pool);
            }
        }
    }

    @Override
    public void stop() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.stop();
    }

    @Override
    public void stopped() {
        for(ChannelEventHandler handler : getActiveEventHandlers()) handler.stopped();
    }

    protected UniversalParameters universalTriggers() {
        return UniversalParameters.get(this.channel,"Triggers",map -> {
            map.put("fade_in",new ParameterInt(0));
            map.put("fade_out",new ParameterInt(0));
            map.put("persistence",new ParameterInt(0));
            map.put("song_delay",new ParameterInt(0));
            map.put("start_delay",new ParameterInt(0));
            map.put("stop_delay",new ParameterInt(0));
            map.put("trigger_delay",new ParameterInt(0));
        });
    }

    protected UniversalParameters universalAudio() {
        return UniversalParameters.get(this.channel,"Audio",map -> {
            map.put("must_finish",new ParameterBoolean(false));
            map.put("pitch",new ParameterFloat(1f));
            map.put("play_once",new ParameterInt(0));
            map.put("volume",new ParameterFloat(1f));
        });
    }
}