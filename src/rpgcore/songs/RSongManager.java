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
		for (File file: songsFolder.listFiles())
		{
			ArrayList<String> lines = CakeLibrary.readFile(file);
			if (lines.size() < 2)
				continue;
			String name = file.getName().substring(0, file.getName().length() - 4);
			ArrayList<Track> tracks = new ArrayList<Track>();
			int BPM = -1;
			int version = 1;
			int offset = 0;
			for (String line: lines)
			{
				if (line.startsWith("bpm: "))
				{
					try
					{
						BPM = Integer.parseInt(line.split(": ")[1]);
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
				String composition = line;
				Sound note = null;
				double spacing = RPGSong.getOneFourthSpacingInTicks(BPM);
				if (line.startsWith("basedrum: "))
					note = Sound.BLOCK_NOTE_BASEDRUM;
				if (line.startsWith("bass: "))
					note = Sound.BLOCK_NOTE_BASS;
				if (line.startsWith("bell: "))
					note = Sound.BLOCK_NOTE_BELL;
				if (line.startsWith("chime: "))
					note = Sound.BLOCK_NOTE_CHIME;
				if (line.startsWith("flute: "))
					note = Sound.BLOCK_NOTE_FLUTE;
				if (line.startsWith("guitar: "))
					note = Sound.BLOCK_NOTE_GUITAR;
				if (line.startsWith("harp: "))
					note = Sound.BLOCK_NOTE_HARP;
				if (line.startsWith("hat: "))
					note = Sound.BLOCK_NOTE_HAT;
				if (line.startsWith("pling: "))
					note = Sound.BLOCK_NOTE_PLING;
				if (line.startsWith("snare: "))
					note = Sound.BLOCK_NOTE_SNARE;
				if (line.startsWith("xylophone: "))
					note = Sound.BLOCK_NOTE_XYLOPHONE;
				if (line.startsWith("hit: "))
					note = Sound.ENTITY_ARROW_HIT_PLAYER;
				try
				{
					note = Sound.valueOf(line.split(": ")[0].toUpperCase());
				} catch (Exception e) {}
				if (note == null)
					continue;
				Track t = new Track(composition, note, spacing, version);
				t.offset = offset;
				tracks.add(t);
			}
			if (BPM == -1)
				continue;
			songs.add(new RPGSong(name, tracks, BPM));
		}
	}
}
