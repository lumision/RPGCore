package rpgcore.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
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
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_12_R1.EnumParticle;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PlayerConnection;
import rpgcore.buff.Buff;
import rpgcore.entities.bosses.CorruptedMage;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.external.InstantFirework;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.kits.RPGKit;
import rpgcore.item.EnhancementInventory;
import rpgcore.item.GlobalGift;
import rpgcore.item.RItem;
import rpgcore.monsterbar.MonsterBar;
import rpgcore.npc.NPCManager;
import rpgcore.player.RPlayer;
import rpgcore.recipes.RPGRecipe;
import rpgcore.skills.RPGSkill.SkillEffect;
import rpgcore.songs.RSongManager;
import rpgcore.songs.RunningTrack;

public class RPGEvents implements Runnable
{
	public static RPGCore instance;
	public static boolean stopped;
	public static ArrayList<Item> itemDropTrails = new ArrayList<Item>();
	public static final Random critRandom = new Random();

	public static LivingEntity customHit;
	public RPGEvents(RPGCore instance)
	{
		RPGEvents.instance = instance;
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, this, 0L, 0L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new RunTimer10(), 0L, 10L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new RunTimer20(), 0L, 20L);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(instance, new RunTimerMinute(), 0L, 1200L);
		new MusicRuntime();
	}

	@Override
	public void run()
	{
		RPGCore.serverAliveTicks++;
		RPGCore.playerManager.playersTick();
		for (int i = 0; i < NPCManager.npcs.size(); i++)
			if (NPCManager.npcs.get(i).tick())
			{
				NPCManager.npcs.remove(i);
				i--;
			}
		for (int i = 0; i < RPGMonster.entities.size(); i++)
			if (RPGMonster.entities.get(i).tick())
			{
				RPGMonster.entities.remove(i);
				i--;
			}
		for (int i = 0; i < Buff.buffs.size(); i++)
			if (Buff.buffs.get(i).tick())
			{
				Buff.buffs.remove(i);
				i--;
			}

		if (RPGCore.serverAliveTicks % 2 == 0)
		{
			for (int i = 0; i < itemDropTrails.size(); i++)
			{
				Item item = itemDropTrails.get(i);
				if (item.isOnGround())
				{
					itemDropTrails.remove(i);
					i--;
					continue;
				}
				new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, item.getLocation(), 0, 1, 0, 20).run();
			}
		}

		SkillEffect.globalTick();

		DamageOverTime.globalTick();
		Bind.globalTick();
	}

	public static class Bind
	{
		public static ArrayList<Bind> binds = new ArrayList<Bind>();

		public LivingEntity victim;
		public int bindDuration;
		private RPGMonster victimR;
		public boolean showEffects;
		private Bind(LivingEntity victim, int bindDuration, boolean showEffects)
		{
			this.victim = victim;
			this.bindDuration = bindDuration;
			this.showEffects = showEffects;

			this.victimR = RPGMonster.getRPGMob(victim.getEntityId());
			if (this.victimR != null)
				this.victimR.bound = true;
		}

		public static void bindTarget(LivingEntity victim, int bindDuration, boolean showEffects)
		{
			victim.removePotionEffect(PotionEffectType.SLOW);
			victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, bindDuration, 32767));

			if (showEffects)
			{
				new RPGEvents.PlayEffect(Effect.STEP_SOUND, victim, 30).run();
				if (victim instanceof Player)
				{
					Player p = (Player) victim;
					RPGCore.msgNoTag(p, "&c * You have been bound! *");
					RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
					if (rp != null)
						rp.globalCastDelay = bindDuration;
				}
			}
			for (Bind bind: binds)
				if (bind.victim == victim)
				{
					if (bind.bindDuration < bindDuration)
						bind.bindDuration = bindDuration;
					return;
				}

			binds.add(new Bind(victim, bindDuration, showEffects));
		}

		public static void globalTick()
		{
			for (int i = 0; i < binds.size(); i++)
			{
				Bind bind = binds.get(i);
				bind.bindDuration--;
				if (bind.bindDuration <= 0)
				{
					if (bind.victimR != null)
						bind.victimR.bound = false;
					binds.remove(i);
					i--;
					continue;
				}
				if (bind.showEffects)
					new RPGEvents.ParticleEffect(EnumParticle.BLOCK_DUST, bind.victim.getEyeLocation(), 0.25F, 8, 0, 30).run();

			}
		}
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
		int length;
		int interval;
		int damage;
		int tick;
		LivingEntity victim;
		Entity damager;

		public static ArrayList<DamageOverTime> list = new ArrayList<DamageOverTime>();

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
			for (int i = 0; i < list.size(); i++)
			{
				DamageOverTime dot = list.get(i);
				if (dot.victim.getHealth() <= 0 || dot.victim.isDead())
				{
					list.remove(i);
					i--;
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
				{
					list.remove(i);
					i--;
					continue;
				}
			}
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

			for (int i = 0; i < RSongManager.runningTracks.size(); i++)
			{
				RunningTrack rt = RSongManager.runningTracks.get(i);
				if (rt == null)
				{
					RSongManager.runningTracks.remove(i);
					i--;
					continue;
				}
				if (rt.stopped)
				{
					RSongManager.runningTracks.remove(i);
					i--;
					continue;
				}
				rt.run();
			}
		}

	}

	public class RunTimerMinute implements Runnable
	{
		@Override
		public void run()
		{
			RPGKit.globalCheck();
			GlobalGift.checkExpiries();
			for (Player p: Bukkit.getOnlinePlayers())
				GlobalGift.checkForPlayer(RPGCore.playerManager.getRPlayer(p.getUniqueId()));
		}
	}

	public class RunTimer20 implements Runnable
	{
		@Override
		public void run()
		{
			for (int w = 0; w < Bukkit.getWorlds().size(); w++)
			{
				World world = Bukkit.getWorlds().get(w);
				for (int e = 0; e < world.getLivingEntities().size(); e++)
				{
					LivingEntity entity = world.getLivingEntities().get(e);
					if (!(entity instanceof Player))
						if (CakeLibrary.getNearbyPlayers(entity.getLocation(), 128).size() <= 0)
							entity.remove();
				}
			}
		}
	}

	public class RunTimer10 implements Runnable
	{
		@Override
		public void run()
		{
		}
	}

	public static void scheduleRunnable(Runnable runnable, int delay)
	{
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, runnable, delay);
	}

	public static class CheckEnhancementInventory implements Runnable
	{
		Inventory inv;
		int state;
		Player p;
		public CheckEnhancementInventory(Inventory inv, Player p, int state)
		{
			this.inv = inv;
			this.p = p;
			this.state = state;
		}

		@Override
		public void run()
		{
			EnhancementInventory.updateInventory(inv, state);
			p.updateInventory();
		}
	}

	public static class UpdateEnhancementInventoryMiddleSlot implements Runnable
	{
		Inventory inv;
		Player p;
		public UpdateEnhancementInventoryMiddleSlot(Inventory inv, Player p)
		{
			this.inv = inv;
			this.p = p;
		}

		@Override
		public void run()
		{
			EnhancementInventory.updateMiddleItem(inv);
			p.updateInventory();
		}
	}

	public static class UpdateMonsterBar implements Runnable
	{
		MonsterBar mb;
		public UpdateMonsterBar(MonsterBar mb)
		{
			this.mb = mb;
		}

		@Override
		public void run()
		{
			mb.updateBar();
		}
	}

	public static class SubtractCursor implements Runnable
	{
		InventoryView view;
		public SubtractCursor(InventoryView view)
		{
			this.view = view;
		}

		@Override
		public void run()
		{
			ItemStack cursor = view.getCursor();
			if (CakeLibrary.isItemStackNull(cursor))
				return;
			if (cursor.getAmount() == 1)
			{
				view.setCursor(new ItemStack(Material.AIR));
				return;
			}
			cursor.setAmount(cursor.getAmount() - 1);
			view.setCursor(cursor);
		}
	}

	public static class SetCursor implements Runnable
	{
		InventoryView view;
		ItemStack item;
		public SetCursor(InventoryView view, ItemStack item)
		{
			this.view = view;
			this.item = item;
		}

		@Override
		public void run()
		{
			view.setCursor(item);
		}
	}

	public static class SetSpawnerType implements Runnable
	{
		CreatureSpawner b;
		EntityType type;
		public SetSpawnerType(CreatureSpawner b, EntityType type)
		{
			this.b = b;
			this.type = type;
		}

		@Override
		public void run()
		{
			b.setSpawnedType(type);
			b.update();
		}
	}

	public static class CheckForRecipe implements Runnable
	{
		CraftingInventory inv;
		public CheckForRecipe(CraftingInventory inv)
		{
			this.inv = inv;
		}

		@Override
		public void run()
		{
			ItemStack[] matrix = inv.getMatrix();
			ItemStack result = inv.getResult();
			boolean remove = false;

			for (RPGRecipe recipe: RPGRecipe.recipes)
				if (recipe.crafted(matrix))
				{
					if (!CakeLibrary.isItemStackNull(result) && RItem.compare(result, recipe.result.createItem()))
						return;
					inv.setItem(0, recipe.getResult());
					for (HumanEntity player: inv.getViewers())
						if (player instanceof Player)
						{
							Player p = (Player) player;
							if (recipe.sound != null)
								p.playSound(player.getLocation(), recipe.sound, recipe.volume, recipe.pitch);
							p.updateInventory();
						}
					return;
				} else if (!CakeLibrary.isItemStackNull(result) && RItem.compare(result, recipe.result.createItem()))
					remove = true;

			if (remove)
			{
				inv.setResult(new ItemStack(Material.AIR));
				for (HumanEntity player: inv.getViewers())
					if (player instanceof Player)
					{
						Player p = (Player) player;
						p.updateInventory();
					}
			}


		}
	}

	public static class SetInventoryItem implements Runnable
	{
		Inventory inv;
		int slot;
		ItemStack item;
		public SetInventoryItem(Inventory inv, int slot, ItemStack item)
		{
			this.inv = inv;
			this.slot = slot;
			this.item = item;;
		}

		@Override
		public void run()
		{
			inv.setItem(slot, item);
			if (inv.getViewers().size() > 0)
				for (HumanEntity he: inv.getViewers())
					if (he instanceof Player)
						((Player) he).updateInventory();
		}
	}

	public static class SendDespawnPacket implements Runnable
	{
		PacketPlayOutPlayerInfo packet;
		PlayerConnection connection;
		public SendDespawnPacket(PacketPlayOutPlayerInfo packet, PlayerConnection connection)
		{
			this.packet = packet;
			this.connection = connection;
		}

		@Override
		public void run()
		{
			connection.sendPacket(packet);
		}
	}

	public static class ApplyDamage implements Runnable
	{
		//DAMAGE APPLICATION
		Entity damager;
		LivingEntity damagee;
		int damage;
		public ApplyDamage(Entity damager, LivingEntity damagee, int damage)
		{
			this.damager = damager;
			this.damagee = damagee;
			this.damage = damage;
		}

		@Override
		public void run()
		{
			if (damager instanceof Player)
			{
				Player p = (Player) damager;
				RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
				if (rp != null)
				{
					MonsterBar mb = MonsterBar.getMonsterBar(damagee);
					if (mb != null)
					{
						if (!(!damagee.isDead() && rp.lastBarTicks > 25))
						{
							if (rp.monsterBar == mb)
							{
								mb.updateBarOneTickLater();
								rp.lastBarTicks = 30;
							} else if (rp.lastBarTicks <= 0)
							{
								mb.updateBarOneTickLater();
								mb.showForPlayer(rp);
								rp.lastBarTicks = 30;
							}
						}
					}
					if (critRandom.nextInt(100) <= rp.getStats().critChanceAdd)
					{
						damage += (int) (damage / 100.0F * (float) rp.getStats().critDamageAdd);
						p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.6F, 0.75F);
					}
					damage *= rp.getStats().totalDamageMultiplier;
				}
				EntityDamageHistory.ApplyDamage(damagee.getEntityId(), p.getUniqueId(), damage);

				RPGMonster ce = RPGMonster.getRPGMob(damagee.getEntityId());
				if (ce != null)
				{
					if (ce.isBoss)
						damage *= rp.getStats().bossDamageMultiplier;
					if (ce.target == null)
					{
						ce.target = p;
						ce.entity.setTarget(p); 
					}
					if (ce instanceof CorruptedMage)
					{
						int phase = ((CorruptedMage) ce).phase;
						if (phase == 1 || phase == 3)
						{
							ce.entity.setHealth(Math.min(ce.entity.getMaxHealth(), ce.entity.getHealth() + damage));
							damage = 0;
						}
					}
				}
			}
			if (damagee instanceof Player)
			{
				RPlayer rp = RPGCore.playerManager.getRPlayer(((Player) damagee).getUniqueId());
				if (rp.invulnerabilityTicks > 0)
					return;
				damage -= (int) (damage / 100.0F * (float) rp.getStats().damageReductionAdd);
			}
			damagee.setNoDamageTicks(0);
			damagee.damage(damage);
		}
	}

	public static class ApplyPotionEffect implements Runnable
	{
		LivingEntity entity;
		PotionEffect pe;
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
		String command;
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
		CommandSender cs;
		String[] msgs;
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
		Location l;
		Entity e;
		Particle particleType;
		float range;
		int particles = 1;
		int r;
		int g;
		int b;

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

			for (Player p: CakeLibrary.getNearbyPlayers(l, 24))
				//p.spawnParticle(Particle.REDSTONE, l.getX(), l.getY(), l.getZ(), 0, r / 255D, g / 255D, b / 255D, 1);
				p.spawnParticle(particleType, l, particles, r / 255D, g / 255D, b / 255D, 1);
			//((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
		}
	}

	public static class CrystalInvCheck implements Runnable
	{
		RPlayer player;
		Inventory inv;
		BonusStatCrystal type;
		public CrystalInvCheck(RPlayer player, Inventory inv, BonusStatCrystal type)
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
			if (player.getPlayer() == null)
				return;
			if (mid.getAmount() > 1)
			{
				RPGCore.msg(player.getPlayer(), "You can only use crystals on one item at a time.");
				return;
			}
			RItem ri = new RItem(mid);
			if (ri.bonusStat == null && type != BonusStatCrystal.STAT_ADDER)
			{
				RPGCore.msg(player.getPlayer(), "This item does not have a bonus stat.");
				return;
			}
			if (!(ri.accessory || ri.magicDamage != 0 || ri.bruteDamage != 0 || ri.attackSpeed != 0
					|| ri.cooldownReduction != 0 || ri.critChance != 0 || ri.critDamage != 0
					|| ri.damageReduction != 0 || ri.recoverySpeed != 0 || ri.xpMultiplier != 0))
			{
				RPGCore.msg(player.getPlayer(), "Only equipments or accessories can have bonus stats.");
				return;
			}
			boolean success = ri.applyCrystal(type);
			if (success)
			{
				inv.setItem(4, new ItemStack(Material.AIR));
				player.giveItem(ri);
				new RPGEvents.PlayEffect(Effect.STEP_SOUND, player.getPlayer(), 20).run();

				ItemStack crystal = inv.getItem(0);
				if (crystal.getAmount() == 1)
				{
					inv.setItem(0, new ItemStack(Material.AIR));
					player.getPlayer().closeInventory();
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
		Location l;
		Sound sound;
		float volume;
		float pitch;

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
		RPlayer player;

		public UpdatePlayerScoreboard(RPlayer player)
		{
			this.player = player;
		}

		@Override
		public void run()
		{
			if (player.getPlayer() != null)
				player.updateScoreboard = true;
		}
	}

	public static class PlayLightningEffect implements Runnable
	{
		Location l;

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
		Player player;
		Sound sound;
		float volume;
		float pitch;

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
		ArrayList<LivingEntity> hit;
		double radius;
		Location l;
		Entity e;
		LivingEntity damager;
		Callable<Void> func;

		public AOEDetectionCustom(ArrayList<LivingEntity> hit, Location l, double radius, LivingEntity damager, Callable<Void> func)
		{
			this.hit = hit;
			this.radius = radius;
			this.l = l;
			this.damager = damager;
			this.func = func;
		}

		public AOEDetectionCustom(ArrayList<LivingEntity> hit, Entity e, double radius, LivingEntity damager, Callable<Void> func)
		{
			this.hit = hit;
			this.radius = radius;
			this.e = e;
			this.damager = damager;
			this.func = func;
		}

		@Override
		public void run()
		{
			if (this.e != null)
				l = e.getLocation();
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
		ArrayList<LivingEntity> hit;
		double radius;
		Location l;
		int damage;
		LivingEntity damager;
		double knockback;

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
		ArrayList<LivingEntity> hit;
		double radius;
		Location l;
		int damage;
		LivingEntity damager;
		int blockID;

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
		ArrayList<LivingEntity> hit;
		double radius;
		Location l;
		int damage;
		LivingEntity damager;
		FireworkEffect fe;

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
		Location l;
		Entity e;
		Effect effect;
		int id;

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
		Location l;
		int particles;
		float speed = 0.0F;
		float range = 0.0F;

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
			for (Player p: CakeLibrary.getNearbyPlayers(l, 24))
				CakeLibrary.spawnParticle(EnumParticle.FIREWORKS_SPARK, l, range, p, particles, speed);
		}
	}

	public static class ParticleEffect implements Runnable
	{
		Location l;
		int particles;
		float speed = 0.0F;
		float range = 0.0F;
		EnumParticle type;
		int[] data;
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
			for (Player p: CakeLibrary.getNearbyPlayers(l, 24))
				CakeLibrary.spawnParticle(type, l, range, p, particles, speed, data);
		}
	}

	public static class InventoryOpen implements Runnable
	{
		Player player;
		Inventory inv;
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
		Player player;
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
