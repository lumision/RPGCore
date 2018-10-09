package rpgcore.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import rpgcore.areas.Area;
import rpgcore.areas.Arena;
import rpgcore.areas.ArenaInstance;
import rpgcore.buff.Buff;
import rpgcore.buff.BuffInventory;
import rpgcore.buff.Stats;
import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.external.Title;
import rpgcore.item.BonusStat;
import rpgcore.item.BonusStat.BonusStatType;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.monsterbar.MonsterBar;
import rpgcore.npc.CustomNPC;
import rpgcore.recipes.RPGRecipe;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
import rpgcore.skills.BladeMastery1;
import rpgcore.skills.IronBody;
import rpgcore.skills.LightFeet;
import rpgcore.skills.MagicMastery1;
import rpgcore.skills.MagicMastery2;
import rpgcore.skills.MagicMastery3;
import rpgcore.skills.PriestsBlessing;
import rpgcore.skills.RPGSkill;
import rpgcore.skills.Vigor1;
import rpgcore.skills.Wisdom;
import rpgcore.songs.RPGSong;
import rpgcore.songs.RSongManager;
import rpgcore.songs.RunningTrack;
import rpgcore.tutorial.Tutorial;

public class RPlayer 
{
	private UUID uuid;
	public ArrayList<RPGClass> classes;
	public ArrayList<RPGSideClass> sideClasses;
	public ClassType currentClass;
	public HashMap<String, Integer> castDelays;
	public int globalCastDelay;
	public String lastSkill;
	public ArrayList<ClassType> unlockedClasses;
	public ArrayList<String> skills;
	//public ArrayList<Integer> skillLevels;
	public HashMap<String, Integer> cooldowns;
	public ArrayList<Title> titleQueue;
	public ArrayList<Buff> buffs;
	public RItem[] rEquips = new RItem[5];
	public Map<String, String> npcFlags;
	public int partyID;
	public int recoverTicks;
	public boolean checkLevel;
	public RPGSideClass checkSideClassLevel;
	public Scoreboard scoreboard;
	public Objective objective;
	public CustomNPC selectedNPC, npcClosure;
	public Tutorial tutorial;
	public boolean tutorialCompleted;
	private int gold;
	private int tokens;
	public int lastSkillbookTier = 1;
	public int pageSwitchTicks;
	public Location pos1, pos2;
	public int arenaInstanceID;
	public Location leftForArenaLocation;
	public int lastInteractTicks;
	public BuffInventory buffInventory;
	private Player player;
	public int lastUpdateScoreboardTicks, lastUpdateEquipTicks;
	public boolean updateScoreboard;
	public String[] lastEquipmentCheck = { "", "", "", "", "" };
	public int consumableCooldownTicks;
	public int arenaEnterLeaveTicks;
	public AccessoryInventory accessoryInventory;
	public MonsterBar monsterBar;
	public int lastBarTicks;
	public HashMap<BossBar, Integer> cooldownBars;
	public int cooldownDisplayMode = 0; //0 = bossbar, 1 = title, 2 = chat
	public int recoverMaxTicks = 60;
	private static final int recoverMaxTicksDefault = 60;
	private Stats lastCalculatedStats;
	private Stats postStatsMultipliers;
	public boolean updateStats;
	public int invulnerabilityTicks;
	public int uiClickDelay;
	public int globalGiftIndex;
	public Mailbox mailbox;
	public Area lastArea;
	public ArrayList<String> recipes;
	public ArrayList<RPGRecipe> viewableRecipes;
	public int lastRecipeBookPage = 1;

	public int sneakTicks;
	public int heartspanTicks;

	public static ItemStack invMailbox = CakeLibrary.renameItem(new ItemStack(Material.CHEST), "&a&nMailbox");
	public static ItemStack invAccessories = CakeLibrary.renameItem(new ItemStack(Material.TOTEM), "&d&nAccessories");
	public static ItemStack invBuffsStats = CakeLibrary.renameItem(new ItemStack(Material.PAPER), "&c&nBuffs / Stats");
	public static ItemStack invSkills = CakeLibrary.renameItem(new ItemStack(Material.BOOK), "&b&nSkills");
	public static ItemStack invRecipes = CakeLibrary.renameItem(new ItemStack(Material.ENCHANTED_BOOK), "&e&nCustom Recipes");

	public RPlayer(UUID uuid)
	{
		this.uuid = uuid;
		this.classes = new ArrayList<RPGClass>();
		this.sideClasses = new ArrayList<RPGSideClass>();
		for (ClassType ct: ClassType.values())
			classes.add(new RPGClass(ct));
		for (SideClassType ct: SideClassType.values())
			sideClasses.add(new RPGSideClass(ct, 0));
		this.skills = new ArrayList<String>();
		//this.skillLevels = new ArrayList<Integer>();
		this.cooldowns = new HashMap<String, Integer>();
		this.cooldownBars = new HashMap<BossBar, Integer>();
		this.castDelays = new HashMap<String, Integer>();
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.currentClass = ClassType.ALL;
		this.lastSkill = "";
		this.partyID = -1;
		this.arenaInstanceID = -1;
		this.gold = 10;
		this.tokens = 0;
		this.npcFlags = new HashMap<String, String>();
		this.tutorial = new Tutorial(this);
		this.buffInventory = new BuffInventory(this);
		this.accessoryInventory = new AccessoryInventory(this);
		this.mailbox = new Mailbox(this);
		this.recipes = new ArrayList<String>();
		setInvButtons();
	}

