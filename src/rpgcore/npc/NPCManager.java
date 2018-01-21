package rpgcore.npc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import es.eltrueno.npc.skin.SkinData;
import es.eltrueno.npc.skin.SkinDataReply;
import es.eltrueno.npc.skin.SkinType;
import es.eltrueno.npc.skin.TruenoNPCSkin;
import es.eltrueno.npc.tinyprotocol.Reflection;
import es.eltrueno.npc.tinyprotocol.TinyProtocol;
import io.netty.channel.Channel;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldServer;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class NPCManager
{
	public RPGCore instance;
	public ArrayList<SkinData> skinDatas = new ArrayList<SkinData>();
	public ArrayList<CustomNPC> npcs = new ArrayList<CustomNPC>();
	public File skinDataFile = new File("plugins/RPGCore/SkinData.yml");

	private static TinyProtocol protocol = null;

	private static Class<?> EntityInteractClass = Reflection.getClass("{nms}.PacketPlayInUseEntity");
	private static Reflection.FieldAccessor<Integer> EntityID = Reflection.getField(EntityInteractClass, int.class, 0);
	private static ArrayList<Player> interactDelay = new ArrayList<Player>();

	public NPCManager(RPGCore instance)
	{
		this.instance = instance;
		readSkinDatas();
		startListening(instance);
	}
	
	public void onDisable()
	{
		for (CustomNPC npc: npcs)
			npc.deleteNPC();	
		npcs.clear();
	}

	public void startListening(Plugin plugin)
	{
		if(protocol == null)
		{
			protocol = new TinyProtocol(plugin) 
			{
				@Override
				public Object onPacketInAsync(Player p, Channel channel, Object packet) 
				{
					if(EntityInteractClass.isInstance(packet))
					{
						if(!interactDelay.contains(p))
						{
							interactDelay.add(p);
							for(CustomNPC npc : npcs)
								if(npc.getId() == EntityID.get(packet)) //NPC INTERACT EVENT
								{
									RPlayer rp = RPGCore.instance.playerManager.getRPlayer(p.getUniqueId());
									if (p.hasPermission("rpgcore.npc") && p.isSneaking())
									{
										rp.selectedNPC = npc;
										RPGCore.msg(p, "NPC Selected: " + npc.getName());
									}
									for (ConversationData cd: ConversationData.dataList)
										if (cd.npcName.equals(npc.getName()))
										{
											NPCConversation c = new NPCConversation(rp, cd);
											p.openInventory(c.getConversationUI());
										}
									break;
								}
							Bukkit.getScheduler().runTaskLaterAsynchronously(instance, new Runnable()
							{
								@Override
								public void run() 
								{
									interactDelay.remove(p);
								}
							}, 2);
						}
					}
					return super.onPacketInAsync(p, channel, packet);
				}
			};
		}
	}

	public void saveSkinData()
	{
		ArrayList<String> lines = new ArrayList<String>();
		for (SkinData sd: skinDatas)
			lines.add(sd.skinName + "::" + sd.getValue() + "::" + sd.getSignature());
		CakeLibrary.writeFile(lines, skinDataFile);
	}

	public void readSkinDatas()
	{
		skinDatas.clear();
		for (String line: CakeLibrary.readFile(skinDataFile))
		{
			try
			{
				String[] split = line.split("::");
				skinDatas.add(new SkinData(split[0], split[1], split[2]));
			} catch (Exception e) {
				RPGCore.msgConsole("&4Error reading a line in skin data");
			}
		}
	}

	public SkinData getSkinData(String skinName)
	{
		for (SkinData sd: skinDatas)
			if (sd.skinName.equalsIgnoreCase(skinName))
				return sd;
		return null;
	}

	public CustomNPC createNPC(Location location, String name)
	{
		WorldServer s = ((CraftWorld) location.getWorld()).getHandle();
		World w = ((CraftWorld) location.getWorld()).getHandle(); //Warning: use NMS world
		CustomNPC npc = new CustomNPC(MinecraftServer.getServer(), s, new GameProfile(UUID.fromString("0-0-0-0-0"), CakeLibrary.recodeColorCodes(name)), new PlayerInteractManager(w), location);
		npc.locX = location.getX();
		npc.locY = location.getY();
		npc.locZ = location.getZ();
		npc.yaw = location.getYaw();
		npc.pitch = location.getPitch();
		npcs.add(npc);
		return npc;
	}

	public CustomNPC createNPC(Location location, String name, String skin)
	{
		WorldServer s = ((CraftWorld) location.getWorld()).getHandle();
		World w = ((CraftWorld) location.getWorld()).getHandle(); //Warning: use NMS world
		TruenoNPCSkin npcSkin = new TruenoNPCSkin(RPGCore.instance, SkinType.IDENTIFIER, skin);
		GameProfile profile = new GameProfile(UUID.randomUUID(), name);
		CustomNPC npc = new CustomNPC(MinecraftServer.getServer(), s, profile, new PlayerInteractManager(w), location);
		npcs.add(npc);
		SkinData cached = getSkinData(skin);
		if (cached != null)
			profile.getProperties().put("textures", new Property("textures", cached.getValue(), cached.getSignature()));
		npcSkin.getSkinDataAsync(new SkinDataReply() 
		{
			@Override
			public void done(SkinData skinData) 
			{
				if (skinData == null)
					return;

				if (cached != null)
					if (skinData.getSignature().equals(cached.getSignature()) && skinData.getValue().equals(cached.getValue()))
						return;

				skinData.skinName = skin;

				ArrayList<SkinData> remove = new ArrayList<SkinData>();
				for (SkinData sd: skinDatas)
					if (sd.skinName.equals(skin))
						remove.add(sd);
				skinDatas.removeAll(remove);
				skinDatas.add(skinData);
				saveSkinData();

				npc.skinData = skinData;
				profile.getProperties().put("textures", new Property("textures", skinData.getValue(), skinData.getSignature()));
				npc.reloadForVisiblePlayers();
			}
		});
		return npc;
	}

	/**
	public CustomNPC createNPC(Location location, String name, String skin)
	{
		return createNPC(location, name, skin);
	}*/

	public UUID getUUIDFromName(String name)
	{
		String data = getPlayerDataFromName(name);
		return formatFromInput(data.split("\"")[3]);
	}

	public String getPlayerDataFromName(String name)
	{
		try {
			URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null)
				return inputLine;
			in.close();
		} catch (Exception e) {
			return null;
		}
		return null;
	}

	public UUID formatFromInput(String uuid) throws IllegalArgumentException{
		if(uuid == null) throw new IllegalArgumentException();
		uuid = uuid.trim();
		return uuid.length() == 32 ? fromTrimmed(uuid.replaceAll("-", "")) : UUID.fromString(uuid);
	}

	public UUID fromTrimmed(String trimmedUUID) throws IllegalArgumentException{
		if(trimmedUUID == null) throw new IllegalArgumentException();
		StringBuilder builder = new StringBuilder(trimmedUUID.trim());
		/* Backwards adding to avoid index adjustments */
		try {
			builder.insert(20, "-");
			builder.insert(16, "-");
			builder.insert(12, "-");
			builder.insert(8, "-");
		} catch (StringIndexOutOfBoundsException e){
			throw new IllegalArgumentException();
		}

		return UUID.fromString(builder.toString());
	}

	public GameProfile getGameProfile(String profilename, SkinData skindata)
	{
		if(skindata!=null) 
		{
			GameProfile profile = new GameProfile(UUID.randomUUID(), profilename);
			profile.getProperties().put("textures", new Property("textures", skindata.getValue(), skindata.getSignature()));
			return profile;
		} else {
			GameProfile profile = new GameProfile(UUID.fromString("8667ba71-b85a-4004-af54-457a9734eed7"), profilename);
			profile.getProperties().put("textures", new Property("textures", "eyJ0aW1lc3RhbXAiOjE1MTUzMzczNTExMjk" +
					"sInByb2ZpbGVJZCI6Ijg2NjdiYTcxYjg1YTQwMDRhZjU0NDU3YTk3MzRlZWQ3IiwicHJvZmlsZU5hbWUiOiJTdGV2ZSIsInNpZ2" +
					"5hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQub" +
					"mV0L3RleHR1cmUvNDU2ZWVjMWMyMTY5YzhjNjBhN2FlNDM2YWJjZDJkYzU0MTdkNTZmOGFkZWY4NGYxMTM0M2RjMTE4OGZlMTM4" +
					"In0sIkNBUEUiOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iNzY3ZDQ4MzI1ZWE1MzI0NTY" +
					"xNDA2YjhjODJhYmJkNGUyNzU1ZjExMTUzY2Q4NWFiMDU0NWNjMiJ9fX0", "oQHxJ9U7oi/JOeC5C9wtLcoqQ/Uj5j8mfSL" +
							"aPo/zMQ1GP/IjB+pFmfy5JOOaX94Ia98QmLLd+AYacnja60DhO9ljrTtL/tM7TbXdWMWW7A2hJkEKNH/wnBkSIm0EH8WhH+m9+8" +
							"2pkTB3h+iDGHyc+Qb9tFXWLiE8wvdSrgDHPHuQAOgGw6BfuhdSZmv2PGWXUG02Uvk6iQ7ncOIMRWFlWCsprpOw32yzWLSD8UeUU" +
							"io6SlUyuBIO+nJKmTRWHnHJgTLqgmEqBRg0B3GdML0BncMlMHq/qe9x6gTlDCJATLTFJg4kDEF+kUa4+P0BDdPFrgApFUeK4Bz1" +
							"w7Qxls4zKQQJNJw58nhvKk/2yQnFOOUqfRx/DeIDLCGSTEJr4VjKIVThnvkocUDsH8DLk4/Xt9qKWh3ZxXtxoKPDvFP5iyxIOfZ" +
							"dkZu/H0qlgRTqF8RP8AnXf2lgnarfty8G7q7/4KQwWC1CIn9MmaMwv3MdFDlwdAjHhvpyBYYTnL11YDBSUg3b6+QmrWWm1DXcHr" +
							"wkcS0HI82VHYdg8uixzN57B3DGRSlh2qBWHJTb0zF8uryveCZppHl/ULa/2vAt6XRXURniWU4cTQKQAGqjByhWSbUM0XHFgcuKj" +
					"GFVlJ4HEzBiXgY3PtRF6NzfsUZ2gQI9o12x332USZiluYrf+OLhCa8="));
			return profile;
		}
	}
}
