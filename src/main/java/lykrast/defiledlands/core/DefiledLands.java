package lykrast.defiledlands.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lykrast.defiledlands.common.util.CommandEnchantLookup;
import lykrast.defiledlands.common.util.EnchantmentLookup;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = DefiledLands.MODID, name = DefiledLands.NAME, version = DefiledLands.VERSION, acceptedMinecraftVersions = "[1.12,1.13)",
	dependencies = "required-after:baubles;after:tconstruct")
public class DefiledLands {
	
	public static final String MODID = "defiledlands";
	public static final String NAME = "Defiled Lands";
    public static final String VERSION = "@VERSION@";
    
    @Instance(MODID)
    public static DefiledLands instance;
    
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    
    @SidedProxy(clientSide = "lykrast.defiledlands.core.ClientProxy", serverSide = "lykrast.defiledlands.core.CommonProxy")
	public static CommonProxy proxy;
    
    @EventHandler
	public void preInit(FMLPreInitializationEvent e) {
		proxy.preInit(e);
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		proxy.init(e);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent e) {
		proxy.postInit(e);
		DefiledLands.LOGGER.info("Initializing enchantment lookup tables...");
		EnchantmentLookup.initialize();
		DefiledLands.LOGGER.info("Enchantment lookup initialization complete.");
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		DefiledLands.LOGGER.info("Registering CommandEnchantLookup");
		// Register the command
		event.registerServerCommand(new CommandEnchantLookup());
	}
}
