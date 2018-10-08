package rpgcore.npc;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import es.eltrueno.npc.skin.SkinData;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.npc.ConversationData.ConversationLine;
import rpgcore.npc.ConversationData.ConversationPartType;
import rpgcore.player.RPlayer;

public class CustomNPC extends EntityPlayer 
{
	public static double visibleDistance = 48.0D * 48.0D;
	public float chatRangeDistance = 5.0F;
	public static int globalID;
	public String name;
	public String databaseName;
	public UUID uuid;
	public SkinData skinData;
	public int id;
	public int tick;
	public int randomLookTicks;
	public boolean removed;
	public boolean lockRotation;
	public float targetYaw, targetPitch, lastUpdatedYaw, lastUpdatedPitch;
	public ConversationData conversationData;
	public static final float rotationStepYaw = 25;
	public static final float rotationStepPitch = 10;
	public ArrayList<UUID> visiblePlayers = new ArrayList<UUID>();
	public ArrayList<UUID> inRangePlayers = new ArrayList<UUID>();
	public CustomNPC(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact, Location location)
	{
		super(srv, world, game, interact);
		this.lockRotation = true;
		this.id = globalID++;
		this.name = game.getName();
		this.uuid = game.getId();
		locX = location.getX();
		locY = location.getY();
		locZ = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
		setDatabaseName(name);
		checkForVisibility();
	}

	public CustomNPC(MinecraftServer srv, WorldServer world, GameProfile game, PlayerInteractManager interact, SkinData skinData, Location location) 
	{
		super(srv, world, game, interact);
		this.lockRotation = true;
		this.id = globalID++;
		this.name = game.getName();
		this.uuid = game.getId();
		this.skinData = skinData;
		locX = location.getX();
		locY = location.getY();
		locZ = location.getZ();
		yaw = location.getYaw();
		pitch = location.getPitch();
		setDatabaseName(name);
		checkForVisibility();
	}

	public void applyNonConstructorVariables(CustomNPC other)
	{
		this.lockRotation = other.lockRotation;
		this.chatRangeDistance = other.chatRangeDistance;
		this.databaseName = other.databaseName;
	}

	public void changeDatabaseName(String change)
	{
		if (databaseName == null)
		{
			setDatabaseName(change);
			return;
		}
		if (databaseName.length() == 0)
		{
			setDatabaseName(change);
			return;
		}
		File file = new File(NPCManager.npcFolder.getPath() + "/" + databaseName + ".yml");
		file.delete();
		setDatabaseName(change);
		saveNPC();
	}

	public void setDatabaseName(String set)
	{
		this.databaseName = CakeLibrary.removeColorCodes(set).toLowerCase();
		int index = 0;
		for (int checkIndex = 0; checkIndex < NPCManager.npcs.size(); checkIndex++)
		{
			CustomNPC check = NPCManager.npcs.get(checkIndex);
			if (check.id == id)
				continue;
			if ((index == 0 && check.databaseName.equals(databaseName)) || (index > 0 && check.databaseName.equals(databaseName + index)))
			{
				index++;
				checkIndex = 0;
			}
		}
		if (index > 0)
			databaseName += index;
	}

	public boolean tick()
	{
		tick++;
		ticksLived++;
		if (tick % 2 == 0)
			tick2();
		if (tick % 5 == 0)
			tick5();
		if (tick % 10 == 0)
			tick10();
		if (tick == 20)
		{
			tick20();
			tick = 0;
		}

		if (!lockRotation)
		{
			while (targetYaw > 180)
				targetYaw = -180 + (targetYaw - 180);
			while (targetYaw < -180)
				targetYaw = 180 - (targetYaw * -1 - 180);

			float multiplierYaw = 0;

			if (targetYaw < 0 && yaw > 0)
				multiplierYaw = ((180 - targetYaw * -1) + (180 - yaw) < yaw - targetYaw) ? 1 : -1;
			else if (targetYaw > 0 && yaw < 0)
				multiplierYaw = ((180 - targetYaw) + (180 - yaw * -1) < targetYaw - yaw) ? -1 : 1;
			else
				multiplierYaw = yaw < targetYaw ? 1 : -1;

			yaw += Math.min(rotationStepYaw, Math.abs(targetYaw - yaw)) * multiplierYaw;

			float multiplierPitch = pitch < targetPitch ? 1 : -1;
			pitch += Math.min(rotationStepPitch, Math.abs(targetPitch - pitch)) * multiplierPitch;

			while (yaw > 180)
			{
				yaw = -180 + (yaw - 180);
				if (Math.abs(targetYaw - yaw) < rotationStepYaw)
					yaw = targetYaw;
			}

			while (yaw < -180)
			{
				yaw = 180 - (yaw * -1 - 180);
				if (Math.abs(targetYaw - yaw) < rotationStepYaw)
					yaw = targetYaw;
			}
			updateRotation();
		}
		return removed;
	}

