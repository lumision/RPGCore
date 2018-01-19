package rpgcore.quest.portion;

public class QuestPortionKill extends QuestPortion
{
	public int amountKilled;
	public String monsterToKill;
	public QuestPortionKill(String monsterToKill)
	{
		this.monsterToKill = monsterToKill;
	}
}
