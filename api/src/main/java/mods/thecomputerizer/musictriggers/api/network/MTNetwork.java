package mods.thecomputerizer.musictriggers.api.network;

import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHandler;

public class MTNetwork {

    public static void init() {
        NetworkHandler.registerMsgToClient(MessageTriggerStates.class,MessageTriggerStates::new);
        NetworkHandler.registerMsgToServer(MessageTriggerStates.class,MessageTriggerStates::new);
    }
}