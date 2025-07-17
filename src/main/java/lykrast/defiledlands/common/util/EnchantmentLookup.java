// EnchantmentLookup.java - Utility class for enchantment lookups
package lykrast.defiledlands.common.util;

import java.util.*;
import java.util.stream.Collectors;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class EnchantmentLookup {
    
    public static class EnchantmentInfo {
        public final Enchantment enchantment;
        public final String name;
        public final String displayName;
        public final int minLevel;
        public final int maxLevel;
        public final boolean isTreasure;
        public final Map<Integer, Integer> levelToMinEnchantability;
        public final Map<Integer, Integer> levelToMaxEnchantability;
        
        public EnchantmentInfo(Enchantment enchantment) {
            this.enchantment = enchantment;
            this.name = enchantment.getName();
            this.displayName = enchantment.getTranslatedName(1);
            this.minLevel = enchantment.getMinLevel();
            this.maxLevel = enchantment.getMaxLevel();
            this.isTreasure = enchantment.isTreasureEnchantment();
            this.levelToMinEnchantability = new HashMap<>();
            this.levelToMaxEnchantability = new HashMap<>();
            
            // Pre-calculate enchantability for all levels
            for (int level = minLevel; level <= maxLevel; level++) {
                levelToMinEnchantability.put(level, enchantment.getMinEnchantability(level));
                levelToMaxEnchantability.put(level, enchantment.getMaxEnchantability(level));
            }
        }
        
        public int getMinEnchantability(int level) {
            return levelToMinEnchantability.getOrDefault(level, -1);
        }
        
        public int getMaxEnchantability(int level) {
            return levelToMaxEnchantability.getOrDefault(level, -1);
        }
    }
    
    private static Map<String, EnchantmentInfo> nameToEnchantment = new HashMap<>();
    private static Map<Integer, List<EnchantmentInfo>> minEnchantabilityToEnchantments = new HashMap<>();
    private static Map<Integer, List<EnchantmentInfo>> maxEnchantabilityToEnchantments = new HashMap<>();
    private static boolean initialized = false;
    
    public static void initialize() {
        if (initialized) return;
        
        nameToEnchantment.clear();
        minEnchantabilityToEnchantments.clear();
        maxEnchantabilityToEnchantments.clear();
        
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if (enchantment == null) continue;
            
            EnchantmentInfo info = new EnchantmentInfo(enchantment);
            
            // Index by name (both registry name and display name)
            nameToEnchantment.put(info.name.toLowerCase(), info);
            nameToEnchantment.put(info.displayName.toLowerCase(), info);
            
            // Index by enchantability levels
            for (int level = info.minLevel; level <= info.maxLevel; level++) {
                int minEnch = info.getMinEnchantability(level);
                int maxEnch = info.getMaxEnchantability(level);
                
                minEnchantabilityToEnchantments.computeIfAbsent(minEnch, k -> new ArrayList<>()).add(info);
                maxEnchantabilityToEnchantments.computeIfAbsent(maxEnch, k -> new ArrayList<>()).add(info);
            }
        }
        
        initialized = true;
    }
    
    /**
     * Search enchantments by name with wildcard support
     */
    public static List<EnchantmentInfo> searchByName(String query) {
        initialize();
        String lowerQuery = query.toLowerCase();
        
        return nameToEnchantment.values().stream()
                .distinct()
                .filter(info -> info.name.toLowerCase().contains(lowerQuery) || 
                               info.displayName.toLowerCase().contains(lowerQuery))
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
    }
    
    /**
     * Get enchantments by exact enchantability level
     */
    public static List<EnchantmentInfo> getByMinEnchantability(int level) {
        initialize();
        return minEnchantabilityToEnchantments.getOrDefault(level, new ArrayList<>());
    }
    
    public static List<EnchantmentInfo> getByMaxEnchantability(int level) {
        initialize();
        return maxEnchantabilityToEnchantments.getOrDefault(level, new ArrayList<>());
    }
    
    /**
     * Get enchantments within an enchantability range
     */
    public static List<EnchantmentInfo> getByEnchantabilityRange(int min, int max) {
        initialize();
        Set<EnchantmentInfo> results = new HashSet<>();
        
        for (int level = min; level <= max; level++) {
            results.addAll(getByMinEnchantability(level));
        }
        
        return results.stream()
                .sorted(Comparator.comparing(info -> info.name))
                .collect(Collectors.toList());
    }
}
