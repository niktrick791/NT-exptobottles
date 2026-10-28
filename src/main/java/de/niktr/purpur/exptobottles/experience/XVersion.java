package de.niktr.purpur.exptobottles.experience;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class XVersion {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final boolean HAS_PDC;
    private static final Material EXP_BOTTLE_MAT;

    static {
        boolean pdc = false;
        try {
            Class.forName("org.bukkit.persistence.PersistentDataContainer");
            pdc = true;
        } catch (Throwable ignored) {}
        HAS_PDC = pdc;

        Material m = Material.matchMaterial("EXPERIENCE_BOTTLE");
        if (m == null) {
            m = Material.matchMaterial("EXP_BOTTLE");
        }
        EXP_BOTTLE_MAT = (m != null) ? m : Material.GLASS_BOTTLE;
    }

    private XVersion() {}

    public static Material getExpBottleMaterial() {
        return EXP_BOTTLE_MAT;
    }

    public static boolean isExpBottleMaterial(Material material) {
        if (material == null) return false;
        return material == EXP_BOTTLE_MAT || material.name().equals("EXP_BOTTLE") || material.name().equals("EXPERIENCE_BOTTLE");
    }

    public static ItemStack getItemInHand(Player player) {
        if (player == null) return null;
        try {
            return player.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            try {
                Method m = player.getClass().getMethod("getItemInHand");
                return (ItemStack) m.invoke(player);
            } catch (Throwable ex) {
                return null;
            }
        }
    }

    public static void setItemInHand(Player player, ItemStack item) {
        if (player == null) return;
        try {
            player.getInventory().setItemInMainHand(item);
        } catch (NoSuchMethodError e) {
            try {
                Method m = player.getClass().getMethod("setItemInHand", ItemStack.class);
                m.invoke(player, item);
            } catch (Throwable ignored) {}
        }
    }

    public static boolean isMainHand(PlayerInteractEvent event) {
        try {
            Object hand = event.getHand();
            if (hand != null) {
                return hand.toString().equals("HAND");
            }
        } catch (Throwable ignored) {}
        return true; // 1.8 has only one hand
    }

    public static String colorize(String text) {
        if (text == null || text.isEmpty()) return "";
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("&x");
            for (char c : hex.toCharArray()) {
                replacement.append('&').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    public static void playSound(Player player, String... soundNames) {
        if (player == null) return;
        for (String name : soundNames) {
            try {
                Sound sound = Sound.valueOf(name);
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                return;
            } catch (Throwable ignored) {}
        }
    }

    public static void storeXp(ItemMeta meta, NamespacedKey key, int xp, String owner) {
        if (meta == null) return;
        if (HAS_PDC && key != null) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                pdc.set(key, PersistentDataType.INTEGER, xp);
            } catch (Throwable ignored) {}
        }
    }

    public static int readXp(ItemStack item, NamespacedKey key) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (HAS_PDC && key != null) {
            try {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                Integer val = pdc.get(key, PersistentDataType.INTEGER);
                if (val != null) return val;
            } catch (Throwable ignored) {}
        }

        // Fallback: parse from Lore e.g. "§8[XP: 500]" or lines with numbers
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    String clean = ChatColor.stripColor(line);
                    if (clean.contains("[XP:") && clean.contains("]")) {
                        try {
                            String numStr = clean.substring(clean.indexOf("[XP:") + 4, clean.indexOf("]")).trim();
                            return Integer.parseInt(numStr);
                        } catch (Throwable ignored) {}
                    }
                    if (clean.toLowerCase().contains("xp:") || clean.toLowerCase().contains("erfahrung:")) {
                        try {
                            String num = clean.replaceAll("[^0-9]", "");
                            if (!num.isEmpty()) {
                                return Integer.parseInt(num);
                            }
                        } catch (Throwable ignored) {}
                    }
                }
            }
        }
        return 0;
    }

    public static int getItemDamage(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            return ((Damageable) meta).getDamage();
        }
        return item.getDurability();
    }

    public static void setItemDamage(ItemStack item, int damage) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable) {
            ((Damageable) meta).setDamage(damage);
            item.setItemMeta(meta);
        } else {
            item.setDurability((short) damage);
        }
    }
}
