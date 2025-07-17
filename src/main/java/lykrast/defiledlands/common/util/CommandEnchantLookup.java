package lykrast.defiledlands.common.util;

import java.util.List;
import javax.annotation.Nullable;

import lykrast.defiledlands.common.util.EnchantmentLookup;
import lykrast.defiledlands.common.util.EnchantmentLookup.EnchantmentInfo;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandEnchantLookup extends CommandBase {

    @Override
    public String getName() {
        return "enchantlookup";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/enchantlookup <name|level> <query> [page]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // Allow all players
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "Usage: " + getUsage(sender)));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Examples:"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  /enchantlookup name sharp"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  /enchantlookup level 15"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "  /enchantlookup range 10 20"));
            return;
        }

        String type = args[0].toLowerCase();
        int page = args.length > 2 ? parseInt(args[2], 1) : 1;
        
        List<EnchantmentInfo> results;
        String searchDescription;

        switch (type) {
            case "name":
                String query = args[1];
                results = EnchantmentLookup.searchByName(query);
                searchDescription = "name containing '" + query + "'";
                break;
                
            case "level":
                int level = parseInt(args[1]);
                results = EnchantmentLookup.getByMinEnchantability(level);
                searchDescription = "min enchantability of " + level;
                break;
                
            case "range":
                if (args.length < 3) {
                    throw new CommandException("Range requires two numbers: /enchantlookup range <min> <max>");
                }
                int min = parseInt(args[1]);
                int max = parseInt(args[2]);
                page = args.length > 3 ? parseInt(args[3], 1) : 1;
                results = EnchantmentLookup.getByEnchantabilityRange(min, max);
                searchDescription = "enchantability between " + min + " and " + max;
                break;
                
            default:
                throw new CommandException("Unknown search type. Use 'name', 'level', or 'range'");
        }

        displayResults(sender, results, searchDescription, page);
    }

    private void displayResults(ICommandSender sender, List<EnchantmentInfo> results, String searchDescription, int page) {
        if (results.isEmpty()) {
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "No enchantments found for " + searchDescription));
            return;
        }

        int itemsPerPage = 10;
        int totalPages = (int) Math.ceil((double) results.size() / itemsPerPage);
        page = Math.max(1, Math.min(page, totalPages));
        
        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, results.size());

        sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "=== Enchantments with " + searchDescription + " ==="));
        sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Page " + page + "/" + totalPages + " (" + results.size() + " total)"));

        for (int i = startIndex; i < endIndex; i++) {
            EnchantmentInfo info = results.get(i);
            StringBuilder sb = new StringBuilder();
            
            sb.append(TextFormatting.AQUA).append(info.displayName);
            if (info.isTreasure) {
                sb.append(TextFormatting.GOLD).append(" [TREASURE]");
            }
            sb.append(TextFormatting.WHITE).append(" (").append(info.name).append(")");
            
            sender.sendMessage(new TextComponentString(sb.toString()));
            
            // Show enchantability for each level
            StringBuilder levels = new StringBuilder();
            levels.append(TextFormatting.GRAY).append("  Levels ").append(info.minLevel).append("-").append(info.maxLevel).append(": ");
            
            for (int level = info.minLevel; level <= info.maxLevel; level++) {
                if (level > info.minLevel) levels.append(", ");
                levels.append("L").append(level).append("=").append(info.getMinEnchantability(level));
            }
            
            sender.sendMessage(new TextComponentString(levels.toString()));
        }

        if (totalPages > 1) {
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "Use page number to see more results"));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "name", "level", "range");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
