package mods.thecomputerizer.musictriggers.api.network;

import mods.thecomputerizer.musictriggers.api.MTRef;
import mods.thecomputerizer.musictriggers.api.data.channel.ChannelHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.common.entity.PlayerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageWrapperAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.MinecraftServerAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.server.ServerHelper;

import javax.annotation.Nullable;
import java.util.Objects;

public class MTNetwork {
    
    public static void initClient() {
        MTRef.logInfo("Initializing client network");
        NetworkHandler.registerMsgToServer(MessageCurrentSong.class,MessageCurrentSong::new);
        NetworkHandler.registerMsgToServer(MessageFinishedInit.class,MessageFinishedInit::new);
        NetworkHandler.registerMsgToServer(MessageInitChannels.class,MessageInitChannels::new);
        NetworkHandler.registerMsgToServer(MessageReload.class,MessageReload::new);
        NetworkHandler.registerMsgToServer(MessageRequestChannels.class, MessageRequestChannels::new);
        NetworkHandler.registerMsgToServer(MessageTriggerStates.class,MessageTriggerStates::new);
        
        NetworkHandler.registerMsgToServerLogin(MessageFinishedInit.class,MessageFinishedInit::new);
        NetworkHandler.registerMsgToServerLogin(MessageInitChannels.class,MessageInitChannels::new);
        NetworkHandler.registerMsgToServerLogin(MessageRequestChannels.class, MessageRequestChannels::new);
    }
    
    public static void initCommon() {
        MTRef.logInfo("Initializing common network");
        NetworkHandler.registerMsgToClient(MessageFinishedInit.class,MessageFinishedInit::new);
        NetworkHandler.registerMsgToClient(MessageInitChannels.class,MessageInitChannels::new);
        NetworkHandler.registerMsgToClient(MessageReload.class,MessageReload::new);
        NetworkHandler.registerMsgToClient(MessageRequestChannels.class, MessageRequestChannels::new);
        NetworkHandler.registerMsgToClient(MessageToggleDebugParameter.class,MessageToggleDebugParameter::new);
        NetworkHandler.registerMsgToClient(MessageTriggerStates.class,MessageTriggerStates::new);
        
        NetworkHandler.registerMsgToClientLogin(MessageFinishedInit.class,MessageFinishedInit::new);
        NetworkHandler.registerMsgToClientLogin(MessageInitChannels.class,MessageInitChannels::new);
        NetworkHandler.registerMsgToClientLogin(MessageRequestChannels.class, MessageRequestChannels::new);
    }
    
    public static <D> void sendToServer(MessageAPI<?> msg, boolean login) {
        ChannelHelper helper = ChannelHelper.getClientHelper();
        finalizePlayerMsg(msg,login,Objects.nonNull(helper) ? helper.getPlayerID() : null);
        D direction = login ? NetworkHelper.getDirToServerLogin() : NetworkHelper.getDirToServer();
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(direction,msg);
        if(Objects.nonNull(wrapper)) wrapper.send();
        else MTRef.logError("Cannot null message to the server!");
    }
    
    public static void sendToClient(MessageAPI<?> msg, String uuid) {
        sendToClient(msg,false,uuid);
    }
    
    public static void sendToClient(MessageAPI<?> msg, boolean login, String uuid) {
        MinecraftServerAPI<?> server = ServerHelper.getAPI();
        if(Objects.nonNull(server)) {
            PlayerAPI<?,?> player = server.getPlayerByUUID(uuid);
            sendToClient(msg,login,player);
        } else MTRef.logError("Failed to send message to the client since the server is null!");
    }
            
    public static <P> void sendToClient(MessageAPI<?> msg, boolean login, @Nullable PlayerAPI<P,?> player) {
        finalizePlayerMsg(msg,login,Objects.nonNull(player) ? player.getUUID().toString() : null);
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
    
    public static boolean send(MessageAPI<?> msg, ChannelHelper helper, boolean login) {
        if(helper.isClient()) sendToServer(msg,login);
        else {
            String uuid = helper.getPlayerID();
            if(Objects.isNull(uuid)) return false;
            sendToClient(msg,login,uuid);
        }
        return true;
    }
    
    private static void finalizePlayerMsg(MessageAPI<?> msg, boolean login, @Nullable String uuid) {
        if(msg instanceof PlayerMessage<?>) {
            PlayerMessage<?> playerMsg = (PlayerMessage<?>)msg;
            playerMsg.setLogin(login);
            if(Objects.nonNull(uuid)) playerMsg.setUuid(uuid);
        }
    }
}