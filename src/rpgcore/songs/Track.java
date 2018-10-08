package rpgcore.songs;

import java.util.ArrayList;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Track
{
	public String composition;
	public ArrayList<String> components;
	public ArrayList<Integer> timings;
	public Sound note;
	public double spacing;
	public double divisor;
	public int version = 1;
	public int offset = 0;
	public float volume = 0.2F;
	public Track(String composition, Sound note, double spacing, int version)
	{
		this.composition = composition;
		this.note = note;
		this.spacing = spacing;
		this.components = new ArrayList<String>();
		this.timings = new ArrayList<Integer>();
		this.version = version;
		this.volume = 0.2F;
		breakDownIntoComponents();
		applyComponentTimings();
	}

	public void breakDownIntoComponents()
	{
		components.clear();
		if (version == 1)
		{
			char[] chars = composition.toCharArray();
			int lastRepeatIndex = 0;
			for (int i = 0; i < chars.length; i++)
			{
				char c = chars[i];

				if (c == '*') //repeat function
				{
					if (i + 1 >= chars.length)
						continue;
					int repeat = 1;
					try
					{
						repeat = Integer.parseInt(chars[i + 1] + "");
					} catch (Exception e) {
						continue;
					}
					String build = "";
					for (int i1 = lastRepeatIndex; i1 < chars.length; i1++)
					{	
						if (chars[i1] == '*')
						{
							lastRepeatIndex = i1 + 2;
							break;
						}
						build += chars[i1];
					}
					char[] chars1 = build.toCharArray();
					for (int r = 1; r < repeat; r++)
						for (int i1 = 0; i1 < chars1.length; i1++)
						{
							char c1 = chars1[i1];
							if (c1 == ' ')
								continue;
							if (c1 == ',')
							{
								for (int i5 = 0; i5 < 16; i5++)
									components.add(".");
							}
							if (c1 == '.')
							{
								components.add(".");
								continue;
							}
							if (c1 == '1' || c1 == '2' || c1 == '3')
							{
								if (i1 + 1 >= chars1.length)
									continue;
								String note = c1 + "" + //Octave
										chars1[i1 + 1] + //Pitch
										(i1 + 2 >= chars1.length ? "" : chars1[i1 + 2] == '#' ? "#" : ""); //Addition of '#'?
								components.add(note);
								continue;
							}
						}
					i++;
					continue;
				}

				if (c == ' ')
					continue;
				if (c == ',')
				{
					for (int i5 = 0; i5 < 16; i5++)
						components.add(".");
					continue;
				}
				if (c == '.')
				{
					components.add(".");
					continue;
				}
				if (c == '1' || c == '2' || c == '3')
				{
					if (i + 1 >= chars.length)
						continue;
					String note = c + "" + //Octave
							chars[i + 1] + //Pitch
							(i + 2 >= chars.length ? "" : chars[i + 2] == '#' ? "#" : ""); //Addition of '#'?
					components.add(note);
					continue;
				}
			}
		} else if (version == 2) {
			String[] split = composition.split(" ");
			for (int i = 0; i < split.length; i++)
			{
				String item = split[i];
				if (item.length() < 1)
					continue;
				components.add(item);
			}
		}
	}

	public void applyComponentTimings()
	{
		if (version == 2)
		{
			divisor = 12;
			return;
		}
		timings.clear();
		double time = 0.0D;
		for (int i = 0; i < components.size(); i++)
		{
			time += spacing;
			timings.add((int) Math.round(time));
		}
	}

	public void play(Player p)
	{
		RunningTrack rt = new RunningTrack(this, p);
		RSongManager.runningTracks.add(rt);
	}

	public void play(Player p, int offset)
	{
		RunningTrack rt = new RunningTrack(this, p);
		int offsetTicks = 0;
		int i;
		for (i = 0; i < timings.size(); i++)
			if (i >= offset)
			{
				offsetTicks = timings.get(i);
				break;
			}
		rt.tick = offsetTicks;
		rt.next = i;
		//rt.taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(RPGCore.instance, rt, 0L, 0L);
		RSongManager.runningTracks.add(rt);
	}
}
