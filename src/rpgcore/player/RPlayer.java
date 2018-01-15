package rpgcore.player;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.classes.RPGClass;
import rpgcore.classes.RPGClass.ClassType;
import rpgcore.external.Title;
import rpgcore.item.RItem;
import rpgcore.main.CakeAPI;
import rpgcore.main.RPGCore;
import rpgcore.skills.Buff;
import rpgcore.skills.LightFeet;

public class RPlayer 
{
	private String playerName;
	public ArrayList<RPGClass> classes;
	public ClassType currentClass;
	public int castDelay;
	public String lastSkill;
	public ArrayList<String> skills;
	public ArrayList<Integer> skillLevels;
	public ArrayList<String> cooldowns;
	public ArrayList<Integer> cooldownValues;
	public ArrayList<String> instantCast;
	public ArrayList<Title> titleQueue;
	public ArrayList<Buff> buffs;
	public RItem[] rEquips = new RItem[5];
	public int partyID;
	public int recoverTicks;
	public boolean checkLevel;

	public int sneakTicks;
	public int heartspanTicks;

	public RPlayer(String playerName)
	{
		this.playerName = playerName;
		this.classes = new ArrayList<RPGClass>();
		for (ClassType ct: ClassType.values())
		{
			RPGClass c = new RPGClass(ct);
			c.skillPoints = 3;
			classes.add(c);
		}
		this.skills = new ArrayList<String>();
		this.skillLevels = new ArrayList<Integer>();
		this.cooldowns = new ArrayList<String>();
		this.cooldownValues = new ArrayList<Integer>();
		this.instantCast = new ArrayList<String>();
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.currentClass = ClassType.MAGE;
		this.lastSkill = "";
		this.castDelay = 0;
		this.partyID = -1;
	}
	public RPlayer(String playerName, ArrayList<RPGClass> classes, ClassType currentClass, ArrayList<String> skills, ArrayList<Integer> skillLevels)
	{
		this.playerName = playerName;
		this.classes = classes;
		this.skills = skills;
		this.skillLevels = skillLevels;
		this.currentClass = currentClass;
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
			c.skillPoints = 3;
			classes.add(c);
		}
		this.cooldowns = new ArrayList<String>();
		this.cooldownValues = new ArrayList<Integer>();
		this.instantCast = new ArrayList<String>();
		this.buffs = new ArrayList<Buff>();
		this.titleQueue = new ArrayList<Title>();
		this.lastSkill = "";
		this.castDelay = 0;
		this.partyID = -1;
	}

	public int getDamageOfClass()
	{
		return (currentClass.getDamageType() == 0) ? calculateBruteDamage() : calculateMagicDamage();
	}

	public void addInstantCast(String skill)
	{
		if (!instantCast.contains(skill))
			instantCast.add(skill);
		removeCooldown(skill);
	}

	public RPGClass getCurrentClass()
	{
		for (RPGClass rc: classes)
			if (rc.classType.equals(currentClass))
				return rc;
		return null;
	}

	public void removeCooldown(String skill)
	{
		for (int i = 0; i < cooldowns.size(); i++)
			if (cooldowns.get(i).equalsIgnoreCase(skill))
			{
				cooldowns.remove(i);
				cooldownValues.remove(i);
				return;
			}
	}

	public void tick10()
	{

		Player p = getPlayer();
		if (p == null)
			return;
		if (currentClass.getTier1Class().equals(ClassType.THIEF))
		{
			int lightFeet = getSkillLevel("Light Feet");
			if (lightFeet > 0) //Light Feet functionality
			{
				int speed = CakeAPI.getPotionEffectAmplifier(p, PotionEffectType.SPEED);
				int jump = CakeAPI.getPotionEffectAmplifier(p, PotionEffectType.JUMP);
				int speed1 = LightFeet.getSwiftnessLevel(lightFeet);
				int jump1 = LightFeet.getJumpLevel(lightFeet);
				if (speed <= speed1)
					p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 19, LightFeet.getSwiftnessLevel(lightFeet)), true);
				if (jump <= jump1)
					p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 19, LightFeet.getJumpLevel(lightFeet)), true);
			}
		}
	}

	public void tick()
	{
		Player p = getPlayer();
		if (p == null)
			return;
		if (castDelay > 0)
			castDelay--;
		if (currentClass.getTier1Class().equals(ClassType.THIEF))
		{
			if (heartspanTicks > 0) //Heartspan functionality
			{
				heartspanTicks--;
				if (heartspanTicks <= 0)
					p.sendMessage(CakeAPI.recodeColorCodes("&c**HEARTSPAN DEACTIVATED**"));
			}
		}
		ArrayList<Integer> cooldownRemove = new ArrayList<Integer>();
		for (int i = 0; i < cooldowns.size(); i++)
		{
			int v = cooldownValues.get(i);
			v--;
			if (v < 1)
			{
				cooldownRemove.add(i);
				continue;
			}
			cooldownValues.set(i, v);
		}
		for (int i: cooldownRemove)
		{
			cooldowns.remove(i);
			cooldownValues.remove(i);
		}

		ArrayList<Integer> buffRemove = new ArrayList<Integer>();
		for (int i = 0; i < buffs.size(); i++)
		{
			Buff b = buffs.get(i);
			b.tick();
			if (b.duration < 1)
			{
				buffRemove.add(i);
				b.removeBuff(p);
			}
		}
		for (int i: buffRemove)
			buffs.remove(i);
		if (checkLevel)
		{
			updateLevel();
			checkLevel = false;
		}
		int health = (int) p.getHealth();
		int maxHealth = (int) p.getMaxHealth();
		if (health < maxHealth && !p.isDead())
		{
			recoverTicks++;
			if (recoverTicks >= 20)
			{
				recoverTicks = 0;
				p.setHealth(health + 1);
			}
		} else
			recoverTicks = 0;
		try
		{
			if (titleQueue.size() > 0)
			{
				Title t = titleQueue.get(0);
				titleQueue.remove(0);
				t.sendPlayer(p);
			}
		} catch (Exception e) {}
	}

	public void removeBuff(String buffName)
	{
		int remove = -1;
		for (int i = 0; i < buffs.size(); i++)
			if (buffs.get(i).buffName.equalsIgnoreCase(buffName))
				remove = i;
		if (remove != -1)
			buffs.remove(remove);
	}

	public boolean isBuffActive(String buffName)
	{
		for (Buff b: buffs)
			if (b.buffName.equalsIgnoreCase(buffName))
				return true;
		return false;
	}

	public String getPlayerName()
	{
		return playerName;
	}

	public Player getPlayer()
	{
		return Bukkit.getPlayer(playerName);
	}

	public void addXP(int xp)
	{
		getCurrentClass().xp += xp;
		Player p = getPlayer();
		if (p != null)
			titleQueue.add(new Title("", CakeAPI.recodeColorCodes("&7+" + xp + "XP (" + getPercentageToNextLevel() + "%)"), 4, 0, 16));
		checkLevel = true;
	}

	public int getPercentageToNextLevel()
	{
		RPGClass c = getCurrentClass();
		double xpRaw = c.xp - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
		double nextXpRaw = RPGClass.getXPRequiredForLevel(c.lastCheckedLevel + 1) - RPGClass.getXPRequiredForLevel(c.lastCheckedLevel);
		int percentage = (int) ((xpRaw / nextXpRaw) * 100.0D);
		return percentage;
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
		int difference = lv - c.lastCheckedLevel;
		c.lastCheckedLevel = lv;
		Player p = getPlayer();
		if (p != null)
		{
			c.skillPoints += (2 * difference);
			RPGCore.msg(p, "&bYou've leveled up to " + lv + "!");
			if (lv <= 3)
				RPGCore.msg(p, "&bUse &3/skills &bto spend your skill points!");
			p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4F, 1.0F);
		}
	}

	public int getSkillLevel(String skill)
	{
		for (int i = 0; i < skills.size(); i++)
			if (skills.get(i).equalsIgnoreCase(skill))
				return skillLevels.get(i);
		return 0;
	}

	public void updatePlayerREquips()
	{
		EntityEquipment ee = getPlayer().getEquipment();
		for (int i = 0; i <= 4; i++)
		{
			ItemStack is = i == 0 ? ee.getItemInOffHand() : i == 1 ? ee.getHelmet() : i == 2 ? ee.getChestplate() : 
				i == 3 ? ee.getLeggings() : i == 4 ? ee.getBoots() : null;
				boolean isWeapon = i == 0;

				if (rEquips[i] == null)
				{
					rEquips[i] = new RItem(is);
					rEquips[i].isWeapon = isWeapon;
				} else 
				{
					rEquips[i].cleanItemStats();
					rEquips[i].setItemStats(is);
					rEquips[i].isWeapon = isWeapon;
				}
		}
	}

	public int calculateCritChance()
	{
		int equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.critChance;
		int additions = 5;
		double multiplier = 1.0D;
		return (int) ((equipment + additions) * multiplier);
	}

	public double calculateCritDamageMultiplier()
	{
		int equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.critDamage;
		double additions = 1.5D;
		double multiplier = 1.0D;
		return ((equipment / 100.0D) + additions) * multiplier;
	}

	public int calculateMagicDamage()
	{
		int equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.magicDamage;
		int additions = 1;
		double multiplier = 1.0D;
		for (int i = 0; i < skills.size(); i++)
		{
			if (currentClass.getTier1Class().equals(ClassType.MAGE))
				if(skills.get(i).equalsIgnoreCase("wisdom"))
					additions += skillLevels.get(i);
		}
		for (Buff b: buffs)
		{
			if (b.buffName.equalsIgnoreCase("Enlightenment"))
				multiplier += 0.05D + (b.buffLevel / 50.0D);
		}
		return (int) ((equipment + additions) * multiplier);
	}

	public int calculateBruteDamage()
	{
		int equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.bruteDamage;
		
		int additions = 1;
		if (currentClass.getAdvancementTree().contains(ClassType.THIEF))
			additions += getSkillLevel("Blade Mastery");
		double multiplier = 1.0D;
		for (Buff b: buffs)
		{
			if (b.buffName.equalsIgnoreCase("Enlightenment"))
				multiplier += 0.05D + (b.buffLevel / 50.0D);
		}
		return (int) ((equipment + additions) * multiplier);
	}

	public int calculateCooldownReduction()
	{
		int equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.cooldownReduction;
		ItemStack is = getPlayer().getEquipment().getItemInOffHand();
		int additions = 0;
		if (currentClass.getAdvancementTree().contains(ClassType.THIEF))
			additions += getSkillLevel("Blade Mastery");

		if (CakeAPI.isItemStackNull(is))
			return additions;

		return equipment + additions;
	}

	public double calculateCastDelayMultiplier()
	{
		double equipment = 0;
		for (RItem eq: rEquips)
			equipment += eq.attackSpeed;

		double modifier = 1.0D;
		if (currentClass.getAdvancementTree().contains(ClassType.THIEF))
			modifier -= getSkillLevel("Blade Mastery") / 100.0D;

		return (equipment == 0 ? 1 : (1.0D / equipment)) * modifier;
	}

	public static int varyDamage(int damage) //Makes the number random up to a 10% change
	{
		Random rand = new Random();
		int max = (int) Math.ceil(damage / 10.0D);
		return damage + rand.nextInt((max * 2) + 1) - max;
	}

	public void offsetSkillLevel(String skill, int levels)
	{
		for (int i = 0; i < skills.size(); i++)
			if (skill.equals(skills.get(i)))
			{
				skillLevels.set(i, skillLevels.get(i) + levels);
				return;
			}
		skills.add(skill);
		skillLevels.add(levels);
	}
}
