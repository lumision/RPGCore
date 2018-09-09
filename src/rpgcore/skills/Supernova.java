package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class Supernova extends RPGSkill
{
	public final static String skillName = "Supernova";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 12;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 9.74F;
	public final static int radius = 16;
	public Supernova(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}
	
	public Supernova()
	{
		super(skillName, null, passiveSkill, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Supernova(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&bS&eu&fp&be&er&fn&bo&ev&fa"),
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
