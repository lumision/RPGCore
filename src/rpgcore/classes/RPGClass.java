package rpgcore.classes;

import java.util.ArrayList;

import rpgcore.player.RPlayer;
import rpgcore.skills.ArcaneBolt;
import rpgcore.skills.Heal;
import rpgcore.skills.HolyBolt;
import rpgcore.skills.IronBody;
import rpgcore.skills.PowerPierce;
import rpgcore.skills.ShadowStab;
import rpgcore.skills.WindDrive;

public class RPGClass 
{
	public static ArrayList<Integer> xpTable = new ArrayList<Integer>();
	public ClassType classType;
	public int xp;
	public int lastCheckedLevel;
	public int skillPoints;
	private boolean unlocked;
	public static enum ClassType
	{
		WARRIOR(null, 1),
		MAGE(null, 1),
		PRIEST(null, 1),
		ASSASSIN(null, 1);

		private ClassType baseClass;
		private int tier;
		private ClassType(ClassType baseClass, int tier)
		{
			this.tier = tier;
			this.baseClass = baseClass;
		}

		/**
		 * @return 0 == brute, 1 == magic
		 */
		public int getDamageType()
		{
			return (getTier1Class().equals(ClassType.WARRIOR) || getTier1Class().equals(ClassType.ASSASSIN)) ? 0 : 1;
		}

		public int getTier()
		{
			return tier;
		}

		public ClassType getBaseClass()
		{
			return baseClass;
		}

		public ClassType getTier1Class()
		{
			ClassType c = baseClass;
			if (c == null)
				return this;
			while (c.getBaseClass() != null)
				c = c.getBaseClass();
			return c;
		}

		public ArrayList<ClassType> getAdvancementTree()
		{
			ArrayList<ClassType> tree = new ArrayList<ClassType>();
			tree.add(this);
			ClassType c = baseClass;
			if (c != null)
			{
				tree.add(c);
				while (c.getBaseClass() != null)
				{
					c = c.getBaseClass();
					tree.add(c);
				}
			}
			return tree;
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
		for (int i = 1; i < 101; i++)
			xpTable.add((int) (Math.pow((i - 1), 2) * 20));
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

	public RPGClass(ClassType classType, int xp, int skillPoints)
	{
		this.classType = classType;
		this.xp = xp;
		this.skillPoints = skillPoints;
		this.lastCheckedLevel = getLevel();
	}

	public boolean isUnlocked()
	{
		if (classType.getTier() == 0)
			return true;
		return unlocked;
	}

	public void setUnlocked(boolean b)
	{
		this.unlocked = b;
	}

	public static int getXPRequiredForLevel(int level)
	{
		if (level < 0 || level > 99)
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
			player.skills.add(Heal.skillName);
			break;
		case ASSASSIN:
			player.skills.add(ShadowStab.skillName);
			break;
		}
	}
}
