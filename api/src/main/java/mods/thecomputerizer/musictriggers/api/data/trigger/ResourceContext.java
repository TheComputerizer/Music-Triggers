package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ResourceContext {

    private final List<String> displayMatchers;
    private final List<String> resourcesMatchers;
    private final BiFunction<String,List<String>,Boolean> displayMatchFunc;
    private final BiFunction<String,List<String>,Boolean> resourceMatchFunc;
    @Setter boolean anyReturns = true;

    public ResourceContext(List<String> resourcesMatchers, List<String> displayMatchers,
                           String resourceMatchType, String displayMatchType) {
        this.displayMatchers = displayMatchers.isEmpty() ? Collections.singletonList("ANY") : displayMatchers;
        this.resourcesMatchers = resourcesMatchers.isEmpty() ? Collections.singletonList("ANY") : resourcesMatchers;
        this.displayMatchFunc = getMatchFunc(displayMatchType);
        this.resourceMatchFunc = getMatchFunc(resourceMatchType);
    }

    protected BiFunction<String,List<String>,Boolean> getMatchFunc(String matcherType) {
        switch(matcherType.toUpperCase()) {
            case "EXACT": return (id,matchThese) -> {
                if(Objects.isNull(id)) return false;
                if(matchThese.get(0).equals("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.equals(matchThis)) return true;
                return false;
            };
            case "PARTIAL": return (id,matchThese) -> {
                if(Objects.isNull(id)) return false;
                if(matchThese.get(0).equals("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.contains(matchThis)) return true;
                return false;
            };
            case "REGEX": return (id,matchThese) -> {
                if(Objects.isNull(id)) return false;
                if(matchThese.get(0).equals("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.matches(matchThis)) return true;
                return false;
            };
            default: return (id,matchThese) -> false;
        }
    }

    public boolean checkDisplayMatch(String display) {
        return this.displayMatchFunc.apply(display,this.displayMatchers);
    }

    public boolean checkMatch(String id, @Nullable String display) {
        return checkResourceMatch(id) && (Objects.isNull(display) || checkDisplayMatch(display));
    }

    public boolean checkResourceMatch(String id) {
        return this.resourceMatchFunc.apply(id,this.resourcesMatchers);
    }
}