package mods.thecomputerizer.musictriggers.api.data.trigger.holder;

import mods.thecomputerizer.musictriggers.api.data.channel.ChannelAPI;
import mods.thecomputerizer.musictriggers.api.data.trigger.TriggerContext;

public class TriggerDifficulty extends HolderTrigger {

    public TriggerDifficulty(ChannelAPI channel) {
        super(channel,"difficulty");
    }
    
    @Override
    public boolean imply(String id) {
        int level = -1;
        switch(id) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4": {
                level = Integer.parseInt(id);
                break;
            }
            case "easy": {
                level = 1;
                break;
            }
            case "hard": {
                level = 3;
                break;
            }
            case "hardcore": {
                level = 4;
                break;
            }
            case "normal": {
                level = 2;
                break;
            }
            case "peaceful": {
                level = 0;
                break;
            }
        }
        if(level!=-1) {
            setExistingParameterValue("level",level);
            return super.imply(id);
        }
        logError("Unable to imply level from identifier {} (Accepts 0,1,2,3,4,peaceful,easy,normal,hard,hardcore)",id);
        return false;
    }

    @Override
    public boolean isPlayableContext(TriggerContext ctx) {
        return ctx.isActiveDifficulty(getParameterAsInt("level"));
    }

    @Override
    public boolean verifyRequiredParameters() {
        String[] parameters = new String[]{"identifier","level"};
        if(hasAllNonDefaultParameter(parameters)) return true;
        logMissingParameters(parameters);
        return false;
    }
}
