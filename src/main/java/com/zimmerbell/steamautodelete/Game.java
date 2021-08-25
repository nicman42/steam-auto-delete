package com.zimmerbell.steamautodelete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Game {

	private final String id;
	private String name;
	private Long size;
	private final LocalDateTime lastPlayed;

	private final File acfFile;
	private File installDirectory;

	public Game(File acfFile, Map<String, LocalDateTime> lastPlayedTimes) {
		Matcher m = Pattern.compile(".*_(.*).acf").matcher(acfFile.getName());
		if (!m.matches()) {
			throw new IllegalArgumentException("unexpected acf file name pattern: " + acfFile.getName());
		}
		this.id = m.group(1);
		this.lastPlayed = lastPlayedTimes.get(getId());
		this.acfFile = acfFile;

		try (final BufferedReader in = new BufferedReader(new FileReader(acfFile))) {
			final Pattern pattern = Pattern.compile("\\s*\"(.*)\"\\s*\"(.*)\"");
			for (String line; (line = in.readLine()) != null;) {
				m = pattern.matcher(line);
				if (!m.matches()) {
					continue;
				}
				final String key = m.group(1);
				final String value = m.group(2);
				switch (key) {
				case "name":
					this.name = value;
					break;
				case "installdir":
					this.installDirectory = new File(new File(acfFile.getParentFile(), "common"), value);

					// fallback for name
					if (this.name.startsWith("appid_")) {
						this.name = value;
					}
					break;
				case "SizeOnDisk":
					this.size = Long.parseLong(value);
					break;
				}

			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

		if (this.name == null) {
			throw new RuntimeException("game name missing");
		}

	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Long getSize() {
		return size;
	}

	public LocalDateTime getLastPlayed() {
		return lastPlayed;
	}

	public File getAcfFile() {
		return acfFile;
	}

	public File getInstallDirectory() {
		return installDirectory;
	}

	@Override
	public String toString() {
		return String.format("%s (name: %s, size: %d, last played: %s)", getId(), getName(), getSize(),
				getLastPlayed());
	}
}
