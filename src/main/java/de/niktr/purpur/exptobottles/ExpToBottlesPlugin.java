package de.niktr.purpur.exptobottles;

import de.niktr.purpur.exptobottles.bottle.BottleManager;
import de.niktr.purpur.exptobottles.commands.BottleCommand;
import de.niktr.purpur.exptobottles.config.ConfigManager;
import de.niktr.purpur.exptobottles.listeners.BottleListener;
import de.niktr.purpur.exptobottles.vacuum.XpVacuumManager;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

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

        // Register event listener
        getServer().getPluginManager().registerEvents(new BottleListener(bottleManager, configManager, vacuumManager), this);

        // Register commands with Paper's modern LifecycleEventManager (Paper 1.20.6+ / 1.21+ / Purpur)
        BottleCommand bottleCmd = new BottleCommand(bottleManager, configManager, vacuumManager);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                "bottle",
                "Fülle deine Erfahrung in Flaschen ab",
                List.of("bottlexp", "xpbottle", "expbottle"),
                bottleCmd
            );
        });

        getLogger().info("ExpToBottles für Purpur MC 1.26.2 wurde erfolgreich aktiviert!");
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
