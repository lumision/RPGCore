package rpgcore.skills;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.player.RPlayer;

public class IceField extends RPGSkill
{
	public final static String skillName = "Ice Field";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 2;
	public final static int castDelay = 15;
	public final static ClassType classType = ClassType.MAGE;
	public final static float damage = 4.6F;
	public final static int radius = 8;
	public final static int debuffLevel = 1;
	public final static int debuffLength = 10 * 20;
	public final static int cooldown = 4;
	public IceField()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		super.applyCooldown(player, cooldown);
		Location target = player.getPlayer().getLocation();
		
		int x = -radius;
		int z = 0;
		for (z = -radius; z < radius; z++)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 79), z + radius);
		}
		
		z = -radius;
		for (x = -radius; x < radius; x++)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 79), x + radius);
		}
		
		x = radius;
		for (z = radius; z > -radius; z--)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 79), -z + radius);
		}
		
		z = radius;
		for (x = radius; x > -radius; x--)
		{
			Location l = target.clone().add(x, 1, z);
			RPGEvents.scheduleRunnable(new RPGEvents.PlayEffect(Effect.STEP_SOUND, l, 79), -x + radius);
		}

		new RPGEvents.AOEDetectionCustom(new ArrayList<LivingEntity>(), target, radius, player.getPlayer(), new Callable<Void>()
				{
					@Override
					public Void call() throws Exception {
						LivingEntity e = RPGEvents.customHit;
						int damage = RPlayer.varyDamage(getUnvariedDamage(player));
						new RPGEvents.ApplyDamage(player.getPlayer(), e, damage).run();
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, e, 79).run();
						CakeLibrary.addPotionEffectIfBetterOrEquivalent(e, new PotionEffect(PotionEffectType.SLOW, debuffLength, debuffLevel));
						return null;
					}
				}).run();
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.ICE), 
				"&bIce Field"),
				"&7Damage: " + (int) (damage * 100) + "%",
				"&7Radius: " + radius + " blocks",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&7Debuff:",
				"&7 * Slow " + CakeLibrary.convertToRoman(debuffLevel + 1),
				"&7 * Duration: " + (debuffLength / 20) + "s",
				"&f",
				"&8&oRelease a surge of ice energy,",
				"&8&odamaging and slowing anyone in",
				"&8&othe vicinity.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
