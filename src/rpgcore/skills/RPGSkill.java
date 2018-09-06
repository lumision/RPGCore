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
	public String skillName;
	public int skillTier;
	public ClassType classType;
	
	public static RPGSkill[] skillList = { //ADDSKILL
			new ArcaneBarrage(),
			new ArcaneBlast(),
			new ArcaneBolt(),
			new Armageddon(),
			new BladeMastery(),
			new Dash(),
			new Enlightenment(),
			new Heal(),
			new Heartspan(),
			new HolyBolt(),
			new IceBolt(),
			new IronBody(),
			new Kunai(),
			new Leap(),
			new LightFeet(),
			new PoisonBolt(),
			new PowerPierce(),
			new Propulsion(),
			new ShadowStab(),
			new TripleKunai(),
			new Warcry(),
			new Wisdom()
	};
	
	public static RPGSkill getSkill(String skillName)
	{
		for (RPGSkill skill: skillList)
			if (skill.skillName.equals(skillName))
				return skill;
		return null;
	}
	
	public RPGSkill(String skillName, RPlayer caster, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier)
	{
		this.skillName = skillName;
		this.classType = classType;
		this.skillTier = skillTier;
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
		
		/*
		if (!caster.currentClass.getAdvancementTree().contains(classType))
			return;
		*/
		
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
	
	public void activate() {}
	public ItemStack getSkillItem() { return null; }
	
	public void insantiate(RPlayer rp) {}
}
