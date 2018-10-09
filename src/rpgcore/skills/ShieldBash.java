package rpgcore.skills;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGEvents;
import rpgcore.main.RPGEvents.Bind;
import rpgcore.player.RPlayer;

public class ShieldBash extends RPGSkill
{
	public final static String skillName = "Shield Bash";
	public final static boolean passiveSkill = false;
	public final static int skillTier = 1;
	public final static int castDelay = 10;
	public final static ClassType classType = ClassType.WARRIOR;
	public final static int cooldown = 4;
	public final static float damage = 1.8F;
	public final static int bindDuration = 3 * 20;
	public ShieldBash()
	{
		super(skillName, passiveSkill, castDelay, damage, classType, skillTier);
	}

	@Override
	public void instantiate(RPlayer player)
	{
		if (!player.getPlayer().isOnGround())
		{
			player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_EGG_THROW, 0.2F, 0.5F);
			return;
		}
		ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
		player.getPlayer().setVelocity(player.getPlayer().getLocation().getDirection().setY(0.1F).normalize().multiply(2.0F));
        player.getPlayer().getWorld().playSound(player.getPlayer().getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.2F, 1.0F);
		for (int i = 0; i < 20; i++)
		{
			if (i % 2 == 0)
				continue;
		RPGEvents.scheduleRunnable(new RPGEvents.AOEDetectionCustom(hit, player.getPlayer(), 0.75D, player.getPlayer(), new Callable<Void>()
				{
					@Override
					public Void call() throws Exception {
						int damage = RPlayer.varyDamage(getUnvariedDamage(player));
						new RPGEvents.ApplyDamage(player.getPlayer(), RPGEvents.customHit, damage).run();
						new RPGEvents.PlayEffect(Effect.STEP_SOUND, RPGEvents.customHit, 20).run();
						Bind.bindTarget(RPGEvents.customHit, bindDuration, true);
						RPGEvents.customHit.setVelocity(
								RPGEvents.customHit.getLocation().subtract(player.getPlayer().getLocation()).toVector().setY(0.2F).normalize()
								);
						return null;
					}
					
				}), i);
		}
		
		super.applyCooldown(player, cooldown);
	}

	@Override
	public ItemStack getSkillItem()
	{
		return CakeLibrary.addLore(CakeLibrary.renameItem(new ItemStack(Material.SHIELD, 1), 
				"&eShield Bash"),
				"&7Damage: " + (int) (damage * 100.0F) + "%",
				"&7Stun Duration: " + (bindDuration / 20) + "s",
				"&7Cooldown: " + cooldown + "s",
				"&f",
				"&8&oCharge forward; damaging and",
				"&8&ostunning anything you hit.",
				"&f",
				"&7Skill Tier: " + RPGSkill.skillTierNames[skillTier],
				"&7Class: " + classType.getClassName());
	}
}
