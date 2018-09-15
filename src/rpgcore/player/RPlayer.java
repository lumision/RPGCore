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
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import rpgcore.areas.Arena;
import rpgcore.areas.ArenaInstance;
import rpgcore.buff.Buff;
import rpgcore.buff.BuffInventory;
import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.external.Title;
import rpgcore.item.RItem;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.main.RPGEvents;
import rpgcore.npc.CustomNPC;
import rpgcore.sideclasses.RPGSideClass;
import rpgcore.sideclasses.RPGSideClass.SideClassType;
import rpgcore.skills.BladeMastery;
import rpgcore.skills.IronBody;
import rpgcore.skills.LightFeet;
import rpgcore.skills.MagicMastery1;
import rpgcore.skills.MagicMastery2;
import rpgcore.skills.MagicMastery3;
import rpgcore.skills.RPGSkill;
import rpgcore.skills.Wisdom;
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
	public int skillbookTierSwitchTicks;
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

	public int sneakTicks;
	public int heartspanTicks;

	public static ItemStack invAccessories = CakeLibrary.renameItem(new ItemStack(Material.TOTEM), "&d&nAccessories");
	public static ItemStack invBuffsStats = CakeLibrary.renameItem(new ItemStack(Material.PAPER), "&c&nBuffs / Stats");
	public static ItemStack invSkills = CakeLibrary.renameItem(new ItemStack(Material.BOOK), "&b&nSkills");

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
		this.castDelays = new HashMap<String, Integer>();
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.currentClass = ClassType.MAGE;
		this.lastSkill = "";
		this.partyID = -1;
		this.arenaInstanceID = -1;
		this.gold = 10;
		this.tokens = 0;
		this.npcFlags = new HashMap<String, String>();
		this.tutorial = new Tutorial(this);
		this.buffInventory = new BuffInventory(this);
		this.accessoryInventory = new AccessoryInventory(this);
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
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.lastSkill = "";
		this.partyID = -1;
		this.arenaInstanceID = -1;
		this.npcFlags = new HashMap<String, String>();
		this.tutorial = new Tutorial(this);
		this.buffInventory = new BuffInventory(this);
		this.accessoryInventory = new AccessoryInventory(this);
		setInvButtons();
	}

	public void setInvButtons()
	{
		if (getPlayer() == null || !RPGCore.inventoryButtons)
			return;
		getPlayer().getInventory().setItem(17, invAccessories.clone());
		getPlayer().getInventory().setItem(26, invBuffsStats.clone());
		getPlayer().getInventory().setItem(35, invSkills.clone());
	}

	public void enterArena(Arena arena)
	{
		leaveArena();
		Player p = getPlayer();
		if (p == null)
			return;
		ArenaInstance ai = null;
		for (Player p1: Bukkit.getOnlinePlayers())
		{
			if (p1.equals(p))
				continue;
			RPlayer rp1 = RPGCore.playerManager.getRPlayer(p1.getUniqueId());
			if (rp1.partyID != partyID)
				continue;
			if (rp1.arenaInstanceID >= 0)
				ai = ArenaInstance.getArenaInstance(arenaInstanceID);
		}
		if (ai == null)
			ai = ArenaInstance.getArenaInstance(arena);
		leftForArenaLocation = p.getLocation();
		p.teleport(ai.getSpawnLocation());
		ai.occupied = true;
		arenaInstanceID = ai.arenaInstanceID;
		arenaEnterLeaveTicks = 20;

		RPGCore.playerManager.writeData(this);
		ArenaInstance.writeArenaInstanceData();
	}

	public void leaveArena()
	{
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

		for (RPlayer rp: RPGCore.playerManager.players)
			if (rp.arenaInstanceID == ai.arenaInstanceID)
				stillOccupied = true;
		ai.occupied = stillOccupied;
		if (!ai.occupied)
		{
			for (Monster m: ai.mobList)
				m.remove();
			ai.mobsSpawned = false;
		}
		p.teleport(ai.arena.exitExternal);
		leftForArenaLocation = null;
		arenaEnterLeaveTicks = 20;

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
		objective.getScore(CakeLibrary.recodeColorCodes("&cDamage: ")).setScore(getDamageOfClass());

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
		return (currentClass.getDamageType() == 0) ? calculateBruteDamage() : calculateMagicDamage();
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
		updatePlayerREquips();
		updateScoreboard = true;
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
			if (skill.startsWith("Vitality"))
				maxHealthAdd += 2;
		getPlayer().setMaxHealth(20 + maxHealthAdd);
	}

	public void tick()
	{
		if (getPlayer() == null)
			return;
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
		if (skillbookTierSwitchTicks > 0)
			skillbookTierSwitchTicks--;
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
		for (String key: cooldowns.keySet())
		{
			int value = cooldowns.get(key) - 1;
			if (value <= 0)
				cooldownRemove.add(key);
			else
				cooldowns.put(key, value);
		}
		for (String remove: cooldownRemove)
		{
			cooldowns.remove(remove);
			titleQueue.add(new Title("&6< "  + CakeLibrary.getItemName(RPGSkill.getSkill(remove).getSkillItem()) + "&6 >", "&eCooldown ended", 
					10, 5, 10));
		}

		ArrayList<Integer> buffRemove = new ArrayList<Integer>();
		for (int i = 0; i < buffs.size(); i++)
		{
			Buff b = buffs.get(i);
			b.tick();
			if (b.duration < 1)
			{
				buffRemove.add(i);
				b.removeBuff(getPlayer());
			}
		}
		for (int i: buffRemove)
			buffs.remove(i);

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
		int health = (int) getPlayer().getHealth();
		int maxHealth = (int) getPlayer().getMaxHealth();
		if (health < maxHealth && !getPlayer().isDead())
		{
			recoverTicks++;
			if (recoverTicks >= 20)
			{
				recoverTicks = 0;
				getPlayer().setHealth(health + 1);
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
		return player;
	}

	public void addXP(int xp)
	{
		getCurrentClass().xp += xp;
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
		float xpRaw = c.xp - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
		float nextXpRaw = RPGClass.getXPRequiredForLevel(c.lastCheckedLevel + 1) - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
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

				lastEquipmentCheck[i] = name;
				boolean isWeapon = i == 0;

				rEquips[i] = new RItem(is);
				rEquips[i].isWeapon = isWeapon;
		}
	}


	public int calculateCritChance()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.critChance;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.critChance;

		int additions = 5;
		float multiplier = 1.0F;

		for (Buff b: buffs)
			additions += b.buffStats.critChanceAdd;

		return (int) ((equipment + additions) * multiplier);
	}

	public double calculateCritDamageMultiplier()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.critDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.critDamage;

		float additions = 1.5F;
		float multiplier = 1.0F;

		for (Buff b: buffs)
			additions += b.buffStats.critDamageAdd / 100.0F;

		return ((equipment / 100.0F) + additions) * multiplier;
	}

	public int calculateMagicDamage()
	{
		int equipment = 0;

		for (RItem eq: rEquips)
			if (eq != null)
				equipment += eq.magicDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.magicDamage;

		int additions = 1;
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
				equipment += eq.bruteDamage;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				equipment += acc.bruteDamage;

		int additions = 1;
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
			if (eq != null)
				percentage += addRemainingPercentage(percentage, eq.cooldownReduction);

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
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
			if (eq != null)
				percentage += addRemainingPercentage(percentage, eq.damageReduction);

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
				percentage += addRemainingPercentage(percentage, acc.damageReduction);

		int additions = 0;

		for (String skill: skills)
		{
			if (skill.equals(IronBody.skillName))
				percentage += addRemainingPercentage(percentage, IronBody.damageReductionAdd);
		}

		for (Buff b: buffs)
			percentage += addRemainingPercentage(percentage, b.buffStats.damageReductionAdd);

		int total = (int) Math.min(100, percentage + additions);
		return total < 0 ? 100 : total;
	}

	public float calculateCastDelayMultiplier()
	{
		float sum = 1;

		for (RItem eq: rEquips)
			if (eq != null && eq.attackSpeed != 0)
				sum *= eq.attackSpeed;

		for (RItem acc: accessoryInventory.slots)
			if (acc != null)
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
