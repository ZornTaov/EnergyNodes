package org.zornco.energynodes;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class Utils {

    public static void sendMessage(PlayerEntity player, String text) {
        ((ServerPlayerEntity)player).func_241151_a_(new StringTextComponent(text), ChatType.CHAT, Util.DUMMY_UUID);
    }
    public static void sendSystemMessage(PlayerEntity player, String text) {
        ((ServerPlayerEntity)player).func_241151_a_(new StringTextComponent(text), ChatType.GAME_INFO, Util.DUMMY_UUID);
    }
}
