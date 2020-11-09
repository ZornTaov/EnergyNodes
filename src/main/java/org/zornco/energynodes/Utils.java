package org.zornco.energynodes;

import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class Utils {

    public static final Codec<List<BlockPos>> LBPCODEC = Codec.list(BlockPos.CODEC);
    public static void sendMessage(PlayerEntity player, String text) {
        ((ServerPlayerEntity)player).func_241151_a_(new StringTextComponent(text), ChatType.CHAT, Util.DUMMY_UUID);
    }
    public static void sendSystemMessage(PlayerEntity player, String text) {
        ((ServerPlayerEntity)player).func_241151_a_(new StringTextComponent(text), ChatType.GAME_INFO, Util.DUMMY_UUID);
    }

    public static String getCoordinatesAsString(Vector3i vec) {
        return "" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }
}
