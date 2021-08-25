package com.zimmerbell.steamautodelete;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class SteamService {
	private Map<String, LocalDateTime> lastPlayedTimes;

	public SteamService() {
	}

	public List<File> getSteamLibraries() {
		return Arrays.asList(new File("C:\\Program Files (x86)\\Steam\\steamapps"));
	}

	public List<File> getSteamUserDataDirectories() {
		return Arrays.asList(new File("C:\\Program Files (x86)\\Steam\\userdata").listFiles(f -> f.isDirectory()));
	}

	public List<Game> getInstalledGames() {
		final List<Game> games = new LinkedList<>();
		for (final File libraryPath : getSteamLibraries()) {
			games.addAll(getInstalledGames(libraryPath));
		}
		return games;
	}

	public List<Game> getInstalledGames(File libraryPath) {
		final List<Game> games = new LinkedList<>();

		for (final File acfFile : libraryPath.listFiles(file -> file.getName().endsWith(".acf"))) {
			final Game game = new Game(acfFile, getLastPlayedTimes());
			games.add(game);
		}

		return games;
	}

	public void deleteGame(Game game) {
		try {
			System.out.println(String.format("delete '%s'", game.getAcfFile()));
			Files.delete(game.getAcfFile().toPath());

			System.out.println(String.format("delete '%s'", game.getInstallDirectory()));
			Files.walkFileTree(game.getInstallDirectory().toPath(), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}

	}

	public Map<String, LocalDateTime> getLastPlayedTimes() {
		if (lastPlayedTimes == null) {
			lastPlayedTimes = new HashMap<String, LocalDateTime>();

			for (final File steamUserDataDirectory : getSteamUserDataDirectories()) {
				final File vdfFile = new File(new File(steamUserDataDirectory, "config"), "localconfig.vdf");

				try (final BufferedReader in = new BufferedReader(new FileReader(vdfFile))) {
					final Pattern pattern = Pattern.compile("\\s*\"(.+?)\"\\s*(\"(.*?)\")?");

					String id = "";
					for (String line; (line = in.readLine()) != null;) {
						final Matcher m = pattern.matcher(line);
						if (!m.matches()) {
							continue;
						}
						final String key = m.group(1);
						final String value = m.group(3);

						if (value == null) {
							id = key;
							continue;
						}

						if ("LastPlayed".equals(key)) {
							lastPlayedTimes.put(id, Stream
									.of(lastPlayedTimes.get(id),
											LocalDateTime.ofEpochSecond(Long.parseLong(value), 0,
													OffsetDateTime.now().getOffset()))
									.filter(Objects::nonNull) //
									.max(LocalDateTime::compareTo).get());
						}
					}
				} catch (final IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return lastPlayedTimes;
	}

}
