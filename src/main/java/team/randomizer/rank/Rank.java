package team.randomizer.rank;

import java.util.List;

public enum Rank {

	UNRATED(0.0),
	IRON_1(0.1), IRON_2(0.2), IRON_3(0.3),
	BRONZE_1(0.4), BRONZE_2(0.5), BRONZE_3(0.6),
	SILVER_1(0.7), SILVER_2(0.8), SILVER_3(0.9),
	GOLD_1(1.0), GOLD_2(1.1), GOLD_3(1.2),
	PLATINUM_1(1.3), PLATINUM_2(1.4), PLATINUM_3(1.5),
	DIAMOND_1(1.6), DIAMOND_2(1.7), DIAMOND_3(1.8),
	IMMORTAL_1(1.9), IMMORTAL_2(2.0), IMMORTAL_3(2.1),
	RADIANT(2.2);

	private static final double ERROR_MARGIN = 0.1;
	private double weight;

	private Rank(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}

	public static boolean isFairMatch(List<Rank> team1, List<Rank> team2) {

		if (team1.isEmpty() || team2.isEmpty()) {
			return true;
		}

		double team1Avg = getAverageWeight(team1);
		double team2Avg = getAverageWeight(team2);

		return team1Avg >= team2Avg - ERROR_MARGIN && team1Avg <= team2Avg + ERROR_MARGIN;
	}

	public static double getAverageWeight(List<Rank> team) {
		double sum = 0.0;
		int rankedTeamSize = 0;

		for (Rank teammate : team) {
			if (teammate != UNRATED) {
				sum += teammate.getWeight();
				rankedTeamSize++;
			}
		}

		return sum / rankedTeamSize;
	}
}
