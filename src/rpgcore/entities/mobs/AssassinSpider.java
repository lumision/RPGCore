package rpgcore.entities.mobs;

import org.bukkit.entity.Monster;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AssassinSpider extends RPGMonster
{
	public static double maxHealth = 150.0D;
	public static String name = "§aAssassin Spider §7Lv. 12";
	
	public static final int dashDelay = 12;

	public static final int shadowStabDamage = 3;
	public static final int shadowStabDelay = 24;

	public AssassinSpider(Monster entity)
	{
		super(entity, false);
		entity.setMaxHealth(maxHealth);
		entity.setHealth(maxHealth);
		entity.setCustomName(name);
		entity.setCustomNameVisible(true);

		entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 4));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));
		entity.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));
	}

	@Override
	public boolean tick()
	{
		super.tick();
		if (isDead())
			return true;
		if (castDelay > 0 || target == null)
			return false;

		int i = rand.nextInt(10) + 1;
		if (i <= 3)
			castDash(12);
		else if (target.getLocation().distanceSquared(entity.getLocation()) < 25)
			castShadowStab(3, 24);
		return false;
	}
}
