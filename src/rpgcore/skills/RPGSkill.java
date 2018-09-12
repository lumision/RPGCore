package rpgcore.skills;

import java.util.Random;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import rpgcore.classes.RPGClass.ClassType;
import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;
import rpgcore.player.RPlayer;

public class RPGSkill 
{
	public RPlayer caster;
	public Player player;
	public double baseDamageMultiplier;
	public int casterDamage;
	public boolean passiveSkill;
	public String skillName;
	public int skillTier;
	public ClassType classType;
	public static Random rand = new Random();
	
	public static final RPGSkill[] skillList = { //ADDSKILL
			
			new ArcaneBarrage(),
			new Armageddon(),
			new Heartspan(),
			new TripleKunai(),
			//new Supernova(),
			//new BlackHole(),
			//new Asteroid(),
			
			//WARRIOR TIER 1
			new PowerPierce(),
			new IronBody(),
			new Leap(),
			
			//MAGE TIER 1
			new ArcaneBolt(),
			new WindDrive(),
			new ArcaneBlast(),
			new ArcaneSpears(),
			new Wisdom(),
			
			//PRIEST TIER 1
			new HolyBolt(),
			new Heal(),
			new Protect(),
			new Bless(),
			
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
			
			//PRIEST TIER 4
			new Enlightenment(),
			
			//MISC
			new Vitality1(),
			new Vitality2(),
			new Vitality3(),
			new Vitality4(),
			new Vitality5(),
			new Vitality6(),
			new Vitality7(),
			new Vitality8(),
			new Vitality9(),
			new Vitality10(),
			
			new CelestialBlessing(),
			
	};
	
	public static final String[] skillTierNames = {
			"",
			"I",
			"II",
			"III",
			"IV",
			"V",
			"VI",
			"VII",
			"VIII",
			"IX",
			"X",
			CakeLibrary.recodeColorCodes("&c&lAlpha"),
			CakeLibrary.recodeColorCodes("&e&lDelta"),
			CakeLibrary.recodeColorCodes("&b&m&lZ&b&leta"),
	};
	
	public static RPGSkill getSkill(String skillName)
	{
		for (RPGSkill skill: skillList)
			if (skill.skillName.equals(skillName))
				return skill;
		return null;
	}
	
	public RPGSkill(String skillName, RPlayer caster, boolean passiveSkill, int castDelay, double baseDamageMultiplier, ClassType classType, int skillTier)
	{
		this.passiveSkill = passiveSkill;
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
	
	public void applyCooldown(float seconds)
	{
		int reduction = caster.calculateCooldownReduction();
		double total = seconds * 20.0F;
		total -= total / 100.0F * reduction;
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