	public void tick2()
	{
		if (visiblePlayers.size() == 0)
			return;
		if (lockRotation)
			return;
		for (int i = visiblePlayers.size() - 1; i >= 0; i--)
		{
			Player p = Bukkit.getPlayer(visiblePlayers.get(i));
			if (p == null)
				continue;
			Location l = p.getLocation();
			if (Math.pow(l.getX() - locX, 2) 
					+ Math.pow(l.getY() - locY, 2) 
					+ Math.pow(l.getZ() - locZ, 2) < chatRangeDistance * chatRangeDistance)
			{
				lookAt(l);
				return;
			}
		}
		randomLookTicks -= 2;
		if (randomLookTicks <= 0)
		{
			randomLookTicks = 40 + RPGCore.rand.nextInt(40);
			targetYaw += 50 - RPGCore.rand.nextInt(101);
			targetPitch += 20 - RPGCore.rand.nextInt(41);
			if (targetPitch < -30)
				targetPitch = -30 - (targetPitch + 30);
			if (targetPitch > 30)
				targetPitch = 30 - (targetPitch - 30);
		}
	}

	public void tick5()
	{
	}

	public void tick10()
	{
	}

	public void tick20()
	{
		checkForVisibility();
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
		//yaw = (getAngle(new Vector(locX, 0, locZ), point.toVector()));
		/**
		double dx = locX - point.getX();
		double dy = locY - point.getY();
		double dz = locZ - point.getZ();
		pitch = (float) -Math.atan2(dy, Math.sqrt(dx * dx + dz * dz));
		yaw = (float) Math.atan2(dz, dy) - 90F;*/

		Location l = getBukkitLocation().setDirection(point.subtract(getBukkitLocation()).toVector().normalize());
		targetYaw = l.getYaw();
		targetPitch = l.getPitch();

		if (targetYaw > 180)
			targetYaw -= 360;
		if (targetYaw < -180)
			targetYaw += 360;
	}

	public void setSkin(String name)
	{
		deleteNPC();
		RPGCore.npcManager.createNPCUsernameSkin(getBukkitLocation(), this.name, name);
	}

	public void deleteNPC()
	{
		File file = new File(NPCManager.npcFolder.getPath() + "/" + databaseName + ".yml");
		file.delete();
		for (Player p: Bukkit.getOnlinePlayers())
			despawnFor(p);
		removed = true;
	}

	public void checkForVisibility()
	{
		for (Player p: Bukkit.getOnlinePlayers())
		{
			if (p.getWorld() != (org.bukkit.World) this.getWorld().getWorld())
			{
				if (inRangePlayers.contains(p.getUniqueId()))
					inRangePlayers.remove(p.getUniqueId());
				if (visiblePlayers.contains(p.getUniqueId()))
					visiblePlayers.remove(p.getUniqueId());
				continue;
			}
			RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
			double distance = p.getLocation().distanceSquared(getBukkitLocation());
			if (visiblePlayers.contains(p.getUniqueId()) && distance > visibleDistance)
			{
				despawnFor(p);
				visiblePlayers.remove(p.getUniqueId());
			} else if (!visiblePlayers.contains(p.getUniqueId()) && distance < visibleDistance)
			{
				spawnFor(p);
				visiblePlayers.add(p.getUniqueId());
			}

			if (!inRangePlayers.contains(p.getUniqueId()) && distance < chatRangeDistance * chatRangeDistance)
			{
				inRangePlayers.add(p.getUniqueId());
				if (rp.tutorialCompleted && getConversationData() != null && getConversationData().conversationLines != null)
				{
					ConversationLine chat = null;
					for (ConversationLine cl: getConversationData().conversationLines)
					{
						if (cl == null || cl.type == null)
							continue;
						if (!cl.type.equals(ConversationPartType.OPENING))
							continue;
						String value = rp.npcFlags.get(cl.flagKey);
						if (cl.flagKey != null && value != null && (cl.flagValue.equals(value) || cl.flagValue.equals("*")))
							chat = cl;
						else if (cl.flagKey == null)
							chat = cl;
					}
					if (chat != null)
						p.sendMessage(chat.getChatLine(getName()));
				}
			} else if (inRangePlayers.contains(p.getUniqueId()) && distance > chatRangeDistance * chatRangeDistance)
			{
				inRangePlayers.remove(p.getUniqueId());
				if (rp.tutorialCompleted && getConversationData() != null && getConversationData().conversationLines != null && rp.npcClosure == this)
				{
					rp.npcClosure = null;
					ConversationLine chat = null;
					for (ConversationLine cl: getConversationData().conversationLines)
					{
						if (!cl.type.equals(ConversationPartType.CLOSING))
							continue;
						String value = rp.npcFlags.get(cl.flagKey);
						if (cl.flagKey != null && value != null && (cl.flagValue.equals(value) || cl.flagValue.equals("*")))
							chat = cl;
						else if (cl.flagKey == null)
							chat = cl;
					}
					if (chat != null)
						p.sendMessage(chat.getChatLine(getName()));
				}
			}
		}
	}

