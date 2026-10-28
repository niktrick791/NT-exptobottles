package de.niktr.purpur.exptobottles.commands;

import de.niktr.purpur.exptobottles.bottle.BottleManager;
import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.experience.ExperienceManager;
import de.niktr.purpur.exptobottles.vacuum.XpVacuumManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class BottleCommand implements BasicCommand, CommandExecutor, TabCompleter {

    private final BottleManager bottleManager;
    private final ConfigManager config;
    private final XpVacuumManager vacuumManager;

    public BottleCommand(BottleManager bottleManager, ConfigManager config, XpVacuumManager vacuumManager) {
        this.bottleManager = bottleManager;
        this.config = config;
        this.vacuumManager = vacuumManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        onCommand(stack.getSender(), null, "bottle", args);
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        List<String> completions = onTabComplete(stack.getSender(), null, "bottle", args);
        return completions != null ? completions : List.of();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("exptobottles.use") || sender.hasPermission("exptobottles.admin");
    }

    @Override
    public String permission() {
        return "exptobottles.use";
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @Nullable Command command, @NotNull String label, @NotNull String[] args) {
        // Admin reload command
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("exptobottles.admin")) {
                sender.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return true;
            }
            config.load();
            if (vacuumManager != null) {
                vacuumManager.start();
            }
            sender.sendMessage(config.getPrefixedMessage(config.getReloaded()));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.getPrefixedMessage("<red>Dieser Befehl kann nur von Spielern ausgeführt werden!</red>"));
            return true;
        }

        // Toggle Vacuum command
        if (args.length > 0 && (args[0].equalsIgnoreCase("vacuum") || args[0].equalsIgnoreCase("toggle"))) {
            if (!player.hasPermission("exptobottles.use")) {
                player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return true;
            }
            if (vacuumManager != null) {
                boolean enabled = vacuumManager.toggleVacuum(player);
                if (enabled) {
                    player.sendMessage(config.getPrefixedMessage(config.getVacuumEnabled()));
                } else {
                    player.sendMessage(config.getPrefixedMessage(config.getVacuumDisabled()));
                }
            }
            return true;
        }

        // Spawn / Generate XP command for server testing
        if (args.length > 0 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("generate"))) {
            if (!player.hasPermission("exptobottles.admin")) {
                player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
                return true;
            }
            int spawnAmount = 100;
            if (args.length >= 2) {
                try {
                    spawnAmount = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(config.getPrefixedMessage("<red>Ungültige XP-Menge: " + args[1] + "</red>"));
                    return true;
                }
            }
            if (spawnAmount <= 0) {
                player.sendMessage(config.getPrefixedMessage("<red>Menge muss größer als 0 sein!</red>"));
                return true;
            }

            final int finalAmount = spawnAmount;
            ExperienceOrb orb = player.getWorld().spawn(player.getLocation(), ExperienceOrb.class, o -> o.setExperience(finalAmount));
            player.sendMessage(config.getPrefixedMessage(config.getXpSpawned(), "xp", finalAmount));
            return true;
        }

        if (!player.hasPermission("exptobottles.use")) {
            player.sendMessage(config.getPrefixedMessage(config.getNoPermission()));
            return true;
        }

        int playerTotal = ExperienceManager.getPlayerTotalExp(player);

        if (args.length == 0) {
            // Default action: bottle 1 level down
            if (player.getLevel() > 0) {
                int expToBottle = ExperienceManager.getExpForLevelsDown(player, 1);
                bottleManager.bottleExp(player, expToBottle);
            } else if (playerTotal > 0) {
                bottleManager.bottleExp(player, playerTotal);
            } else {
                player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", 0));
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("all")) {
            if (playerTotal <= 0) {
                player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", 0));
                return true;
            }
            bottleManager.bottleExp(player, playerTotal);
            return true;
        }

        if (sub.equals("until") && args.length >= 2) {
            try {
                int targetLevel = Integer.parseInt(args[1]);
                if (targetLevel < 0) {
                    player.sendMessage(config.getPrefixedMessage("<red>Level muss 0 oder höher sein!</red>"));
                    return true;
                }
                int targetExp = ExperienceManager.getTotalExpAtLevel(targetLevel);
                int expDifference = playerTotal - targetExp;
                if (expDifference <= 0) {
                    player.sendMessage(config.getPrefixedMessage("<red>Du bist bereits auf oder unter Level " + targetLevel + "!</red>"));
                    return true;
                }
                bottleManager.bottleExp(player, expDifference);
            } catch (NumberFormatException e) {
                player.sendMessage(config.getPrefixedMessage("<red>Ungültiges Level: " + args[1] + "</red>"));
            }
            return true;
        }

        // Check if argument ends with 'l' (e.g. 5l, 30l)
        if (sub.endsWith("l")) {
            String numStr = sub.substring(0, sub.length() - 1);
            try {
                int levels = Integer.parseInt(numStr);
                if (levels <= 0) {
                    player.sendMessage(config.getPrefixedMessage("<red>Level-Anzahl muss größer als 0 sein!</red>"));
                    return true;
                }
                int expToTake = ExperienceManager.getExpForLevelsDown(player, levels);
                if (expToTake <= 0) {
                    player.sendMessage(config.getPrefixedMessage(config.getNotEnoughXp(), "xp", playerTotal));
                    return true;
                }
                bottleManager.bottleExp(player, expToTake);
            } catch (NumberFormatException e) {
                player.sendMessage(config.getPrefixedMessage(config.getUsage()));
            }
            return true;
        }

        // Plain number (e.g. /bottle 500)
        try {
            int amount = Integer.parseInt(args[0]);
            if (amount <= 0) {
                player.sendMessage(config.getPrefixedMessage("<red>XP-Menge muss größer als 0 sein!</red>"));
                return true;
            }
            bottleManager.bottleExp(player, amount);
        } catch (NumberFormatException e) {
            player.sendMessage(config.getPrefixedMessage(config.getUsage()));
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @Nullable Command command, @NotNull String label, @NotNull String[] args) {
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
            List<String> levels = List.of("0", "15", "30", "50");
            for (String lvl : levels) {
                if (lvl.startsWith(input)) {
                    completions.add(lvl);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("generate"))) {
            if (sender.hasPermission("exptobottles.admin")) {
                String input = args[1].toLowerCase(Locale.ROOT);
                List<String> amounts = List.of("100", "500", "1000", "5000", "10000");
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
