package rpgcore.skills;

import java.util.ArrayList;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class ArcaneSpears extends RPGSkill
{
	public final static String skillName = "Arcane Spears";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 0.7F;
	public final static int hits = 4;
	public final static int cooldown = 4;
	public ArcaneSpears(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new ArcaneSpears(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ARROW, 1), 
				"&dArcane Spears"),
				"&7Damage: " + (int) (damage * 100.0F) + "% x " + hits + " Hits",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oRain arcane spears on",
				"&8&othe target area.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		super.applyCooldown(cooldown);
		Location target = player.getTargetBlock(CakeLibrary.getPassableBlocks(), 16).getLocation();
		
		Vector direction = player.getLocation().getDirection().normalize();
		int check = 0;
		boolean b = false;
		while (check < 16)
		{
			check++;
			Location point1 = player.getEyeLocation().add(direction.clone().multiply(check));
			ArrayList<LivingEntity> nearby = CakeLibrary.getNearbyLivingEntities(point1, 1.0F);
			for (LivingEntity n: nearby)
				if (!(n instanceof Player))	
				{
					b = true;
					target = n.getEyeLocation();
					break;
				}
			if (b)
				break;
		}

        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 0.3F, 0.5F);
		for (int i = 0; i < hits; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			Location start = target.clone().add
					(3 - rand.nextInt(7), 5 + rand.nextInt(5), 3 - rand.nextInt(7));
			Vector vector = target.clone().subtract(start).toVector().normalize().multiply(0.75F);
			
			int multiplier = 0;
			int length = (int) start.distance(target) * 2;
			int delay = 0;
	        
			while (multiplier < length)
			{
				multiplier++;
				delay = multiplier / 2;
				Location point = start.clone().add(vector.clone().multiply(multiplier));
				if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), delay);
					break;
				}
				RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0, 1), delay);
				RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.05F, 1.25F), delay);
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.0D, getUnvariedDamage(), player, 20), delay);
			}
		}
	}
}
