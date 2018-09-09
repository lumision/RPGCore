package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class BlackHole extends RPGSkill
{
	public final static String skillName = "Black Hole";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 13;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 12.86F;
	public final static int radius = 16;
	public BlackHole(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public BlackHole()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new BlackHole(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&8B&7l&fa&8c&7k &fH&8o&7l&fe"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: 60s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(60);
	}
}
