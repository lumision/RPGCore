package rpgcore.sideclasses;

import java.util.ArrayList;

import rpgcore.classes.RPGClass;
import rpgcore.main.CakeLibrary;

public class RPGSideClass 
{
	public static ArrayList<Integer> xpTable = new ArrayList<Integer>();
	public int xp;
	public int lastCheckedLevel;
	public SideClassType sideClassType;
	public static enum SideClassType
	{
		ALCHEMIST("&d"), CRAFTER("&a"), PROSPECTOR("&c"), ENCHANTER("&e");
		
		private String colorCode;
		private SideClassType(String colorCode)
		{
			this.colorCode = CakeLibrary.recodeColorCodes(colorCode);
		}
		
		public String getColorCode()
		{
			return colorCode;
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
	
	public RPGSideClass(SideClassType classType, int xp)
	{
		this.sideClassType = classType;
		this.xp = xp;
		this.lastCheckedLevel = getLevel();
	}
	
	public int getLevel()
	{
		for (int i = RPGClass.xpTable.size() - 1; i > 1; i--)
			if (xp >= RPGClass.xpTable.get(i))
				return i;
		return 1;
	}
	
	public static void setXPTable()
	{
		xpTable.clear();
		for (int i = 1; i < 100; i++)
			xpTable.add((int) (Math.pow((i - 1), 2)));
	}
}
