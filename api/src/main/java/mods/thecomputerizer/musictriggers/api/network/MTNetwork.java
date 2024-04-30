package mods.thecomputerizer.musictriggers.api.network;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHandler;

public class MTNetwork {

    public static void init() {
        MTRef.logInfo("Initializing network info");
        NetworkHandler.registerMsgToClient(MessageReload.class,MessageReload::new);
        NetworkHandler.registerMsgToClient(MessageTriggerStates.class,MessageTriggerStates::new);
        NetworkHandler.registerMsgToServer(MessageTriggerStates.class,MessageTriggerStates::new);
    }
}