	public RPlayer(UUID uuid, ArrayList<RPGClass> classes, ArrayList<RPGSideClass> sideClasses, ClassType currentClass, ArrayList<String> skills, int gold, int tokens)
	{
		this.uuid = uuid;
		this.classes = classes;
		this.sideClasses = sideClasses;
		this.skills = skills;
		//this.skillLevels = skillLevels;
		this.currentClass = currentClass;
		this.gold = gold;
		for (ClassType ct: ClassType.values())
		{
			boolean cont = false;
			for (RPGClass c: classes)
				if (c.classType.equals(ct))
				{
					cont = true;
					break;
				}
			if (cont)
				continue;
			RPGClass c = new RPGClass(ct);
			classes.add(c);
		}
		for (SideClassType ct: SideClassType.values())
		{
			boolean cont = false;
			for (RPGSideClass c: sideClasses)
				if (c.sideClassType.equals(ct))
				{
					cont = true;
					break;
				}
			if (cont)
				continue;
			RPGSideClass c = new RPGSideClass(ct, 0);
			sideClasses.add(c);
		}
		this.castDelays = new HashMap<String, Integer>();
		this.cooldowns = new HashMap<String, Integer>();
		this.cooldownBars = new HashMap<BossBar, Integer>();
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.lastSkill = "";
		this.partyID = -1;
		this.arenaInstanceID = -1;
		this.npcFlags = new HashMap<String, String>();
		this.tutorial = new Tutorial(this);
		this.buffInventory = new BuffInventory(this);
		this.accessoryInventory = new AccessoryInventory(this);
		this.mailbox = new Mailbox(this);
		this.recipes = new ArrayList<String>();
		setInvButtons();
	}

	public void giveItem(ItemStack item)
	{
		if (getPlayer() == null)
		{
			mailbox.items.add(new RItem(item));
			RPGCore.playerManager.writeData(this);
			return;
		}
		if (!CakeLibrary.playerHasVacantSlots(getPlayer()))
		{
			mailbox.items.add(new RItem(item));
			RPGCore.playerManager.writeData(this);
			RPGCore.msg(getPlayer(), "An item was sent to your mailbox because your inventory was full");
			return;
		}
		getPlayer().getInventory().addItem(item.clone());
	}

	public void giveItem(RItem item)
	{
		if (getPlayer() == null)
		{
			mailbox.items.add(item);
			RPGCore.playerManager.writeData(this);
			return;
		}
		if (!CakeLibrary.playerHasVacantSlots(getPlayer()))
		{
			mailbox.items.add(item);
			RPGCore.playerManager.writeData(this);
			RPGCore.msg(getPlayer(), "An item was sent to your mailbox because your inventory was full");
			return;
		}
		getPlayer().getInventory().addItem(item.createItem());
	}

	public void setInvButtons()
	{
		if (getPlayer() == null || !RPGCore.inventoryButtons)
			return;
		Inventory inv = getPlayer().getInventory();
		for (int i = 0; i < 5; i++)
		{
			int slot = i == 0 ? 9 : i == 1 ? 17 : i == 2 ? 26 : i == 3 ? 27 : 35;
			ItemStack item = inv.getItem(slot);
			if (CakeLibrary.isItemStackNull(item))
				continue;
			String name = CakeLibrary.removeColorCodes(CakeLibrary.getItemName(item));
			if (!name.equals("Mailbox") 
					&& !name.equals("Accessories") 
					&& !name.equals("Buffs / Stats") 
					&& !name.equals("Custom Recipes")
					&& !name.equals("Skills"))
				mailbox.items.add(new RItem(item.clone()));
		}
		int items = mailbox.items.size();
		ItemStack mailboxItem = invMailbox.clone();
		mailboxItem = CakeLibrary.addLore(mailboxItem, "&7 * " + (items == 0 ? "Empty" : items == 1 ? "1 item" : items + " items"));
		inv.setItem(9, mailboxItem);
		inv.setItem(27, invRecipes.clone());
		inv.setItem(17, invAccessories.clone());
		inv.setItem(26, invBuffsStats.clone());
		inv.setItem(35, invSkills.clone());
	}

	public void enterArena(Arena arena)
	{
		if (getPlayer() == null)
			return;
		arenaEnterLeaveTicks = 20;
		leaveArena(false);
		ArenaInstance ai = null;
		if (partyID != -1)
			for (Player p1: Bukkit.getOnlinePlayers())
			{
				if (p1 == getPlayer())
					continue;
				RPlayer rp1 = RPGCore.playerManager.getRPlayer(p1.getUniqueId());
				if (rp1.partyID != partyID)
					continue;
				if (rp1.arenaInstanceID >= 0)
				{
					ai = ArenaInstance.getArenaInstance(rp1.arenaInstanceID);
					if (!ai.arena.schematicName.equals(arena.schematicName))
						ai = null;
				}
			}
		if (ai == null)
			ai = ArenaInstance.getArenaInstance(arena);
		leftForArenaLocation = getPlayer().getLocation();
		getPlayer().teleport(ai.getSpawnLocation());
		ai.occupied = true;
		arenaInstanceID = ai.arenaInstanceID;

		RPGCore.playerManager.writeData(this);
		ArenaInstance.writeArenaInstanceData();
	}

