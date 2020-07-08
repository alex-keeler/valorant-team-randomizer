package team.randomizer.listener;

import java.util.List;
import java.util.Map;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import team.randomizer.rank.Rank;
import team.randomizer.rank.RankDataService;

public class GetRankListener extends ListenerAdapter {
	
	public static final String GET_RANK_COMMAND = "!rank";
	public static final String GET_RANK_COMMAND_USAGE = "<user mention(s)>";
	public static final String GET_RANKS_COMMAND = "!ranks";

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
				
		if (event.getAuthor().isBot()) {
			return;
		}
		
		Guild guild = event.getGuild();
		
		String messageContent = event.getMessage().getContentRaw();
		
		if (messageContent.split(" ")[0].equals(GET_RANK_COMMAND)) {
			
			List<Member> members = event.getMessage().getMentionedMembers();
			if (members.isEmpty()) {
				event.getChannel().sendMessage("No users were mentioned!\nUse " + GET_RANK_COMMAND + " " + GET_RANK_COMMAND_USAGE + ".").queue();
				return;
			}
			
			Map<String, Rank> userRankMap = RankDataService.getRankData(guild);
			if (userRankMap == null) {
				event.getChannel().sendMessage("An error has occurred while retrieving rank data. Sorry for the inconvenience.").queue();
				return;
			}
			
			String response = "";
			for (Member member : members) {
				Rank rank = userRankMap.get(member.getId());
				if (rank == null) {
					response += member.getEffectiveName() + " does not have a rank.";
				} else {
					response += member.getEffectiveName() + " is " + rank.toString() + ".\n";
				}
			}
						
			event.getChannel().sendMessage(response).queue();
		}
		
		if (messageContent.split(" ")[0].equals(GET_RANKS_COMMAND)) {
						
			Map<String, Rank> userRankMap = RankDataService.getRankData(guild);
			if (userRankMap == null) {
				event.getChannel().sendMessage("An error has occurred while retrieving rank data. Sorry for the inconvenience.").queue();
				return;
			}
						
			String response = "";
			
			if (userRankMap.entrySet().isEmpty()) {
				response += "There are no users with saved ranks in this server.\n" +
						"\tUse " + SetRankListener.SET_RANK_COMMAND + " " + SetRankListener.SET_RANK_COMMAND_USAGE + " to set user ranks.";
			}
			
			for (Map.Entry<String, Rank> rankEntry : userRankMap.entrySet()) {
				Member member = guild.getMemberById(rankEntry.getKey());
				response += member.getEffectiveName() + " is " + rankEntry.getValue().toString() + ".\n";
			}
			
			event.getChannel().sendMessage(response).queue();
		}
	}
}
