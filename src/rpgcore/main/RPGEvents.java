package rpgcore.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.areas.Area;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.external.InstantFirework;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.RItem;
import rpgcore.npc.CustomNPC;
import rpgcore.player.RPlayer;
import rpgcore.skills.effect.ArmageddonE;
import rpgcore.songs.RSongManager;
import rpgcore.songs.RunningTrack;

public class RPGEvents implements Runnable
{
	public static RPGCore instance;
	public static boolean stopped;

	public static LivingEntity customHit;
	public RPGEvents(RPGCore instance)
	{
		RPGEvents.instance = instance;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, this, 0L, 0L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new RunTimer10(), 0L, 10L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new RunTimer20(), 0L, 10L);
		new MusicRuntime();
	}

	@Override
	public void run()
	{
		RPGCore.serverAliveTicks++;
		RPGCore.playerManager.playersTick();
		ArrayList<CustomNPC> remove = new ArrayList<CustomNPC>();
		for (CustomNPC n: RPGCore.npcManager.npcs)
		{
			n.tick();
			if (n.removed)
				remove.add(n);
		}
		RPGCore.npcManager.npcs.removeAll(remove);
		for (RPGMonster ce: RPGMonster.entities)
			ce.tick();
		RPGMonster.entities.removeAll(RPGMonster.remove);
		RPGMonster.remove.clear();

		ArmageddonE.globalTick();
		DamageOverTime.globalTick();
	}

	public static class EntityDamageHistory
	{
		public int entityID;
		public Map<UUID, Integer> damageHistory = new HashMap<UUID, Integer>();

		public static ArrayList<EntityDamageHistory> damageHistories = new ArrayList<EntityDamageHistory>();
		public static ArrayList<EntityDamageHistory> remove = new ArrayList<EntityDamageHistory>();
		private EntityDamageHistory(int entityID)
		{
			this.entityID = entityID;
			damageHistories.add(this);
		}

		public static void ApplyDamage(int entityID, UUID damager, int damage)
		{
			damageHistories.removeAll(remove);
			remove.clear();

			for (EntityDamageHistory history: damageHistories)
			{
				if (history.entityID == entityID)
				{
					if (!history.damageHistory.containsKey(damager))
						continue;
					int get = history.damageHistory.get(damager);
					history.damageHistory.put(damager, get + damage);
					return;
				}
			}

			EntityDamageHistory history = new EntityDamageHistory(entityID);
			history.damageHistory.put(damager, damage);
		}
	}

	public static class DamageOverTime
	{
		public int length;
		public int interval;
		public int damage;
		public int tick;
		public LivingEntity victim;
		public Entity damager;

		public static ArrayList<DamageOverTime> list = new ArrayList<DamageOverTime>();
		public static ArrayList<DamageOverTime> remove = new ArrayList<DamageOverTime>();

		public DamageOverTime(int length, int interval, int damage, Entity damager, LivingEntity victim)
		{
			this.length = length;
			this.interval = interval;
			this.damage = damage;
			this.damager = damager;
			this.victim = victim;

			this.tick = interval;
			list.add(this);
		}

		public static void globalTick()
		{
			for (DamageOverTime dot: list)
			{
				if (dot.victim.getHealth() <= 0 || dot.victim.isDead())
				{
					remove.add(dot);
					continue;
				}

				dot.length--;
				dot.tick--;

				if (dot.tick <= 0)
				{
					new ApplyDamage(dot.damager, dot.victim, dot.damage).run();
					dot.tick = dot.interval;
				}

				if (dot.length <= 0)
					remove.add(dot);
			}
			list.removeAll(remove);
			remove.clear();
		}
	}

	public class MusicRuntime extends TimerTask
	{
		public MusicRuntime()
		{
			RPGCore.timer.scheduleAtFixedRate(this, 0L, 4L);
		}

		@Override
		public void run()
		{
			if (stopped)
				return;
			ArrayList<RunningTrack> remove = new ArrayList<RunningTrack>();
			ArrayList<RunningTrack> run = (ArrayList<RunningTrack>) RSongManager.runningTracks.clone();
			for (RunningTrack rt: run)
				if (rt != null)
				{
					if (rt.stopped)
					{
						remove.add(rt);
						continue;
					}
					rt.run();
				} else {
					remove.add(rt);
					continue;
				}
			RSongManager.runningTracks.removeAll(remove);
		}

	}

	public class RunTimer20 implements Runnable
	{
		@Override
		public void run()
		{
			RPGCore.playerManager.playersTick20();
			Area.tick();
		}
	}

	public class RunTimer10 implements Runnable
	{
		@Override
		public void run()
		{
			for (RPGMonster ce: RPGMonster.entities)
				ce.findTarget();
			RPGCore.playerManager.playersTick10();
		}
	}

	public static void scheduleRunnable(Runnable runnable, int delay)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
	}

	public static class ApplyDamage implements Runnable
	{
		public Entity damager;
		public LivingEntity damagee;
		public int damage;
		public ApplyDamage(Entity damager, LivingEntity damagee, int damage)
		{
			this.damager = damager;
			this.damagee = damagee;
			this.damage = damage;
		}

		@Override
		public void run()
		{
			if (damager != null)
				if (damager instanceof Player)
				{
					Player p = (Player) damager;
					EntityDamageHistory.ApplyDamage(damagee.getEntityId(), p.getUniqueId(), damage);
				}
			if (damagee instanceof Player)
			{
				RPlayer rp = RPGCore.playerManager.getRPlayer(((Player) damagee).getUniqueId());
				damage = (int) (damage - (damage / 100D * rp.calculateDamageReduction()));
			}
			damagee.setNoDamageTicks(0);
			damagee.damage(damage);
		}
	}

	public static class ApplyPotionEffect implements Runnable
	{
		public LivingEntity entity;
		public PotionEffect pe;
		public ApplyPotionEffect(LivingEntity damager, PotionEffect pe)
		{
			this.entity = damager;
			this.pe = pe;
		}

		@Override
		public void run()
		{
			entity.addPotionEffect(pe, true);
		}
	}

	public static class PlaceBlock implements Runnable
	{
		public Block block;
		public int toID;
		public PlaceBlock(Block block, int toID)
		{
			this.block = block;
			this.toID = toID;
		}

		@Override
		public void run()
		{
			block.setTypeId(toID);
		}
	}

	public static class ConsoleCommand implements Runnable
	{
		public String command;
		public ConsoleCommand(String command)
		{
			this.command = command;
		}

		@Override
		public void run()
		{
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
		}
	}

	public static class Message implements Runnable
	{
		public CommandSender cs;
		public String[] msgs;
		public Message(CommandSender cs, String... msgs)
		{
			this.cs = cs;
			this.msgs = msgs;
		}

		@Override
		public void run()
		{
			for (String msg: msgs)
				cs.sendMessage(CakeLibrary.recodeColorCodes(msg));
		}
	}

	/**
	public static class ColoredParticle implements Runnable
	{
		public Location l;
		public Entity e;
		public EnumParticle particleType;
		public float range;
		public int particles = 1;
		public int r;
		public int g;
		public int b;

		public ColoredParticle(Location l, float range, EnumParticle particleType, int r, int g, int b, int particles)
		{
			this.l = l;
			this.e = null;
			this.range = range;
			this.particles = particles;
			this.particleType = particleType;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public ColoredParticle(Entity e, float range, EnumParticle particleType, int r, int g, int b, int particles)
		{
			this.e = e;
			this.l = null;
			this.range = range;
			this.particles = particles;
			this.particleType = particleType;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		@Override
		public void run()
		{
			if (e != null)
				this.l = e.getLocation();
			ParticleEffects.valueOf(particleType.name()).display(new ParticleEffects.OrdinaryColor(r, g, b), l, 64);
			if (this.e != null && e instanceof LivingEntity)
				for (int i = 0; i < ((LivingEntity) e).getEyeHeight(); i++)
				{
					l.setY(l.getY() + 1);
					ParticleEffects.valueOf(particleType.name()).display(new ParticleEffects.OrdinaryColor(r, g, b), l, 64);
				}
		}
	}**/

	public static class ColoredParticle implements Runnable
	{
		public Location l;
		public Entity e;
		public Particle particleType;
		public float range;
		public int particles = 1;
		public int r;
		public int g;
		public int b;

		public ColoredParticle(Location l, float range, Particle particleType, int r, int g, int b, int particles)
		{
			this.l = l;
			this.e = null;
			this.range = range;
			this.particles = particles;
			this.particleType = particleType;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public ColoredParticle(Entity e, float range, Particle particleType, int r, int g, int b, int particles)
		{
			this.e = e;
			this.l = null;
			this.range = range;
			this.particles = particles;
			this.particleType = particleType;
			this.r = r;
			this.g = g;
			this.b = b;
		}

		@Override
		public void run()
		{
			if (e != null)
				this.l = e.getLocation();
			//PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleType, true, (float) l.getX(), (float) l.getY(), (float) l.getZ(), r / 255F, g / 255F, b / 255F, 0, particles, 0);
			//PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles(particleType, true, (float) l.getX(), (float) l.getY(), (float) l.getZ(), range, range, range, 255, particles, 0, 128, 128);

			for (Player p: CakeLibrary.getNearbyPlayers(l, 16))
				//p.spawnParticle(Particle.REDSTONE, l.getX(), l.getY(), l.getZ(), 0, r / 255D, g / 255D, b / 255D, 1);
				p.spawnParticle(particleType, l, particles, r / 255D, g / 255D, b / 255D, 1);
			//((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}

	public static class CrystalInvCheck implements Runnable
	{
		public Player player;
		public Inventory inv;
		public BonusStatCrystal type;
		public CrystalInvCheck(Player player, Inventory inv, BonusStatCrystal type)
		{
			this.player = player;
			this.inv = inv;
			this.type = type;
		}

		@Override
		public void run()
		{
			ItemStack mid = inv.getItem(4);
			if (CakeLibrary.isItemStackNull(mid))
				return;
			if (!CakeLibrary.playerHasVacantSlots(player))
			{
				RPGCore.msg(player, "You do not have inventory space.");
				return;
			}
			if (mid.getAmount() > 1)
			{
				RPGCore.msg(player, "You can only use this crystal on one item at a time.");
				return;
			}
			RItem ri = new RItem(mid);
			if (ri.bonusStat == null && type != BonusStatCrystal.STAT_ADDER)
			{
				RPGCore.msg(player, "This item does not have a bonus stat.");
				return;
			}
			boolean success = ri.applyCrystal(type);
			if (success)
			{
				inv.setItem(4, new ItemStack(Material.AIR));
				player.getInventory().addItem(ri.createItem());
				new RPGEvents.PlayEffect(Effect.STEP_SOUND, player, 20).run();

				ItemStack crystal = inv.getItem(0);
				if (crystal.getAmount() == 1)
				{
					inv.setItem(0, new ItemStack(Material.AIR));
					player.closeInventory();
				} else {
					ItemStack change = crystal.clone();
					change.setAmount(crystal.getAmount() - 1);
					inv.setItem(0, change);
				}
			}
		}
	}

	public static class PlaySoundEffect implements Runnable
	{
		public Location l;
		public Sound sound;
		public float volume;
		public float pitch;

		public PlaySoundEffect(Location location, Sound sound, float volume, float pitch)
		{
			this.l = location;
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		public PlaySoundEffect(Entity location, Sound sound, float volume, float pitch)
		{
			this.l = location.getLocation();
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		@Override
		public void run()
		{
			l.getWorld().playSound(l, sound, volume, pitch);
		}
	}

	public static class InitializePlayerScoreboard implements Runnable
	{
		public RPlayer player;

		public InitializePlayerScoreboard(RPlayer player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if (player.getPlayer() != null)
				player.initializeScoreboard();
		}
	}

	public static class UpdatePlayerScoreboard implements Runnable
	{
		public RPlayer player;

		public UpdatePlayerScoreboard(RPlayer player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if (player.getPlayer() != null)
				player.updateScoreboard();
		}
	}

	public static class PlayLightningEffect implements Runnable
	{
		public Location l;

		public PlayLightningEffect(Location location)
		{
			this.l = location;
		}

		public PlayLightningEffect(Entity entity)
		{
			this.l = entity.getLocation();
		}

		@Override
		public void run()
		{
			l.getWorld().strikeLightningEffect(l);
		}
	}

	public static class PlayExplosionEffect implements Runnable
	{
		public Location l;

		public PlayExplosionEffect(Location location)
		{
			this.l = location;
		}

		public PlayExplosionEffect(Entity entity)
		{
			this.l = entity.getLocation();
		}

		@Override
		public void run()
		{
			l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 0.0000000001F, false, false);
		}
	}

	public static class PlaySoundEffectForPlayer implements Runnable
	{
		public Player player;
		public Sound sound;
		public float volume;
		public float pitch;

		public PlaySoundEffectForPlayer(Player player, Sound sound, float volume, float pitch)
		{
			this.player = player;
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		@Override
		public void run()
		{
			player.playSound(player.getLocation(), sound, volume, pitch);
		}
	}

	public static class AOEDetectionCustom implements Runnable
	{
		public ArrayList<LivingEntity> hit;
		public double radius;
		public Location l;
		public LivingEntity damager;
		public Callable<Void> func;

		public AOEDetectionCustom(ArrayList<LivingEntity> hit, Location l, double radius, LivingEntity damager, Callable<Void> func)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damager = damager;
			this.func = func;
		}

		@Override
		public void run()
		{
			boolean player = damager instanceof Player;
			boolean monster = damager instanceof Monster;
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(l, radius))
			{
				if (e == damager)
					continue;
				if (player && e instanceof Player)
					continue;
				if (monster && e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);

				try {
					customHit = e;
					func.call();
					customHit = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public static class AOEDetectionAttack implements Runnable
	{
		public ArrayList<LivingEntity> hit;
		public double radius;
		public Location l;
		public int damage;
		public LivingEntity damager;
		public double knockback;

		public AOEDetectionAttack(ArrayList<LivingEntity> hit, Location l, double radius, int damage, LivingEntity damager)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damage = damage;
			this.damager = damager;
		}

		public AOEDetectionAttack(ArrayList<LivingEntity> hit, Location l, double radius, int damage, LivingEntity damager, double knockback)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damage = damage;
			this.damager = damager;
			this.knockback = knockback;
		}

		@Override
		public void run()
		{
			boolean player = damager instanceof Player;
			boolean monster = damager instanceof Monster;
			Location o = damager.getLocation();
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(l, radius))
			{
				if (e == damager)
					continue;
				if (player && e instanceof Player)
					continue;
				if (monster && e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				if (damager instanceof Player)
					damage = RPlayer.varyDamage(damage);
				new RPGEvents.ApplyDamage(damager, e, damage).run();
				if (knockback > 0)
				{
					Location p = e.getLocation();
					Vector direction = (new Vector(p.getX() - o.getX(), knockback, p.getZ() - o.getZ())).normalize().multiply(knockback);
					direction.setY(knockback / 4.0D);
					e.setVelocity(direction);
				}
			}
		}
	}

	public static class AOEDetectionAttackWithBlockBreakEffect implements Runnable
	{
		public ArrayList<LivingEntity> hit;
		public double radius;
		public Location l;
		public int damage;
		public LivingEntity damager;
		public int blockID;

		public AOEDetectionAttackWithBlockBreakEffect(ArrayList<LivingEntity> hit, Location l, double radius, int damage, LivingEntity damager, int blockID)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damage = damage;
			this.damager = damager;
			this.blockID = blockID;
		}

		@Override
		public void run()
		{
			boolean player = damager instanceof Player;
			boolean monster = damager instanceof Monster;
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(l, radius))
			{
				if (e == damager)
					continue;
				if (player && e instanceof Player)
					continue;
				if (monster && e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				damage = RPlayer.varyDamage(damage);
				new RPGEvents.ApplyDamage(damager, e, damage).run();
				new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, blockID).run();
			}
		}
	}

	public static class AOEDetectionAttackWithFireworkEffect implements Runnable
	{
		public ArrayList<LivingEntity> hit;
		public double radius;
		public Location l;
		public int damage;
		public LivingEntity damager;
		public FireworkEffect fe;

		public AOEDetectionAttackWithFireworkEffect(ArrayList<LivingEntity> hit, Location l, double radius, int damage, LivingEntity damager, FireworkEffect fe)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damage = damage;
			this.damager = damager;
			this.fe = fe;
		}

		@Override
		public void run()
		{
			boolean player = damager instanceof Player;
			boolean monster = damager instanceof Monster;
			for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(l, radius))
			{
				if (e == damager)
					continue;
				if (player && e instanceof Player)
					continue;
				if (monster && e instanceof Monster)
					continue;
				if (hit.contains(e))
					continue;
				hit.add(e);
				damage = RPlayer.varyDamage(damage);
				new RPGEvents.ApplyDamage(damager, e, damage).run();
				new InstantFirework(fe, e.getLocation());
			}
		}
	}

	public static class PlayEffect implements Runnable
	{
		public Location l;
		public Entity e;
		public Effect effect;
		public int id;

		public PlayEffect(Effect effect, Location location, int id)
		{
			this.effect = effect;
			this.l = location;
			this.e = null;
			this.id = id;
		}

		public PlayEffect(Effect effect, Entity location, int id)
		{
			this.effect = effect;
			this.e = location;
			this.l = null;
			this.id = id;
		}

		@Override
		public void run()
		{
			if (e != null)
				this.l = e.getLocation();
			if (e instanceof LivingEntity)
				for (double i = Math.ceil(((LivingEntity) e).getEyeHeight()); i > 0; i--)
					l.getWorld().playEffect(l.clone().add(0, i - 1, 0), effect, id);
			else
				l.getWorld().playEffect(l, effect, id);
		}
	}

	public static class FireworkTrail implements Runnable
	{
		public Location l;
		public int particles;
		public float speed = 0.0F;
		public float range = 0.0F;

		public FireworkTrail(Location l, float range, int particles)
		{
			this.l = l;
			this.particles = particles;
			this.range = range;
		}

		public FireworkTrail(Entity e, float range, int particles)
		{
			this.l = e.getLocation();
			this.particles = particles;
			this.range = range;
		}

		public FireworkTrail(Location l, float range, int particles, float speed)
		{
			this.l = l;
			this.particles = particles;
			this.speed = speed;
			this.range = range;
		}

		@Override
		public void run()
		{
			for (Player p: CakeLibrary.getNearbyPlayers(l, 16))
				CakeLibrary.spawnParticle(EnumParticle.FIREWORKS_SPARK, l, range, p, particles, speed);
		}
	}

	public static class ParticleEffect implements Runnable
	{
		public Location l;
		public int particles;
		public float speed = 0.0F;
		public float range = 0.0F;
		public EnumParticle type;
		public int[] data;
		public ParticleEffect(EnumParticle type, Location l, float range, int particles)
		{
			this.type = type;
			this.l = l;
			this.particles = particles;
			this.range = range;
		}

		public ParticleEffect(EnumParticle type, Entity e, float range, int particles)
		{
			this.type = type;
			this.l = e.getLocation();
			this.particles = particles;
			this.range = range;
		}

		public ParticleEffect(EnumParticle type, Location l, float range, int particles, float speed)
		{
			this.type = type;
			this.l = l;
			this.particles = particles;
			this.speed = speed;
			this.range = range;
		}

		public ParticleEffect(EnumParticle type, Location l, float range, int particles, float speed, int... data)
		{
			this.type = type;
			this.l = l;
			this.particles = particles;
			this.speed = speed;
			this.range = range;
			this.data = data;
		}

		@Override
		public void run()
		{
			for (Player p: CakeLibrary.getNearbyPlayers(l, 16))
				CakeLibrary.spawnParticle(type, l, range, p, particles, speed, data);
		}
	}

	public static class InventoryOpen implements Runnable
	{
		public Player player;
		public Inventory inv;
		public InventoryOpen(Player player, Inventory inv)
		{
			this.player = player;
			this.inv = inv;
		}

		@Override
		public void run()
		{
			player.openInventory(inv);
		}
	}

	public static class InventoryClose implements Runnable
	{
		public Player player;
		public InventoryClose(Player player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			player.closeInventory();
		}
	}
}
