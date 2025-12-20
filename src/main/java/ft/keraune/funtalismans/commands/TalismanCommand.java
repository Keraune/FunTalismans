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
            send(sender, plugin.getMessageManager().getMessage("usage_force_update"));
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

            logHeader("§9Reloaded configuration!");
            logLine("§2Reloaded §fconfig.conf!");
            logLine("§2Reloaded §ftalismans.conf!");
            logLine("§2Reloaded §frarities.conf!");

            plugin.reloadAll();
            plugin.getTalismanManager().updateTalismanContainers();

            int talismanCount = plugin.getTalismanManager().getTalismans().size();
            int rarityCount = plugin.getRarityManager().getRarities().size();
            int updatedPlayers = plugin.getTalismanManager().getLastReloadUpdatedPlayers();

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

            if (args.length < 3 || args.length > 4) {
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

            int amount = 1;

            if (args.length == 4) {
                try {
                    amount = Integer.parseInt(args[3]);

                    if (amount <= 0) {
                        send(sender, plugin.getMessageManager()
                                .getMessage("amount_must_be_positive"));
                        return true;
                    }

                } catch (NumberFormatException e) {
                    send(sender, plugin.getMessageManager()
                            .getMessage("invalid_amount"));
                    return true;
                }
            }

            ItemStack item = TalismanItemBuilder.build(t);
            item.setAmount(amount);

            // Manejo seguro del inventario (stacks grandes)
            var leftover = target.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                send(sender, plugin.getMessageManager().getMessage("inventory_full"));
                return true;
            }

            // Mensaje de éxito
            send(sender, plugin.getMessageManager().getMessage(
                    "give_success",
                    Map.of(
                            "talisman", id,
                            "player", target.getName(),
                            "amount", String.valueOf(amount)
                    )
            ));

            return true;
        }

        // ----------------------
        // /talisman forceupdate (NUEVO)
        // ----------------------
        if (args[0].equalsIgnoreCase("forceupdate")) {
            if (!sender.hasPermission("funtalismans.reload")) {
                send(sender, plugin.getMessageManager().getMessage("no_permission"));
                return true;
            }

            send(sender, plugin.getMessageManager().getMessage("force_update_success"));
            plugin.getTalismanManager().forceUpdateAll();

            // Actualizar todo
            for (Player player : Bukkit.getOnlinePlayers()) {
                plugin.getTalismanManager().updateInventoryTalismansQuietly(player.getInventory());
                plugin.getTalismanManager().updateInventoryTalismansQuietly(player.getEnderChest());
            }

            plugin.getTalismanManager().updateTalismanContainers();

            send(sender, plugin.getMessageManager().getMessage("update_success"));
            return true;
        }

        send(sender, plugin.getMessageManager().getMessage("unknown_subcommand"));
        return true;
    }

    private void send(CommandSender sender, String msg) {
        sender.sendMessage(plugin.getPrefix() + msg);
    }

    private void logHeader(String title) {
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