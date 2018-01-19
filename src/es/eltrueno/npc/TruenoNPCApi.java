package es.eltrueno.npc;

import es.eltrueno.npc.nms.*;
import es.eltrueno.npc.skin.TruenoNPCSkin;
import es.eltrueno.npc.tinyprotocol.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.logging.Level;

public class TruenoNPCApi {

	/**
	 *
	 *
	 * @author el_trueno
	 *
	 *
	 **/

	private static String version;
	private static Plugin plugin;
	private static ArrayList<TruenoNPC> npcs = new ArrayList<TruenoNPC>();

	public static Plugin getPlugin(){
		return plugin;
	}

	private static void setupVersion(){
		try {
			version = Bukkit.getServer().getClass().getPackage().getName().replace(".",  ",").split(",")[3];
		} catch (ArrayIndexOutOfBoundsException ex) {
			ex.printStackTrace();
		}
	}

	public static void removeCacheFile(){
		TruenoNPC_v1_12_r1.removeCacheFile();
	}

	public static ArrayList<TruenoNPC> getNPCs(){
		ArrayList<TruenoNPC> list = new ArrayList<TruenoNPC>();
		for(TruenoNPC npc : npcs){
			if(!npc.isDeleted()){
				list.add(npc);
			}	
		}
		return list;
	}

	/**
	 * Create a NPC
	 * @param plugin
	 * @param location NPC Location
	 * @param skin NPC skin ussing a playername
	 */

	public static TruenoNPC createNPC(Plugin plugin, Location location, TruenoNPCSkin skin){
		TruenoNPCApi.plugin = plugin;
		if(version==null){
			setupVersion();
		}if (version.equals("v1_12_R1")) {
			TruenoNPC_v1_12_r1.startTask(plugin);
			TruenoNPC npc = new TruenoNPC_v1_12_r1(location, skin);
			npcs.add(npc);
			PacketListener.startListening(plugin);
			return npc;
		}else{
			Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED+"Unsopported server version.");
			return null;
		}
	}

}