	public void leaveArena(boolean teleport)
	{
		arenaEnterLeaveTicks = 20;
		if (arenaInstanceID == -1)
			return;
		Player p = getPlayer();
		if (p == null)
			return;
		ArenaInstance ai = ArenaInstance.getArenaInstance(arenaInstanceID);
		if (ai == null)
			return;
		boolean stillOccupied = false;
		arenaInstanceID = -1;

		for (int i = 0; i < RPGCore.playerManager.players.size(); i++)
			if (RPGCore.playerManager.players.get(i).arenaInstanceID == ai.arenaInstanceID)
				stillOccupied = true;
		ai.occupied = stillOccupied;
		if (!ai.occupied)
		{
			for (int i = 0; i < ai.mobList.size(); i++)
				ai.mobList.get(i).remove();
			ai.mobsSpawned = false;
		}
		if (teleport)
			p.teleport(ai.arena.exitExternal);
		leftForArenaLocation = null;

		RPGCore.playerManager.writeData(this);
		ArenaInstance.writeArenaInstanceData();
	}

	public void initializeScoreboard()
	{
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective = scoreboard.registerNewObjective("xp", "test");
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(CakeLibrary.recodeColorCodes("&6Class: " + getCurrentClass().classType.getClassName()));
		this.objective = objective;
		this.updateScoreboard = true;
		Player p = getPlayer();
		if (p != null)
			getPlayer().setScoreboard(scoreboard);
	}

	public void updateScoreboard()
	{
		if (objective == null)
			return;
		if (getPlayer() == null)
			return;
		objective.setDisplayName(CakeLibrary.recodeColorCodes("&6" + currentClass.getClassName()));

		//objective.getScore(CakeLibrary.recodeColorCodes("&bPower Level: ")).setScore(getPowerLevel());
		objective.getScore(CakeLibrary.recodeColorCodes("&eLevel: ")).setScore(getLevel());
		objective.getScore(CakeLibrary.recodeColorCodes("&e% EXP: ")).setScore(getPercentageToNextLevel());
		objective.getScore(CakeLibrary.recodeColorCodes("&aGold: ")).setScore(gold);

		updateScoreboard = false;
		lastUpdateScoreboardTicks = 20;
	}

	public void addGold(int amount)
	{
		gold += amount;
		updateScoreboard = true;
	}

	public int getGold()
	{
		return gold;
	}

	public int getDamageOfClass()
	{
		return (currentClass.getDamageType() == 0) ? getStats().bruteDamageAdd : getStats().magicDamageAdd;
	}

	public RPGSideClass getSideClass(SideClassType sideClassType)
	{
		for (RPGSideClass rc: sideClasses)
			if (rc.sideClassType.equals(sideClassType))
				return rc;
		return null;
	}

	public RPGClass getCurrentClass()
	{
		for (RPGClass rc: classes)
			if (rc.classType.equals(currentClass))
				return rc;
		return null;
	}

	public void tick20()
	{
		if (getPlayer() == null)
			return;
		if (lastArea != null)
		{
			if (!lastArea.isInArea(getPlayer().getLocation()))
			{
				for (RunningTrack rt: RSongManager.runningTracks)
					if (rt.player.getName().equalsIgnoreCase(getPlayer().getName()))
						rt.stop();
				lastArea = null;
			} else if (lastArea.bgm != null)
			{
				boolean playing = false;
				for (RunningTrack rt: RSongManager.runningTracks)
					if (rt.player.getName().equalsIgnoreCase(getPlayer().getName()))
					{
						playing = true;
						break;
					}
				if (!playing)
				{
					RPGSong song = RPGCore.songManager.getSong(lastArea.bgm);
					if (song != null)
						song.play(getPlayer(), song.loop);
				}
			}
		} else
			for (Area a: Area.areas)
			{
				if (a.isInArea(getPlayer().getLocation()))
				{
					if (a.bgm != null)
					{
						RPGSong song = RPGCore.songManager.getSong(a.bgm);
						if (song != null)
							song.play(getPlayer(), song.loop);
					}
					lastArea = a;
					break;
				}
			}
		updatePlayerREquips();
		if (updateStats)
		{
			updateStats = false;
			updateStats();
		}
		updateScoreboard = true;
		int debuffTick = 0;
		for (String skill: skills)
			if (skill.startsWith("Tenacity"))
				debuffTick += 20;
		if (debuffTick > 0)
			for (PotionEffect pe: getPlayer().getActivePotionEffects())
			{
				if (pe.getType().equals(PotionEffectType.BLINDNESS) 
						|| pe.getType().equals(PotionEffectType.CONFUSION)
						|| pe.getType().equals(PotionEffectType.HUNGER)
						|| pe.getType().equals(PotionEffectType.POISON)
						|| pe.getType().equals(PotionEffectType.SLOW)
						|| pe.getType().equals(PotionEffectType.SLOW_DIGGING)
						|| pe.getType().equals(PotionEffectType.UNLUCK)
						|| pe.getType().equals(PotionEffectType.WEAKNESS)
						|| pe.getType().equals(PotionEffectType.WITHER))
				{
					int newDuration = pe.getDuration() - debuffTick;
					getPlayer().removePotionEffect(pe.getType());
					if (newDuration > 0)
					{
						PotionEffect n = new PotionEffect(pe.getType(), newDuration, pe.getAmplifier());
						getPlayer().addPotionEffect(n);
					}
				}
			}
	}

