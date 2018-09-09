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
import rpgcore.npc.ConversationData.ConversationLine;
import rpgcore.npc.ConversationData.ConversationPartType;
import rpgcore.player.RPlayer;

public class CustomNPC extends EntityPlayer 
{
	public static double visibleDistance = 32.0D;
	public static double outRangeDistance = 5.0D;
	public static int globalID;
	public String name;
	public UUID uuid;
	public SkinData skinData;
	public int id;
	public int tick;
	public int randomLookTicks;
	public boolean removed;
	public float targetYaw, targetPitch, lastUpdatedYaw, lastUpdatedPitch;
	public ConversationData conversationData;
	public static final float rotationStepYaw = 25;
	public static final float rotationStepPitch = 10;
	public ArrayList<UUID> visiblePlayers = new ArrayList<UUID>();
	public ArrayList<UUID> inRangePlayers = new ArrayList<UUID>();
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
		checkForVisibility();
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
		checkForVisibility();
	}

	public void tick()
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

	public void tick2()
	{
		ArrayList<Player> near = CakeLibrary.getNearbyPlayers(getBukkitLocation(), 5);
		if (near.size() > 0)
		{
			lookAt(near.get(near.size() - 1).getLocation());
		} else {
			randomLookTicks -= 2;
			if (randomLookTicks <= 0)
			{
				randomLookTicks = 40 + RPGCore.rand.nextInt(40);
				targetYaw += 50 - RPGCore.rand.nextInt(101);
				targetPitch += 20 - RPGCore.rand.nextInt(41);
				if (targetPitch < -45)
					targetPitch = -45 - (targetPitch + 45);
				if (targetPitch > 45)
					targetPitch = 45 - (targetPitch - 45);
			}
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
		File file = new File("plugins/RPGCore/npcs/" + getName() + ".yml");
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
				continue;
			RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
			double distance = p.getLocation().distance(getBukkitLocation());
			if (visiblePlayers.contains(p.getUniqueId()) && distance > visibleDistance)
			{
				despawnFor(p);
				visiblePlayers.remove(p.getUniqueId());
			} else if (!visiblePlayers.contains(p.getUniqueId()) && distance < visibleDistance)
			{
				spawnFor(p);
				visiblePlayers.add(p.getUniqueId());
			}
			
			if (!inRangePlayers.contains(p.getUniqueId()) && distance < outRangeDistance)
			{
				inRangePlayers.add(p.getUniqueId());
				if (getConversationData() != null && getConversationData().conversationLines != null)
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
			} else if (inRangePlayers.contains(p.getUniqueId()) && distance > outRangeDistance)
			{
				inRangePlayers.remove(p.getUniqueId());
				if (getConversationData() != null && getConversationData().conversationLines != null && rp.npcClosure == this)
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
			if (cd.npcName.equals(getName()))
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

		PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
		co.sendPacket(pi);
		co.sendPacket(spawn);
		co.sendPacket(rotation);
	}

	public void despawnFor(Player p)
	{
		PacketPlayOutPlayerInfo pi = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER, this);
		PacketPlayOutEntityDestroy pe = new PacketPlayOutEntityDestroy(this.getId());

		PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
		co.sendPacket(pi);
		co.sendPacket(pe);
	}

	public void updateRotation()
	{
		if (lastUpdatedYaw == yaw && lastUpdatedPitch == pitch)
			return;
		
		for (UUID uuid: visiblePlayers)
		{
			Player p = Bukkit.getPlayer(uuid);
			PacketPlayOutEntityHeadRotation packet = new PacketPlayOutEntityHeadRotation(this, (byte) ((int) (yaw * 256.0F / 360.0F)));
			PacketPlayOutEntityLook l = new PacketPlayOutEntityLook(getId(), (byte) ((int) (yaw * 256.0F / 360.0F)), (byte) pitch, true);

			PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
			co.sendPacket(l);
			co.sendPacket(packet);
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
		File file = new File("plugins/RPGCore/npcs/" + getName() + ".yml");
		ArrayList<String> lines = new ArrayList<String>();
		Location l = getBukkitLocation();
		lines.add("name: " + getName());
		lines.add("location:");
		lines.add(" world: " + l.getWorld().getName());
		lines.add(" position: " + l.getX() + ", " + l.getY() + ", " + l.getZ());
		lines.add(" rotation: " + l.getYaw() + ", " + l.getPitch());
		if (skinData != null)
			lines.add("skin: " + skinData.skinName);
		CakeLibrary.writeFile(lines, file);
	}
}