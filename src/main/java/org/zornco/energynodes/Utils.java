package org.zornco.energynodes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;

public class Utils {

    public static void sendMessage(PlayerEntity player, ITextComponent text) {
        ((ServerPlayerEntity)player).func_241151_a_(text, ChatType.CHAT, Util.DUMMY_UUID);
    }
    public static void sendSystemMessage(PlayerEntity player, ITextComponent text) {
        ((ServerPlayerEntity)player).func_241151_a_(text, ChatType.GAME_INFO, Util.DUMMY_UUID);
    }
}
