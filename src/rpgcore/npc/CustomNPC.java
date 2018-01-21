package rpgcore.npc;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.mojang.authlib.GameProfile;

import es.eltrueno.npc.skin.SkinData;
import es.eltrueno.npc.skin.TruenoNPCSkin;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.IScoreboardCriteria;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.ScoreboardObjective;
import net.minecraft.server.v1_12_R1.WorldServer;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class CustomNPC extends EntityPlayer 
{
	public static double visibleDistance = 128.0D;
	public static int globalID;
	public String name;
	public UUID uuid;
	public TruenoNPCSkin skin;
	public SkinData skinData;
	public int id;
	public int tick;
	public boolean removed;
	public ArrayList<UUID> visiblePlayers = new ArrayList<UUID>();
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
		RPGCore.npcManager.createNPC(getBukkitLocation(), this.name, name);
	}

	public void deleteNPC()
	{
		for (Player p: Bukkit.getOnlinePlayers())
			despawnFor(p);
		removed = true;
	}

	public void checkForVisibility()
	{
		for (Player p: Bukkit.getOnlinePlayers())
		{
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
		}
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