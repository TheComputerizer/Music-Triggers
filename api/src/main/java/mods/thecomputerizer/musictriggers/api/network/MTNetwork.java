package mods.thecomputerizer.musictriggers.api.network;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageWrapperAPI;

import javax.annotation.Nullable;
import java.util.Objects;

public class MTNetwork {

    public static void init() {
        MTRef.logInfo("Initializing network info");
        NetworkHandler.registerMsgToClient(MessageReload.class,MessageReload::new);
        NetworkHandler.registerMsgToClient(MessageToggleDebugParameter.class,MessageToggleDebugParameter::new);
        NetworkHandler.registerMsgToClient(MessageTriggerStates.class,MessageTriggerStates::new);
        NetworkHandler.registerMsgToServer(MessageTriggerStates.class,MessageTriggerStates::new);
    }
    
    public static <D> void sendToServer(MessageAPI<?> msg, boolean login) {
        D direction = login ? NetworkHelper.getDirToServerLogin() : NetworkHelper.getDirToServer();
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(direction,msg);
        if(Objects.nonNull(wrapper)) wrapper.send();
        else MTRef.logError("Cannot null message to the server!");
    }
    
    public static <P> void sendToClient(MessageAPI<?> msg, boolean login, @Nullable PlayerAPI<P,?> player) {
        sendToClient(msg,login,Objects.nonNull(player) ? player.getEntity() : null);
    }
    
    @SuppressWarnings("unchecked")
    public static <P,D> void sendToClient(MessageAPI<?> msg, boolean login, @Nullable P player) {
        D direction = login ? NetworkHelper.getDirToClientLogin() : NetworkHelper.getDirToClient();
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(direction,msg);
        if(Objects.nonNull(wrapper)) {
            if(Objects.nonNull(player)) {
                ((MessageWrapperAPI<P,?>)wrapper).setPlayer(player);
                wrapper.send();
            } else MTRef.logError("Cannot send message to null player!");
        } else MTRef.logError("Cannot null message to a player!");
    }
}