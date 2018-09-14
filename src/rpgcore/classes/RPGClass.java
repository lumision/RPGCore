package rpgcore.classes;

import java.util.ArrayList;

import rpgcore.player.RPlayer;
import rpgcore.skills.Accelerate;
import rpgcore.skills.ArcaneBeam;
import rpgcore.skills.ArcaneBlast;
import rpgcore.skills.ArcaneBolt;
import rpgcore.skills.ArcaneSpears;
import rpgcore.skills.Heal1;
import rpgcore.skills.HolyBolt;
import rpgcore.skills.IceBolt;
import rpgcore.skills.IronBody;
import rpgcore.skills.Lightning;
import rpgcore.skills.MagicMastery1;
import rpgcore.skills.MagicMastery2;
import rpgcore.skills.PoisonBolt;
import rpgcore.skills.PowerPierce;
import rpgcore.skills.RPGSkill;
import rpgcore.skills.ShadowStab;
import rpgcore.skills.Teleport;
import rpgcore.skills.Vitality1;
import rpgcore.skills.Vitality2;
import rpgcore.skills.WindDrive;
import rpgcore.skills.Wisdom;

public class RPGClass 
{
	public static ArrayList<Integer> xpTable = new ArrayList<Integer>();
	public ClassType classType;
	public int xp;
	public int lastCheckedLevel;
	private boolean unlocked;
	public static enum ClassType
	{
		WARRIOR,
		MAGE,
		PRIEST,
		ASSASSIN,
		ALL;

		/**
		 * @return 0 == brute, 1 == magic
		 */
		public int getDamageType()
		{
			return (this.equals(ClassType.WARRIOR) || this.equals(ClassType.ASSASSIN)) ? 0 : 1;
		}

		public String getClassName()
		{
			String build = "";
			char[] chars = this.toString().toCharArray();
			build += (chars[0] + "").toUpperCase();
			for (int i = 1; i < chars.length; i++)
				build += (chars[i] + "").toLowerCase();
			return build;
		}
	}

	public static void setXPTable()
	{
		xpTable.clear();
		for (int i = 0; i < 100; i++)
			xpTable.add((int) (Math.pow(i, 3) * 20));
	}

	public RPGClass(ClassType classType)
	{
		this.classType = classType;
		this.lastCheckedLevel = getLevel();

	}

	public RPGClass(ClassType classType, int xp)
	{
		this.classType = classType;
		this.xp = xp;
		this.lastCheckedLevel = getLevel();
	}

	public boolean isUnlocked()
	{
		return unlocked;
	}

	public void setUnlocked(boolean b)
	{
		this.unlocked = b;
	}

	public static int getXPRequiredForLevel(int level)
	{
		if (level < 0 || level >= xpTable.size())
			return -1;
		return xpTable.get(level);
	}

	public int getLevel()
	{
		for (int i = xpTable.size() - 1; i > 1; i--)
			if (xp >= xpTable.get(i))
				return i;
		return 1;
	}

	public static void unlockBasicSkills(RPlayer player, ClassType classType)
	{
		switch (classType)
		{
		case WARRIOR:
			player.skills.add(PowerPierce.skillName);
			player.skills.add(IronBody.skillName);
			break;
		case MAGE:
			player.skills.add(ArcaneBolt.skillName);
			player.skills.add(WindDrive.skillName);
			break;
		case PRIEST:
			player.skills.add(HolyBolt.skillName);
			player.skills.add(Heal1.skillName);
			break;
		case ASSASSIN:
			player.skills.add(ShadowStab.skillName);
			break;
		case ALL:
			break;
		}
	}
	
	public static void unlockLevelSkills(RPlayer player)
	{
		int level = player.getCurrentClass().lastCheckedLevel;
		switch (player.currentClass)
		{
		case WARRIOR:
			
			break;
		case MAGE:
			if (level >= 3)
				player.learnSkill(RPGSkill.getSkill(ArcaneBlast.skillName));
			if (level >= 5)
				player.learnSkill(RPGSkill.getSkill(Vitality1.skillName));
			if (level >= 6)
				player.learnSkill(RPGSkill.getSkill(MagicMastery1.skillName));
			if (level >= 7)
				player.learnSkill(RPGSkill.getSkill(ArcaneSpears.skillName));
			if (level >= 8)
				player.learnSkill(RPGSkill.getSkill(IceBolt.skillName));
			
			if (level >= 10)
				player.learnSkill(RPGSkill.getSkill(Teleport.skillName));
			if (level >= 11)
				player.learnSkill(RPGSkill.getSkill(PoisonBolt.skillName));
			if (level >= 12)
				player.learnSkill(RPGSkill.getSkill(Accelerate.skillName));
			if (level >= 13)
				player.learnSkill(RPGSkill.getSkill(Lightning.skillName));
			if (level >= 14)
				player.learnSkill(RPGSkill.getSkill(Wisdom.skillName));
			if (level >= 15)
				player.learnSkill(RPGSkill.getSkill(Vitality2.skillName));
			if (level >= 16)
				player.learnSkill(RPGSkill.getSkill(ArcaneBeam.skillName));

			if (level >= 22)
				player.learnSkill(RPGSkill.getSkill(MagicMastery2.skillName));
			break;
		case PRIEST:
			break;
		case ASSASSIN:
			break;
		case ALL:
			break;
		}
	}
}
