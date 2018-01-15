package rpgcore.songs;

import java.util.ArrayList;

import org.bukkit.entity.Player;

public class RPGSong 
{
	public String name;
	public ArrayList<Track> tracks;
	public int BPM;
	public int loop;
	public RPGSong(String name, ArrayList<Track> tracks, int BPM)
	{
		this.name = name;
		this.tracks = tracks;
		this.BPM = BPM;
	}

	public static double getOneFourthSpacingInTicks(int BPM)
	{
		//120BPM = 120 1/1Ticks per minute
		//       = 2 1/1Ticks per second
		//       = 8 1/4Ticks per second
		//       = 0.4 1/4Ticks per MCTick
		//       = 1/0.4 = 2.5 Spacing in between each 1/4Tick in a second
		return 1.0D / (BPM / 60.0D * 4.0D / 250.0D);
	}
	
	public double getOneFourthSpacingInTicks()
	{
		return getOneFourthSpacingInTicks(BPM);
	}
	
	public void play(Player p)
	{
		for (Track track: tracks)
			track.play(p);
	}
	
	public void play(Player p, int offset)
	{
		for (Track track: tracks)
			track.play(p, offset);
	}
}
