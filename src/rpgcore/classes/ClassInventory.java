package rpgcore.classes;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skillinventory.SkillInventory;

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
				inv.setItem(1, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7Tier: " + c.classType.getTier(),
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
				inv.setItem(3, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7Tier: " + c.classType.getTier(),
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
				inv.setItem(5, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&e&ki!i!i!i!i!&7     ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Priest class utilizes buffs",
						color + "and heals to aid party members",
						color + "whilst giving opponents debuffs.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.THIEF))
			{
				String color = "&a";
				inv.setItem(7, CakeLibrary.addLore(CakeLibrary.renameItem(SkillInventory.getClassIcon(c.classType), "&fClass: " + color + c.classType.getClassName()),
						"&7Tier: " + c.classType.getTier(),
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
	
	public static Inventory getClassInventory(RPlayer rp)
	{
		//Each bar has 10 "slots
		//"i!" formatted with "&k" = 1 slot
		//A spacebar is also 1 slot
		
		//tier 1 = 10 slots
		//tier 2 = 12 slots
		//tier 3 = 14 slots
		Inventory inv = Bukkit.createInventory(null, 27, CakeLibrary.recodeColorCodes("&1Class Selection"));
		for (RPGClass c: rp.classes)
		{
			if (c.classType.equals(ClassType.WARRIOR))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(19, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&e&ki!i!i!i!&7      ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!&7    ]",
						color + "The Warrior class prides on being",
						color + "able to receive direct attacks",
						color + "and being able to survive whilst",
						color + "simultaneously dealing damage.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.KNIGHT))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(10, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&e&ki!i!i!i!i!&7     ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						color + "The Knight advancement moves",
						color + "towardsall-rounded stats, and",
						color + "includes party-defending skills.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.HERO))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(1, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&2&ki!i!i!i!i!i!i!i!&7  ]",
						color + "The Hero advancement moves",
						color + "towards buffs and ultimates.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.PALADIN))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(9, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!&7    ]",
						color + "The Paladin advancement moves",
						color + "purely towards DPS and focuses",
						color + "on a variety of attacks.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.ODIN))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(0, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&2&ki!i!i!i!i!i!i!i!&7  ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!&7    ]",
						color + "The Odin advancement moves",
						color + "towards more DPS and ultimates.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.MAGE))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(25, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&e&ki!i!i!i!&7      ]",
						color + "The Mage class focuses on",
						color + "long-range attacks that deal",
						color + "high damage.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.SORCERER))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(17, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!i!&7  ]",
						"&7DEF: &7[&e&ki!i!i!i!&7      ]",
						color + "The Sorcerer advancement",
						color + "moves purely towards DPS",
						color + "with elemental spells.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.ARCHMAGE))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(8, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&2&ki!i!i!i!i!i!i!i!i!&7 ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Archmage advancement",
						color + "includes ultimates and",
						color + "a variety of spells.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.SHAMAN))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(16, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Shaman advancement moves",
						color + "towards all-rounded stats, and",
						color + "focuses on dark spells.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.THAUMATURGE))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(7, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!i!&7  ]",
						"&7DEF: &7[&e&ki!i!i!i!i!i!&7    ]",
						color + "The Thaumaturge advancement",
						color + "includes some buffs and",
						color + "a variety of spells.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.PRIEST))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(23, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&e&ki!i!i!i!i!&7     ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Priest class utilizes buffs",
						color + "and heals to aid party members",
						color + "whilst giving opponents debuffs.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.FRIAR))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(14, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&e&ki!i!i!i!i!i!&7    ]",
						"&7DEF: &7[&e&ki!i!i!i!i!i!&7    ]",
						color + "The Friar advancement moves",
						color + "towards more party support.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.BISHOP))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(5, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						"&7DEF: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						color + "The Bishop advancement moves",
						color + "towards ultimates and more",
						color + "support.",
						"&c",
						"&7Level: " + c.getLevel()));
			}

			if (c.classType.equals(ClassType.THIEF))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(21, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!&7   ]",
						"&7DEF: &7[&c&ki!i!i!&7       ]",
						color + "The Thief class focuses on skill",
						color + "combinations and evasion",
						color + "techniques to hit and run.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.ASSASSIN))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(12, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&a&ki!i!i!i!i!i!i!i!&7  ]",
						"&7DEF: &7[&e&ki!i!i!i!&7      ]",
						color + "The Assassin advancement moves",
						color + "towards more dodging abilities",
						color + "and overall swiftness",
						"&c",
						"&7Level: " + c.getLevel()));
			}
			if (c.classType.equals(ClassType.DUALIST))
			{
				String color = SkillInventory.getClassColor(c.classType);
				inv.setItem(3, CakeLibrary.addLore(SkillInventory.getClassIcon(c.classType),
						"&7Tier: " + c.classType.getTier(),
						"&7DMG: &7[&2&ki!i!i!i!i!i!i!i!i!&7 ]",
						"&7DEF: &7[&e&ki!i!i!i!i!&7     ]",
						color + "The Dualist advancement moves",
						color + "towards ultimates and buffs.",
						"&c",
						"&7Level: " + c.getLevel()));
			}
		}
		return inv;
	}
}
