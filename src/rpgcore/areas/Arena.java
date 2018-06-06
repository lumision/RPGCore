package rpgcore.areas;

import java.io.File;
import java.io.IOException;

import org.bukkit.Location;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;

public class Arena 
{
	public String schematicName;
	public Location location;
	
	public void pasteArena()
	{
		try {
			File file = new File("plugins/WorldEdit/schematics/" + schematicName + ".schematic");
			EditSession editSession = new EditSession(new BukkitWorld(location.getWorld()), 999999999);
		    CuboidClipboard clip = SchematicFormat.MCEDIT.load(file);
		    try {
		        clip.paste(editSession, new Vector(location.getX(), location.getY(), location.getZ()), true);
		    } catch (MaxChangedBlocksException e) {
		    e.printStackTrace();
		    }
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (DataException e) {
		    e.printStackTrace();
		}
	}
}
