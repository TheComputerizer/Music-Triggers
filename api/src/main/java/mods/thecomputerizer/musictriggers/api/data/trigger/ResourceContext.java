package mods.thecomputerizer.musictriggers.api.data.trigger;

import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class ResourceContext {

    private final boolean defaultDisplay;
    private final boolean defaultResource;
    @Getter private final List<String> displayMatchers;
    @Getter private final List<String> resourcesMatchers;
    private final BiFunction<String,List<String>,Boolean> displayMatchFunc;
    private final BiFunction<String,List<String>,Boolean> resourceMatchFunc;
    @Setter boolean anyReturns = true;

    public ResourceContext(List<String> resourcesMatchers, List<String> displayMatchers,
                           String resourceMatchType, String displayMatchType) {
        this.displayMatchers = displayMatchers.isEmpty() ? Collections.singletonList("any") : displayMatchers;
        this.defaultDisplay = displayMatchers.contains("any");
        this.resourcesMatchers = resourcesMatchers.isEmpty() ? Collections.singletonList("any") : resourcesMatchers;
        this.defaultResource = resourcesMatchers.contains("any");
        this.displayMatchFunc = getMatchFunc(displayMatchType);
        this.resourceMatchFunc = getMatchFunc(resourceMatchType);
    }

    protected BiFunction<String,List<String>,Boolean> getMatchFunc(String matcherType) {
        switch(matcherType.toLowerCase()) {
            case "exact": return (id,matchThese) -> {
                if(Objects.isNull(id)) return matchThese.contains(null) || matchThese.contains("");
                if(matchThese.contains("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.equals(matchThis)) return true;
                return false;
            };
            case "partial": return (id,matchThese) -> {
                if(Objects.isNull(id)) return matchThese.contains(null) || matchThese.contains("");
                if(matchThese.contains("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.contains(matchThis)) return true;
                return false;
            };
            case "regex": return (id,matchThese) -> {
                if(Objects.isNull(id)) return matchThese.contains(null) || matchThese.contains("");
                if(matchThese.contains("any")) return this.anyReturns;
                for(String matchThis : matchThese)
                    if(id.matches(matchThis)) return true;
                return false;
            };
            default: return (id,matchThese) -> false;
        }
    }

    public boolean checkDisplayMatch(@Nullable Object display) {
        return this.displayMatchFunc.apply(Objects.nonNull(display) ? display.toString() : null,this.displayMatchers);
    }
    
    /**
     * If both are null or both are not null, check both.
     * If one is null while the other is not, check whichever is not null.
     */
    public boolean checkMatch(@Nullable Object id, @Nullable Object display) {
        boolean checkDisplay = shouldCheck(display,id,this.defaultDisplay,this.defaultResource);
        boolean checkResource = shouldCheck(id,display,this.defaultResource,this.defaultDisplay);
        return checkMatch(id,display,checkDisplay,checkResource);
    }
    
    private boolean checkMatch(@Nullable Object id, @Nullable Object display, boolean checkDisplay,
            boolean checkResource) {
        return (checkDisplay && checkDisplayMatch(display)) || (checkResource && checkResourceMatch(id));
    }

    public boolean checkResourceMatch(@Nullable Object id) {
        return this.resourceMatchFunc.apply(Objects.nonNull(id) ? id.toString() : null,this.resourcesMatchers);
    }
    
    private boolean shouldCheck(@Nullable Object main, @Nullable Object other, boolean defaultMain,
            boolean defaultOther) {
        return Objects.isNull(other) || defaultOther || (Objects.nonNull(main) && !defaultMain);
    }
}