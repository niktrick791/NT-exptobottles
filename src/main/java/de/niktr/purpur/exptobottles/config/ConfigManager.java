package de.niktr.purpur.exptobottles.config;

import de.niktr.purpur.exptobottles.experience.XVersion;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigManager {

    private final JavaPlugin plugin;

    // Language
    private String language = "en";

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
    private String langChanged;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.language = config.getString("language", "en").toLowerCase(Locale.ROOT);
        if (!"de".equals(language) && !"en".equals(language)) {
            this.language = "en";
        }
        boolean isDe = "de".equals(language);

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

        String defItemName = isDe ? "&aErfahrungsflasche &7(&e{xp} XP&7)" : "&aExperience Bottle &7(&e{xp} XP&7)";
        this.itemName = convertTags(config.getString("item.name", defItemName));

        List<String> rawLore = config.getStringList("item.lore");
        this.itemLore = new ArrayList<String>();
        if (rawLore.isEmpty()) {
            if (isDe) {
                this.itemLore.add("&7Gespeicherte Erfahrung: &e{xp} XP");
                this.itemLore.add("&7Ungefähre Level: &b~{levels} Level");
                this.itemLore.add("&7Abgefüllt von: &f{player}");
            } else {
                this.itemLore.add("&7Stored Experience: &e{xp} XP");
                this.itemLore.add("&7Estimated Levels: &b~{levels} Levels");
                this.itemLore.add("&7Bottled by: &f{player}");
            }
            this.itemLore.add("&8[XP:{xp}]");
        } else {
            for (String l : rawLore) {
                this.itemLore.add(convertTags(l));
            }
            this.itemLore.add("&8[XP:{xp}]");
        }

        this.prefix = convertTags(config.getString("messages.prefix", "&8[&aXP-Bottles&8] &r"));
        this.notEnoughXp = convertTags(config.getString("messages.not-enough-xp", isDe
                ? "&cDu hast nicht genug Erfahrung! (Du hast nur &e{xp} XP&c)"
                : "&cYou do not have enough experience! (You only have &e{xp} XP&c)"));
        this.noBottle = convertTags(config.getString("messages.no-bottle", isDe
                ? "&cDu benötigst eine leere Glasflasche in der Hand!"
                : "&cYou need an empty glass bottle in your hand!"));
        this.bottledSuccess = convertTags(config.getString("messages.bottled-success", isDe
                ? "&aDu hast erfolgreich &e{xp} XP &ain eine Flasche gefüllt!"
                : "&aSuccessfully bottled &e{xp} XP &ainto a bottle!"));
        this.bottledSuccessTax = convertTags(config.getString("messages.bottled-success-tax", isDe
                ? "&aErfolgreich &e{xp} XP &aabgefüllt (&c{tax} XP &7Steuer abgezogen)."
                : "&aSuccessfully bottled &e{xp} XP &a(&c{tax} XP &7tax deducted)."));
        this.consumedSuccess = convertTags(config.getString("messages.consumed-success", isDe
                ? "&aDu hast &e{xp} XP &aerhalten!"
                : "&aYou received &e{xp} XP&a!"));
        this.minXpError = convertTags(config.getString("messages.min-xp-error", isDe
                ? "&cDie Mindestmenge zum Abfüllen beträgt &e{min} XP&c."
                : "&cThe minimum amount to bottle is &e{min} XP&c."));
        this.maxXpError = convertTags(config.getString("messages.max-xp-error", isDe
                ? "&cDie Höchstmenge pro Flasche beträgt &e{max} XP&c."
                : "&cThe maximum amount per bottle is &e{max} XP&c."));
        this.noPermission = convertTags(config.getString("messages.no-permission", isDe
                ? "&cDazu hast du keine Berechtigung!"
                : "&cYou do not have permission for this!"));
        this.reloaded = convertTags(config.getString("messages.reloaded", isDe
                ? "&aKonfiguration wurde erfolgreich neu geladen!"
                : "&aConfiguration was successfully reloaded!"));
        this.usage = convertTags(config.getString("messages.usage", isDe
                ? "&eVerwendung: &f/bottle [amount | all | Xl | until Y | vacuum | spawn X | lang <de|en>]"
                : "&eUsage: &f/bottle [amount | all | Xl | until Y | vacuum | spawn X | lang <de|en>]"));
        this.xpSpawned = convertTags(config.getString("messages.xp-spawned", isDe
                ? "&aEs wurden &e{xp} XP &aam Standort gespawnt!"
                : "&aSpawned &e{xp} XP &aat your location!"));
        this.vacuumEnabled = convertTags(config.getString("messages.vacuum-enabled", isDe
                ? "&aXP-Schnelleinsammler wurde &eaktiviert&a!"
                : "&aXP vacuum collector has been &aenabled&a!"));
        this.vacuumDisabled = convertTags(config.getString("messages.vacuum-disabled", isDe
                ? "&cXP-Schnelleinsammler wurde &edeaktiviert&c!"
                : "&cXP vacuum collector has been &cdisabled&c!"));
        this.langChanged = convertTags(config.getString("messages.lang-changed", isDe
                ? "&aSprache erfolgreich auf &eDeutsch &ageändert!"
                : "&aLanguage successfully set to &eEnglish&a!"));
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String lang) {
        String clean = (lang != null && lang.equalsIgnoreCase("de")) ? "de" : "en";
        this.language = clean;
        plugin.getConfig().set("language", clean);
        plugin.saveConfig();
        loadConfig();
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
    public String getLangChanged() { return langChanged; }
}
