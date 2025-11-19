package ft.keraune.funtalismans.commands;

import ft.keraune.funtalismans.FunTalismans;
import ft.keraune.funtalismans.api.Talisman;
import ft.keraune.funtalismans.items.TalismanItemBuilder;
import ft.keraune.funtalismans.utils.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class TalismanCommand implements CommandExecutor {

    private final FunTalismans plugin = FunTalismans.getInstance();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length == 0) {
            send(sender, plugin.getMessageManager().getMessage("usage_give_cmd"));
            send(sender, plugin.getMessageManager().getMessage("usage_reload"));
            return true;
        }

        // ----------------------
        // /talisman reload
        // ----------------------
        if (args[0].equalsIgnoreCase("reload")) {

            if (!sender.hasPermission("funtalismans.reload")) {
                send(sender, plugin.getMessageManager().getMessage("no_permission"));
                return true;
            }

            long ms = System.currentTimeMillis();

            // LOGS HARCODEADOS
            logHeader("§9Reloaded configuration!");
            logLine("§2Reloaded §fconfig.conf!");
            logLine("§2Reloaded §ftalismans.conf!");
            logLine("§2Reloaded §frarities.conf!");

            plugin.reloadAll();

            int talismanCount = plugin.getTalismanManager().getTalismans().size();
            int rarityCount = plugin.getRarityManager().getRarities().size();
            int updatedPlayers = plugin.getTalismanManager().getLastReloadUpdatedPlayers();

            // LOGS HARCODEADOS con placeholders
            logLine("§2Loaded §f" + talismanCount + " §2talisman(s)!");
            logLine("§2Loaded §f" + rarityCount + " §2rarity(ies)!");
            logLine("§2Updated §f" + updatedPlayers + " §2player(s)!");
            logFooter("§2Successful reload!");

            send(sender, plugin.getMessageManager().getMessage("reload_success",
                    Map.of("ms", String.valueOf(System.currentTimeMillis() - ms))));
            return true;
        }

        // ----------------------
        // /talisman give
        // ----------------------
        if (args[0].equalsIgnoreCase("give")) {

            if (!sender.hasPermission("funtalismans.give")) {
                send(sender, plugin.getMessageManager().getMessage("no_permission"));
                return true;
            }

            if (args.length < 3) {
                send(sender, plugin.getMessageManager().getMessage("usage_give"));
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                send(sender, plugin.getMessageManager().getMessage("player_not_found"));
                return true;
            }

            String id = args[2];
            Talisman t = plugin.getTalismanManager().getTalisman(id);

            if (t == null) {
                send(sender, plugin.getMessageManager().getMessage("talisman_not_found"));
                return true;
            }

            ItemStack item = TalismanItemBuilder.build(t);

            if (target.getInventory().firstEmpty() == -1) {
                send(sender, plugin.getMessageManager().getMessage("inventory_full"));
                return true;
            }

            target.getInventory().addItem(item);

            send(sender, plugin.getMessageManager().getMessage("give_success",
                    Map.of("talisman", id, "player", target.getName())));
            return true;
        }

        send(sender, plugin.getMessageManager().getMessage("unknown_subcommand"));
        return true;
    }

    // ----------------------
    // Util: mensajes uniformes
    // ----------------------
    private void send(CommandSender sender, String msg) {
        // msg ya viene con colores procesados por MessageManager.getMessage()
        sender.sendMessage(plugin.getPrefix() + msg);
    }

    // ----------------------
    // Util: logs para consola
    // ----------------------
    private void logHeader(String title) {
        // PREFIJO HARCODEADO EN CELESTE para consola
        Bukkit.getConsoleSender().sendMessage("§b[FunTalismans] §9" + title);
        Bukkit.getConsoleSender().sendMessage("§b╔════════════════════════════════════");
    }

    private void logLine(String msg) {
        Bukkit.getConsoleSender().sendMessage("§b╠ §2" + msg);
    }

    private void logFooter(String msg) {
        Bukkit.getConsoleSender().sendMessage("§b╚ §2" + msg);
    }
}