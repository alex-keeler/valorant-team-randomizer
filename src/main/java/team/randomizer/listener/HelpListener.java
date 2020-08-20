package team.randomizer.listener;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import team.randomizer.rank.Rank;

public class HelpListener extends ListenerAdapter {

	public static final String HELP_COMMAND = "!help";
	public static final String RANK_HELP_COMMAND = "!rankhelp";


	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		if (event.getAuthor().isBot()) {
			return;
		}

		String messageContent = event.getMessage().getContentRaw();

		if (messageContent.startsWith(HELP_COMMAND)) {
			String response = "**Commands:**\n";

			// RandomizeListener
			response += "***" + RandomizeListener.RANDOMIZE_COMMAND + " " + RandomizeListener.RANDOMIZE_COMMAND_USAGE + "*** : Creates randomized ranks using a poll system. Automatically balances teams based on rank.\n" +
					"\tUse the *" + RandomizeListener.USE_VC_FLAG + "* flag to pull the list of users from your current voice channel instead.\n" +
					"\tUse the *" + RandomizeListener.UNRANKED_FLAG + "* flag to create an unranked lobby, where player's ranks are ignored when creating a team.\n";

			// SetRankListener
			response += "***" + SetRankListener.SET_RANK_COMMAND + " " + SetRankListener.SET_RANK_COMMAND_USAGE + "*** : Manually sets the rank of one or more users. See " + RANK_HELP_COMMAND + " to get the available ranks.\n";

			// GetRankListener
			response += "***" + GetRankListener.GET_RANK_COMMAND + " " + GetRankListener.GET_RANK_COMMAND_USAGE + "*** : Gets the rank of one or more users.\n";
			response += "***" + GetRankListener.GET_RANKS_COMMAND + "*** : Gets the ranks of all known users.\n";

			// HelpListener
			response += "***" + HELP_COMMAND + "*** : Lists all available commands.\n";
			response += "***" + RANK_HELP_COMMAND + "*** : Lists all available ranks.\n";

			event.getChannel().sendMessage(response).queue();
		}

		if (messageContent.startsWith(RANK_HELP_COMMAND)) {
			String response = "**Available Ranks:**\n";

			Rank[] ranks = Rank.values();
			for (int i = 0; i < ranks.length; i++) {
				response += (i == 0 ? "" : ", ") + ranks[i].toString();
			}

			event.getChannel().sendMessage(response).queue();
		}
	}
}
