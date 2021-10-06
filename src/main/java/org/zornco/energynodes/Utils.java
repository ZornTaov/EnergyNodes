package org.zornco.energynodes;

import com.mojang.serialization.Codec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class Utils {
    public static final Codec<List<BlockPos>> BLOCK_POS_LIST_CODEC = Codec.list(BlockPos.CODEC );

    public static void sendMessage(PlayerEntity player, String text) {
        ((ServerPlayerEntity)player).sendMessage(new StringTextComponent(text), ChatType.CHAT, Util.NIL_UUID);
    }

    public static void sendSystemMessage(PlayerEntity player, String text) {
        sendSystemMessage(player, new StringTextComponent(text));
    }

    public static void sendSystemMessage(PlayerEntity player, ITextComponent text) {
        ((ServerPlayerEntity)player).sendMessage(text, ChatType.GAME_INFO, Util.NIL_UUID);
    }

    public static void SendSystemMessage(@Nonnull ItemUseContext context, ITextComponent s) {
        if (!context.getLevel().isClientSide) {
            sendSystemMessage(context.getPlayer(), s);
        }
    }

    public static String getCoordinatesAsString(Vector3i vec) {
        return "" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }
}