	public void tick10()
	{
		if (getPlayer() == null)
			return;
		if (skills.contains(LightFeet.skillName)) //Light Feet functionality
		{
			CakeLibrary.addPotionEffectIfBetterOrEquivalent(getPlayer(), new PotionEffect(PotionEffectType.SPEED, 19, LightFeet.swiftness));
			CakeLibrary.addPotionEffectIfBetterOrEquivalent(getPlayer(), new PotionEffect(PotionEffectType.JUMP, 19, LightFeet.jump));
		}
		if (!tutorialCompleted)
			tutorial.check();
		if (buffInventory.isOpen())
			buffInventory.updateInventory();

		int maxHealthAdd = 0;
		for (String skill: skills)
			if (skill.startsWith("Vitality "))
				maxHealthAdd += 2;
		getPlayer().setMaxHealth(20 + maxHealthAdd);
	}

	public void tick()
	{
		if (getPlayer() == null)
			return;
		if (uiClickDelay > 0)
			uiClickDelay--;
		if (invulnerabilityTicks > 0)
			invulnerabilityTicks--;
		if (lastBarTicks > -100)
			lastBarTicks--;
		else if (monsterBar != null)
		{
			if (monsterBar.bar.getPlayers().size() == 1)
				monsterBar.destroy();
			else
			{
				monsterBar.bar.removePlayer(getPlayer());
				monsterBar = null;
			}
		}
		if (arenaEnterLeaveTicks > 0)
			arenaEnterLeaveTicks--;
		if (lastUpdateScoreboardTicks > 0)
			lastUpdateScoreboardTicks--;
		if (consumableCooldownTicks > 0)
			consumableCooldownTicks--;
		if (updateScoreboard && lastUpdateScoreboardTicks <= 0)
			updateScoreboard();
		if (lastInteractTicks > 0)
			lastInteractTicks--;
		if (pageSwitchTicks > 0)
			pageSwitchTicks--;
		if (heartspanTicks > 0) //Heartspan functionality
		{
			heartspanTicks--;
			if (heartspanTicks <= 0)
				getPlayer().sendMessage(CakeLibrary.recodeColorCodes("&c**HEARTSPAN DEACTIVATED**"));
		}
		if (globalCastDelay > 0)
			globalCastDelay--;
		ArrayList<String> castDelayRemove = new ArrayList<String>();
		for (String key: castDelays.keySet())
		{
			int value = castDelays.get(key) - 1;
			if (value <= 0)
				castDelayRemove.add(key);
			else
				castDelays.put(key, value);
		}
		for (String remove: castDelayRemove)
			castDelays.remove(remove);

		ArrayList<String> cooldownRemove = new ArrayList<String>();
		ArrayList<BossBar> cooldownbarRemove = new ArrayList<BossBar>();
		for (String key: cooldowns.keySet())
		{
			int value = cooldowns.get(key);
			if (value == 40)
			{
				BossBar bar = Bukkit.createBossBar("§a" + key, BarColor.GREEN, BarStyle.SOLID);
				bar.addPlayer(getPlayer());
				cooldownBars.put(bar, 40);
			}
			if (value <= 1)
				cooldownRemove.add(key);
			else
				cooldowns.put(key, value - 1);
		}
		if (RPGCore.serverAliveTicks % 2 == 0)
			for (BossBar key: cooldownBars.keySet())
			{
				int value = cooldownBars.get(key) - 2;
				if (value < 0)
					cooldownbarRemove.add(key);
				else
				{
					key.setProgress(value / 40.0D);
					cooldownBars.put(key, value);
				}
			}
		for (String remove: cooldownRemove)
		{
			cooldowns.remove(remove);
			if (cooldownDisplayMode == 1)
				titleQueue.add(new Title("&6< "  + CakeLibrary.getItemName(RPGSkill.getSkill(remove).getSkillItem()) + "&6 >", "&eCooldown ended", 
						10, 5, 10));
			else if (cooldownDisplayMode == 2)
			{
				RPGCore.msgNoTag(getPlayer(), "&a*** Cooldown time for &2" 
						+ CakeLibrary.getItemName(RPGSkill.getSkill(remove).getSkillItem()) 
						+ " &2ended&a ***");
			}
		}
		for (BossBar remove: cooldownbarRemove)
		{
			remove.removeAll();
			cooldownBars.remove(remove);
		}

		for (int i = 0; i < buffs.size(); i++)
		{
			Buff b = buffs.get(i);
			if (b.duration < 1)
			{
				b.removeBuff(getPlayer());
				updateStats = true;

				buffs.remove(i);
				i--;
			}
		}

		if (checkLevel)
		{
			updateLevel();
			checkLevel = false;
			updateScoreboard = true;
		}
		if (checkSideClassLevel != null)
		{
			int lv = checkSideClassLevel.getLevel();
			if (checkSideClassLevel.lastCheckedLevel != lv)
			{
				checkSideClassLevel.lastCheckedLevel = lv;
				RPGCore.msg(getPlayer(), "&bYou've leveled your &3" + checkSideClassLevel.sideClassType.getClassName() + " &bclass up to " + lv + "!");
				getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.0F);
				RPGCore.playerManager.writeData(this);
				checkSideClassLevel = null;
			}
		}
		if (getPlayer().getHealth() < getPlayer().getMaxHealth() && !getPlayer().isDead())
		{
			recoverTicks++;
			if (recoverTicks >= recoverMaxTicks)
			{
				recoverTicks = 0;
				getPlayer().setHealth((int) getPlayer().getHealth() + 1);
			}
		} else
			recoverTicks = 0;