	public ConversationData getConversationData()
	{
		if (conversationData != null)
			return conversationData;
		for (ConversationData cd: ConversationData.dataList)
			if (cd.npcName.equalsIgnoreCase(databaseName))
				conversationData = cd;
		return conversationData;
	}

	public void reloadForVisiblePlayers()
	{
		for (UUID uuid: visiblePlayers)
		{
			Player p = Bukkit.getPlayer(uuid);
			if (p == null)
				continue;
			despawnFor(p);
			spawnFor(p);
		}
	}

	public void spawnFor(Player p)
	{
		this.setLocation(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
		PacketPlayOutPlayerInfo pi = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER, this);
		PacketPlayOutNamedEntitySpawn spawn = new PacketPlayOutNamedEntitySpawn(this);
		PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(this, (byte) ((int) (yaw * 256.0F / 360.0F)));
		PacketPlayOutEntityLook look = new PacketPlayOutEntityLook(getId(), (byte) ((int) (yaw * 256.0F / 360.0F)), (byte) pitch, true);

		PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
		co.sendPacket(pi);
		co.sendPacket(spawn);
		co.sendPacket(look);
		co.sendPacket(rotation);

		PacketPlayOutPlayerInfo po = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this);
		RPGEvents.scheduleRunnable(new RPGEvents.SendDespawnPacket(po, co), 3 * 20);
	}

	public void despawnFor(Player p)
	{
		PacketPlayOutEntityDestroy pe = new PacketPlayOutEntityDestroy(this.getId());

		PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
		co.sendPacket(pe);
	}

	public void updateRotation()
	{
		if (lastUpdatedYaw == yaw && lastUpdatedPitch == pitch)
			return;

		for (UUID uuid: visiblePlayers)
		{
			Player p = Bukkit.getPlayer(uuid);
			PacketPlayOutEntityHeadRotation rotation = new PacketPlayOutEntityHeadRotation(this, (byte) ((int) (yaw * 256.0F / 360.0F)));
			PacketPlayOutEntityLook look = new PacketPlayOutEntityLook(getId(), (byte) ((int) (yaw * 256.0F / 360.0F)), (byte) pitch, true);

			PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
			co.sendPacket(look);
			co.sendPacket(rotation);
		}

		lastUpdatedYaw = yaw;
		lastUpdatedPitch = pitch;
	}

	public void updatePosition()
	{
		for (UUID uuid: visiblePlayers)
		{
			Player p = Bukkit.getPlayer(uuid);
			PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport(this);

			PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
			co.sendPacket(packet);
		}
	}

	public void saveNPC()
	{
		File file = new File(NPCManager.npcFolder.getPath() + "/" + databaseName + ".yml");
		ArrayList<String> lines = new ArrayList<String>();
		Location l = getBukkitLocation();
		lines.add("name: " + getName());
		lines.add("lockRotation: " + lockRotation);
		lines.add("chatRangeDistance: " + chatRangeDistance);
		lines.add("location:");
		lines.add(" world: " + l.getWorld().getName());
		lines.add(" position: " + l.getX() + ", " + l.getY() + ", " + l.getZ());
		lines.add(" rotation: " + l.getYaw() + ", " + l.getPitch());
		if (skinData != null)
			lines.add("skin: " + skinData.skinName);
		CakeLibrary.writeFile(lines, file);
	}
}