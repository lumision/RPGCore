package rpgcore.entities.mobs;

import org.bukkit.entity.Monster;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class TricksterSpider extends RPGMonster
{
	public static double maxHealth = 840.0D;
	public static String name = "§aTrickster Spider §7Lv. 29";

	public TricksterSpider(Monster entity)
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
		
		double distanceSq = target.getLocation().distanceSquared(entity.getLocation());

		int i = rand.nextInt(10) + 1;
		if (i <= 5 && distanceSq <= 25)
			castShadowStab(4, 12);
		else if (i <= 8 && distanceSq > 25)
			castDash(6);
		else
			castKunai(7, 20);
		return false;
	}
}
