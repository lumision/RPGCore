package rpgcore.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
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
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.entities.mobs.MageZombie;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.entities.mobs.ReinforcedSkeleton;
import rpgcore.entities.mobs.ReinforcedZombie;
import rpgcore.entities.mobs.WarriorZombie;
import rpgcore.entities.mobs.WeakSlime;
import rpgcore.external.Title;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.RItem;
import rpgcore.main.RPGEvents.EntityDamageHistory;
import rpgcore.npc.ConversationData.ConversationPart;
import rpgcore.npc.ConversationData.ConversationPartType;
import rpgcore.npc.CustomNPC;
import rpgcore.npc.NPCConversation;
import rpgcore.player.RPlayer;
import rpgcore.player.RPlayerManager;
import rpgcore.recipes.RPGRecipe;
import rpgcore.shop.Shop;
import rpgcore.shop.ShopItem;
import rpgcore.shop.ShopManager;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
import rpgcore.skillinventory.SkillInventory;
import rpgcore.skillinventory2.SkillInventory2;
import rpgcore.skills.Heartspan;
import rpgcore.skills.RPGSkill;

public class RPGListener implements Listener
{
	public RPGCore instance;
	public RPlayerManager playerManager;
	public static Random dropsRand = new Random();

	ItemStack calcite, platinum, topaz, sapphire, ruby, etheryte, excaryte, luminyte;
	public RPGListener(RPGCore instance)
	{
		this.instance = instance;
		playerManager = RPGCore.playerManager;

		calcite = RPGCore.getItemFromDatabase("Calcite").createItem();
		platinum = RPGCore.getItemFromDatabase("Platinum").createItem();
		topaz = RPGCore.getItemFromDatabase("Topaz").createItem();
		sapphire = RPGCore.getItemFromDatabase("Sapphire").createItem();
		ruby = RPGCore.getItemFromDatabase("Ruby").createItem();
		etheryte = RPGCore.getItemFromDatabase("Etheryte").createItem();
		excaryte = RPGCore.getItemFromDatabase("Excaryte").createItem();
		luminyte = RPGCore.getItemFromDatabase("Luminyte").createItem();
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePrepareItemCraft(PrepareItemCraftEvent event)
	{
		ItemStack[] matrix = event.getInventory().getMatrix();
		for (RPGRecipe recipe: RPGRecipe.recipes)
			if (recipe.crafted(matrix))
			{
				event.getInventory().setResult(recipe.result.createItem());
				if (recipe.sound != null)
					for (HumanEntity player: event.getViewers())
						if (player instanceof Player)
							((Player) player).playSound(player.getLocation(), recipe.sound, recipe.volume, recipe.pitch);
				return;
			}

		//Prevents crafting vanilla recipes with custom items
		for (int i = 0; i < matrix.length; i++)
		{
			if (!CakeLibrary.isItemStackNull(matrix[i]) && CakeLibrary.hasColor(CakeLibrary.getItemName(matrix[i])))
				event.getInventory().setResult(new ItemStack(Material.AIR));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleItemCraft(CraftItemEvent event)
	{
		ItemStack[] matrix = event.getInventory().getMatrix();
		for (int i = 0; i < matrix.length; i++)
		{
			if (!CakeLibrary.isItemStackNull(matrix[i]) && CakeLibrary.hasColor(CakeLibrary.getItemName(matrix[i])))
				event.setCancelled(true);
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
			if (spawnDistance > 300.0D && rand.nextInt(10) == 0)
			{
				new WarriorZombie((Monster) e);
				return;
			} else if (spawnDistance > 250.0D && rand.nextInt(10) == 0)
			{
				new MageZombie((Monster) e);
				return;
			} else if (spawnDistance > 100.0D && rand.nextInt(5) == 0) 
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
		if (e instanceof Pig || e instanceof Cow || e instanceof Chicken)
		{
			if (spawnDistance < 150.0D && rand.nextInt(5) == 0)
			{
				event.setCancelled(true);
				Slime slime = (Slime) e.getWorld().spawnEntity(e.getLocation(), EntityType.SLIME);
				new WeakSlime(slime);
			}
		}
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
	public void handlePlayerQuit(PlayerQuitEvent event)
	{
		Player p = event.getPlayer();
		for (CustomNPC npc: RPGCore.npcManager.npcs)
			npc.visiblePlayers.remove(p.getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerJoin(PlayerJoinEvent event)
	{
		Player p = event.getPlayer();
		RPlayer rp = playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
		{
			rp = playerManager.addRPlayer(p.getUniqueId());
			//p.openInventory(ClassInventory.getClassInventory(rp));
		}
		rp.updatePlayerREquips();
		RPGEvents.scheduleRunnable(new RPGEvents.InitializePlayerScoreboard(rp), 20);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryDrag(InventoryDragEvent event)
	{
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (!CakeLibrary.hasColor(name))
			return;
		name = CakeLibrary.removeColorCodes(name);
		boolean crystal = false;
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				crystal = true;
				break;
			}
		if (name.equals("Party Info") || name.equals("Class Selection") || name.startsWith("Skillbook: ") || name.startsWith("Learnt Skills") || crystal)
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

		String cn = e.getCustomName();
		if (cn != null)
		{
			RPGMonster ce = RPGMonster.getRPGMob(e.getEntityId());
			if (ce != null)
			{
				ItemStack[] drops = ce.getDrops();
				if (drops != null && drops.length > 0)
				{
					for (ItemStack is: drops)
						e.getWorld().dropItem(e.getLocation(), is).setVelocity(new Vector(0, 0.5F, 0));
				}
			}
		}

		for (EntityDamageHistory history: RPGEvents.EntityDamageHistory.damageHistories)
		{
			if (history.entityID == e.getEntityId())
			{
				EntityDamageHistory.remove.add(history);
				ArrayList<UUID> uuids = new ArrayList<UUID>();
				uuids.addAll(history.damageHistory.keySet());
				for (UUID uuid: uuids)
				{
					int xp = (int) Math.min(history.damageHistory.get(uuid), e.getMaxHealth());
					RPlayer rp = RPGCore.playerManager.getRPlayer(uuid);
					if (rp != null)
						if (rp.getPlayer() != null)
							rp.addXP(xp);
				}
				break;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryClose(InventoryCloseEvent event)
	{
		Player p = (Player) event.getPlayer();
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return;
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (!CakeLibrary.hasColor(name))
			return;
		name = CakeLibrary.removeColorCodes(name);
		if (name.startsWith("Conversation - "))
		{
			NPCConversation remove = null;
			for (NPCConversation c: NPCConversation.conversations)
				if (c.player == rp)
				{
					c.closed = true;
					remove = c;
				}
			if (remove != null)
				NPCConversation.conversations.remove(remove);
		}
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				for (int i = 0; i < 2; i++)
				{
					int slot = i == 0 ? 0 : 4;
					ItemStack give = inv.getItem(slot);
					if (CakeLibrary.isItemStackNull(give))
						continue;
					CakeLibrary.givePlayerItem(p, give);
					inv.setItem(slot, new ItemStack(Material.AIR));
				}
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryClick(InventoryClickEvent event)
	{
		Player p = (Player) event.getWhoClicked();
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return;

		Inventory inv = event.getInventory();
		InventoryHolder holder = inv.getHolder();
		if (holder instanceof Chest)
		{
			Location l = ((Chest) holder).getBlock().getLocation();
			if (RPGCore.previewChestManager.getPreviewChest(l) != null)
			{
				if (p.hasPermission("rpgcore.previewchest"))
					RPGCore.msg(p, "Bypassed preview chest with permissions.");
				else
					event.setCancelled(true);
				return;
			}
		} else if (holder instanceof DoubleChest)
		{
			Location l = ((Chest) ((DoubleChest) holder).getLeftSide()).getLocation();
			if (RPGCore.previewChestManager.getPreviewChest(l) != null)
			{
				if (p.hasPermission("rpgcore.previewchest"))
					RPGCore.msg(p, "Bypassed preview chest with permissions.");
				else
					event.setCancelled(true);
				return;
			}

			l = ((Chest) ((DoubleChest) holder).getRightSide()).getLocation();
			if (RPGCore.previewChestManager.getPreviewChest(l) != null)
			{
				if (p.hasPermission("rpgcore.previewchest"))
					RPGCore.msg(p, "Bypassed preview chest with permissions.");
				else
					event.setCancelled(true);
				return;
			}
		}

		String name = inv.getName();
		if (!CakeLibrary.hasColor(name))
			return;
		name = CakeLibrary.removeColorCodes(name);
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

		for (Shop shop: ShopManager.shopDatabase)
			if (CakeLibrary.removeColorCodes(shop.shopName).equals(name))
			{
				event.setCancelled(true);
				ShopItem si = shop.getShopItem(event.getSlot());
				if (si == null)
					return;
				int cost = p.hasPermission("rpgcore.gold") ? 0 : si.cost;
				if (rp.getGold() < cost)
				{
					RPGCore.msgNoTag(p, "&cYou need more money for that!");
					return;
				}
				if (!CakeLibrary.playerHasVacantSlots(p))
				{
					RPGCore.msgNoTag(p, "&cYou need inventory space to buy items!");
					return;
				}
				p.getInventory().addItem(si.item.createItem());
				rp.addGold(-cost);
				RPGCore.msgNoTag(p, "&6You've bought " + CakeLibrary.getItemName(si.item.createItem()) + "&6 for &e&n" + cost + " Gold&6.");
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 41), 0);
				return;
			}

		if (name.startsWith("Conversation - "))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(is))
				return;
			String itemName = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(is));
			itemName = itemName.substring(1, itemName.length() - 1);
			for (NPCConversation c: NPCConversation.conversations)
				if (c.player == rp && !c.closed)
				{
					if (c.part == null)
						return;
					ConversationPart clicked = null;
					if (c.part.string.startsWith(itemName))
						clicked = c.part;
					else 
					{
						for (ConversationPart cp: c.part.next)
							if (cp.string.startsWith(itemName))
								clicked = cp;
					}
					if (clicked == null)
						return;
					if (c.part.next.size() <= 0)
					{
						p.closeInventory();
						return;
					}
					if (c.part.next.get(0).type == ConversationPartType.PLAYER)
					{
						if (clicked.type == ConversationPartType.NPC)
							return;
						c.part = clicked.next.get(0);
					} else if (clicked == c.part)
						c.part = c.part.next.get(0);
					c.lastClickedSlot = event.getSlot();
					c.updateUI();
				}
		}
		if (name.equals("Class Selection"))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(is))
				return;
			String itemName = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(is));

			ClassType change = rp.currentClass;
			for (ClassType ct: ClassType.values())
				if (itemName.toLowerCase().contains(ct.toString().toLowerCase()))
					change = ct;
			rp.currentClass = change;
			RPGClass.unlockBasicSkills(rp, change);

			RPGCore.playerManager.writeData(rp);
			p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.1F, 1.0F);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
			if (inv.getSize() == 27)
				p.openInventory(SkillInventory.getSkillInventory(rp, 0));
			else
				RPGEvents.scheduleRunnable(new RPGEvents.InventoryClose(p), 1);
			rp.updateScoreboard();
			return;
		}
		if (name.equals("Party Info"))
			event.setCancelled(true);
		if (name.startsWith("Learnt Skills"))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(is))
				return;
			//RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 20), 0);
			String itemName = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(is));
			if (itemName.startsWith("Skillbook Tier: "))
				return;
			else if (itemName.startsWith("<-- "))
			{
				if (rp.skillbookTierSwitchTicks > 0 || rp.lastSkillbookTier <= 1)
					return;
				rp.lastSkillbookTier--;
				inv.setContents(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier).getContents());
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.1F, 0.9F);
				rp.skillbookTierSwitchTicks = 3;
			} else if (itemName.startsWith("Next Tier"))
			{
				if (rp.skillbookTierSwitchTicks > 0 || rp.lastSkillbookTier >= 10)
					return;
				rp.lastSkillbookTier++;
				inv.setContents(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier).getContents());
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.1F, 1.1F);
				rp.skillbookTierSwitchTicks = 3;
			} else
			{
				for (String line: CakeLibrary.getItemLore(is))
					if (CakeLibrary.removeColorCodes(line).startsWith("Passive Skill:"))
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
					if (CakeLibrary.isItemStackNull(item))
						continue;
					if (CakeLibrary.getItemName(item).equals(CakeLibrary.getItemName(event.getCurrentItem())))
					{
						p.getInventory().setItem(i, add.clone());
						r = true;
					}
				}
				if (r)
				{
					RPGCore.msg(p, "Skill(s) in your inventory have been updated.");
					RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
					return;
				}
				p.getInventory().addItem(add);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
				RPGCore.msg(p, "The skill has been added into your inventory.");
			}
			return;
		}
		/*
		if (name.startsWith("Skillbook: "))
		{
			event.setCancelled(true);
			ItemStack is = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(is))
				return;
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 20), 0);
			String itemName = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(is));
			if (itemName.startsWith("You have sufficient permissions to use these:"))
			{
				rp.getCurrentClass().skillPoints += 10;
				//SkillInventory.updateSkillInventory(event.getInventory(), rp);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
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
					RPGCore.msg(p, "Level 10 is the maximum level.");
					return;
				}
				if (rp.getCurrentClass().skillPoints < 1)
				{
					RPGCore.msg(p, "You don't have any skill points!");
					return;
				}
				rp.offsetSkillLevel(itemName, 1);
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
				rp.getCurrentClass().skillPoints--;
				if (rp.getSkillLevel(itemName) == 1)
				{
					ItemStack add = null;
					for (RPGSkill skill: RPGSkill.skillList)
						if (itemName.equals(skill.skillName))
							add = skill.instanceGetSkillItem(rp);
					if (add != null)
					{
						add = SkillInventory.changeForInventory(add, p.getName());
						p.getInventory().addItem(add);
					}
				} else
					SkillInventory.updatePlayerInventorySkills(rp);
				//SkillInventory.updateSkillInventory(event.getInventory(), rp);
				RPGCore.playerManager.writeData(rp);
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
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
				rp.getCurrentClass().skillPoints++;
				//SkillInventory.updateSkillInventory(event.getInventory(), rp);
				SkillInventory.updatePlayerInventorySkills(rp);
				RPGCore.playerManager.writeData(rp);
				return;
			}
			int level = rp.getSkillLevel(itemName);
			if (level < 1)
			{
				RPGCore.msg(p, "That skill is not unlocked.");
				return;
			}
			for (String line: CakeLibrary.getItemLore(is))
				if (CakeLibrary.removeColorCodes(line).startsWith("Passive Skill:"))
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
				if (CakeLibrary.isItemStackNull(item))
					continue;
				if (CakeLibrary.getItemName(item).equals(CakeLibrary.getItemName(event.getCurrentItem())))
				{
					p.getInventory().setItem(i, add.clone());
					r = true;
				}
			}
			if (r)
			{
				RPGCore.msg(p, "Skill(s) in your inventory have been updated.");
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
				return;
			}
			p.getInventory().addItem(add);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
			RPGCore.msg(p, "The skill has been added into your inventory.");
			return;
		}
		 */
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleBlockBreak(BlockBreakEvent event)
	{
		Player p = event.getPlayer();
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return;
		ItemStack is = p.getItemInHand();
		if (!CakeLibrary.isItemStackNull(is))
		{
			String name = CakeLibrary.getItemName(is);
			if (name.contains("§"))
			{
				name = CakeLibrary.removeColorCodes(name);
				for (String skill: rp.skills)
					if (skill.equalsIgnoreCase(name))
					{
						event.setCancelled(true);
						return;
					}
			}
		}

		Block b = event.getBlock();
		if (b.getType().equals(Material.STONE))
		{
			RPGSideClass prospector = rp.getSideClass(SideClassType.PROSPECTOR);

			int xp = 0;
			switch (b.getData())
			{
			case 0:
				xp = 3;
				break;
			case 1:
				xp = 5;
				break;
			case 3:
				xp = 5;
				break;
			case 5:
				xp = 5;
				break;
			}

			if (prospector.lastCheckedLevel >= 0 && RPGCore.rand.nextInt(20) == 0)
			{
				xp += 10;
				b.getWorld().dropItem(b.getLocation(), calcite.clone()).setVelocity(new Vector(0, 0.5F, 0));
			}
			rp.addSideclassXP(SideClassType.PROSPECTOR, xp);
		} else if (b.getType().equals(Material.COBBLESTONE))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 2);
		else if (b.getType().equals(Material.COAL_ORE))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 10);
		else if (b.getType().equals(Material.IRON_ORE))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 15);
		else if (b.getType().equals(Material.GOLD_ORE))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 25);
		else if (b.getType().equals(Material.DIAMOND_ORE))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 100);
		else if (b.getType().equals(Material.OBSIDIAN))
			rp.addSideclassXP(SideClassType.PROSPECTOR, 25);
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
			RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
			if (rp == null)
				return;

			event.setDamage(event.getDamage() - (event.getDamage() / 100D * rp.calculateDamageReduction()));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		RPlayer rp = RPGCore.playerManager.getRPlayer(player.getUniqueId());
		if (rp == null)
			return;
		ItemStack is = player.getItemInHand();
		if (CakeLibrary.isItemStackNull(is))
			return;
		String name = CakeLibrary.getItemName(is);
		if (!CakeLibrary.hasColor(name))
			return;
		if (isSkill(name, rp))
			event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerEat(PlayerItemConsumeEvent event)
	{
		Player player = event.getPlayer();
		RPlayer rp = RPGCore.playerManager.getRPlayer(player.getUniqueId());
		if (rp == null)
			return;
		ItemStack is = player.getItemInHand();
		if (CakeLibrary.isItemStackNull(is))
			return;
		String name = CakeLibrary.getItemName(is);
		if (!CakeLibrary.hasColor(name))
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
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		ItemStack is = p.getItemInHand();
		if (!CakeLibrary.isItemStackNull(is))
		{
			String name = CakeLibrary.getItemName(is);
			if (CakeLibrary.hasColor(name))
			{
				name = CakeLibrary.removeColorCodes(name);
				if (name.startsWith("Skillbook < "))
				{
					String skillName = name.replace("Skillbook < ", "");
					skillName = skillName.substring(0, skillName.length() - 2);

					RPGSkill skill = null;
					for (RPGSkill check: RPGSkill.skillList)
						if (check.skillName.equals(skillName))
							skill = check;
					
					if (skill != null && !rp.skills.contains(skillName) && skill.classType.equals(rp.currentClass))
					{
						rp.skills.add(skillName);
						rp.titleQueue.add(new Title("&6 < " + CakeLibrary.getItemName(skill.getSkillItem()) + "&6 >", "&eSkill Learnt", 20, 60, 20));
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.0f);
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 20).run();
						p.setItemInHand(null);
						RPGCore.playerManager.writeData(rp);
					}
					rp.updateScoreboard();
					event.setCancelled(true);
					return;
				}
				if (name.startsWith("Gold ("))
				{
					int gold = Integer.parseInt(name.substring(6, name.length() - 1).replaceAll(",", "")) * is.getAmount();
					rp.addGold(gold);
					p.setItemInHand(null);
					RPGCore.msg(p, "&e" + gold + " Gold &6has been added to your bank");
					event.setCancelled(true);
					return;
				}
				for (BonusStatCrystal crystal: BonusStatCrystal.values())
				{
					if (name.equals(crystal.getItemName()))
					{
						p.openInventory(crystal.getCrystalInventory(is.getAmount()));
						p.setItemInHand(null);
						event.setCancelled(true);
						break;
					}
				}
			}
		}

		if (rp.heartspanTicks > 0 && rp.castDelay <= 0)
		{
			Heartspan.strike(rp);
			return;
		}
		handleSkillCast(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleTab(TabCompleteEvent event)
	{
		String msg = event.getBuffer();
		if (msg.length() < 1)
			return;

		String s = event.getSender() instanceof Player ? "/" : "";
		ArrayList<String> completions = new ArrayList<String>();

		if (msg.startsWith(s + "gi ") || msg.startsWith(s + "getitem "))
		{
			String[] split = msg.split(" ");
			if (split.length >= 2 && split[1].length() > 0)
			{
				for (RItem ri: RPGCore.itemDatabase)
					if (ri.databaseName.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(ri.databaseName);
			}
		}

		if (msg.startsWith(s + "skillbook ") || msg.startsWith(s + "sb "))
		{
			String[] split = msg.split(" ");
			if (split.length >= 2 && split[1].length() > 0)
			{
				for (RPGSkill skill: RPGSkill.skillList)
				{
					String skillName = skill.skillName.replace(" ", "");
					if (skillName.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(skillName);
				}
			}
		}

		if (msg.startsWith(s + "sound "))
		{
			String[] split = msg.split(" ");
			if (split.length >= 2 && split[1].length() > 0 && split.length < 3)
			{
				for (Sound sound: Sound.values())
				{
					if (sound.name().toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(sound.name());
				}
			}
		}

		if (completions.size() > 0)
			event.setCompletions(completions);
	}

	public boolean handleSkillCast(Player p)
	{
		ItemStack is = p.getItemInHand();
		if (CakeLibrary.isItemStackNull(is))
			return false;
		RPlayer rp = playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return false;
		String name = CakeLibrary.getItemName(is);
		if (!name.contains("§"))
			return false;
		name = CakeLibrary.removeColorCodes(name);
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
					{
						skill.insantiate(rp);
						return true;
					}

		return false;
	} 

	public boolean isSkill(String itemName, RPlayer rp)
	{
		itemName = CakeLibrary.removeColorCodes(itemName);
		for (String skill: rp.skills)
			if (skill.equalsIgnoreCase(itemName))
				return true;
		return false;
	}
}
