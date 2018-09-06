package rpgcore.skills;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;

public class ArcaneBlast extends RPGSkill
{
	public final static String skillName = "Arcane Blast";
	public final static int skillTier = 4;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public ArcaneBlast(RPlayer caster)
	{
		super(skillName, caster, castDelay, calculateDamage(10), classType, skillTier);
	}
	
	public ArcaneBlast()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneBlast(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&fArcane Blast"),
				"&7Damage: " + (int) (calculateDamage(10) * 100.0D) + "%",
				"&7Radius: " + calculateRadius(10) + " blocks",
				"&7Cooldown: 2s",
				"&f",
				"&8&oBlasts a plane of arcane energy",
				"&8&ounto the target area.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}
	
	public static double calculateDamage(int level)
	{
		return 1.0D + (level / 5.0D);
	}
	
	public static int calculateRadius(int level)
	{
		return (int) (2.0D + (level / 5.0D));
	}
	
	@Override
	public void activate()
	{
		super.applyCooldown(2.0D);

		Location target = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 16).getLocation();
		
	}
}
