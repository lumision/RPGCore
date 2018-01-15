package rpgcore.main;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;

import net.minecraft.server.v1_12_R1.EnumParticle;
import rpgcore.classes.ClassInventory;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.entities.mobs.MageZombie;
import rpgcore.entities.mobs.ReinforcedSkeleton;
import rpgcore.entities.mobs.ReinforcedZombie;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.player.RPlayer;
import rpgcore.player.RPlayerManager;
import rpgcore.skillinventory.SkillInventory;
import rpgcore.skills.Heartspan;
import rpgcore.skills.RPGSkill;

public class RPGListener implements Listener
{
	public RPGCore instance;
	public RPlayerManager playerManager;
	public RPGListener(RPGCore instance)
	{
		this.instance = instance;
		playerManager = instance.playerManager;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleRegainHealth(EntityRegainHealthEvent event)
	{
		if (!(event.getEntity() instanceof Player))
			return;
		if (event.getRegainReason().equals(RegainReason.CUSTOM))
			return;
		event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		if (playerManager.getRPlayer(p.getName()) == null)
		{
			RPlayer rp = playerManager.addRPlayer(p.getName());
			rp.updatePlayerREquips();
			p.openInventory(ClassInventory.getClassInventory(rp));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryDrag(InventoryDragEvent event)
	{
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (!CakeAPI.hasColor(name))
			return;
		name = CakeAPI.removeColorCodes(name);
		boolean crystal = false;
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				crystal = true;
				break;
			}
		if (name.equals("Party Info") || name.equals("Class Selection") || name.startsWith("Skillbook: ") || crystal)
		{
			for (int i: event.getRawSlots())
				if (i < event.getView().getTopInventory().getSize())
					event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleEntityDeath(EntityDeathEvent event)
	{
		LivingEntity e = event.getEntity();
		if (e.hasMetadata("RPGCore.Killer"))
		{
			List<MetadataValue> mlist = e.getMetadata("RPGCore.Killer");
			String name = mlist.get(mlist.size() - 1).asString();
			RPlayer rp = instance.playerManager.getRPlayer(name);
			if (rp == null)
				return;
			rp.addXP((int) e.getMaxHealth());
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryClose(InventoryCloseEvent event)
	{
		Player p = (Player) event.getPlayer();
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (!CakeAPI.hasColor(name))
			return;
		name = CakeAPI.removeColorCodes(name);
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				for (int i = 0; i < 2; i++)
				{
					int slot = i == 0 ? 0 : 4;
					ItemStack give = inv.getItem(slot);
					if (CakeAPI.isItemStackNull(give))
						continue;
					CakeAPI.givePlayerItem(p, give);
					inv.setItem(slot, new ItemStack(Material.AIR));
				}
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryClick(InventoryClickEvent event)
	{
		Player p = (Player) event.getWhoClicked();
		RPlayer rp = instance.playerManager.getRPlayer(p.getName());
		if (rp == null)
			return;
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (!CakeAPI.hasColor(name))
			return;
		name = CakeAPI.removeColorCodes(name);
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.CrystalInvCheck(p, inv, type), 1);
				if (event.getRawSlot() < 4 || (event.getRawSlot() > 4 && event.getRawSlot() <= 8))
					event.setCancelled(true);
				return;
			}
		boolean bottom = event.getRawSlot() >= event.getView().getTopInventory().getSize();
		if (bottom)
		{
			if (event.isShiftClick())
				event.setCancelled(true);
			return;
		}
		RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 20), 0);
		if (name.equals("Class Selection"))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeAPI.isItemStackNull(is))
				return;
			String itemName = CakeAPI.removeColorCodes(CakeAPI.getItemName(is));
			ClassType change = rp.currentClass;
			for (ClassType ct: ClassType.values())
				if (itemName.toLowerCase().contains(ct.toString().toLowerCase()))
					change = ct;
			rp.currentClass = change;
			instance.playerManager.writePlayerData(rp);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2F, 1.0F);
			p.openInventory(SkillInventory.getSkillInventory(rp, 0));
			return;
		}
		if (name.equals("Party Info"))
			event.setCancelled(true);
		if (name.startsWith("Skillbook: "))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeAPI.isItemStackNull(is))
				return;
			String itemName = CakeAPI.removeColorCodes(CakeAPI.getItemName(is));
			if (itemName.startsWith("You have sufficient permissions to use these:"))
			{
				rp.getCurrentClass().skillPoints++;
				SkillInventory.updateSkillInventory(event.getInventory(), rp);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
				return;
			}
			if (itemName.equals("Spend skill points"))
			{
				inv.setItem(inv.getSize() - 9, SkillInventory.modeLevel.clone());
				return;
			}
			if (itemName.equals("Reclaim skill points"))
			{
				inv.setItem(inv.getSize() - 9, SkillInventory.modeDelevel.clone());
				return;
			}
			if (itemName.equals("Return to skill selection"))
			{
				inv.setItem(inv.getSize() - 9, SkillInventory.modeSelect.clone());
				return;
			}
			if (itemName.startsWith("Mode: "))
				return;
			if (itemName.startsWith("Skill Points: ") || itemName.startsWith("Class: "))
				return;
			ItemStack mode = inv.getItem(inv.getSize() - 9);
			if (mode == null)
				return;
			if (mode.getDurability() == (short) 5) //Spend skill points
			{
				int level = rp.getSkillLevel(itemName);
				if (level >= 10)
				{
					RPGCore.msg(p, "Level 10 is the maximum level for any skill.");
					return;
				}
				if (rp.getCurrentClass().skillPoints < 1)
				{
					RPGCore.msg(p, "You don't have any skill points!");
					return;
				}
				rp.offsetSkillLevel(itemName, 1);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
				rp.getCurrentClass().skillPoints--;
				SkillInventory.updateSkillInventory(event.getInventory(), rp);
				SkillInventory.updatePlayerInventorySkills(rp);
				instance.playerManager.writePlayerData(rp);
				return;
			}
			if (mode.getDurability() == (short) 14) //Reclaim skill points
			{
				int level = rp.getSkillLevel(itemName);
				if (level < 1)
				{
					RPGCore.msg(p, "That skill is not unlocked.");
					return;
				}
				rp.offsetSkillLevel(itemName, -1);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
				rp.getCurrentClass().skillPoints++;
				SkillInventory.updateSkillInventory(event.getInventory(), rp);
				SkillInventory.updatePlayerInventorySkills(rp);
				instance.playerManager.writePlayerData(rp);
				return;
			}
			if (is.getTypeId() == 383)
				return;
			for (String line: CakeAPI.getItemLore(is))
				if (CakeAPI.removeColorCodes(line).startsWith("Passive Skill:"))
				{
					RPGCore.msg(p, "Passive Skills do not require activation.");
					return;
				}
			ItemStack add = is.clone();
			add = SkillInventory.changeForInventory(add, p.getName());
			boolean r = false;
			for (int i = 0; i < p.getInventory().getSize(); i++)
			{
				ItemStack item = p.getInventory().getItem(i);
				if (CakeAPI.isItemStackNull(item))
					continue;
				if (CakeAPI.getItemName(item).equals(CakeAPI.getItemName(event.getCurrentItem())))
				{
					p.getInventory().setItem(i, add.clone());
					r = true;
				}
			}
			if (r)
			{
				RPGCore.msg(p, "Skill(s) in your inventory have been updated.");
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
				return;
			}
			p.getInventory().addItem(add);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 169), 0);
			RPGCore.msg(p, "The skill has been added into your inventory.");
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleCreatureSpawn(EntitySpawnEvent event)
	{
		File file = new File("nospawn.txt");
		if (file.exists())
			event.setCancelled(true);

		Random rand = RPGCore.rand;
		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity))
			return;
		LivingEntity e = (LivingEntity) entity;

		double spawnDistance = e.getWorld().getSpawnLocation().distance(e.getLocation());

		if (e instanceof Zombie)
		{
			if (spawnDistance > 250.0D && rand.nextInt(10) == 0)
			{
				new MageZombie((Monster) e);
				return;
			}
			if (spawnDistance > 100.0D && rand.nextInt(5) == 0)
			{
				new ReinforcedZombie((Monster) e);
				return;
			}
		}
		if (e instanceof Skeleton)
		{
			if (spawnDistance > 100.0D && rand.nextInt(5) == 0)
			{
				new ReinforcedSkeleton((Monster) e);
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleBlockBreak(BlockBreakEvent event)
	{
		Player p = event.getPlayer();
		RPlayer rp = instance.playerManager.getRPlayer(p.getName());
		if (rp == null)
			return;
		ItemStack is = p.getItemInHand();
		if (CakeAPI.isItemStackNull(is))
			return;
		String name = CakeAPI.getItemName(is);
		if (!name.contains("§"))
			return;
		name = CakeAPI.removeColorCodes(name);
		for (String skill: rp.skills)
			if (skill.equalsIgnoreCase(name))
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleEntityDamage(EntityDamageByEntityEvent event)
	{
		if (event.getCause().equals(DamageCause.ENTITY_EXPLOSION))
			event.setCancelled(true);
		if (event.getDamager() instanceof Player)
		{
			if (handleSkillCast((Player) event.getDamager()))
				event.setCancelled(true);
			return;
		}
		if (event.getEntity() instanceof Player) //basically remove armor effects and breaking of armor
		{
			event.setDamage(DamageModifier.ARMOR, 0);
			event.setDamage(DamageModifier.ABSORPTION, 0);
			Player p = (Player) event.getEntity();
			RPlayer rp = instance.playerManager.getRPlayer(p.getName());
			if (rp == null)
				return;
			if (rp.currentClass.getTier1Class().equals(ClassType.THIEF))
			{
				int time = rp.getSkillLevel("Evade");
				if (p.isSneaking() && rp.sneakTicks <= time)
				{
					event.setCancelled(true);
					RPGCore.msgNoTag(p, "&8--- DAMAGE EVADED ---");
					CakeAPI.spawnParticle(EnumParticle.SMOKE_LARGE, p.getLocation().add(0, 1, 0), 0.5F, p, 8, 0);
				}
			}
			if (rp.currentClass.getTier1Class().equals(ClassType.WARRIOR))
			{
				event.setDamage(event.getDamage() - (event.getDamage() * (rp.getSkillLevel("iron body") / 20.0D)));
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		RPlayer rp = instance.playerManager.getRPlayer(player.getName());
		if (rp == null)
			return;
		ItemStack is = player.getItemInHand();
		if (CakeAPI.isItemStackNull(is))
			return;
		String name = CakeAPI.getItemName(is);
		if (!CakeAPI.hasColor(name))
			return;
		if (isSkill(name, rp))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerEat(PlayerItemConsumeEvent event)
	{
		Player player = event.getPlayer();
		RPlayer rp = instance.playerManager.getRPlayer(player.getName());
		if (rp == null)
			return;
		ItemStack is = player.getItemInHand();
		if (CakeAPI.isItemStackNull(is))
			return;
		String name = CakeAPI.getItemName(is);
		if (!CakeAPI.hasColor(name))
			return;
		if (isSkill(name, rp))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK
				&& event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		//CRYSTALS
		Player p = event.getPlayer();
		ItemStack is = p.getItemInHand();
		if (!CakeAPI.isItemStackNull(is))
		{
			String name = CakeAPI.getItemName(is);
			if (CakeAPI.hasColor(name))
			{
				name = CakeAPI.removeColorCodes(name);
				for (BonusStatCrystal crystal: BonusStatCrystal.values())
				{
					if (name.equals(crystal.getItemName()))
					{
						p.openInventory(crystal.getCrystalInventory(is.getAmount()));
						p.setItemInHand(new ItemStack(Material.AIR));
						break;
					}
				}
			}
		}

		RPlayer rp = playerManager.getRPlayer(p.getName());
		if (rp.heartspanTicks > 0 && rp.castDelay <= 0)
		{
			Heartspan.strike(rp);
			return;
		}
		handleSkillCast(event.getPlayer());
	}

	public boolean handleSkillCast(Player p)
	{
		ItemStack is = p.getItemInHand();
		if (CakeAPI.isItemStackNull(is))
			return false;
		RPlayer rp = playerManager.getRPlayer(p.getName());
		if (rp == null)
			return false;
		String name = CakeAPI.getItemName(is);
		if (!name.contains("§"))
			return false;
		name = CakeAPI.removeColorCodes(name);
		if (rp.castDelay > 0)
		{
			if (rp.instantCast.contains(name))
				rp.instantCast.remove(name);
			else
				return false;
		}
		for (RPGSkill skill: RPGSkill.skillList)
			if (skill != null)
				if (skill.skillName != null)
					if (name.contains(skill.skillName))
						skill.insantiate(rp);

		/** - don't use this anymore
		if (name.contains(ArcaneBolt.skillName))
			new ArcaneBolt(rp);
		if (name.contains(ArcaneBarrage.skillName))
			new ArcaneBarrage(rp);
		if (name.contains(HolyBolt.skillName))
			new HolyBolt(rp);
		if (name.contains(PowerPierce.skillName))
			new PowerPierce(rp);
		if (name.contains(Kunai.skillName))
			new Kunai(rp);
		if (name.contains(Dash.skillName))
			new Dash(rp);
		if (name.contains(ShadowStab.skillName))
			new ShadowStab(rp);
		if (name.contains(Heal.skillName))
			new Heal(rp);
		if (name.contains(Enlightenment.skillName))
			new Enlightenment(rp);
		if (name.contains(Propulsion.skillName))
			new Propulsion(rp);
		- don't use this anymore
		 */
		return true;
	} 

	public boolean isSkill(String itemName, RPlayer rp)
	{
		itemName = CakeAPI.removeColorCodes(itemName);
		for (String skill: rp.skills)
			if (skill.equalsIgnoreCase(itemName))
				return true;
		return false;
	}
}
