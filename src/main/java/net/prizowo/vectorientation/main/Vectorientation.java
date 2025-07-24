package net.prizowo.vectorientation.main;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.prizowo.vectorientation.main.config.SimpleConfig;

import java.util.HashSet;

@Mod("vectorientation")
public class Vectorientation {

	@EventBusSubscriber(modid = "vectorientation", value = Dist.CLIENT)
	public static class ClientEvents {
		@SubscribeEvent
		public static void onClientSetup(FMLClientSetupEvent event) {
			event.enqueueWork(() -> {
				if(CONFIG.isBroken()){
					System.out.println("[Vectorientation] Config found to be corrupted or outdated, resetting...");
					CONFIG.reconstructFile();
				}
				parseBlacklist();
				System.out.println("[Vectorientation] Initialized.");
			});
		}
	}
	private static SimpleConfig CONFIG = SimpleConfig.of( "vectorientation" ).provider( Vectorientation::provider ).request();
	public static ResourceLocation TNT_ID = ResourceLocation.fromNamespaceAndPath("minecraft", "tnt");

	public static String VAR_SQUETCH = "squetch";
	public static String VAR_MIN_WARP = "min_warp";
	public static String VAR_WARP_FACTOR = "warp_factor";
	public static String VAR_MINECARTS = "minecarts";
	public static String VAR_PISTONS = "pistons";
	public static String VAR_BLACKLIST = "blacklist";

	public static boolean SQUETCH = CONFIG.getOrDefault(VAR_SQUETCH, true);
	public static double MIN_WARP = CONFIG.getOrDefault(VAR_MIN_WARP, 0.75d);
	public static double WARP_FACTOR = CONFIG.getOrDefault(VAR_WARP_FACTOR, 1.0d);
	private static String BLACKLIST_STRING = CONFIG.getOrDefault(VAR_BLACKLIST, "anvil,chipped_anvil,damaged_anvil");
	public static HashSet<ResourceLocation> BLACKLIST;
	public static boolean MINECARTS =  CONFIG.getOrDefault(VAR_MINECARTS, false);
	public static boolean PISTONS = CONFIG.getOrDefault(VAR_PISTONS, true);
	public static boolean TNT = true;
	private static void parseBlacklist(){
		BLACKLIST = new HashSet<>();
		String[] entries = BLACKLIST_STRING.replace(" ","").split(",");
		for(String entry : entries){
			ResourceLocation identifier = ResourceLocation.tryParse(entry);
			if(identifier != null && BuiltInRegistries.BLOCK.containsKey(identifier)){
				BLACKLIST.add(identifier);
			} else {
				System.out.println("[Vectorientation] Could not find block id \""+entry+"\"");
			}
		}
	}

	public static void setConfig(String key, String value){
		CONFIG.set(key, value);
		if(key.equals(VAR_MINECARTS)) MINECARTS = CONFIG.getOrDefault(key, false);
		if(key.equals(VAR_PISTONS)) PISTONS = CONFIG.getOrDefault(key, true);
		if(key.equals(VAR_SQUETCH)) SQUETCH = CONFIG.getOrDefault(key, true);
		if(key.equals(VAR_MIN_WARP)) MIN_WARP = CONFIG.getOrDefault(key, 0.75d);
		if(key.equals(VAR_WARP_FACTOR)) WARP_FACTOR = CONFIG.getOrDefault(key, 1.0d);
		if(key.equals(VAR_BLACKLIST)){
			BLACKLIST_STRING = CONFIG.getOrDefault(VAR_BLACKLIST, "anvil,chipped_anvil,damaged_anvil");
			parseBlacklist();
		}
	}

	public static void writeConfig(){
		CONFIG.writeToFile();
	}
	
	private static String provider( String filename ) {
		return "# Enable Squash & Stretch:\n"
				+ "squetch=true\n\n"
				+ "# Vertical squish at 0 velocity:\n"
				+ "min_warp=0.75\n"
				+ "# Amount of squish increase with velocity:\n"
				+ "warp_factor=1.0\n\n"
				+ "# Whether Minecarts should be affected: (Quite janky)\n"
				+ "minecarts=false\n\n"
				+ "# Whether Piston heads should be affected:\n"
				+ "pistons=true\n\n"
				+ "# List of blocks that should NOT be squished: (Comma separated)\n"
				+ "blacklist=";
    }
}
