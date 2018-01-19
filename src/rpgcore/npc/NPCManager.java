package rpgcore.npc;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import es.eltrueno.npc.skin.SkinData;
import es.eltrueno.npc.skin.SkinDataReply;
import es.eltrueno.npc.skin.SkinType;
import es.eltrueno.npc.skin.TruenoNPCSkin;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
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

public class NPCManager
{
	public ArrayList<SkinData> skinDatas = new ArrayList<SkinData>();
	public ArrayList<CustomNPC> npcs = new ArrayList<CustomNPC>();
	public File skinDataFile = new File("plugins/RPGCore/SkinData.yml");
	public int globalID;

	public NPCManager()
	{
		readSkinDatas();
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
		npcSkin.getSkinDataAsync(new SkinDataReply() 
		{
			@Override
			public void done(SkinData skinData) 
			{
				if (skinData == null)
					skinData = getSkinData(skin);
				skinData.skinName = skin;
				skinDatas.add(skinData);
				saveSkinData();
				profile.getProperties().put("textures", new Property("textures", skinData.getValue(), skinData.getSignature()));
				npc.spawnPlayer();
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

	/**
	 * Custom NPC Class 
	 */
	public class CustomNPC extends EntityPlayer 
	{
		public String name;
		public UUID uuid;
		public TruenoNPCSkin skin;
		public SkinData skinData;
		public int id;
		public int tick;
		public CustomNPC(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact, Location location)
		{
			super(srv, world, game, interact);
			this.id = globalID++;
			this.name = game.getName();
			this.uuid = game.getId();
			locX = location.getX();
			locY = location.getY();
			locZ = location.getZ();
			yaw = location.getYaw();
			pitch = location.getPitch();
			spawnPlayer();
		}

		public CustomNPC(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact, SkinData skinData, Location location) 
		{
			super(srv, world, game, interact);
			this.id = globalID++;
			this.name = game.getName();
			this.uuid = game.getId();
			this.skinData = skinData;
			locX = location.getX();
			locY = location.getY();
			locZ = location.getZ();
			yaw = location.getYaw();
			pitch = location.getPitch();
			spawnPlayer();
		}

		public void tick()
		{
			tick++;
			if (tick % 2 == 0)
				tick2();
			if (tick % 5 == 0)
				tick5();
			if (tick == 20)
			{
				tick20();
				tick = 0;
			}
		}

		public void tick2()
		{
			/**
			ArrayList<Player> near = CakeLibrary.getNearbyPlayers(getBukkitLocation(), 5);
			if (near.size() > 0)
			{
				lookAt(near.get(near.size() - 1).getLocation());
				updateRotation();
			}*/
		}

		public void tick5()
		{

		}

		public void tick20()
		{
		}

		public Location getBukkitLocation()
		{
			return new Location((org.bukkit.World) this.getWorld().getWorld(), locX, locY, locZ, yaw, pitch);
		}

		public float getAngle(Vector point1, Vector point2) {
			double dx = point2.getX() - point1.getX();
			double dz = point2.getZ() - point1.getZ();
			float angle = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
			if (angle < 0) {
				angle += 360.0F;
			}
			return angle;
		}

		public void lookAt(Location point)
		{
			yaw = (getAngle(new Vector(locX, 0, locZ), point.toVector()));
			/**
			double dx = locX - point.getX();
			double dy = locY - point.getY();
			double dz = locZ - point.getZ();
			pitch = (float) -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
			yaw = (float) Math.atan2(dz, dy) - 90F;*/
		}

		public void setSkin(String name)
		{
			deleteNPC();
			createNPC(((Player) this).getLocation(), this.name, name);
		}

		public void deleteNPC()
		{

		}

		public void spawnPlayer()
		{
			for (Player p: CakeLibrary.getNearbyPlayers(getBukkitLocation(), 128))
				spawnFor(p);

		}

		public void spawnFor(Player p)
		{
			this.setLocation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
			PacketPlayOutPlayerInfo pi = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this);
			PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(this);
			PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(this, (byte) ((int) (yaw * 256.0F / 360.0F)));

			PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
			co.sendPacket(pi);
			co.sendPacket(spawn);
			co.sendPacket(rotation);
		}

		public void updateRotation()
		{
			for (Player p: CakeLibrary.getNearbyPlayers(getBukkitLocation(), 128))
			{
				PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation(this, (byte) ((int) (yaw * 256.0F / 360.0F)));

				PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
				co.sendPacket(packet);
			}
		}

		public void updatePosition()
		{
			for (Player p: CakeLibrary.getNearbyPlayers(getBukkitLocation(), 128))
			{
				PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(this);

				PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
				co.sendPacket(packet);
			}
		}
	}
}
