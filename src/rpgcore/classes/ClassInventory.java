package rpgcore.classes;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory2.SkillInventory2;

public class ClassInventory 
{
	public static Inventory getClassInventory1(RPlayer rp)
	{
		//Each bar has 10 "slots
		//"i!" formatted with "&k" = 1 slot
		//A spacebar is also 1 slot
		
		//tier 1 = 10 slots
		//tier 2 = 12 slots
		//tier 3 = 14 slots
		Inventory inv = Bukkit.createInventory(null, 9, CakeLibrary.recodeColorCodes("&1Class Selection"));
		for (RPGClass c: rp.classes)
		{
			if (c.classType.equals(ClassType.WARRIOR))
			{
				String color = "&c";
				inv.setItem(1, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&e&ki!i!i!i!&7      ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!&7    ]",
						color + "The Warrior class is able to last",
						color + "the longest in battle, with its",
						color + "high damage reduction and recovery.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.MAGE))
			{
				String color = "&b";
				inv.setItem(3, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&a&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&e&ki!i!i!i!&7      ]",
						color + "The Mage class performs long-range",
						color + "attacks that deal high damage;",
						color + "being the core damager of a team.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.PRIEST))
			{
				String color = "&e";
				inv.setItem(5, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&e&ki!i!i!i!i!&7     ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Priest class utilizes unique",
						color + "buffs and heals to aid party members'",
						color + "supporting a team's survivability.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.ASSASSIN))
			{
				String color = "&a";
				inv.setItem(7, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						"&7DEF: &7[&c&ki!i!i!&7       ]",
						color + "The Assassin class focuses on skill",
						color + "combinations and evasion techniques",
						color + "to hit critically and run.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
		}
		return inv;
	}
}
