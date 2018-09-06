package rpgcore.skills;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.player.RPlayer;
import rpgcore.skills.effect.ArmageddonE;

public class Armageddon extends RPGSkill
{
	public final static String skillName = "Armageddon";
	public final static int skillTier = 8;
	public final static int castDelay = 100;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 24.8F;
	public Armageddon(RPlayer caster)
	{
		super(skillName, caster, castDelay, damage, classType, skillTier);
	}
	
	public Armageddon()
	{
		super(skillName, null, castDelay, 0, classType, skillTier);
	}
	
	@Override
	public void insantiate(RPlayer rp)
	{
		new Armageddon(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FIREBALL, 1), 
				"&9A&br&fm&9a&bg&fe&9d&bd&fo&9n"),
				"&7Damage/Projectile: " + (int) (damage * 100) + "%",
				"&7Cooldown: 60s",
				"&f",
				"&8&oUnleashes a barrage of arcane",
				"&8&oprojectiles unto the target.",
				"&f",
				"&7Skill Tier: " + CakeLibrary.convertToRoman(skillTier),
				"&7Class: " + classType.getClassName());
	}

	public static double calculateDamage(int level)
	{
		return 5.4D + (level * 2.2D);
	}

	@Override
	public void activate()
	{
		ArmageddonE.newEffect(this);
		super.applyCooldown(60);
	}
}
