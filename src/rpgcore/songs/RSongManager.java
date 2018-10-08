package rpgcore.songs;

import java.io.File;
import java.util.ArrayList;

import org.bukkit.Sound;

import rpgcore.main.CakeLibrary;
import rpgcore.main.RPGCore;

public class RSongManager
{
	public RPGCore instance;
	public ArrayList<RPGSong> songs;
	public File songsFolder = new File("plugins/RPGCore/songs");
	public static ArrayList<RunningTrack> runningTracks = new ArrayList<RunningTrack>();
	public RSongManager(RPGCore instance)
	{
		this.instance = instance;
		this.songs = new ArrayList<RPGSong>();
		songsFolder.mkdirs();
		readSongs();
	}
	
	public static void cleanRunningTracks()
	{
		ArrayList<RunningTrack> remove = new ArrayList<RunningTrack>();
		for (RunningTrack rt: runningTracks)
			if (rt.stopped)
				remove.add(rt);
		runningTracks.removeAll(remove);
	}

	public RPGSong getSong(String songName)
	{
		for (RPGSong song: songs)
			if (song.name.equalsIgnoreCase(songName))
				return song;
		return null;
	}

	public void readSongs()
	{
		songs.clear();
		Track t;
		RPGSong song;
		for (File file: songsFolder.listFiles())
		{
			if (!file.getName().endsWith(".yml"))
				continue;
			ArrayList<String> lines = CakeLibrary.readFile(file);
			if (lines.size() < 2)
				continue;
			String name = file.getName().substring(0, file.getName().length() - 4);
			ArrayList<Track> tracks = new ArrayList<Track>();
			int BPM = -1;
			int version = 1;
			int offset = 0;
			int loop = 0;
			for (String line: lines)
			{
				String[] args = line.toLowerCase().split(": ")[0].split(", ");
				if (line.startsWith("bpm: "))
				{
					try
					{
						BPM = Integer.parseInt(line.split(": ")[1]);
					} catch (Exception e) {}
					continue;
				}
				if (line.startsWith("loop: "))
				{
					try
					{
						loop = Integer.parseInt(line.split(": ")[1]);
					} catch (Exception e) {}
					continue;
				}
				if (line.startsWith("version: "))
				{
					try
					{
						version = Integer.parseInt(line.split(": ")[1]);
					} catch (Exception e) {}
					continue;
				}
				if (line.startsWith("offset: "))
				{
					try
					{
						offset = Integer.parseInt(line.split(": ")[1]);
					} catch (Exception e) {}
					continue;
				}
				if (BPM == -1)
					break;
				Sound note = null;
				double spacing = RPGSong.getOneFourthSpacingInTicks(BPM);
				if (args[0].startsWith("basedrum"))
					note = Sound.BLOCK_NOTE_BASEDRUM;
				else if (args[0].startsWith("bass"))
					note = Sound.BLOCK_NOTE_BASS;
				else if (args[0].startsWith("bell"))
					note = Sound.BLOCK_NOTE_BELL;
				else if (args[0].startsWith("chime"))
					note = Sound.BLOCK_NOTE_CHIME;
				else if (args[0].startsWith("flute"))
					note = Sound.BLOCK_NOTE_FLUTE;
				else if (args[0].startsWith("guitar"))
					note = Sound.BLOCK_NOTE_GUITAR;
				else if (args[0].startsWith("harp"))
					note = Sound.BLOCK_NOTE_HARP;
				else if (args[0].startsWith("hat"))
					note = Sound.BLOCK_NOTE_HAT;
				else if (args[0].startsWith("pling"))
					note = Sound.BLOCK_NOTE_PLING;
				else if (args[0].startsWith("snare"))
					note = Sound.BLOCK_NOTE_SNARE;
				else if (args[0].startsWith("xylophone"))
					note = Sound.BLOCK_NOTE_XYLOPHONE;
				else if (args[0].startsWith("hit"))
					note = Sound.ENTITY_ARROW_HIT_PLAYER;
				try
				{
					note = Sound.valueOf(args[0].toUpperCase());
				} catch (Exception e) {}
				if (note == null)
					continue;
				t = new Track(line.split(": ")[1], note, spacing, version);
				t.offset = offset;
				if (args.length > 1)
					t.volume = Float.valueOf(args[1]);
				tracks.add(t);
			}
			if (BPM == -1)
				continue;
			song = new RPGSong(name, tracks, BPM);
			song.loop = loop;
			songs.add(song);
		}
	}
}
