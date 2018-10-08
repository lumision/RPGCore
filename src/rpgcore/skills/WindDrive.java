package rpgcore.skills;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class WindDrive extends RPGSkill
{
	public final static String skillName = "Wind Drive";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static int radius = 5;
	public final static int cooldown = 8;
	public WindDrive(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, 0, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer rp)
	{
		for (RPGSkill skill: rp.skillCasts)
			if (skill.skillName.equals(skillName))
			{
				skill.casterDamage = rp.getDamageOfClass();
				skill.caster.lastSkill = skillName;
				skill.caster.castDelays.put(skillName, (int) (castDelay * skill.caster.getStats().attackSpeedMultiplier));
				skill.caster.globalCastDelay = 1;
				skill.activate();
				return;
			}
		rp.skillCasts.add(new WindDrive(rp));
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.FEATHER, 1), 
				"&fWind Drive"),
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oKnocks back all monsters",
				"&8&owithin a set radius.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}

	@Override
	public void activate()
	{
		super.applyCooldown(cooldown);
		Location origin = player.getLocation();

		new RPGEvents.PlayExplosionEffect(player.getLocation()).run();
		
		for (LivingEntity e: CakeLibrary.getNearbyLivingEntities(player.getLocation(), radius))
		{
			if (e instanceof Player)
				continue;
			Location end = e.getLocation();
			Vector direction = new Vector(end.getX() - origin.getX(), 0.5D, end.getZ() - origin.getZ()).normalize().multiply(2.0D);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 20), 0);
			e.setVelocity(direction);
		}
	}
}
