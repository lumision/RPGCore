package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class PowerSlash2 extends RPGSkill
{
	public final static String skillName = "Power Slash II";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 20;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static float damage = 2.4F;
	public final static int cooldown = 3;
	public final static int size = 7;
	public PowerSlash2()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, 3);

		Vector direction = player.getPlayer().getLocation().getDirection().normalize();
		Location startPointCenter = player.getPlayer().getEyeLocation();
		int multiplier = 1;
		while (multiplier < 6)
		{
			startPointCenter = startPointCenter.add(direction);
			if (CakeLibrary.getNearbyLivingEntitiesExcludePlayers(startPointCenter, 1.0D).size() > 0)
				break;
			multiplier++;
		}
		
		for (int i = 0; i < 2; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			
			Location horizon = player.getPlayer().getLocation();
			horizon.setYaw(i == 0 ? horizon.getYaw() - 90F : horizon.getYaw() + 75F);
			horizon.setPitch(i == 0 ? 25F : 60F);
			
			if (i == 1)
				startPointCenter = startPointCenter.add(direction);
			
			Vector slashDirection = horizon.getDirection().normalize();
			Location startPoint = startPointCenter.clone().add(slashDirection.clone().multiply(-size));
			
			
			multiplier = 1;
			int delay = 0;
	        player.getPlayer().getWorld().playSound(player.getPlayer().getEyeLocation(), Sound.ENTITY_GHAST_SHOOT, 0.1F, 1.5F);
			while (multiplier < size * 2)
			{
				multiplier++;
				delay = multiplier / 2 + (i * 4);
				Location point = startPoint.clone().add(slashDirection.clone().multiply(multiplier));
				RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, 20), delay);
				if (multiplier % 2 == 0)
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(player), player.getPlayer(), 20), delay);
			}
		}
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.IRON_SWORD, 1), 
				"&cPower Slash II"),
				"&7Damage: " + (int) (damage * 100) + "% x 2 hits",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oUnleash two powerful slashes",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
