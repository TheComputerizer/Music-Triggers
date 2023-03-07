package mods.thecomputerizer.musictriggers.network.packets;


import net.minecraft.network.FriendlyByteBuf;

public interface IPacket {

    FriendlyByteBuf encode();
}