		try
		{
			if (titleQueue.size() > 0)
			{
				Title t = titleQueue.get(0);
				titleQueue.remove(0);
				t.sendPlayer(getPlayer());
			}
		} catch (Exception e) {}
	}

	public String getPlayerName()
	{
		Player player = Bukkit.getPlayer(uuid);
		if (player == null)
		{
			OfflinePlayer oplayer = Bukkit.getOfflinePlayer(uuid);
			if (oplayer == null)
				return null;
			return oplayer.getName();
		}
		return player.getName();
	}

	public UUID getUniqueID()
	{
		return uuid;
	}

	public void quit()
	{
		player = null;
	}

	public Player getPlayer()
	{
		if (player == null)
			return player = Bukkit.getPlayer(uuid);
		if (!player.isOnline())
			return player = null;
		return player;
	}

	public void addXP(int xp)
	{
		getCurrentClass().xp += xp;
		if (getCurrentClass().xp < 0)
			getCurrentClass().xp = 0;
		Player p = getPlayer();
		if (p != null)
			titleQueue.add(new Title("", CakeLibrary.recodeColorCodes("&7+" + xp + " XP (" + getPercentageToNextLevel() + "%)"), 4, 0, 16));
		checkLevel = true;
	}

	public void addSideclassXP(SideClassType sideClassType, int xp)
	{
		RPGSideClass sc = getSideClass(sideClassType);
		sc.xp += xp;
		//Player p = getPlayer();
		//if (p != null)
		//titleQueue.add(new Title("", CakeLibrary.recodeColorCodes(sc.sideClassType.getColorCode() + "+" + xp + " " + sc.sideClassType.getClassName() + " XP"), 4, 0, 16));
		checkSideClassLevel = sc;
	}

	public int getPercentageToNextLevel()
	{
		RPGClass c = getCurrentClass();
		double xpRaw = c.xp - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
		double nextXpRaw = RPGClass.getXPRequiredForLevel(c.lastCheckedLevel + 1) - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
		int percentage = (int) ((xpRaw / nextXpRaw) * 100.0F);
		return percentage < 0 ? 0 : percentage;
	}

	public int getLevel()
	{
		for (RPGClass c: classes)
			if (c.classType.equals(currentClass))
				return c.lastCheckedLevel;
		return 0;
	}

	public void updateLevel()
	{
		RPGClass c = getCurrentClass();
		int lv = c.getLevel();
		if (c.lastCheckedLevel == lv)
			return;
		c.lastCheckedLevel = lv;
		Player p = getPlayer();
		if (p != null)
		{
			RPGCore.msg(p, "&bYou've leveled your &3" + c.classType.getClassName() + " &bclass up to " + lv + "!");
			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.0F);
		}
		RPGClass.unlockLevelSkills(this);
		RPGCore.playerManager.writeData(this);
	}

	public void learnSkill(RPGSkill skill)
	{
		if (skills.contains(skill.skillName))
			return;
		skills.add(skill.skillName);
		titleQueue.add(new Title("&6 < " + CakeLibrary.getItemName(skill.getSkillItem()) + "&6 >", "&eSkill Learnt", 20, 60, 20));
		if (getPlayer() != null)
		{
			getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.0f);
			new RPGEvents.PlayEffect(Effect.STEP_SOUND, getPlayer(), 20).run();
		}
		updateStats();
		RPGCore.playerManager.writeData(this);
	}

	public void updatePlayerREquips()
	{
		EntityEquipment ee = getPlayer().getEquipment();
		for (int i = 0; i < 5; i++)
		{
			ItemStack is = i == 0 ? ee.getItemInOffHand() : i == 1 ? ee.getHelmet() : i == 2 ? ee.getChestplate() : 
				i == 3 ? ee.getLeggings() : i == 4 ? ee.getBoots() : null;

				String name = CakeLibrary.getItemName(is);

				if (name.equals(lastEquipmentCheck[i]))
					continue;

				updateStats = true;

				lastEquipmentCheck[i] = name;
				boolean isWeapon = i == 0;

				rEquips[i] = new RItem(is);
				rEquips[i].isWeapon = isWeapon;
		}
	}

	public Stats getStats()
	{
		if (lastCalculatedStats != null)
			return lastCalculatedStats;
		updateStats();
		return lastCalculatedStats;
	}

	public void updateStats()
	{
		if (lastCalculatedStats == null)
			lastCalculatedStats = Stats.createStats("", new ItemStack(Material.AIR));
		if (postStatsMultipliers == null)
			postStatsMultipliers = Stats.createStats("", new ItemStack(Material.AIR));

		lastCalculatedStats.attackSpeedMultiplier = 1.0F;
		lastCalculatedStats.bruteDamageAdd = currentClass.getDamageType() == 0 ? getCurrentClass().lastCheckedLevel : 0;
		lastCalculatedStats.cooldownReductionAdd = 0;
		lastCalculatedStats.critChanceAdd = 5;
		lastCalculatedStats.critDamageAdd = 150;
		lastCalculatedStats.damageReductionAdd = 0;
		lastCalculatedStats.magicDamageAdd = currentClass.getDamageType() == 1 ? getCurrentClass().lastCheckedLevel : 0;
		lastCalculatedStats.recoverySpeedAdd = 0;
		lastCalculatedStats.xpMultiplier = 1.0F;
		lastCalculatedStats.totalDamageMultiplier = 1.0F;
		lastCalculatedStats.bossDamageMultiplier = 1.0F;

		postStatsMultipliers.bruteDamageAdd = 0;
		postStatsMultipliers.magicDamageAdd = 0;
		postStatsMultipliers.xpMultiplier = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				addToStats(eq);
		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				addToStats(acc);

		for (Buff b: buffs)
		{
			if (b.buffStats.attackSpeedMultiplier != 0)
				lastCalculatedStats.attackSpeedMultiplier *= b.buffStats.attackSpeedMultiplier;
			lastCalculatedStats.bruteDamageAdd += b.buffStats.bruteDamageAdd;
			if (b.buffStats.bruteDamageMultiplier != 0)
				postStatsMultipliers.bruteDamageAdd += CakeLibrary.convertMultiplierToAddedPercentage(b.buffStats.bruteDamageMultiplier);
			lastCalculatedStats.cooldownReductionAdd += addRemainingPercentage(lastCalculatedStats.cooldownReductionAdd, b.buffStats.cooldownReductionAdd);
			lastCalculatedStats.critChanceAdd += b.buffStats.critChanceAdd;
			lastCalculatedStats.critDamageAdd += b.buffStats.critDamageAdd;
			lastCalculatedStats.damageReductionAdd += addRemainingPercentage(lastCalculatedStats.damageReductionAdd, b.buffStats.damageReductionAdd);
			lastCalculatedStats.magicDamageAdd += b.buffStats.magicDamageAdd;
			if (b.buffStats.magicDamageMultiplier != 0)
				postStatsMultipliers.magicDamageAdd += CakeLibrary.convertMultiplierToAddedPercentage(b.buffStats.magicDamageMultiplier);
			lastCalculatedStats.recoverySpeedAdd += addRemainingPercentage(lastCalculatedStats.recoverySpeedAdd, b.buffStats.recoverySpeedAdd);
			if (b.buffStats.xpMultiplier != 0)
				lastCalculatedStats.xpMultiplier *= b.buffStats.xpMultiplier;
		}

		boolean priestsBlessing = false;

		if (!priestsBlessing && partyID != -1)
		{
			boolean b = false;
			for (RPlayer partyMember: RPGCore.partyManager.getParty(partyID).players)
			{
				if (b)
					break;
				for (String skill: partyMember.skills)
				{
					if (skill.equals(PriestsBlessing.skillName))
						priestsBlessing = true;
					b = true;
					break;
				}
			}
		}

		if (priestsBlessing)
			lastCalculatedStats.xpMultiplier *= PriestsBlessing.xpMultiplier;

		for (String skill: skills)
		{
			//Additions
			if(skill.equals(MagicMastery1.skillName))
				lastCalculatedStats.magicDamageAdd += MagicMastery1.magicDamageAdd;
			if(skill.equals(MagicMastery2.skillName))
				lastCalculatedStats.magicDamageAdd += MagicMastery2.magicDamageAdd;
			if(skill.equals(MagicMastery3.skillName))
				lastCalculatedStats.magicDamageAdd += MagicMastery3.magicDamageAdd;

			if(skill.equals(BladeMastery1.skillName))
				lastCalculatedStats.bruteDamageAdd += BladeMastery1.bruteDamageAdd;

			if (skill.equals(IronBody.skillName))
				lastCalculatedStats.damageReductionAdd += addRemainingPercentage(lastCalculatedStats.damageReductionAdd, IronBody.damageReductionAdd);

			if (skill.startsWith("Vigor "))
				lastCalculatedStats.recoverySpeedAdd += addRemainingPercentage(lastCalculatedStats.recoverySpeedAdd, Vigor1.recoverySpeedAdd);

			//Multipliers
			if (skill.equals(BladeMastery1.skillName))
				lastCalculatedStats.attackSpeedMultiplier *= BladeMastery1.attackSpeedMultiplier;

			if(skill.equals(Wisdom.skillName))
				lastCalculatedStats.magicDamageAdd *= Wisdom.magicDamageMultiplier;

			if (skill.equals(PriestsBlessing.skillName))
				priestsBlessing = true;
		}

		lastCalculatedStats.bruteDamageAdd *= CakeLibrary.convertAddedPercentageToMultiplier(postStatsMultipliers.bruteDamageAdd);
		lastCalculatedStats.magicDamageAdd *= CakeLibrary.convertAddedPercentageToMultiplier(postStatsMultipliers.magicDamageAdd);
		lastCalculatedStats.attackSpeedMultiplier *= CakeLibrary.convertAddedPercentageToMultiplier((int) postStatsMultipliers.attackSpeedMultiplier);

		if (lastCalculatedStats.bruteDamageAdd < 0)
			lastCalculatedStats.bruteDamageAdd = 2147483647;
		if (lastCalculatedStats.cooldownReductionAdd < 0 || lastCalculatedStats.cooldownReductionAdd > 100)
			lastCalculatedStats.cooldownReductionAdd = 100;
		if (lastCalculatedStats.critChanceAdd < 0 || lastCalculatedStats.critChanceAdd > 100)
			lastCalculatedStats.critChanceAdd = 100;
		if (lastCalculatedStats.critDamageAdd < 0)
			lastCalculatedStats.critDamageAdd = 2147483647;
		if (lastCalculatedStats.damageReductionAdd < 0 || lastCalculatedStats.damageReductionAdd > 100)
			lastCalculatedStats.damageReductionAdd = 100;
		if (lastCalculatedStats.magicDamageAdd < 0)
			lastCalculatedStats.magicDamageAdd = 2147483647;
		if (lastCalculatedStats.recoverySpeedAdd < 0 || lastCalculatedStats.recoverySpeedAdd > 100)
			lastCalculatedStats.recoverySpeedAdd = 100;

		lastCalculatedStats.attackSpeedMultiplier = 1.0F / lastCalculatedStats.attackSpeedMultiplier;
		recoverMaxTicks = (int) (recoverMaxTicksDefault - (recoverMaxTicksDefault / 100.0F * lastCalculatedStats.recoverySpeedAdd));
	}

	private void addToStats(RItem item)
	{
		if (item.attackSpeed != 0)
			lastCalculatedStats.attackSpeedMultiplier *= item.attackSpeed;
		lastCalculatedStats.bruteDamageAdd += item.bruteDamage + item.addedBruteDamage;
		lastCalculatedStats.cooldownReductionAdd += addRemainingPercentage(lastCalculatedStats.cooldownReductionAdd, item.cooldownReduction);
		lastCalculatedStats.critChanceAdd += item.critChance;
		lastCalculatedStats.critDamageAdd += item.critDamage;
		lastCalculatedStats.damageReductionAdd += addRemainingPercentage(lastCalculatedStats.damageReductionAdd, item.damageReduction);
		lastCalculatedStats.magicDamageAdd += item.magicDamage + item.addedMagicDamage;
		lastCalculatedStats.recoverySpeedAdd += addRemainingPercentage(lastCalculatedStats.recoverySpeedAdd, item.recoverySpeed);
		if (item.xpMultiplier != 0)
			lastCalculatedStats.xpMultiplier *= item.xpMultiplier;

		if (item.bonusStat != null && RPGCore.bonusStatEnabled)
			for (int i = 0; i < item.bonusStat.statLines.size(); i++)
			{
				BonusStatType type = item.bonusStat.statLines.get(i);
				float multiplier = item.bonusStat.statLower.get(i) ? BonusStat.lowerStatMultiplier : 1.0F;
				int value = (int) (type.getMultiplier() * (float) item.bonusStat.tier * multiplier);
				if (type.equals(BonusStatType.BRUTE_DAMAGE))
					lastCalculatedStats.bruteDamageAdd += value;
				else if (type.equals(BonusStatType.CAST_SPEED_PERCENTAGE))
					postStatsMultipliers.attackSpeedMultiplier += value;
				else if (type.equals(BonusStatType.BRUTE_DAMAGE_PERCENTAGE))
					postStatsMultipliers.bruteDamageAdd += value;
				else if (type.equals(BonusStatType.CRIT_CHANCE))
					lastCalculatedStats.critChanceAdd += value;
				else if (type.equals(BonusStatType.CRIT_DAMAGE))
					lastCalculatedStats.critDamageAdd += value;
				else if (type.equals(BonusStatType.MAGIC_DAMAGE))
					lastCalculatedStats.magicDamageAdd += value;
				else if (type.equals(BonusStatType.MAGIC_DAMAGE_PERCENTAGE))
					postStatsMultipliers.magicDamageAdd += value;
				else if (type.equals(BonusStatType.TOTAL_DAMAGE_PERCENTAGE))
					lastCalculatedStats.totalDamageMultiplier *= 
					CakeLibrary.convertAddedPercentageToMultiplier(value);
				else if (type.equals(BonusStatType.BOSS_DAMAGE_PERCENTAGE))
					lastCalculatedStats.bossDamageMultiplier *= 
					CakeLibrary.convertAddedPercentageToMultiplier(value);
			}
	}

	/*
	public int calculateCritChance()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
			{
				equipment += eq.critChance;
			}

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.critChance;

		int additions = 5;

		for (Buff b: buffs)
			additions += b.buffStats.critChanceAdd;

		int total = (int) (equipment + additions);
		return total < 0 ? 2147483647 : total;
	}

	public int calculateCritDamage()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.critDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.critDamage;

		int additions = 150;

		for (Buff b: buffs)
			additions += b.buffStats.critDamageAdd;

		int total = equipment + additions;
		return total < 0 ? 2147483647 : total;
	}

	public int calculateMagicDamage()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.magicDamage + eq.addedMagicDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.magicDamage;

		int additions = 4 + getCurrentClass().lastCheckedLevel;
		float multiplier = 1.0F;

		for (String skill: skills)
		{
			if(skill.equals(Wisdom.skillName))
				multiplier += Wisdom.magicDamagePercentageAdd;
			if(skill.equals(MagicMastery1.skillName))
				additions += MagicMastery1.magicDamageAdd;
			if(skill.equals(MagicMastery2.skillName))
				additions += MagicMastery2.magicDamageAdd;
			if(skill.equals(MagicMastery3.skillName))
				additions += MagicMastery3.magicDamageAdd;
		}

		for (Buff b: buffs)
		{
			if (b.buffStats.magicDamageMultiplier != 0)
				multiplier *= b.buffStats.magicDamageMultiplier;
			additions += b.buffStats.magicDamageAdd;
		}

		int total = (int) ((equipment + additions) * multiplier);
		return total < 0 ? 2147483647 : total;
	}

	public int calculateBruteDamage()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.bruteDamage + eq.addedBruteDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.bruteDamage;

		int additions = 4 + getCurrentClass().lastCheckedLevel;
		float multiplier = 1.0F;

		for (String skill: skills)
		{
			if(skill.equals(BladeMastery.skillName))
				multiplier *= BladeMastery.bruteDamageMultiplier;
		}

		for (Buff b: buffs)
		{
			if (b.buffStats.bruteDamageMultiplier != 0)
				multiplier *= b.buffStats.bruteDamageMultiplier;
			additions += b.buffStats.bruteDamageAdd;
		}
		int total = (int) ((equipment + additions) * multiplier);
		return total < 0 ? 2147483647 : total;
	}

	public int calculateCooldownReduction()
	{
		float percentage = 0;

		for (RItem eq: rEquips)
			if (eq != null && eq.cooldownReduction != 0)
				percentage += addRemainingPercentage(percentage, eq.cooldownReduction);

		for (RItem acc: accessoryInventory.slots)
			if (acc != null && acc.cooldownReduction != 0)
				percentage += addRemainingPercentage(percentage, acc.cooldownReduction);

		for (Buff b: buffs)
			percentage += addRemainingPercentage(percentage, b.buffStats.cooldownReductionAdd);

		int total = (int) Math.min(100, percentage);
		return total < 0 ? 100 : total;
	}

	public int calculateDamageReduction()
	{
		float percentage = 0;

		for (RItem eq: rEquips)
			if (eq != null && eq.damageReduction != 0)
				percentage += addRemainingPercentage(percentage, eq.damageReduction);

		for (RItem acc: accessoryInventory.slots)
			if (acc != null && acc.damageReduction != 0)
				percentage += addRemainingPercentage(percentage, acc.damageReduction);


		for (String skill: skills)
		{
			if (skill.equals(IronBody.skillName))
				percentage += addRemainingPercentage(percentage, IronBody.damageReductionAdd);
		}

		for (Buff b: buffs)
			percentage += addRemainingPercentage(percentage, b.buffStats.damageReductionAdd);

		int total = (int) Math.min(100, percentage);
		return total < 0 ? 100 : total;
	}

	public int calculateRecoverySpeed()
	{
		float percentage = 0;

		for (RItem eq: rEquips)
			if (eq != null && eq.recoverySpeed != 0)
				percentage += addRemainingPercentage(percentage, eq.recoverySpeed);

		for (RItem acc: accessoryInventory.slots)
			if (acc != null && acc.recoverySpeed != 0)
				percentage += addRemainingPercentage(percentage, acc.recoverySpeed);

		for (String skill: skills)
		{
			if (skill.startsWith("Vigor "))
				percentage += addRemainingPercentage(percentage, Vigor1.recoverySpeedAdd);
		}

		for (Buff b: buffs)
			percentage += addRemainingPercentage(percentage, b.buffStats.recoverySpeedAdd);

		int total = (int) Math.min(100, percentage);

		recoverMaxTicks = (int) (recoverMaxTicksDefault - (recoverMaxTicksDefault / 100.0F * total));

		return total < 0 ? 100 : total;
	}

	public float calculateCastDelayMultiplier()
	{
		float sum = 1;

		for (RItem eq: rEquips)
			if (eq != null && eq.attackSpeed != 0)
				sum *= eq.attackSpeed;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null && acc.attackSpeed != 0)
				sum *= acc.attackSpeed;

		float multiplier = 1.0F;

		for (String skill: skills)
		{
			if (skill.equals(BladeMastery.skillName))
				multiplier *= BladeMastery.attackSpeedMultiplier;
		}
		for (Buff b: buffs)
		{
			if (b.buffStats.attackSpeedMultiplier != 0)
				multiplier *= b.buffStats.attackSpeedMultiplier;
		}

		return (sum == 0 ? 1 : (1.0F / sum)) / multiplier;
	}

	public float calculateXPMultiplier()
	{
		float sum = 1;

		for (RItem eq: rEquips)
			if (eq != null && eq.xpMultiplier != 0)
				sum *= eq.xpMultiplier;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null && acc.xpMultiplier != 0)
				sum *= acc.xpMultiplier;

		for (Buff b: buffs)
		{
			if (b.buffStats.xpMultiplier != 0)
				sum *= b.buffStats.xpMultiplier;
		}
		return sum;
	}
	 */

	public static int varyDamage(int damage) //Makes the number random up to a 10% change
	{
		if (damage < 0)
			return 2147483647;
		Random rand = new Random();
		int max = (int) Math.ceil(damage / 10.0F);
		return damage + rand.nextInt((max * 2) + 1) - max;
	}

	public static float addRemainingPercentage(float percentage, float addedPercentage)
	{
		return ((100.0F - percentage) / 100.0F) * addedPercentage;
	}
}
