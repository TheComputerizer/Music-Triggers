package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

import java.util.Collections;
import java.util.List;

public class TriggerSeason extends HolderTrigger {

    public TriggerSeason(ChannelAPI channel) {
        super(channel,"season");
    }
    
    @Override
    public boolean imply(String id) {
        int season = -1;
        switch(id) {
            case "0":
            case "1":
            case "2":
            case "3": {
                season = Integer.parseInt(id);
                break;
            }
            case "autumn":
            case "fall": {
                season = 2;
                break;
            }
            case "spring": {
                season = 0;
                break;
            }
            case "summer": {
                season = 1;
                break;
            }
            case "winter": {
                season = 3;
                break;
            }
        }
        if(season!=-1) {
            setExistingParameterValue("season",season);
            return super.imply(id);
        }
        logError("Unable to imply season from identifier {} (accepts 0,1,2,3,spring,summer,autumn,fall,winter)",id);
        return false;
    }

    @Override
    public List<String> getRequiredMods() {
        return Collections.singletonList("sereneseasons");
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveSeason(getParameterAsInt("season"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","season"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
