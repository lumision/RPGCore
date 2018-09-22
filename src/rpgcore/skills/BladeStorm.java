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

public class BladeStorm extends RPGSkill
{
	public final static String skillName = "Blade Storm";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.ASSASSIN;
	public final static float damage = 1.2F;
	public final static int hits = 16;
	public final static int cooldown = 6;
	public BladeStorm(RPlayer caster)
	{
		super(skillName, caster, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void insantiate(RPlayer rp)
	{
		new BladeStorm(rp);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.GHAST_TEAR), 
				"&4Blade Storm"),
				"&7Damage: " + (int) (damage * 100.0F) + "% x " + hits + " Hits",
				"&7Radius: 2 blocks",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oSummon a set of blades",
				"&8&oto fall around you.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
	
	public void activate()
	{
		super.applyCooldown(cooldown);
		Location target = player.getLocation();
		int delay = 0;
        player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_CREEPER_DEATH, 1.0F, 0.7F);
		for (int i = 0; i < hits; i++)
		{
			ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
			Vector offset = new Vector(2 - rand.nextInt(5), 8 + rand.nextInt(3), 2 - rand.nextInt(5));
			Location start = target.clone().add(offset);
			Vector vector = new Vector(0, -1, 0).normalize().multiply(1.0F);
			
			int multiplier = 0;
	        
			while (multiplier < 16)
			{
				multiplier++;
				delay = multiplier + i;
				Location point = start.clone().add(vector.clone().multiply(multiplier));
				if (!CakeLibrary.getPassableBlocks().contains(point.getBlock().getType()))
				{
					RPGEvents.scheduleRunnable(new RPGEvents.PlaySoundEffect(point, Sound.BLOCK_GLASS_BREAK, 0.1F, 1.25F), delay);
					RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, point, point.getBlock().getTypeId()), delay);
					break;
				}
				RPGEvents.scheduleRunnable(new RPGEvents.FireworkTrail(point, 0.1F, 3), delay);
				RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionAttackWithBlockBreakEffect(hit, point, 1.25D, getUnvariedDamage(), player, 20), delay);
			}
		}
	}
}
