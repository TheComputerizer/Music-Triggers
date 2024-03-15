package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.parameter.Parameter;
import mods.thecomputerizer.musictriggers.api.data.parameter.ParameterWrapper;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterBoolean;
import mods.thecomputerizer.musictriggers.api.data.parameter.primitive.ParameterInt;
import mods.thecomputerizer.theimpossiblelibrary.api.toml.Table;

import java.util.*;

@Getter
public abstract class TriggerAPI extends ParameterWrapper {

    private final Set<TriggerCombination> parents;
    private final String name;
    @Setter private boolean enabled;
    private ResourceContext resourceCtx;

    protected TriggerAPI(ChannelAPI channel, String name) {
        super(channel);
        this.parents = new HashSet<>();
        this.name = name;
    }

    public String getIdentifier() {
        String id = getParameterAsString("identifier");
        return Objects.nonNull(id) ? id : "not_set";
    }

    public String getNameWithID() {
        return getName();
    }

    public List<String> getRequiredMods() {
        return Collections.emptyList();
    }

    @Override
    protected String getTypeName() {
        return "Trigger `"+getName()+"`";
    }

    @Override
    protected Map<String,Parameter<?>> initParameterMap() {
        Map<String,Parameter<?>> map = new HashMap<>();
        addParameter(map,"fade_in",new ParameterInt(0));
        addParameter(map,"fade_out",new ParameterInt(0));
        addParameter(map,"max_tracks",new ParameterInt(0));
        addParameter(map,"not",new ParameterBoolean(false));
        addParameter(map,"passive_persistence",new ParameterBoolean(false));
        addParameter(map,"persistence",new ParameterInt(0));
        addParameter(map,"priority",new ParameterInt(0));
        addParameter(map,"song_delay",new ParameterInt(0));
        addParameter(map,"start_delay",new ParameterInt(0));
        addParameter(map,"start_toggled",new ParameterBoolean(true));
        addParameter(map,"stop_delay",new ParameterInt(0));
        addParameter(map,"toggle_inactive_playable",new ParameterBoolean(false));
        addParameter(map,"toggle_save_status",new ParameterInt(0));
        addParameter(map,"trigger_delay",new ParameterInt(0));
        initExtraParameters(map);
        return map;
    }

    public abstract boolean isActive(TriggerContextAPI<?,?> context);

    public boolean isContained(Collection<TriggerAPI> triggers) {
        return TriggerHelper.matchesAny(triggers,this);
    }

    public boolean isServer() {
        return false;
    }

    public boolean matches(Collection<TriggerAPI> triggers) {
        return triggers.size()==1 && isContained(triggers);
    }

    public abstract boolean matches(TriggerAPI trigger);

    public void onConnect() {

    }

    public void onDisconnect() {

    }

    public boolean parse(Table table) {
        if(parseParameters(table)) {
            setResourceContext();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    protected void setResourceContext() {
        if(hasParameter("resource_name")) {
            List<String> resourceName = (List<String>)getParameterAsList("resource_name");
            List<String> displayeName = (List<String>)getParameterAsList("display_name");
            String resourceMatcher = getParameterAsString("resource_matcher");
            String displayMatcher = getParameterAsString("display_matcher");
            this.resourceCtx = new ResourceContext(resourceName,displayeName,resourceMatcher,displayMatcher);
        } else this.resourceCtx = null;
    }
}