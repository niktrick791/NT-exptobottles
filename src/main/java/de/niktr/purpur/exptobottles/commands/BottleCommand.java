package de.niktr.purpur.exptobottles.commands;

import de.niktr.purpur.exptobottles.bottle.BottleManager;
import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import de.niktr.purpur.exptobottles.vacuum.XpVacuumManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BottleCommand implements CommandExecutor, TabCompleter {

    private final BottleManager bottleManager;
    private final ConfigManager config;
    private final XpVacuumManager vacuumManager;

    public BottleCommand(BottleManager bottleManager, ConfigManager config, XpVacuumManager vacuumManager) {
        this.bottleManager = bottleManager;
        this.config = config;
        this.vacuumManager = vacuumManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Admin reload command
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("exptobottles.admin")) {
                sender.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return true;
            }
            config.loadConfig();
            if (vacuumManager != null) {
                vacuumManager.start();
            }
            sender.sendMessage(config.getPrefixedMessage(config.getReloaded()));
            return true;
        }

        // Must be player for all other commands
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("exptobottles.use")) {
            player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(config.getPrefixedMessage(config.getUsage()));
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // Toggle vacuum pickup
        if (sub.equals("vacuum") || sub.equals("pickup") || sub.equals("fast")) {
            boolean active = vacuumManager.toggleVacuum(player);
            if (active) {
                player.sendMessage(config.getPrefixedMessage(config.getVacuumEnabled()));
            } else {
                player.sendMessage(config.getPrefixedMessage(config.getVacuumDisabled()));
            }
            return true;
        }

        // Admin spawn XP orbs
        if ((sub.equals("spawn") || sub.equals("generate")) && args.length >= 2) {
            if (!player.hasPermission("exptobottles.admin")) {
                player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return true;
            }
            try {
                int amount = Integer.parseInt(args[1]);
                if (amount <= 0) return true;
                ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class);
                orb.setExperience(amount);
                player.sendMessage(config.getPrefixedMessage(config.getXpSpawned(), "xp", amount));
                return true;
            } catch (NumberFormatException ignored) {}
        }

        // Bottle all XP
        if (sub.equals("all") || sub.equals("max")) {
            int total = ExperienceManager.getPlayerTotalExp(player);
            if (total <= 0) {
                player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", 0));
                return true;
            }
            bottleManager.bottleExp(player, total);
            return true;
        }

        // Bottle until target level
        if (sub.equals("until") && args.length >= 2) {
            try {
                int targetLevel = Integer.parseInt(args[1]);
                int currentLevel = player.getLevel();
                if (targetLevel >= currentLevel) {
                    player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", ExperienceManager.getPlayerTotalExp(player)));
                    return true;
                }
                int targetExp = ExperienceManager.getTotalExpAtLevel(targetLevel);
                int currentExp = ExperienceManager.getPlayerTotalExp(player);
                int diff = currentExp - targetExp;
                if (diff > 0) {
                    bottleManager.bottleExp(player, diff);
                }
                return true;
            } catch (NumberFormatException e) {
                player.sendMessage(config.getPrefixedMessage(config.getUsage()));
                return true;
            }
        }

        // Level notation e.g. "1l", "5l", "30l"
        if (sub.endsWith("l") || sub.endsWith("lvl") || sub.endsWith("levels")) {
            try {
                String clean = sub.replace("levels", "").replace("lvl", "").replace("l", "");
                int levels = Integer.parseInt(clean);
                if (levels <= 0) return true;
                int expNeeded = ExperienceManager.getExpForLevelsDown(player, levels);
                if (expNeeded <= 0) {
                    player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", ExperienceManager.getPlayerTotalExp(player)));
                    return true;
                }
                bottleManager.bottleExp(player, expNeeded);
                return true;
            } catch (NumberFormatException ignored) {}
        }

        // Direct XP number
        try {
            int amount = Integer.parseInt(sub);
            bottleManager.bottleExp(player, amount);
        } catch (NumberFormatException e) {
            player.sendMessage(config.getPrefixedMessage(config.getUsage()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);
            List<String> options = new ArrayList<>();
            options.add("all");
            options.add("until");
            options.add("vacuum");
            options.add("1l");
            options.add("5l");
            options.add("10l");
            options.add("30l");
            options.add("100");
            options.add("500");
            options.add("1000");

            if (sender.hasPermission("exptobottles.admin")) {
                options.add("reload");
                options.add("spawn");
                options.add("generate");
            }

            for (String opt : options) {
                if (opt.toLowerCase(Locale.ROOT).startsWith(input)) {
                    completions.add(opt);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("until")) {
            String input = args[1].toLowerCase(Locale.ROOT);
            List<String> levels = Arrays.asList("0", "15", "30", "50");
            for (String lvl : levels) {
                if (lvl.startsWith(input)) {
                    completions.add(lvl);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("generate"))) {
            if (sender.hasPermission("exptobottles.admin")) {
                String input = args[1].toLowerCase(Locale.ROOT);
                List<String> amounts = Arrays.asList("100", "500", "1000", "5000", "10000");
                for (String amt : amounts) {
                    if (amt.startsWith(input)) {
                        completions.add(amt);
                    }
                }
            }
        }

        return completions;
    }
}
