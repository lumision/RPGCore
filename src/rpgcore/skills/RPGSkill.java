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
			new Armageddon(),
			new Heartspan(),
			new TripleKunai(),
			
			//WARRIOR TIER 1
			new PowerPierce(),
			new IronBody(),
			new Leap(),
			
			//MAGE TIER 1
			new ArcaneBolt(),
			new ArcaneBlast(),
			new WindDrive(),
			new Wisdom(),
			
			//PRIEST TIER 1
			new HolyBolt(),
			new Heal(),
			new Enlightenment(),
			
			//ASSASSIN TIER 1
			new ShadowStab(),
			new Dash(),
			new BladeMastery(),
			
			
			
			//MAGE TIER 2
			new IceBolt(),
			new PoisonBolt(),
			new Accelerate(),
			
			//PRIEST TIER 2
			
			//WARRIOR TIER 2
			new Warcry(),
			
			//ASSASSIN TIER 2
			new Kunai(),
			new LightFeet(),
			
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
