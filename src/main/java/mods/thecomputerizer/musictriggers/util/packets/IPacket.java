package mods.thecomputerizer.musictriggers.util.packets;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface IPacket {

    Identifier getID();

    PacketByteBuf encode();
}
