package team.randomizer.util;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

public class MemberUtil {

	public static List<String> getNames(List<String> userIds, Guild guild) {
		List<String> names = new ArrayList<>();
		
		for (String userId : userIds) {
			Member member = guild.getMemberById(userId);
			names.add(member.getEffectiveName());
		}
		
		return names;
	}
	
	public static List<String> getMentions(List<String> userIds, Guild guild) {
		List<String> mentions = new ArrayList<>();
		
		for (String userId : userIds) {
			Member member = guild.getMemberById(userId);
			mentions.add(member.getAsMention());
		}
		
		return mentions;
	}
}
