package com.zimmerbell.steamautodelete;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

public class SteamAutoDelete {

	public static void main(String[] args) {
		final int months = args.length > 0 ? Integer.parseInt(args[0]) : 1;

		if (args.length == 0) {
			System.out.println("------------------------------------------------------");
			System.out.println("usage: SteamAutoDelete [MONTH] [--delete]");
			System.out.println("\tMONTH\t\tnumber of month a game is not played (default 1)");
			System.out.println("\t--delete\trealy delete not played games");
			System.out.println("------------------------------------------------------");
		}

		final boolean delete = Arrays.stream(args).filter(a -> "--delete".equals(a)).count() > 0;

		final SteamService steamService = new SteamService();

		final LocalDateTime deleteTime = LocalDateTime.now().minusMonths(months);

		for (final Game game : steamService.getInstalledGames()) {
			System.out.println();
			System.out.println(game);

			if (Optional.ofNullable(game.getLastPlayed()).map(t -> t.isBefore(deleteTime)).orElse(false)) {
				if (delete) {
					System.out.println("\tdelete " + game.getName());
					steamService.deleteGame(game);
				} else {
					System.out.println(String.format("\tcall with '--delete' to delete '%s'", game.getName()));
				}
			}
		}
	}
}
