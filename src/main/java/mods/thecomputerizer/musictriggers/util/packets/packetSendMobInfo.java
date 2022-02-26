package mods.thecomputerizer.musictriggers.util.packets;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.musictriggers.common.calculateFeature;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class packetSendMobInfo implements IMessageHandler<packetSendMobInfo.packetSendMobInfoMessage, IMessage> {

    @Override
    public IMessage onMessage(packetSendMobInfo.packetSendMobInfoMessage message, MessageContext ctx)
    {
        if(message.getDataUUID()==null) {
            return null;
        }
        calculateFeature.calculateMobAndSend(message.getDataTriggerName(), message.getDataUUID(), message.getMobName(), message.getDetectionRange(),
                message.getTargettingBoolean(), message.getHordeTargettingPercentage(), message.getHealth(),
                message.getHealthTargettingPercentage(), message.getVictoryBoolean(), message.getVictoryID(), message.getInfernalID(),
                message.getMobNumber(), message.getTime(), message.getTimeOut());
        return null;
    }

    public static class packetSendMobInfoMessage implements IMessage {
        String s;

        public packetSendMobInfoMessage() {}

        public packetSendMobInfoMessage(String trigger, UUID u, String n, String r, String t, String tp, String h, String hp, String v, String vi, String i, String num, int time, int to)
        {
            this.s = trigger+","+u.toString()+","+n+","+r+","+t+","+tp+","+h+","+hp+","+v+","+vi+","+i+","+num+","+time+","+to;
        }

        @Override
        public void fromBytes(ByteBuf buf)
        {
            this.s = ((String) buf.readCharSequence(buf.readableBytes(), StandardCharsets.UTF_8));
        }

        @Override
        public void toBytes(ByteBuf buf)
        {
            buf.writeCharSequence(s, StandardCharsets.UTF_8);
        }
        public String getDataTriggerName() {
            if(s==null) {
                return null;
            }
            return stringBreaker(s)[0];
        }
        public UUID getDataUUID() {
            return UUID.fromString(stringBreaker(s)[1]);
        }
        public String getMobName() {
            return stringBreaker(s)[2];
        }
        public Integer getDetectionRange() {
            return Integer.parseInt(stringBreaker(s)[3]);
        }
        public Boolean getTargettingBoolean() {
            return Boolean.parseBoolean(stringBreaker(s)[4]);
        }
        public Integer getHordeTargettingPercentage() {
            return Integer.parseInt(stringBreaker(s)[5]);
        }
        public Integer getHealth() {
            return Integer.parseInt(stringBreaker(s)[6]);
        }
        public Integer getHealthTargettingPercentage() {
            return Integer.parseInt(stringBreaker(s)[7]);
        }
        public Boolean getVictoryBoolean() {
            return Boolean.parseBoolean(stringBreaker(s)[8]);
        }
        public Integer getVictoryID() {
            return Integer.parseInt(stringBreaker(s)[9]);
        }
        public String getInfernalID() {
            return stringBreaker(s)[10];
        }
        public Integer getMobNumber() {
            return Integer.parseInt(stringBreaker(s)[11]);
        }
        public Integer getTime() {
            return Integer.parseInt(stringBreaker(s)[12]);
        }
        public Integer getTimeOut() {
            return Integer.parseInt(stringBreaker(s)[13]);
        }

        public static String[] stringBreaker(String s) {
            return s.split(",");
        }
    }
}
