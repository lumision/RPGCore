package rpgcore.quest.portion;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class QuestPortionCollect
{
	public ItemStack toCollect;
	public boolean checkName;
	public boolean checkId;
	public ArrayList<String> requiredLoreLines;

	public QuestPortionCollect(ItemStack toCollect)
	{
		this.toCollect = toCollect;
	}
	
	public QuestPortionCollect(ItemStack toCollect, boolean checkName, boolean checkId, ArrayList<String> requiredLoreLines)
	{
		this.toCollect = toCollect;
		this.checkName = checkName;
		this.checkId = checkId;
		this.requiredLoreLines = requiredLoreLines;
	}
	
	public int calculateAmountCollected(Player player)
	{
		return 0;
	}
}
