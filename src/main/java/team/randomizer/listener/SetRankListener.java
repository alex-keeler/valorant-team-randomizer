package team.randomizer.listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import team.randomizer.rank.Rank;
import team.randomizer.rank.RankDataService;

public class SetRankListener extends ListenerAdapter {

	public static final String SET_RANK_COMMAND = "!setrank";
	public static final String SET_RANK_COMMAND_USAGE = "<user mention(s)> <rank>";

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		if (event.getAuthor().isBot()) {
			return;
		}

		Guild guild = event.getGuild();

		String messageContent = event.getMessage().getContentRaw();
		String command = messageContent.split(" ")[0];

		if (command.equals(SET_RANK_COMMAND)) {
			String[] argList = messageContent.split(" ");

			String argErrorStr = "";

			List<Member> members = event.getMessage().getMentionedMembers();
			if (members.isEmpty()) {
				argErrorStr += "No users were mentioned!\n";
			}

			List<String> rankArgs = new ArrayList<>();
			for (String arg : argList) {
				if (!arg.startsWith("!") && !arg.startsWith("<")) { // not command or mention
					rankArgs.add(arg);
				}
			}

			if (rankArgs.isEmpty()) {
				argErrorStr += "No rank provided! Use " + HelpListener.RANK_HELP_COMMAND + " to view the available ranks.\n";
			} else if (rankArgs.size() > 1) {
				argErrorStr += "Multiple rank arguments provided! (" + Arrays.toString(rankArgs.toArray()) +
						"). Please only provide one rank.\n";
			}

			if (!argErrorStr.equals("")) {
				argErrorStr += "Use " + SET_RANK_COMMAND + " " + SET_RANK_COMMAND_USAGE + ".";
				event.getChannel().sendMessage(argErrorStr).queue();
				return;
			}

			Rank rank;
			try {
				rank = Rank.valueOf(rankArgs.get(0));
			} catch (IllegalArgumentException e) {
				event.getChannel().sendMessage("Invalid rank: " + rankArgs.get(0) + ". Use " + HelpListener.RANK_HELP_COMMAND + " to view the available ranks.").queue();
				return;
			}

			for (Member member : members) {
				if (RankDataService.updateRankEntryManual(member.getId(), rank, guild)) {
					event.getChannel().sendMessage("Successfully set " + member.getEffectiveName() + " to " + rank.toString() + ".").queue();
				} else {
					event.getChannel().sendMessage("An error occured when setting " + member.getEffectiveName() + "'s rank. The change has not been made.").queue();
				}
			}

		}
	}
}
