package de.niktr.purpur.exptobottles.config;

import de.niktr.purpur.exptobottles.experience.XVersion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin plugin;

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
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.requireEmptyBottle = config.getBoolean("bottling.require-empty-bottle", true);
        this.returnEmptyBottleOnConsume = config.getBoolean("bottling.return-empty-bottle-on-consume", true);
        this.minXp = config.getInt("bottling.min-xp", 1);
        this.maxXpPerBottle = config.getInt("bottling.max-xp-per-bottle", 0);
        this.taxPercent = config.getDouble("bottling.tax-percent", 0.0);
        this.sneakRightClickToFill = config.getBoolean("bottling.sneak-right-click-to-fill", true);
        this.sneakFillMode = config.getString("bottling.sneak-fill-mode", "ALL");
        this.directConsumeOnRightClick = config.getBoolean("bottling.direct-consume-on-right-click", true);
        this.allowThrow = config.getBoolean("bottling.allow-throw", true);

        this.fastPickupEnabled = config.getBoolean("vacuum.enabled", true);
        this.vacuumRadius = config.getDouble("vacuum.radius", 7.0);
        this.instantPickupRadius = config.getDouble("vacuum.instant-pickup-radius", 1.8);
        this.intervalTicks = config.getInt("vacuum.interval-ticks", 2);
        this.maxOrbsPerBatch = config.getInt("vacuum.max-orbs-per-batch", 32);
        this.pullEffect = config.getBoolean("vacuum.pull-effect", true);
        this.repairMending = config.getBoolean("vacuum.repair-mending", true);
        this.playSoundOnPickup = config.getBoolean("vacuum.play-sound-on-pickup", false);
        this.particlesOnPickup = config.getBoolean("vacuum.particles-on-pickup", true);

        this.soundsEnabled = config.getBoolean("effects.sounds", true);
        this.soundFill = config.getString("effects.sound-fill", "ENTITY_EXPERIENCE_ORB_PICKUP");
        this.soundConsume = config.getString("effects.sound-consume", "ENTITY_PLAYER_LEVELUP");
        this.particlesEnabled = config.getBoolean("effects.particles", true);

        this.itemName = convertTags(config.getString("item.name", "&aErfahrungsflasche &7(&e{xp} XP&7)"));
        List<String> rawLore = config.getStringList("item.lore");
        this.itemLore = new ArrayList<>();
        if (rawLore.isEmpty()) {
            this.itemLore.add("&7Gespeicherte Erfahrung: &e{xp} XP");
            this.itemLore.add("&7Ungefähre Level: &b~{levels} Level");
            this.itemLore.add("&7Abgefüllt von: &f{player}");
            this.itemLore.add("&8[XP:{xp}]");
        } else {
            for (String l : rawLore) {
                this.itemLore.add(convertTags(l));
            }
            this.itemLore.add("&8[XP:{xp}]");
        }

        this.prefix = convertTags(config.getString("messages.prefix", "&8[&aXP-Bottles&8] &r"));
        this.notEnoughXp = convertTags(config.getString("messages.not-enough-xp", "&cDu hast nicht genug Erfahrung! (Du hast nur &e{xp} XP&c)"));
        this.noBottle = convertTags(config.getString("messages.no-bottle", "&cDu benötigst eine leere Glasflasche in der Hand!"));
        this.bottledSuccess = convertTags(config.getString("messages.bottled-success", "&aDu hast erfolgreich &e{xp} XP &ain eine Flasche gefüllt!"));
        this.bottledSuccessTax = convertTags(config.getString("messages.bottled-success-tax", "&aErfolgreich &e{xp} XP &aabgefüllt (&c{tax} XP &7Steuer abgezogen)."));
        this.consumedSuccess = convertTags(config.getString("messages.consumed-success", "&aDu hast &e{xp} XP &aerhalten!"));
        this.minXpError = convertTags(config.getString("messages.min-xp-error", "&cDie Mindestmenge zum Abfüllen beträgt &e{min} XP&c."));
        this.maxXpError = convertTags(config.getString("messages.max-xp-error", "&cDie Höchstmenge pro Flasche beträgt &e{max} XP&c."));
        this.noPermission = convertTags(config.getString("messages.no-permission", "&cDazu hast du keine Berechtigung!"));
        this.reloaded = convertTags(config.getString("messages.reloaded", "&aKonfiguration wurde erfolgreich neu geladen!"));
        this.usage = convertTags(config.getString("messages.usage", "&eVerwendung: &f/bottle [amount | all | Xl | until Y | vacuum | spawn X]"));
        this.xpSpawned = convertTags(config.getString("messages.xp-spawned", "&aEs wurden &e{xp} XP &aam Standort gespawnt!"));
        this.vacuumEnabled = convertTags(config.getString("messages.vacuum-enabled", "&aXP-Schnelleinsammler wurde &eaktiviert&a!"));
        this.vacuumDisabled = convertTags(config.getString("messages.vacuum-disabled", "&cXP-Schnelleinsammler wurde &edeaktiviert&c!"));
    }

    private String convertTags(String text) {
        if (text == null) return "";
        return text.replace("<green>", "&a").replace("</green>", "")
                .replace("<red>", "&c").replace("</red>", "")
                .replace("<gold>", "&6").replace("</gold>", "")
                .replace("<yellow>", "&e").replace("</yellow>", "")
                .replace("<white>", "&f").replace("</white>", "")
                .replace("<gray>", "&7").replace("</gray>", "")
                .replace("<dark_gray>", "&8").replace("</dark_gray>", "")
                .replace("<blue>", "&9").replace("</blue>", "")
                .replace("<aqua>", "&b").replace("</aqua>", "")
                .replace("<b>", "&l").replace("</b>", "")
                .replace("<i>", "&o").replace("</i>", "");
    }

    public String parse(String message, Object... replacements) {
        String formatted = message;
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                formatted = formatted.replace("{" + replacements[i] + "}", String.valueOf(replacements[i + 1]));
            }
        }
        return XVersion.colorize(formatted);
    }

    public String getPrefixedMessage(String rawMessage, Object... replacements) {
        return XVersion.colorize(prefix) + parse(rawMessage, replacements);
    }

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
