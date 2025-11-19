package ft.keraune.funtalismans.commands;

import ft.keraune.funtalismans.FunTalismans;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class TalismanTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {

        List<String> list = new ArrayList<>();

        // /talisman <subcommand>
        if (args.length == 1) {
            if (sender.hasPermission("funtalismans.give")) list.add("give");
            if (sender.hasPermission("funtalismans.reload")) list.add("reload");
            return filter(list, args[0]);
        }

        // /talisman give <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null; // Bukkit autocompleta jugadores
        }

        // /talisman give <player> <id>
        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            list.addAll(FunTalismans.getInstance().getTalismanManager().getTalismanIds());
            return filter(list, args[2]);
        }

        return list;
    }

    private List<String> filter(List<String> list, String arg) {
        if (arg.isEmpty()) return list;
        List<String> filtered = new ArrayList<>();
        for (String s : list) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) filtered.add(s);
        }
        return filtered;
    }
}
