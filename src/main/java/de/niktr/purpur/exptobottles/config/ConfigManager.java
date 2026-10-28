package de.niktr.purpur.exptobottles.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    // Bottling settings
    private boolean requireEmptyBottle;
    private boolean returnEmptyBottleOnConsume;
    private int minXp;
    private int maxXpPerBottle;
    private double taxPercent;
    private boolean sneakRightClickToFill;
    private String sneakFillMode;
    private boolean directConsumeOnRightClick;
    private boolean allowThrow;

    // Fast Pickup / Vacuum settings
    private boolean fastPickupEnabled;
    private double vacuumRadius;
    private double instantPickupRadius;
    private int intervalTicks;
    private int maxOrbsPerBatch;
    private boolean pullEffect;
    private boolean repairMending;
    private boolean playSoundOnPickup;
    private boolean particlesOnPickup;

    // Effects
    private boolean soundsEnabled;
    private String soundFill;
    private String soundConsume;
    private boolean particlesEnabled;

    // Item appearance
    private String itemName;
    private List<String> itemLore;

    // Messages
    private String prefix;
    private String notEnoughXp;
    private String noBottle;
    private String bottledSuccess;
    private String bottledSuccessTax;
    private String consumedSuccess;
    private String minXpError;
    private String maxXpError;
    private String noPermission;
    private String reloaded;
    private String usage;
    private String xpSpawned;
    private String vacuumEnabled;
    private String vacuumDisabled;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        // Bottling
        this.requireEmptyBottle = config.getBoolean("bottling.require-empty-bottle", true);
        this.returnEmptyBottleOnConsume = config.getBoolean("bottling.return-empty-bottle-on-consume", true);
        this.minXp = config.getInt("bottling.min-xp", 1);
        this.maxXpPerBottle = config.getInt("bottling.max-xp-per-bottle", -1);
        this.taxPercent = config.getDouble("bottling.tax-percent", 0.0);
        this.sneakRightClickToFill = config.getBoolean("bottling.sneak-right-click-to-fill", true);
        this.sneakFillMode = config.getString("bottling.sneak-fill-mode", "LEVEL");
        this.directConsumeOnRightClick = config.getBoolean("bottling.direct-consume-on-right-click", true);
        this.allowThrow = config.getBoolean("bottling.allow-throw", true);

        // Fast Pickup
        this.fastPickupEnabled = config.getBoolean("fast-pickup.enabled", true);
        this.vacuumRadius = config.getDouble("fast-pickup.vacuum-radius", 5.0);
        this.instantPickupRadius = config.getDouble("fast-pickup.instant-pickup-radius", 2.5);
        this.intervalTicks = config.getInt("fast-pickup.interval-ticks", 2);
        this.maxOrbsPerBatch = config.getInt("fast-pickup.max-orbs-per-batch", 50);
        this.pullEffect = config.getBoolean("fast-pickup.pull-effect", true);
        this.repairMending = config.getBoolean("fast-pickup.repair-mending", true);
        this.playSoundOnPickup = config.getBoolean("fast-pickup.play-sound", true);
        this.particlesOnPickup = config.getBoolean("fast-pickup.particles", true);

        // Effects
        this.soundsEnabled = config.getBoolean("effects.enabled", true);
        this.soundFill = config.getString("effects.sound-fill", "ITEM_BOTTLE_FILL");
        this.soundConsume = config.getString("effects.sound-consume", "ENTITY_PLAYER_LEVELUP");
        this.particlesEnabled = config.getBoolean("effects.particles", true);

        // Item
        this.itemName = config.getString("item.name", "<gradient:#00FFAA:#00AAFF><bold>Erfahrungsflasche</bold></gradient>");
        this.itemLore = config.getStringList("item.lore");
        if (this.itemLore.isEmpty()) {
            this.itemLore = List.of(
                    "<gray>Gespeicherte XP: <green><bold>{xp}</bold></green> <dark_gray>(ca. {levels} Lvl)</dark_gray>",
                    "<gray>Abgefüllt von: <yellow>{player}</yellow>",
                    "<dark_gray>---------------------------</dark_gray>",
                    "<yellow>Rechtsklick:</yellow> <white>XP direkt trinken</white>",
                    "<yellow>Werfen:</yellow> <white>XP Flasche werfen</white>"
            );
        }

        // Messages
        this.prefix = config.getString("messages.prefix", "<dark_gray>[<gradient:#00FFAA:#00AAFF>ExpBottles</gradient>]</dark_gray> ");
        this.notEnoughXp = config.getString("messages.not-enough-xp", "<red>Du hast nicht genug Erfahrung! Du besitzt nur <yellow>{xp}</yellow> XP.</red>");
        this.noBottle = config.getString("messages.no-bottle", "<red>Du benötigst eine leere Glasflasche in der Hand oder im Inventar!</red>");
        this.bottledSuccess = config.getString("messages.bottled-success", "<green>Erfolgreich <gold>{xp} XP</gold> in eine Flasche abgefüllt!</green>");
        this.bottledSuccessTax = config.getString("messages.bottled-success-tax", "<green>Erfolgreich <gold>{xp} XP</gold> abgefüllt! <gray>(Steuer: {tax} XP)</gray></green>");
        this.consumedSuccess = config.getString("messages.consumed-success", "<green>Du hast <gold>{xp} XP</gold> erhalten!</green>");
        this.minXpError = config.getString("messages.min-xp-error", "<red>Die Mindestmenge zum Abfüllen beträgt <yellow>{min}</yellow> XP.</red>");
        this.maxXpError = config.getString("messages.max-xp-error", "<red>Die Höchstmenge pro Flasche beträgt <yellow>{max}</yellow> XP.</red>");
        this.noPermission = config.getString("messages.no-permission", "<red>Dazu hast du keine Berechtigung!</red>");
        this.reloaded = config.getString("messages.reloaded", "<green>Konfiguration wurde erfolgreich neu geladen!</green>");
        this.usage = config.getString("messages.usage", "<yellow>Verwendung: <white>/bottle [amount | all | Xl | until Y | vacuum | spawn X]</white></yellow>");
        this.xpSpawned = config.getString("messages.xp-spawned", "<green>Es wurden <gold>{xp} XP</gold> am Standort gespawnt!</green>");
        this.vacuumEnabled = config.getString("messages.vacuum-enabled", "<green>XP-Schnelleinsammler wurde <yellow>aktiviert</yellow>!</green>");
        this.vacuumDisabled = config.getString("messages.vacuum-disabled", "<red>XP-Schnelleinsammler wurde <yellow>deaktiviert</yellow>!</red>");
    }

    public Component parse(String message, Object... replacements) {
        String formatted = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formatted = formatted.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
            }
        }
        return miniMessage.deserialize(formatted);
    }

    public Component getPrefixedMessage(String rawMessage, Object... replacements) {
        return miniMessage.deserialize(prefix).append(parse(rawMessage, replacements));
    }

    // Getters
    public boolean isRequireEmptyBottle() { return requireEmptyBottle; }
    public boolean isReturnEmptyBottleOnConsume() { return returnEmptyBottleOnConsume; }
    public int getMinXp() { return minXp; }
    public int getMaxXpPerBottle() { return maxXpPerBottle; }
    public double getTaxPercent() { return taxPercent; }
    public boolean isSneakRightClickToFill() { return sneakRightClickToFill; }
    public String getSneakFillMode() { return sneakFillMode; }
    public boolean isDirectConsumeOnRightClick() { return directConsumeOnRightClick; }
    public boolean isAllowThrow() { return allowThrow; }

    public boolean isFastPickupEnabled() { return fastPickupEnabled; }
    public double getVacuumRadius() { return vacuumRadius; }
    public double getInstantPickupRadius() { return instantPickupRadius; }
    public int getIntervalTicks() { return intervalTicks; }
    public int getMaxOrbsPerBatch() { return maxOrbsPerBatch; }
    public boolean isPullEffect() { return pullEffect; }
    public boolean isRepairMending() { return repairMending; }
    public boolean isPlaySoundOnPickup() { return playSoundOnPickup; }
    public boolean isParticlesOnPickup() { return particlesOnPickup; }

    public boolean isSoundsEnabled() { return soundsEnabled; }
    public String getSoundFill() { return soundFill; }
    public String getSoundConsume() { return soundConsume; }
    public boolean isParticlesEnabled() { return particlesEnabled; }

    public String getItemName() { return itemName; }
    public List<String> getItemLore() { return itemLore; }

    public String getPrefix() { return prefix; }
    public String getNotEnoughXp() { return notEnoughXp; }
    public String getNoBottle() { return noBottle; }
    public String getBottledSuccess() { return bottledSuccess; }
    public String getBottledSuccessTax() { return bottledSuccessTax; }
    public String getConsumedSuccess() { return consumedSuccess; }
    public String getMinXpError() { return minXpError; }
    public String getMaxXpError() { return maxXpError; }
    public String getNoPermission() { return noPermission; }
    public String getReloaded() { return reloaded; }
    public String getUsage() { return usage; }
    public String getXpSpawned() { return xpSpawned; }
    public String getVacuumEnabled() { return vacuumEnabled; }
    public String getVacuumDisabled() { return vacuumDisabled; }
}
