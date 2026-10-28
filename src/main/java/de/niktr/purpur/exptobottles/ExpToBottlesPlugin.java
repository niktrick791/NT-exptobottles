package de.niktr.purpur.exptobottles;

import de.niktr.purpur.exptobottles.bottle.BottleManager;
import de.niktr.purpur.exptobottles.commands.BottleCommand;
import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.listeners.BottleListener;
import de.niktr.purpur.exptobottles.vacuum.XpVacuumManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExpToBottlesPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private BottleManager bottleManager;
    private XpVacuumManager vacuumManager;

    @Override
    public void onEnable() {
        // Save and load config
        this.configManager = new ConfigManager(this);
        this.bottleManager = new BottleManager(this, configManager);
        this.vacuumManager = new XpVacuumManager(this, configManager);

        // Start vacuum task for fast XP pickup at farms
        this.vacuumManager.start();

        // Register event listener (compatible across 1.8 through 1.21.4+)
        getServer().getPluginManager().registerEvents(new BottleListener(this, bottleManager, configManager, vacuumManager), this);

        // Register command via standard Bukkit API
        BottleCommand bottleCmd = new BottleCommand(bottleManager, configManager, vacuumManager);
        PluginCommand cmd = getCommand("bottle");
        if (cmd != null) {
            cmd.setExecutor(bottleCmd);
            cmd.setTabCompleter(bottleCmd);
        } else {
            getLogger().warning("Befehl 'bottle' konnte nicht registriert werden. Bitte plugin.yml prüfen.");
        }

        getLogger().info("=========================================");
        getLogger().info(" ExpToBottles v" + getDescription().getVersion() + " wurde erfolgreich aktiviert!");
        getLogger().info(" Multi-Version Support: Minecraft 1.8 - 1.21.4+");
        getLogger().info("=========================================");
    }

    @Override
    public void onDisable() {
        if (vacuumManager != null) {
            vacuumManager.stop();
        }
        getLogger().info("ExpToBottles wurde deaktiviert.");
    }

    public ConfigManager getConfiguration() {
        return configManager;
    }

    public BottleManager getBottleManager() {
        return bottleManager;
    }

    public XpVacuumManager getVacuumManager() {
        return vacuumManager;
    }
}
