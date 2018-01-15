package rpgcore.songs;

import org.bukkit.entity.Player;

import rpgcore.main.CakeAPI;
import rpgcore.main.RPGCore;

public class RunningTrack 
{
	public Track track;
	public Player player;
	public int tick;
	public int taskID;
	public int next;
	public int lastTimestamp;
	public boolean stopped;
	public RunningTrack(Track track, Player player)
	{
		this.track = track;
		this.player = player;
		this.tick = 0;
		this.next = 0;
	}

	public void run()
	{
		if (player == null)
		{
			stop();
			return;
		}
		if (next >= track.components.size() || !player.isOnline())
		{
			stop();
			return;
		}
		if (track.version == 1)
		{
			if (tick == track.timings.get(next))
			{
				String component = track.components.get(next);
				if (!component.equals("."))
				{
					float pitch = CakeAPI.getPitchFromNote(component, track.offset);
					if (pitch != 0.0F)
						player.playSound(player.getEyeLocation(), track.note, 0.2F, pitch);
				}
				next++;
			}
		} else if (track.version == 2) {
			int highestTimestamp = lastTimestamp;
			for (int i = next; i < track.components.size(); i++)
			{
				String item = track.components.get(i);
				String[] split = item.split("/");
				if (split.length < 2)
					continue;
				String note = split[0];
				int timestamp = (int) (Integer.parseInt(split[1]) / track.divisor);
				if (tick < timestamp)
					break;
				float pitch = CakeAPI.getPitchFromNote(note, track.offset);
				if (timestamp > lastTimestamp)
				{
					next = i;
					highestTimestamp = timestamp;
					if (pitch == 0.0F)
					{
						RPGCore.msgConsole("Unreachable note: " + note + " @" + split[1] + " (" + track.note.toString() + ", " + i + ")");
						continue;
					}
					player.playSound(player.getEyeLocation(), track.note, 0.2F, pitch);
				}
			}
			lastTimestamp = highestTimestamp;
		}
		tick++;
	}

	public void stop()
	{
		stopped = true;
	}
}
