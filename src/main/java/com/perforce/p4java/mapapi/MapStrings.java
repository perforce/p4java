package com.perforce.p4java.mapapi;

import com.perforce.p4java.Log;
import com.perforce.p4java.server.callback.ILogCallback.LogTraceLevel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Mapstrings.h - MapTable substrings
 * <p>
 * Public Classes:
 * <p>
 * MapStrings - probe strings for fast mapping
 * <p>
 * Public Methods:
 * <p>
 * MapStrings::Count()
 * Returns the number of initial substrings found in the
 * MapStrings.
 * <p>
 * MapStrings::Dump()
 * Print out the strings on the standard output.
 * <p>
 * MapStrings::Get( int n )
 * Gets the n'th string (starting at 0) from the MapStrings.
 */

public class MapStrings extends ArrayList<MapStrings.MapString> {

	public class MapString {
		public boolean hasSubDirs;    // Subdirs after wildcard?
		public MapHalf mapHalf;    // actual mapHalf

		MapString(MapHalf mapHalf, boolean hasSubDirs) {
			this.hasSubDirs = hasSubDirs;
			this.mapHalf = mapHalf;
		}

		@Override
		public String toString() {
			return this.mapHalf.toString();
		}
	}

	public void add(MapHalf mapHalf, boolean hasSubDirs) {
		add(new MapString(mapHalf, hasSubDirs));
	}

	public void dump(StringBuffer out) {

		if (out == null) {
			Log.trace(LogTraceLevel.FINE, "strings for map:\n");
		} else {
			out.append("strings for map:\n");
		}

		for (int i = 0; i < this.size(); i++) {
			String fmt = String.format("\t-> %d: %s (%s)\n", i,
					get(i).mapHalf.get().substring(0, get(i).mapHalf.getFixedLen()),
					get(i).hasSubDirs ? "true" : "false");
			if (out == null) {
				Log.trace(LogTraceLevel.FINE, fmt);
			} else {
				out.append(fmt);
			}
		}
	}

	public void get(int n, MapHalf string, AtomicBoolean hasSubDirs) {
		MapString ms = get(n);
		string.set(ms.mapHalf.get());
		hasSubDirs.set(ms.hasSubDirs);
	}

	public String[] toArray() {
		ArrayList<String> strings = new ArrayList<>();
		for (int i = 0; i < this.size(); i++) {
			strings.add(this.get(i).toString());
		}
		return strings.toArray(new String[0]);
	}
}

