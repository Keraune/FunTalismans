package ft.keraune.funtalismans.utils;

import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.command.CommandSender;

public class MessageUtil {

    public static void send(CommandSender sender, String message) {
        String prefix = FunTalismans.getInstance().getPrefix();
        sender.sendMessage(TextUtil.color(prefix + " " + message));
    }

    public static String format(String message) {
        String prefix = FunTalismans.getInstance().getPrefix();
        return TextUtil.color(prefix + " " + message);
    }

    public static String color(String message) {
        return TextUtil.color(message);
    }
}

