package rpgcore.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import rpgcore.main.CakeLibrary;

public class Title 
{
	public String main;
	public String sub;
	public int fadeIn;
	public int show;
	public int fadeOut;
	public Title(String main, String sub, int fadeIn, int show, int fadeOut)
	{
		this.main = CakeLibrary.recodeColorCodes(main);
		this.sub = CakeLibrary.recodeColorCodes(sub);
		this.fadeIn = fadeIn;
		this.show = show;
		this.fadeOut = fadeOut;
	}
	
	public void sendPlayer(Player player)
	{
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " title {\"text\":\"" + main + "\"}");
		if (sub != "")
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " subtitle {\"text\":\"" + sub + "\"}");
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "title " + player.getName() + " times " + fadeIn + " " + show + " " + fadeOut);
	}
}
