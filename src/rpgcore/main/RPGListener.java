package rpgcore.main;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Spider;
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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import es.eltrueno.npc.skin.SkinData;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.TileEntitySkull;
import rpgcore.areas.Arena;
import rpgcore.areas.ArenaInstance;
import rpgcore.classes.ClassInventory;
import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.entities.mobs.RPGMonster;
import rpgcore.entities.mobs.RPGMonsterSpawn;
import rpgcore.item.BonusStat.BonusStatCrystal;
import rpgcore.item.EnhancementInventory;
import rpgcore.item.RItem;
import rpgcore.main.RPGEvents.EntityDamageHistory;
import rpgcore.npc.ConversationData.ConversationPart;
import rpgcore.npc.ConversationData.ConversationPartType;
import rpgcore.npc.CustomNPC;
import rpgcore.npc.NPCConversation;
import rpgcore.npc.NPCManager;
import rpgcore.player.AccessoryInventory;
import rpgcore.player.RPlayer;
import rpgcore.player.RPlayerManager;
import rpgcore.recipes.RPGRecipe;
import rpgcore.shop.GuildShop;
import rpgcore.shop.Shop;
import rpgcore.shop.ShopItem;
import rpgcore.shop.ShopManager;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
import rpgcore.skillinventory2.SkillInventory2;
import rpgcore.skills.Heartspan;
import rpgcore.skills.RPGSkill;

public class RPGListener implements Listener
{
	public RPGCore instance;
	public RPlayerManager playerManager;
	public int allowSpawn = 0;

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

