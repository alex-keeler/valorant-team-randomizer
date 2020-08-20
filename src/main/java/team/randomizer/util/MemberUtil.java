package team.randomizer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import team.randomizer.rank.Rank;
import team.randomizer.rank.RankDataService;

public class MemberUtil {

	public static List<String> getNames(List<String> userIds, Guild guild) {
		List<String> names = new ArrayList<>();

		for (String userId : userIds) {
			Member member = getMemberById(userId, guild);
			names.add(member.getEffectiveName());
		}

		return names;
	}

	public static List<String> getNamesWithRankWarnings(List<String> userIds, Guild guild) {
		List<String> names = new ArrayList<>();

		Map<String, Rank> userInfoMap = RankDataService.getRankData(guild);

		for (String userId : userIds) {
			String rankWarning = "";
			if (userInfoMap != null) {
				Rank rank = userInfoMap.get(userId);
				if (rank == null) {
					rankWarning += " **(NO RANK PROVIDED)**";
				} else if (rank == Rank.UNRATED) {
					rankWarning += " **(UNRATED)**";
				}
			}
			Member member = getMemberById(userId, guild);
			names.add(member.getEffectiveName() + rankWarning);
		}

		return names;
	}

	public static List<String> getMentions(List<String> userIds, Guild guild) {
		List<String> mentions = new ArrayList<>();

		for (String userId : userIds) {
			Member member = getMemberById(userId, guild);
			mentions.add(member.getAsMention());
		}

		return mentions;
	}

	public static Member getMemberById(String userId, Guild guild) {
		return guild.retrieveMemberById(userId).complete();
	}
}
