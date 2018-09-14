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
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
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
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import es.eltrueno.npc.skin.SkinData;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.TileEntitySkull;
import rpgcore.areas.Arena;
import rpgcore.areas.ArenaInstance;
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

		if (CakeLibrary.getNearbyEntities(e.getLocation(), 64).size() > 128)
		{
			event.setCancelled(true);
			return;
		}

		double spawnDistance = e.getWorld().getSpawnLocation().distance(e.getLocation());

		for (RPGMonsterSpawn spawn: RPGMonsterSpawn.spawns)
		{
			if (!spawn.isNaturalSpawn())
				continue;
			if (!spawn.monsterType.isInstance(e))
				continue;
			if (spawnDistance < spawn.minSpawnDistance || spawnDistance > spawn.maxSpawnDistance)
				continue;
			if (rand.nextInt(spawn.spawnRoll) != 0)
				continue;
			spawn.replaceMonster(e);
			break;
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
		rp.setInvButtons();
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
		if (name.equals("Party Info") || name.equals("Equipment Enhancement") || name.equals("Stats / Buffs") || name.equals("Class Selection") || name.startsWith("Skillbook: ") || name.startsWith("Learnt Skills") || crystal)
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
				ArrayList<ItemStack >drops = ce.getDrops();
				if (drops != null && drops.size() > 0)
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
		} else if (name.equals("Equipment Enhancement"))
		{
			for (int i = 10; i < 17; i += 3)
			{
				ItemStack item = inv.getItem(i);
				if (CakeLibrary.isItemStackNull(item))
					continue;
				String itemName = CakeLibrary.getItemName(item);
				if (itemName.equals(CakeLibrary.getItemName(EnhancementInventory.slotEnhance))
						|| itemName.equals(CakeLibrary.getItemName(EnhancementInventory.slotInstruct))
						|| itemName.equals(CakeLibrary.getItemName(EnhancementInventory.slotFail))
						|| itemName.equals(CakeLibrary.getItemName(EnhancementInventory.slotItem))
						|| itemName.startsWith(CakeLibrary.getItemName(EnhancementInventory.getSlotEnhance(0)))
						)
					continue;
				p.getInventory().addItem(item);
				inv.setItem(i, new ItemStack(Material.AIR));
			}
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
		if (RPGCore.inventoryButtons)
			if (event.getView().getTopInventory().getSize() == 5) //Player inventory.
			{
				if (event.getRawSlot() == 17) //Accessories
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
					p.openInventory(rp.accessoryInventory.getInventory());
					return;
				} else if (event.getRawSlot() == 26) //Buffs / Stats
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
					rp.buffInventory.updateInventory();
					p.openInventory(rp.buffInventory.getInventory());
					return;
				} else if (event.getRawSlot() == 35) //Skills
				{
					event.setCancelled(true);
					if (!CakeLibrary.isItemStackNull(event.getCursor()))
						return;
					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
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
		if (RPGCore.inventoryButtons && event.getView().getTopInventory().getSize() != 5)
			if (invSlot == 8 || invSlot == 17 || invSlot == 26) //Accessories
			{
				event.setCancelled(true);
				if ((hasColor 
						&& !name.equals("Equipment Enhancement") && !name.equals("Class Selection") && !name.startsWith("Conversation - ")) 
						|| !hasColor)
				{
					int check = invSlot + 9;
					if (check == 17) //Accessories
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
						p.openInventory(rp.accessoryInventory.getInventory());
						return;
					} else if (check == 26) //Buffs / Stats
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
						rp.buffInventory.updateInventory();
						p.openInventory(rp.buffInventory.getInventory());
						return;
					} else if (check == 35) //Skills
					{
						if (!CakeLibrary.isItemStackNull(event.getCursor()))
							return;
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.0F).run();
						p.openInventory(SkillInventory2.getSkillInventory(rp, rp.lastSkillbookTier));
						return;
					}
					return;
				}
				RPGCore.msg(p, "Close this window first");
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
			if (name.equals("Accessories") && event.isShiftClick())
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
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
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
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
						EnhancementInventory.updateMiddleItem(inv);
						return;
					}
				}
				return;
			}
			if (event.isShiftClick() || event.getAction().equals(InventoryAction.COLLECT_TO_CURSOR))
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

					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
					RPGCore.playerManager.writeData(rp);
				} else
				{
					if (event.isShiftClick())
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						event.setCancelled(false);
						RPGEvents.scheduleRunnable(new RPGEvents.SetInventoryItem(inv, event.getSlot(), AccessoryInventory.slotItem.clone()), 1);
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
						rp.accessoryInventory.slots[riSlot] = null;
						return;
					}
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
					{
						event.setCursor(inSlot);
						event.setCurrentItem(AccessoryInventory.slotItem.clone());
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
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
						event.setCursor(inSlot);
						event.setCurrentItem(cursor);
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
						rp.accessoryInventory.slots[riSlot] = new RItem(cursor);
					}
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

					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
					EnhancementInventory.updateMiddleItem(inv);
				} else
				{
					if (event.isShiftClick())
					{
						if (!CakeLibrary.playerHasVacantSlots(p))
							return;
						event.setCancelled(false);
						RPGEvents.scheduleRunnable(new RPGEvents.SetInventoryItem(inv, event.getSlot(), EnhancementInventory.slotItem.clone()), 1);
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
						EnhancementInventory.updateMiddleItem(inv);
						return;
					}
					ItemStack cursor = event.getCursor();
					if (CakeLibrary.isItemStackNull(cursor))
					{
						event.setCursor(inSlot);
						event.setCurrentItem(EnhancementInventory.slotItem.clone());
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
					} else
					{
						if (cursor.getAmount() > 1)
						{
							RPGCore.msg(p, "Retrive the item first, or replace with a stack of 1");
							return;
						}
						event.setCursor(inSlot);
						event.setCurrentItem(cursor);
						new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 1.1F).run();
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
					new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
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
				if (!ri1.compare(ri2))
				{
					RPGCore.msg(p, "These 2 items are not the same.");
					return;
				}
				if (ri1.getTier() >= 5 || ri2.getTier() >= 5)
				{
					RPGCore.msg(p, "These items have already reached the maximum enhancement tier.");
					return;
				}
				inv.setItem(13, EnhancementInventory.getSlotEnhance(0));
				new RPGEvents.PlaySoundEffect(p, Sound.UI_BUTTON_CLICK, 0.2F, 0.9F).run();
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
		Player p = event.getPlayer();

		Block b = event.getClickedBlock();
		if (b != null && b.getType().equals(Material.SKULL) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
		{
			String texture = ((TileEntitySkull)((CraftWorld)b.getWorld()).getHandle().getTileEntity(new BlockPosition(b.getX(), b.getY(), b.getZ())))
					.getGameProfile()
					.getProperties().get("textures").iterator().next()
					.getValue();
			for (String key: RPGCore.heads.keySet())
				if (key.equalsIgnoreCase("equipmentenhancement") && RPGCore.heads.get(key).equals(texture))
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
					rp.lastInteractTicks = 2;
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
					for (int i = 0; i < 5; i++)
						RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(p, Sound.ENTITY_GENERIC_EAT, 0.1F, 1.0F), i * 5);
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(p, Sound.ENTITY_PLAYER_BURP, 0.1F, 1.0F), 25);
					return;
				}
				name = CakeLibrary.removeColorCodes(name);
				if (name.startsWith("Skillbook < "))
				{
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

		if (rp.heartspanTicks > 0 && !rp.castDelays.containsKey(Heartspan.skillName))
		{
			Heartspan.strike(rp);
			return;
		}
		if (rp.lastInteractTicks == 0)
		{
			rp.lastInteractTicks = 2;
			handleSkillCast(event.getPlayer());
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
					if (a.entrance != null && a.exitExternal != null && a.exitInternal != null)
						if (a.entrance.getBlockX() == to.getBlockX())
							if (a.entrance.getBlockY() == to.getBlockY())
								if (a.entrance.getBlockZ() == to.getBlockZ())
								{
									rp.enterArena(a);
									return;
								}
			}
			if (rp.arenaInstanceID != -1)
			{
				ArenaInstance ai = ArenaInstance.getArenaInstance(rp.arenaInstanceID);
				if (!ai.mobsSpawned)
					ai.spawnMobs();
				if (ai != null)
					if (ai.getExitLocation() != null)
						if (ai.getExitLocation().getBlockX() == to.getBlockX())
							if (ai.getExitLocation().getBlockY() == to.getBlockY())
								if (ai.getExitLocation().getBlockZ() == to.getBlockZ())
								{
									rp.leaveArena();
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
			else if (split.length >= 2 && split[1].length() > 0)
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
					"cooldownReduction",
					"damageReduction",
					"unbreakable",
					"accessory",
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

		else if (msg.startsWith(s + "mob "))
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
			String[] arg1 = { "list", "create", "del", "setSpawnRotation", "tpSpawnTest", "addMobSpawn", "enter", "leave", "setEntrance", "setExternalExit", "setInternalExit" };
			String[] arg2Arena = { "create", "del", "setspawnrotation", "tpspawntest", "addmobspawn", "enter", "setentrance", "setexternalexit", "setinternalexit" };
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

		if (rp.cooldowns.containsKey(name))
		{
			RPGCore.msgNoTag(p, "&a=== Cooldown time for &2" 
					+ CakeLibrary.getItemName(RPGSkill.getSkill(name).getSkillItem()) 
					+ "&a: &2" + rp.cooldowns.get(name) / 20.0D + "s&a ===");
			return false;
		}
		for (String skillName: rp.skills)
			if (skillName.equals(name))
			{
				RPGSkill skill = RPGSkill.getSkill(skillName);
				if (skill != null)
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
