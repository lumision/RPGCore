package rpgcore.skills;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class RPGSkill 
{
	public RPlayer caster;
	public Player player;
	public double baseDamageMultiplier;
	public int casterDamage;
	public 	String skillName;
	
	public static RPGSkill[] skillList = { //ADDSKILL (not needed for passives)
			new ArcaneBarrage(),
			new ArcaneBlast(),
			new ArcaneBolt(),
			new Dash(),
			new Enlightenment(),
			new Heal(),
			new HolyBolt(),
			new Kunai(),
			new PowerPierce(),
			new Propulsion(),
			new ShadowStab(),
			new Heartspan(),
			new TripleKunai(),
			new Armageddon(),
	};
	
	public RPGSkill(String skillName, RPlayer caster, int castDelay, double baseDamageMultiplier, ClassType classType)
	{
		this.skillName = skillName;
		if (caster == null)
			return;
		this.caster = caster;
		this.player = caster.getPlayer();
		this.baseDamageMultiplier = baseDamageMultiplier;
		this.casterDamage = caster.getDamageOfClass();
		if (this.player == null)
			return;
		if (this.player.getPlayer() == null)
			return;
		if (!caster.currentClass.getAdvancementTree().contains(classType))
			return;
		if (caster.getSkillLevel(skillName) < 1)
			return;
		for (int i = 0; i < caster.cooldowns.size(); i++)
		{
			String cd = caster.cooldowns.get(i);
			if (cd.equalsIgnoreCase(skillName))
			{
				RPGCore.msg(player, "Cooldown time left: &4" + caster.cooldownValues.get(i) / 20.0D + "s");
				return;
			}
		}
		caster.lastSkill = skillName;
		caster.castDelay = (int) (castDelay * caster.calculateCastDelayMultiplier());
		activate();
	}
	
	public ItemStack instanceGetSkillItem(RPlayer player)
	{
		return null;
	}
	
	public void applyCooldown(double seconds)
	{
		double reductionPercentage = caster.calculateCooldownReduction();
		double total = seconds * 20.0D;
		total -= (total * reductionPercentage / 100.0D);
		if (total <= 0)
			return;
		caster.cooldowns.add(skillName);
		caster.cooldownValues.add((int) total);
	}
	
	public int getUnvariedDamage()
	{
		return (int) (casterDamage * baseDamageMultiplier);
	}
	
	public void activate(){}
	
	public void insantiate(RPlayer rp) {}
}
