package com.stratecide.potion_config;

import net.minecraft.network.PacketByteBuf;

import java.util.*;

public record PotionColorList(Map<Integer, Integer> collection) {
    public static PotionColorList readPotionColors(PacketByteBuf buf) {
        int count = buf.readVarInt();
        Map<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(buf.readVarInt(), buf.readVarInt());
        }
        return new PotionColorList(map);
    }
    public static void writePotionColors(PacketByteBuf buf, PotionColorList self) {
        buf.writeVarInt(self.collection.size());
        for (Map.Entry<Integer, Integer> entry : self.collection.entrySet()) {
            buf.writeVarInt(entry.getKey());
            buf.writeVarInt(entry.getValue());
        }
    }
}
