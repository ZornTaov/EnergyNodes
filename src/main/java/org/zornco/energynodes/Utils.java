package org.zornco.energynodes;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import java.util.List;

public class Utils {
    public static final Codec<List<BlockPos>> BLOCK_POS_LIST_CODEC = Codec.list(BlockPos.CODEC );
    public static final Codec<Direction> DIRECTION_CODEC = StringRepresentable.fromEnum(Direction::values, Direction::byName);
    public static final Codec<List<Direction>> DIRECTION_LIST_CODEC = Codec.list(DIRECTION_CODEC);

    public static void sendMessage(Player player, String text) {
        ((ServerPlayer)player).sendMessage(new TextComponent(text), ChatType.CHAT, Util.NIL_UUID);
    }

    public static void sendSystemMessage(Player player, String text) {
        sendSystemMessage(player, new TextComponent(text));
    }

    public static void sendSystemMessage(Player player, Component text) {
        ((ServerPlayer)player).sendMessage(text, ChatType.GAME_INFO, Util.NIL_UUID);
    }

    public static void SendSystemMessage(@Nonnull UseOnContext context, Component s) {
        if (!context.getLevel().isClientSide) {
            sendSystemMessage(context.getPlayer(), s);
        }
    }

    public static String getCoordinatesAsString(Vec3i vec) {
        return "" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }

    @Nonnull
    public static Direction getFacingFromBlockPos(@Nonnull BlockPos pos, @Nonnull BlockPos neighbor) {
        return Direction.getNearest(
                (float) (pos.getX() - neighbor.getX()),
                (float) (pos.getY() - neighbor.getY()),
                (float) (pos.getZ() - neighbor.getZ()));
    }
}
