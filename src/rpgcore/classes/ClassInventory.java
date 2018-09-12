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
						color + "The Warrior class prides on being",
						color + "able to receive direct attacks",
						color + "and being able to survive whilst",
						color + "simultaneously dealing damage.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.MAGE))
			{
				String color = "&b";
				inv.setItem(3, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&a&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&e&ki!i!i!i!&7      ]",
						color + "The Mage class focuses on",
						color + "long-range attacks that deal",
						color + "high damage.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.PRIEST))
			{
				String color = "&e";
				inv.setItem(5, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&e&ki!i!i!i!i!&7     ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Priest class utilizes buffs",
						color + "and heals to aid party members",
						color + "whilst giving opponents debuffs.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.ASSASSIN))
			{
				String color = "&a";
				inv.setItem(7, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory2.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						"&7DEF: &7[&c&ki!i!i!&7       ]",
						color + "The Thief class focuses on skill",
						color + "combinations and evasion",
						color + "techniques to hit and run.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
		}
		return inv;
	}
}