		//Prevents crafting vanilla recipes with custom items
		for (int i = 0; i < matrix.length; i++)
		{
			if (!CakeLibrary.isItemStackNull(matrix[i]) && CakeLibrary.hasColor(CakeLibrary.getItemName(matrix[i])))
				event.getInventory().setResult(new ItemStack(Material.AIR));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerRespawn(PlayerRespawnEvent event)
	{
		if (event.getRespawnLocation().getWorld().getName().equals(RPGCore.areaInstanceWorld))
			event.setRespawnLocation(Bukkit.getWorlds().get(0).getSpawnLocation());
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

		Entity entity = event.getEntity();
		if (!(entity instanceof LivingEntity))
			return;
		if (entity instanceof Creeper)
		{
			event.setCancelled(true);
			return;
		}
		LivingEntity e = (LivingEntity) entity;
		Location l = event.getLocation();

		if (l.getWorld().getName().equals(RPGCore.areaInstanceWorld))
		{
			if (l.getY() < 8)
				event.setCancelled(true);
			return;
		}

		if (CakeLibrary.getNearbyLivingEntities(e.getLocation(), 64).size() > 48 && allowSpawn == 0)
		{
			event.setCancelled(true);
			return;
		}

		double spawnDistanceSquared = e.getWorld().getSpawnLocation().distanceSquared(e.getLocation());

		boolean customSpawn = false;

		if (allowSpawn > 0)
		{
			allowSpawn--;
			customSpawn = true;
		}
		else
		{

			for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
			{
				if (!spawn.isNaturalSpawn())
					continue;
				if (!spawn.monsterType.isInstance(e))
					continue;
				if (spawnDistanceSquared < spawn.minSpawnDistanceSquared || spawnDistanceSquared > spawn.maxSpawnDistanceSquared)
					continue;
				if (RPGMonster.rand.nextInt(spawn.spawnRoll) != 0)
					continue;
				spawn.replaceMonster(e);
				customSpawn = true;
				break;
			}
		}
		if (e instanceof Zombie || e instanceof Spider || e instanceof Skeleton)
			if (!customSpawn && spawnDistanceSquared > 40000)
			{
				double health = Math.sqrt(spawnDistanceSquared) / 10;
				e.setMaxHealth(health);
				e.setHealth(health);
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
		for (CustomNPC npc: NPCManager.npcs)
			npc.visiblePlayers.remove(p.getUniqueId());
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp != null)
			rp.quit();
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
		rp.updateStats();
		rp.setInvButtons();
		RPGEvents.scheduleRunnable(new RPGEvents.InitializePlayerScoreboard(rp), 20);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryDrag(InventoryDragEvent event)
	{
		Inventory inv = event.getInventory();
		String name = inv.getName();
		if (inv instanceof CraftingInventory)
			RPGEvents.scheduleRunnable(new RPGEvents.CheckForRecipe((CraftingInventory) inv), 1);
		if (!CakeLibrary.hasColor(name))
			return;
		for (int i: event.getRawSlots())
			if (i < event.getView().getTopInventory().getSize())
				event.setCancelled(true);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleEntityDeath(EntityDeathEvent event)
	{
		LivingEntity e = event.getEntity();

		String cn = e.getCustomName();
		ArrayList<String> messages = new ArrayList<String>();
		if (cn != null)
		{
			RPGMonster ce = RPGMonster.getRPGMob(e.getEntityId());
			if (ce != null)
			{
				for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
					if (spawn.rpgMonster.isInstance(ce))
					{
						for (RItem drop: spawn.drops)
						{
							int roll = drop.dropRoll;
							if (RPGMonster.rand.nextInt(roll) != 0)
								continue;
							ItemStack item = drop.createItem();
							item.setAmount(1);
							if (roll >= 4)
								mobDropItem(e, item, messages);
							else
								mobDropItem(e, item);
						}
						break;
					}
			}
		}

		for (EntityDamageHistory history: RPGEvents.EntityDamageHistory.damageHistories)
		{
			if (history.entityID == e.getEntityId())
			{
				if (e instanceof Zombie || e instanceof Spider || e instanceof Skeleton)
				{
					if (RPGMonster.rand.nextInt(100) == 0)
						mobDropItem(e, BonusStatCrystal.STAT_ADDER.getItemStack(), messages);
					else if (RPGMonster.rand.nextInt(100) == 0)
						mobDropItem(e, BonusStatCrystal.ALL_LINES_REROLL.getItemStack(), messages);
					else if (RPGMonster.rand.nextInt(150) == 0)
						mobDropItem(e, BonusStatCrystal.TIER_REROLL.getItemStack(), messages);
					else if (RPGMonster.rand.nextInt(300) == 0)
						mobDropItem(e, BonusStatCrystal.LINE_AMOUNT_ADDER.getItemStack(), messages);
				}

				EntityDamageHistory.remove.add(history);
				ArrayList<UUID> uuids = new ArrayList<UUID>();
				uuids.addAll(history.damageHistory.keySet());
				for (UUID uuid: uuids)
				{
					int xp = (int) Math.min(history.damageHistory.get(uuid), e.getMaxHealth());
					RPlayer rp = RPGCore.playerManager.getRPlayer(uuid);
					if (rp != null)
						if (rp.getPlayer() != null)
						{
							rp.addXP((int) (xp * rp.getStats().xpMultiplier));
							for (String msg: messages)
								rp.getPlayer().sendMessage(msg);
						}
				}
				break;
			}
		}
	}

	public void mobDropItem(LivingEntity e, ItemStack item)
	{
		item = CakeLibrary.addLore(item, "§f§e§d" + RPGMonster.rand.nextLong());
		Item itemDrop = e.getWorld().dropItem(e.getLocation(), item);
		itemDrop.setVelocity(new Vector(RPGMonster.rand.nextFloat() - RPGMonster.rand.nextFloat(), 
				5.0F, 
				RPGMonster.rand.nextFloat() - RPGMonster.rand.nextFloat()).normalize().multiply(0.5D));
		RPGEvents.itemDropTrails.add(itemDrop);
	}

	public void mobDropItem(LivingEntity e, ItemStack item, ArrayList<String> messages)
	{
		messages.add(CakeLibrary.recodeColorCodes("&c * &4\"&f" + (e.getCustomName() == null ? e.getName() : e.getCustomName()) + "&4\"&c dropped &4\"&f" + CakeLibrary.getItemName(item) + "&4\""));
		mobDropItem(e, item);
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
		} else if (name.equals("Class Selection"))
		{
			if (rp.currentClass.equals(ClassType.ALL))
				RPGEvents.scheduleRunnable(new RPGEvents.InventoryOpen(p, ClassInventory.getClassInventory1(rp)), 1);
		}
		else if (name.equals("Equipment Enhancement"))
		{
			for (int i = 10; i < 17; i += 3)
			{
				ItemStack item = inv.getItem(i);
				if (EnhancementInventory.isItemPartOfLayout(item))
					continue;
				p.getInventory().addItem(item);
				inv.setItem(i, new ItemStack(Material.AIR));
			}
			return;
		} else if (name.equals("Receptionist"))
		{
			for (int i = 0; i < inv.getSize() - 2; i++)
			{
				ItemStack item = inv.getItem(i);
				if (CakeLibrary.isItemStackNull(item))
					break;
				p.getInventory().addItem(GuildShop.convertToUnpriced(item.clone()));
				inv.setItem(i, new ItemStack(Material.AIR));
			}
			return;
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
				return;
			}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleItemPickup(PlayerPickupItemEvent event)
	{
		if (CakeLibrary.hasColor(event.getPlayer().getOpenInventory().getTitle()))
		{
			event.setCancelled(true);
			return;
		}
		ItemStack item = event.getItem().getItemStack();
		ItemMeta im = item.getItemMeta();
		int remove = -1;
		List<String> lore = im.getLore();
		if (im.getLore() != null)
		{
			for (int i = 0; i < lore.size(); i++)
				if (lore.get(i).startsWith("§f§e§d"))
					remove = i;
		}
		if (remove != -1)
		{
			lore.remove(remove);
			im.setLore(lore);
			item.setItemMeta(im);
			event.getItem().setItemStack(item);
		}
		if (RPGEvents.itemDropTrails.contains(event.getItem()))
			RPGEvents.itemDropTrails.remove(event.getItem());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleAnvilPrepare(PrepareAnvilEvent event)
	{
		AnvilInventory inv = event.getInventory();
		ItemStack first = inv.getItem(0);
		if (!CakeLibrary.isItemStackNull(first) && !CakeLibrary.isItemStackNull(event.getResult()))
			if (CakeLibrary.hasColor(CakeLibrary.getItemName(first)))
				event.setResult(CakeLibrary.renameItem(event.getResult(), CakeLibrary.getItemName(first)));
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleInventoryClick(InventoryClickEvent event)
	{
		Player p = (Player) event.getWhoClicked();
		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		if (rp == null)
			return;

		if (rp.uiClickDelay > 0)
		{
			event.setCancelled(true);
			return;
		}

		Inventory inv = event.getInventory();

		if (RPGCore.inventoryButtons)
			if (event.getView().getTopInventory().getSize() == 5 && inv instanceof CraftingInventory) //Player inventory.
			{
				if (event.getRawSlot() == 17) //Accessories
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
					p.openInventory(rp.accessoryInventory.getInventory());
					return;
				} else if (event.getRawSlot() == 26) //Buffs / Stats
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
					rp.buffInventory.updateInventory();
					p.openInventory(rp.buffInventory.getInventory());
					return;
				} else if (event.getRawSlot() == 35) //Skills
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
					p.openInventory(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier));
					return;
				}
			}

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

		boolean bottom = event.getRawSlot() >= event.getView().getTopInventory().getSize();
		int invSlot = event.getRawSlot() - event.getView().getTopInventory().getSize();
		String name = inv.getName();
		boolean hasColor = CakeLibrary.hasColor(name);
		name = CakeLibrary.removeColorCodes(name);
		if (RPGCore.inventoryButtons && 
				(!(inv instanceof CraftingInventory) || 
						((inv instanceof CraftingInventory) && event.getView().getTopInventory().getSize() != 5)))
			if (invSlot == 8 || invSlot == 17 || invSlot == 26) //Accessories
			{
				event.setCancelled(true);
				if ((hasColor 
						&& !name.equals("Equipment Enhancement") 
						&& !name.equals("Class Selection") 
						&& !name.equals("Receptionist") 
						&& !name.startsWith("Conversation - ")) 
						|| !hasColor)
				{
					int check = invSlot + 9;
					if (check == 17) //Accessories
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
						p.openInventory(rp.accessoryInventory.getInventory());
						return;
					} else if (check == 26) //Buffs / Stats
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
						rp.buffInventory.updateInventory();
						p.openInventory(rp.buffInventory.getInventory());
						return;
					} else if (check == 35) //Skills
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.0F);
						p.openInventory(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier));
						return;
					}
					return;
				}
				RPGCore.msg(p, "Close this window first");
			}

		if (inv instanceof CraftingInventory)
		{
			CraftingInventory craftingInv = (CraftingInventory) inv;

			if (event.getRawSlot() == 0) //Result slot
			{
				ItemStack result = craftingInv.getItem(0);
				if (CakeLibrary.isItemStackNull(result))
					return;

				RItem ri = new RItem(result);
				RPGRecipe rpgRecipe = null;

				for (RPGRecipe recipe: RPGRecipe.recipes)
					if (recipe.result.compare(ri))
						rpgRecipe = recipe;

				if (rpgRecipe == null)
					return;
				event.setCancelled(true);

				if (event.isShiftClick())
				{
					boolean left = true;

					while (left)
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						p.getInventory().addItem(rpgRecipe.result.createItem());

						ItemStack[] matrix = craftingInv.getMatrix();
						for (int i = 0; i < matrix.length; i++)
						{
							ItemStack item = matrix[i];
							if (CakeLibrary.isItemStackNull(item))
								continue;
							if (item.getAmount() == 1)
							{
								matrix[i] = new ItemStack(Material.AIR);
								continue;
							}
							item.setAmount(item.getAmount() - 1);
							matrix[i] = item;
						}
						craftingInv.setMatrix(matrix);

						left = rpgRecipe.crafted(matrix);
					}
					craftingInv.setItem(0, new ItemStack(Material.AIR));
					p.updateInventory();
				}
				else if (CakeLibrary.isItemStackNull(event.getCursor()))
				{
					event.setCursor(result);

					ItemStack[] matrix = craftingInv.getMatrix();
					for (int i = 0; i < matrix.length; i++)
					{
						ItemStack item = matrix[i];
						if (CakeLibrary.isItemStackNull(item))
							continue;
						if (item.getAmount() == 1)
						{
							matrix[i] = new ItemStack(Material.AIR);
							continue;
						}
						item.setAmount(item.getAmount() - 1);
						matrix[i] = item;
					}
					craftingInv.setMatrix(matrix);

					if (rpgRecipe.crafted(matrix))
						craftingInv.setItem(0, rpgRecipe.result.createItem());

					p.updateInventory();
				} else if (event.getCursor().getAmount() < 64)
				{
					ItemStack cursor = event.getCursor();
					cursor.setAmount(cursor.getAmount() + 1);
					event.setCursor(cursor);

					ItemStack[] matrix = craftingInv.getMatrix();
					for (int i = 0; i < matrix.length; i++)
					{
						ItemStack item = matrix[i];
						if (CakeLibrary.isItemStackNull(item))
							continue;
						if (item.getAmount() == 1)
						{
							matrix[i] = new ItemStack(Material.AIR);
							continue;
						}
						item.setAmount(item.getAmount() - 1);
						matrix[i] = item;
					}
					craftingInv.setMatrix(matrix);

					if (rpgRecipe.crafted(matrix))
						craftingInv.setItem(0, rpgRecipe.result.createItem());

					p.updateInventory();
				}
			}

			RPGEvents.scheduleRunnable(new RPGEvents.CheckForRecipe(craftingInv), 1);

			return;
		}


		if (!hasColor)
			return;
		for (BonusStatCrystal type: BonusStatCrystal.values())
			if (name.equals(type.getItemName()))
			{
				RPGEvents.scheduleRunnable(new RPGEvents.CrystalInvCheck(p, inv, type), 1);
				if (event.getRawSlot() < 4 || (event.getRawSlot() > 4 && event.getRawSlot() <= 8))
					event.setCancelled(true);
				return;
			}
		if (bottom)
		{
			if (name.startsWith("Drops - "))
			{
				event.setCancelled(true);
				ItemStack current = event.getCurrentItem().clone();
				if (CakeLibrary.isItemStackNull(current))
					return;
				RPGMonsterSpawn spawn = RPGMonsterSpawn.getRPGMonsterSpawn(name.split(" - ")[1]);
				if (spawn == null)
					return;

				if (spawn.drops.size() >= inv.getSize() - 1)
					return;

				if (event.isShiftClick())
				{
					RItem ri = new RItem(current);
					ri.dropRoll = current.getAmount();
					spawn.drops.add(ri);
					for (int i = 0; i < inv.getSize(); i++)
					{
						ItemStack item = inv.getItem(i);
						if (CakeLibrary.isItemStackNull(item))
						{
							inv.setItem(i, current.clone());
							break;
						}
					}
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					spawn.saveDrops();
					return;
				}

				current.setAmount(1);
				RItem ri = new RItem(current);
				ri.dropRoll = 1;

				int riCheck = -1;
				for (int i = 0; i < spawn.drops.size(); i++)
					if (spawn.drops.get(i).compare(ri))
						riCheck = i;

				if (riCheck != -1)
				{
					int dropRoll = 0;
					for (int i = 0; i < inv.getSize(); i++)
					{
						ItemStack check = inv.getItem(i);
						if (CakeLibrary.getItemName(check).equals(CakeLibrary.getItemName(current)))
						{
							check.setAmount(dropRoll = check.getAmount() + 1);
							inv.setItem(i, check);
							break;
						}
					}
					ri.dropRoll = dropRoll;
					spawn.drops.set(riCheck, ri);
				}
				else
				{
					spawn.drops.add(ri);
					inv.addItem(current.clone());
				}
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
				spawn.saveDrops();
				return;
			}
			else if (name.equals("Receptionist"))
			{
				event.setCancelled(true);
				ItemStack current = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(current))
					return;

				int amount = current.getAmount();

				if (event.isRightClick())
				{
					if (!GuildShop.canBeSold(current))
					{
						RPGCore.msg(p, "This item cannot be sold.");
						return;
					}
					current.setAmount(1);

					int slot = 0;
					boolean exists = false;
					ItemStack item = null;
					int itemAmount = 0;
					for (; slot < inv.getSize(); slot++)
					{
						item = inv.getItem(slot);
						if (CakeLibrary.isItemStackNull(item))
							continue;
						item = GuildShop.convertToUnpriced(item.clone());
						itemAmount = item.getAmount();
						if (itemAmount >= 64)
							continue;
						item.setAmount(1);
						if (item.equals(current))
						{
							exists = true;
							break;
						}
					}

					if (exists)
					{
						item.setAmount(itemAmount + 1);
						inv.setItem(slot, GuildShop.convertToPriced(item));
					} else
					{
						if (CakeLibrary.getInventoryVacantSlots(inv) == 0)
						{
							current.setAmount(amount);
							return;
						}
						inv.addItem(GuildShop.convertToPriced(current.clone()));
					}

					current.setAmount(amount - 1);
					event.setCurrentItem(current);
					GuildShop.updateTotalPrice(inv);
					p.updateInventory();
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					return;
				} else if (event.isLeftClick())
				{
					if (!GuildShop.canBeSold(current))
					{
						RPGCore.msg(p, "This item cannot be sold.");
						return;
					}
					if (CakeLibrary.getInventoryVacantSlots(inv) == 0)
						return;
					inv.addItem(GuildShop.convertToPriced(current));
					event.setCurrentItem(new ItemStack(Material.AIR));
					GuildShop.updateTotalPrice(inv);
					p.updateInventory();
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					return;
				} else
					return;
			}
			else if (name.equals("Accessories") && event.isShiftClick())
			{
				event.setCancelled(true);
				ItemStack current = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(current))
					return;
				RItem ri = new RItem(current);
				if (!ri.accessory)
				{
					RPGCore.msg(p, "That item is not an accessory.");
					return;
				}
				boolean duplicate = false;
				for (RItem check: rp.accessoryInventory.slots)
					if (check != null && CakeLibrary.getItemName(check.itemVanilla).equals(CakeLibrary.getItemName(current)))
						duplicate = true;
				if (duplicate)
				{
					RPGCore.msg(p, "You can only equip one accessory of its kind.");
					return;
				}
				for (int slot = 10; slot < 17; slot += 3)
				{
					ItemStack slot1 = inv.getItem(slot);
					int riSlot = (slot - 10) / 3;
					if (CakeLibrary.getItemName(slot1).equals(CakeLibrary.getItemName(AccessoryInventory.slotItem)))
					{
						int amount = current.getAmount();
						if (amount > 1)
						{
							current.setAmount(current.getAmount() - 1);
							event.setCurrentItem(current.clone());
							current.setAmount(1);
							inv.setItem(slot, current);
							rp.accessoryInventory.slots[riSlot] = ri;
						} else
						{
							event.setCurrentItem(new ItemStack(Material.AIR));
							inv.setItem(slot, current);
							rp.accessoryInventory.slots[riSlot] = ri;
						}
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
						rp.updateStats = true;
						RPGCore.playerManager.writeData(rp);
						return;
					}
				}
			}
			else if (name.equals("Equipment Enhancement") && event.isShiftClick())
			{
				event.setCancelled(true);
				ItemStack current = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(current))
					return;
				for (int slot = 10; slot < 17; slot += 6)
				{
					ItemStack slot1 = inv.getItem(slot);
					if (CakeLibrary.getItemName(slot1).equals(CakeLibrary.getItemName(EnhancementInventory.slotItem)))
					{
						int amount = current.getAmount();
						if (amount > 1)
						{
							current.setAmount(current.getAmount() - 1);
							event.setCurrentItem(current.clone());
							current.setAmount(1);
							inv.setItem(slot, current);
						} else
						{
							event.setCurrentItem(new ItemStack(Material.AIR));
							inv.setItem(slot, current);
						}
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
						EnhancementInventory.updateMiddleItem(inv);
						return;
					}
				}
				return;
			}
			if ((event.isShiftClick() || event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR))
					&& !name.equals("Trash Can"))
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
		else if (name.startsWith("Equipment - "))
		{
			event.setCancelled(true);
			if (!p.hasPermission("rpgcore.eq.steal"))
				return;
			ItemStack inSlot = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(inSlot))
				return;
			p.getInventory().addItem(inSlot.clone());
			p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
			return;
		}
		else if (name.startsWith("Item Prices - Page "))
		{
			event.setCancelled(true);
			ItemStack inSlot = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(inSlot))
				return;
			p.getInventory().addItem(GuildShop.convertToUnpriced(inSlot.clone()));
			p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
			return;
		}
		else if (name.startsWith("Receptionist"))
		{
			event.setCancelled(true);
			if (event.getSlot() == inv.getSize() - 1)
			{
				int gold = GuildShop.calculateTotalInventoryPrice(inv);
				if (gold == 0)
					return;
				for (int i = 0; i < inv.getSize() - 2; i++)
					inv.setItem(i, new ItemStack(Material.AIR));
				rp.addGold(gold);
				RPGCore.msg(p, "&e" + gold + " Gold &6has been added to your account");
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.2F, 1.0F);
				new RPGEvents.PlayEffect(Effect.STEP_SOUND, p, 89).run();
				inv.setItem(inv.getSize() - 1, GuildShop.slotSell.clone());
				return;
			}
			if (event.getSlot() == inv.getSize() - 2)
				return;
			ItemStack current = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(current))
				return;

			if (event.isRightClick())
			{
				int amount = current.getAmount();
				current.setAmount(1);
				p.getInventory().addItem(GuildShop.convertToUnpriced(current.clone()));
				if (amount == 1)
				{
					event.setCurrentItem(new ItemStack(Material.AIR));
					if (event.getSlot() < inv.getSize() - 1)
						for (int i = event.getSlot() + 1; i < inv.getSize() - 2; i++)
						{
							if (i == inv.getSize() - 3)
							{
								inv.setItem(i, new ItemStack(Material.AIR));
								break;
							}
							ItemStack move = inv.getItem(i);
							if (CakeLibrary.isItemStackNull(move))
							{
								inv.setItem(i - 1, new ItemStack(Material.AIR));
								break;
							}
							inv.setItem(i - 1, move.clone());
						}
				} else
				{
					current.setAmount(amount - 1);
					event.setCurrentItem(GuildShop.convertToPriced(GuildShop.convertToUnpriced(current)));
				}
				GuildShop.updateTotalPrice(inv);
				p.updateInventory();
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
			} else if (event.isLeftClick())
			{
				p.getInventory().addItem(GuildShop.convertToUnpriced(current));
				event.setCurrentItem(new ItemStack(Material.AIR));

				if (event.getSlot() < inv.getSize() - 1)
					for (int i = event.getSlot() + 1; i < inv.getSize() - 2; i++)
					{
						if (i == inv.getSize() - 3)
						{
							inv.setItem(i, new ItemStack(Material.AIR));
							break;
						}
						ItemStack move = inv.getItem(i);
						if (CakeLibrary.isItemStackNull(move))
						{
							inv.setItem(i - 1, new ItemStack(Material.AIR));
							break;
						}
						inv.setItem(i - 1, move.clone());
					}
				GuildShop.updateTotalPrice(inv);
				p.updateInventory();
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
			} else
				return;
			return;
		}
		else if (name.startsWith("Drops - "))
		{
			event.setCancelled(true);
			ItemStack current = event.getCurrentItem();
			if (CakeLibrary.isItemStackNull(current))
				return;
			RPGMonsterSpawn spawn = RPGMonsterSpawn.getRPGMonsterSpawn(name.split(" - ")[1]);
			if (spawn == null)
				return;

			RItem ri = new RItem(current);

			int riCheck = -1;
			for (int i = 0; i < spawn.drops.size(); i++)
				if (spawn.drops.get(i).compare(ri))
					riCheck = i;

			if (riCheck == -1)
				return;

			if (event.isShiftClick())
			{
				p.getInventory().addItem(spawn.drops.get(riCheck).createItem());
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
				return;
			}

			int amt = spawn.drops.get(riCheck).dropRoll - 1;
			if (amt <= 0)
				spawn.drops.remove(riCheck);
			else
			{
				ri.dropRoll = amt;
				spawn.drops.set(riCheck, ri);
			}

			int stack = current.getAmount() - 1;
			if (stack <= 0)
				event.setCurrentItem(new ItemStack(Material.AIR));
			else
			{
				current.setAmount(stack);
				event.setCurrentItem(current);
			}
			p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
			spawn.saveDrops();
			return;
		}
		else if (name.equals("Accessories"))
		{
			event.setCancelled(true);
			if (event.getSlot() == 10 || event.getSlot() == 13 || event.getSlot() == 16)
			{
				int riSlot = (event.getSlot() - 10) / 3;
				ItemStack inSlot = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(inSlot))
					return;
				if (CakeLibrary.getItemName(inSlot).equals(CakeLibrary.getItemName(AccessoryInventory.slotItem)))
				{
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
						return;
					RItem ri = new RItem(cursor);
					if (!ri.accessory)
					{
						RPGCore.msg(p, "That item is not an accessory.");
						return;
					}
					boolean duplicate = false;
					for (RItem check: rp.accessoryInventory.slots)
						if (check != null && CakeLibrary.getItemName(check.itemVanilla).equals(CakeLibrary.getItemName(cursor)))
							duplicate = true;
					if (duplicate)
					{
						RPGCore.msg(p, "You can only equip one accessory of its kind.");
						return;
					}
					int amount = cursor.getAmount();
					if (cursor.getAmount() > 1)
					{
						cursor.setAmount(1);
						event.setCurrentItem(cursor.clone());
						cursor.setAmount(amount - 1);
						event.setCursor(cursor);
						rp.accessoryInventory.slots[riSlot] = ri;
					} else
					{
						event.setCurrentItem(cursor);
						event.setCursor(new ItemStack(Material.AIR));
						rp.accessoryInventory.slots[riSlot] = ri;
					}
					rp.updateStats = true;
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					RPGCore.playerManager.writeData(rp);
				} else
				{
					if (event.isShiftClick())
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						event.setCancelled(false);
						RPGEvents.scheduleRunnable(new RPGEvents.SetInventoryItem(inv, event.getSlot(), AccessoryInventory.slotItem.clone()), 1);
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
						rp.accessoryInventory.slots[riSlot] = null;
						rp.updateStats = true;
						return;
					}
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
					{
						event.setCursor(inSlot);
						event.setCurrentItem(AccessoryInventory.slotItem.clone());
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
						rp.accessoryInventory.slots[riSlot] = null;
					} else
					{
						if (cursor.getAmount() > 1)
						{
							RPGCore.msg(p, "Retrive the item first, or replace with a stack of 1");
							return;
						}
						RItem ri = new RItem(cursor);
						if (!ri.accessory)
						{
							RPGCore.msg(p, "That item is not an accessory.");
							return;
						}
						boolean duplicate = false;
						for (RItem check: rp.accessoryInventory.slots)
							if (check != null && CakeLibrary.getItemName(check.itemVanilla).equals(CakeLibrary.getItemName(cursor)))
								duplicate = true;
						if (duplicate)
						{
							RPGCore.msg(p, "You can only equip one accessory of its kind.");
							return;
						}
						event.setCursor(inSlot);
						event.setCurrentItem(cursor);
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
						rp.accessoryInventory.slots[riSlot] = new RItem(cursor);
					}
					rp.updateStats = true;
					RPGCore.playerManager.writeData(rp);
				}
				return;
			}
			return;
		}
		else if (name.equals("Equipment Enhancement"))
		{
			event.setCancelled(true);

			//Cancel if enhancement is in progress
			ItemStack check = inv.getItem(13);
			if (!CakeLibrary.isItemStackNull(check))
				if (CakeLibrary.getItemName(check).startsWith(CakeLibrary.getItemName(EnhancementInventory.getSlotEnhance(0))))
					return;

			if (event.getSlot() == 10 || event.getSlot() == 16)
			{
				ItemStack inSlot = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(inSlot))
					return;
				if (CakeLibrary.getItemName(inSlot).equals(CakeLibrary.getItemName(EnhancementInventory.slotItem)))
				{
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
						return;
					int amount = cursor.getAmount();
					if (cursor.getAmount() > 1)
					{
						cursor.setAmount(1);
						event.setCurrentItem(cursor.clone());
						cursor.setAmount(amount - 1);
						event.setCursor(cursor);
					} else
					{
						event.setCurrentItem(cursor);
						event.setCursor(new ItemStack(Material.AIR));
					}

					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					EnhancementInventory.updateMiddleItem(inv);
				} else
				{
					if (event.isShiftClick())
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						event.setCancelled(false);
						RPGEvents.scheduleRunnable(new RPGEvents.SetInventoryItem(inv, event.getSlot(), EnhancementInventory.slotItem.clone()), 1);
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
						EnhancementInventory.updateMiddleItem(inv);
						return;
					}
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
					{
						event.setCursor(inSlot);
						event.setCurrentItem(EnhancementInventory.slotItem.clone());
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
					} else
					{
						if (cursor.getAmount() > 1)
						{
							RPGCore.msg(p, "Retrive the item first, or replace with a stack of 1");
							return;
						}
						event.setCursor(inSlot);
						event.setCurrentItem(cursor);
						p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 1.1F);
					}
					EnhancementInventory.updateMiddleItem(inv);
				}
				return;
			}
			if (event.getSlot() == 13)
			{
				ItemStack inSlot = event.getCurrentItem();
				if (CakeLibrary.isItemStackNull(inSlot))
					return;
				if (!EnhancementInventory.isItemPartOfLayout(inSlot))
				{
					if (!CakeLibrary.isItemStackNull(event.getCursor()) && !event.isShiftClick())
					{
						RPGCore.msg(p, "Please retrive the item with an empty cursor");
						return;
					}
					if (event.isShiftClick())
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						event.setCancelled(false);
					}
					else
					{
						event.setCursor(inSlot);
						inv.setItem(13, new ItemStack(Material.AIR));
					}
					p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
					RPGEvents.scheduleRunnable(new RPGEvents.UpdateEnhancementInventoryMiddleSlot(inv, p), 1);
					return;
				}
				ItemStack slot1 = inv.getItem(10);
				ItemStack slot2 = inv.getItem(16);
				if (CakeLibrary.getItemName(slot1).equals(CakeLibrary.getItemName(EnhancementInventory.slotItem))
						|| CakeLibrary.getItemName(slot2).equals(CakeLibrary.getItemName(EnhancementInventory.slotItem)))
				{
					RPGCore.msg(p, "Place 2 copies of an item on the left and right slots.");
					return;
				}
				RItem ri1 = new RItem(slot1);
				RItem ri2 = new RItem(slot2);
				RItem riBase1 = new RItem(ri1.createBaseItem());
				RItem riBase2 = new RItem(ri2.createBaseItem());
				if (!riBase1.compare(riBase2))
				{
					RPGCore.msg(p, "These 2 items are not the same.");
					return;
				}
				if (ri1.getTier() != ri2.getTier())
				{
					RPGCore.msg(p, "These 2 items are not of the same tier.");
					return;
				}
				if (ri1.magicDamage == 0 && ri1.bruteDamage == 0)
				{
					RPGCore.msg(p, "Only items with damage can be enhanced.");
					return;
				}
				if (ri1.getTier() >= 5)
				{
					RPGCore.msg(p, "These items have already reached the maximum enhancement tier.");
					return;
				}
				if (CakeLibrary.getItemName(inSlot).equals(CakeLibrary.getItemName(EnhancementInventory.slotEnhance)))
				{
					if (ri2.bonusStat != null)
						if (!CakeLibrary.getItemName(inv.getItem(13)).equals(CakeLibrary.getItemName(EnhancementInventory.slotWarn)))
						{
							inv.setItem(13, EnhancementInventory.slotWarn.clone());
							p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
							RPGEvents.scheduleRunnable(new RPGEvents.SetInventoryItem(inv, 13, EnhancementInventory.slotWarn1), 20);
							rp.uiClickDelay = 20;
							return;
						}
				}
				inv.setItem(13, EnhancementInventory.getSlotEnhance(0));
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.2F, 0.9F);
				for (int i = 1; i < EnhancementInventory.maxState + 1; i++)
					RPGEvents.scheduleRunnable(new RPGEvents.CheckEnhancementInventory(inv, p, i), i * 2);
				return;
			}
			return;
		}
		else if (name.equals("Class Selection"))
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
			if (!change.equals(ClassType.MAGE) && !p.hasPermission("rpgcore.class"))
			{
				RPGCore.msg(p, "Sorry! We're currently in testing and only Mage is playable!");
				return;
			}
			rp.currentClass = change;
			RPGClass.unlockBasicSkills(rp, change);

			RPGCore.playerManager.writeData(rp);
			p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 0.1F, 1.0F);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, p.getLocation().add(0, 2, 0), 169), 0);
			RPGEvents.scheduleRunnable(new RPGEvents.InventoryClose(p), 1);
			rp.updateScoreboard = true;
			return;
		}
		else if (name.equals("Party Info"))
			event.setCancelled(true);
		else if (name.equals("Stats / Buffs"))
			event.setCancelled(true);
		else if (name.startsWith("Learnt Skills"))
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
				if (!itemName.startsWith("<-- (") || rp.skillbookTierSwitchTicks > 0)
					return;
				rp.lastSkillbookTier--;
				inv.setContents(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier).getContents());
				p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.1F, 0.9F);
				rp.skillbookTierSwitchTicks = 3;
			} else if (itemName.startsWith("Next Tier "))
			{
				if (!itemName.startsWith("Next Tier (") || rp.skillbookTierSwitchTicks > 0)
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void handleBlockBreakMonitor(BlockBreakEvent event)
	{
		if (event.isCancelled())
			return;
		Block b = event.getBlock();
		if (b.getWorld().getName().equals(RPGCore.areaInstanceWorld) && !event.getPlayer().hasPermission("rpgcore.arena"))
			event.setCancelled(true);
		else if (b.getType().equals(Material.SKULL))
		{
			String texture = ((TileEntitySkull)((CraftWorld)b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ())))
					.getGameProfile()
					.getProperties().get("textures").iterator().next()
					.getValue();
			for (String key: RPGCore.heads.keySet())
				if (key.equalsIgnoreCase("equipmentenhancer") && RPGCore.heads.get(key).equals(texture))
				{
					event.setDropItems(false);
					b.getWorld().dropItem(b.getLocation(), RPGCore.getItemFromDatabase("EquipmentEnhancer").createItem())
					.setVelocity(new Vector(0, 0.5F, 0));
					return;
				}
		}
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

		if (p.getGameMode().equals(GameMode.CREATIVE))
			return;

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
			} else if (prospector.lastCheckedLevel >= 3 && RPGCore.rand.nextInt(30) == 0)
			{
				xp += 30;
				b.getWorld().dropItem(b.getLocation(), platinum.clone()).setVelocity(new Vector(0, 0.5F, 0));
			} else if (prospector.lastCheckedLevel >= 5 && RPGCore.rand.nextInt(40) == 0)
			{
				xp += 50;
				b.getWorld().dropItem(b.getLocation(), topaz.clone()).setVelocity(new Vector(0, 0.5F, 0));
			} else if (prospector.lastCheckedLevel >= 8 && RPGCore.rand.nextInt(50) == 0)
			{
				xp += 80;
				b.getWorld().dropItem(b.getLocation(), sapphire.clone()).setVelocity(new Vector(0, 0.5F, 0));
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
			if (rp.invulnerabilityTicks > 0)
			{
				event.setDamage(0);
				event.setCancelled(true);
				return;
			}
			event.setDamage(event.getDamage() - (event.getDamage() / 100D * rp.getStats().damageReductionAdd));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handleBlockPlace(BlockPlaceEvent event)
	{
		Player player = event.getPlayer();
		RPlayer rp = RPGCore.playerManager.getRPlayer(player.getUniqueId());
		if (rp == null)
			return;
		ItemStack is = event.getItemInHand();
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
		if (event.getAction().equals(Action.PHYSICAL))
			return;
		Player p = event.getPlayer();

		Block b = event.getClickedBlock();
		if (b != null && b.getType().equals(Material.SKULL) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			String texture = ((TileEntitySkull)((CraftWorld)b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ())))
					.getGameProfile()
					.getProperties().get("textures").iterator().next()
					.getValue();
			for (String key: RPGCore.heads.keySet())
				if (key.equalsIgnoreCase("equipmentenhancer") && RPGCore.heads.get(key).equals(texture))
				{
					p.openInventory(EnhancementInventory.getNewInventory());
					return;
				}
		}

		RPlayer rp = RPGCore.playerManager.getRPlayer(p.getUniqueId());
		ItemStack is = p.getItemInHand();
		if (!CakeLibrary.isItemStackNull(is))
		{
			String name = CakeLibrary.getItemName(is);
			if (CakeLibrary.hasColor(name))
			{
				RItem ri = new RItem(is);
				if (ri.consumable)
				{
					event.setCancelled(true);
					if (rp.lastInteractTicks > 0)
						return;
					if (rp.consumableCooldownTicks > 0)
					{
						RPGCore.msg(p, "Cooldown time left: &4" + CakeLibrary.convertTimeToString(rp.consumableCooldownTicks / 20));
						return;
					}
					rp.lastInteractTicks = 1;
					if (is.getAmount() == 1)
						p.setItemInHand(null);
					else
					{
						is.setAmount(is.getAmount() - 1);
						p.setItemInHand(is);
					}
					ri.getBuff().applyBuff(rp);
					int food = p.getFoodLevel() + ri.satiate;
					p.setFoodLevel(food > 20 ? 20 : food);
					rp.consumableCooldownTicks = ri.consumableCooldown;
					rp.updateStats = true;
					for (int i = 0; i < 5; i++)
						RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(p, Sound.ENTITY_GENERIC_EAT, 0.1F, 1.0F), i * 5);
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(p, Sound.ENTITY_PLAYER_BURP, 0.1F, 1.0F), 25);
					return;
				}
				name = CakeLibrary.removeColorCodes(name);
				if (name.startsWith("Skillbook < "))
				{
					if (rp.lastInteractTicks > 0)
						return;
					rp.lastInteractTicks = 1;
					String skillName = name.replace("Skillbook < ", "");
					skillName = skillName.substring(0, skillName.length() - 2);

					RPGSkill skill = null;
					for (RPGSkill check: RPGSkill.skillList)
						if (check.skillName.equals(skillName))
							skill = check;

					if (skill != null && !rp.skills.contains(skillName) 
							&& (skill.classType.equals(ClassType.ALL) || skill.classType.equals(rp.currentClass)))
					{
						rp.learnSkill(skill);
						p.setItemInHand(null);
					}
					rp.updateScoreboard = true;
					event.setCancelled(true);
					return;
				}
				else if (name.equals("Unique Compass"))
				{
					if (rp.lastInteractTicks > 0)
						return;
					rp.lastInteractTicks = 1;
					RPGCore.msgNoTag(p, 
							"&cYou are &4" + CakeLibrary.seperateNumberWithCommas((int)p.getWorld().getSpawnLocation().distance(p.getLocation()), false) + " &cblocks from spawn.");
					event.setCancelled(true);
					return;
				}
				else if (name.startsWith("Gold ("))
				{
					if (rp.lastInteractTicks > 0)
						return;
					rp.lastInteractTicks = 1;
					int gold = Integer.parseInt(name.substring(6, name.length() - 1).replaceAll(",", "")) * is.getAmount();
					rp.addGold(gold);
					p.setItemInHand(null);
					RPGCore.msg(p, "&e" + gold + " Gold &6has been added to your account");
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
				if (RPGSkill.getSkill(name) != null)
					event.setCancelled(true);
				if (rp.lastInteractTicks == 0)
				{
					rp.lastInteractTicks = 1;
					handleSkillCast(event.getPlayer());
				}
			}
		}

		if (rp.heartspanTicks > 0 && !rp.castDelays.containsKey(Heartspan.skillName))
		{
			Heartspan.strike(rp);
			return;
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void handlePlayerMove(PlayerMoveEvent event)
	{
		Location to = event.getTo();
		RPlayer rp = RPGCore.playerManager.getRPlayer(event.getPlayer().getUniqueId());
		if (rp.arenaEnterLeaveTicks == 0)
		{
			for (Arena a: Arena.arenaList)
			{
				if (a != null)
					if (a.enabled)
						for (Location entrance: a.entrances)
							if (entrance.getBlockX() == to.getBlockX())
								if (entrance.getBlockY() == to.getBlockY())
									if (entrance.getBlockZ() == to.getBlockZ())
									{
										rp.enterArena(a);
										return;
									}
			}
			if (rp.arenaInstanceID != -1)
			{
				if (!event.getPlayer().getWorld().getName().equals(RPGCore.areaInstanceWorld))
				{
					rp.leaveArena(false);
					return;
				}
				ArenaInstance ai = ArenaInstance.getArenaInstance(rp.arenaInstanceID);
				if (!ai.mobsSpawned)
					ai.spawnMobs();
				if (ai != null)
					if (ai.getExitLocations().length > 0)
						for (Location exitLocation: ai.getExitLocations())
							if (exitLocation.getBlockX() == to.getBlockX())
								if (exitLocation.getBlockY() == to.getBlockY())
									if (exitLocation.getBlockZ() == to.getBlockZ())
									{
										rp.leaveArena(true);
										return;
									}
			}
		}
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
			if (split.length < 2)
			{
				for (RItem ri: RPGCore.itemDatabase)
					completions.add(ri.databaseName);
			}
			else if (split.length == 2 && split[1].length() > 0)
			{
				for (RItem ri: RPGCore.itemDatabase)
					if (ri.databaseName.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(ri.databaseName);
			}
		}

		else if (msg.startsWith(s + "skillbook ") || msg.startsWith(s + "sb "))
		{
			String[] split = msg.split(" ");
			if (split.length < 2)
			{
				for (RPGSkill skill: RPGSkill.skillList)
					completions.add(skill.skillName);
			}
			else if (split.length >= 2 && split[1].length() > 0)
			{
				for (RPGSkill skill: RPGSkill.skillList)
				{
					String skillName = skill.skillName.replace(" ", "");
					if (skillName.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(skillName);
				}
			}
		}

		else if (msg.startsWith(s + "sound "))
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

		else if (msg.startsWith(s + "skull "))
		{
			String[] split = msg.split(" ");
			if (split.length < 2)
			{
				completions.addAll(RPGCore.heads.keySet());
			}
			else if (split.length >= 2 && split[1].length() > 0)
			{
				for (String key: RPGCore.heads.keySet())
				{
					if (key.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(key);
				}
			}
		}

		else if (msg.startsWith(s + "npc skin "))
		{
			String[] split = msg.split(" ");
			if (split.length < 3)
			{
				for (SkinData sd: RPGCore.npcManager.skinDatas)
					completions.add(sd.skinName);
			}
			else if (split.length >= 3 && split[2].length() > 0)
			{
				for (SkinData sd: RPGCore.npcManager.skinDatas)
					if (sd.skinName.toLowerCase().startsWith(split[2].toLowerCase()))
						completions.add(sd.skinName);
			}
		}

		else if (msg.startsWith(s + "npcflag "))
		{
			RPlayer rp = RPGCore.playerManager.getRPlayer(event.getSender().getName());
			if (rp == null)
				return;
			String[] split = msg.split(" ");
			if (split.length < 2)
				return;
			if (!split[1].equalsIgnoreCase("del") && !split[1].equalsIgnoreCase("set"))
				return;
			if (split.length == 2 && msg.endsWith(" "))
				completions.addAll(rp.npcFlags.keySet());
			else if (split.length >= 3 && split[2].length() > 0)
			{
				for (String key: rp.npcFlags.keySet())
					if (key.toLowerCase().startsWith(split[2].toLowerCase()))
						completions.add(key);
			}
		}

		else if (msg.startsWith(s + "npc "))
		{
			String[] split = msg.split(" ");
			String[] arg1 = { "create", "skin", "rename", "del", "lockRotation", "chatRange", "databaseName" };
			if (split.length == 1 && msg.endsWith(" "))
			{
				for (String a: arg1)
					completions.add(a);
			}
			else if (split.length == 2 && !msg.endsWith(" "))
			{
				for (String a: arg1)
					if (a.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(a);
			}
		}

		else if (msg.startsWith(s + "item "))
		{
			String[] split = msg.split(" ");
			String[] arg1 = { 
					"tier", 
					"lvRequirement", 
					"magicDamage", 
					"bruteDamage", 
					"attackSpeed", 
					"critChance", 
					"critDamage",
					"recoverySpeed",
					"cooldownReduction",
					"damageReduction",
					"xpMultiplier",
					"unbreakable",
					"accessory",
					"desc"
			};
			if (split.length == 1 && msg.endsWith(" "))
			{
				for (String a: arg1)
					completions.add(a);
			}
			else if (split.length == 2 && !msg.endsWith(" "))
			{
				for (String a: arg1)
					if (a.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(a);
			}
		}

		else if (msg.startsWith(s + "itemfood ") || msg.startsWith(s + "if "))
		{
			String[] split = msg.split(" ");
			String[] arg1 = { 
					"satiate",
					"buffDuration",
					"consumableCooldown",
					"magicDamageAdd", 
					"bruteDamageAdd", 
					"damageReductionAdd", 
					"recoverySpeedAdd", 
					"cooldownReductionAdd", 
					"magicDamageMultiplier", 
					"bruteDamageMultiplier", 
					"attackSpeedMultiplier",
					"critChanceAdd",
					"critDamageAdd",
					"xpMultiplier"
			};
			if (split.length == 1 && msg.endsWith(" "))
			{
				for (String a: arg1)
					completions.add(a);
			}
			else if (split.length == 2 && !msg.endsWith(" "))
			{
				for (String a: arg1)
					if (a.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(a);
			}
		}

		else if (msg.startsWith(s + "mob ") || msg.startsWith(s + "mobdrops "))
		{
			String[] split = msg.split(" ");
			if (msg.endsWith(" "))
			{
				for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
					completions.add(spawn.rpgMonsterName);
			} else if (split.length == 2 && split[1].length() > 0)
			{
				for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
					if (spawn.rpgMonsterName.toLowerCase().startsWith(split[1].toLowerCase()))
						completions.add(spawn.rpgMonsterName);
			}
		}

		else if (msg.startsWith(s + "arena "))
		{
			String[] split = msg.split(" ");
			String[] arg1 = { "list", "create", "del", "setSpawnRotation", 
					"tpSpawnTest", "addMobSpawn", "enter", "leave", "setEntrance", 
					"setExternalExit", "setInternalExit", "clearEntrances", "clearInternalExits",
					"enable", "disable", "createInstance" };
			String[] arg2Arena = { "create", "del", "setspawnrotation", "tpspawntest", 
					"addmobspawn", "enter", "setentrance", "setexternalexit", "setinternalexit",
					"clearentrances", "clearinternalexits", "enable", "disable", "createInstance" };
			boolean complete = false;
			if (split.length >= 2)
				for (String check: arg2Arena)
					if (split[1].equalsIgnoreCase(check))
						complete = true;
			if (split.length == 1 && msg.endsWith(" "))
			{
				for (String a: arg1)
					completions.add(a);
			}
			else if (split.length == 2)
			{
				if (msg.endsWith(" ") && complete)
				{
					for (Arena a: Arena.arenaList)
						completions.add(a.schematicName);
				} else
					for (String a: arg1)
						if (a.toLowerCase().startsWith(split[1].toLowerCase()))
							completions.add(a);
			} else if (split.length == 3 && complete)
			{
				for (Arena a: Arena.arenaList)
					if (a.schematicName.toLowerCase().startsWith(split[2].toLowerCase()))
						completions.add(a.schematicName);
			} else if (split.length == 3 && msg.endsWith(" ") && split[1].equalsIgnoreCase("addmobspawn"))
			{
				for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
					completions.add(spawn.rpgMonsterName);
			} else if (split.length == 4 && split[1].equalsIgnoreCase("addmobspawn"))
			{
				for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
					if (spawn.rpgMonsterName.toLowerCase().startsWith(split[3].toLowerCase()))
						completions.add(spawn.rpgMonsterName);
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
		if (rp.globalCastDelay > 0 || rp.castDelays.containsKey(name))
			return false;

		for (String skillName: rp.skills)
			if (skillName.equals(name))
			{
				RPGSkill skill = RPGSkill.getSkill(skillName);
				if (skill != null)
				{
					if (rp.cooldowns.containsKey(skill.cooldownName))
					{
						RPGCore.msgNoTag(p, "&a=== Cooldown time for &2" 
								+ skill.cooldownName
								+ "&a: &2" + rp.cooldowns.get(skill.cooldownName) / 20.0D + "s&a ===");
						return false;
					}
					skill.insantiate(rp);
					return true;
				}
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
