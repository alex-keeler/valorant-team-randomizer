package team.randomizer.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javatuples.Pair;
import org.javatuples.Triplet;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import team.randomizer.rank.Rank;
import team.randomizer.rank.RankDataService;
import team.randomizer.util.MemberUtil;

public class RandomizeListener extends ListenerAdapter {

	public static final String RANDOMIZE_COMMAND = "!randomize";
	public static final String RANDOMIZE_COMMAND_USAGE = "<flag(s)>";
	public static final String USE_VC_FLAG = "-v";
	public static final String UNRANKED_FLAG = "-u";

	private static final int MAX_LOBBY_SIZE = 10;

	private static final String CHECK_UNICODE = "U+2705";
	private static final String CHECK_CODE = ":white_check_mark:";
	private static final String START_UNICODE = "U+25B6";
	private static final String START_CODE = ":arrow_forward:";

	private Map<String, Triplet<Boolean, Boolean, List<String>>> lobbyInfoMap = new HashMap<>(); // message id -> (active flag, ranked flag, list of usernames)

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		if (event.getAuthor().isBot()) {
			return;
		}

		Guild guild = event.getGuild();

		String messageContent = event.getMessage().getContentRaw();

		if (messageContent.startsWith(RANDOMIZE_COMMAND)) {
			boolean ranked = !messageContent.contains(UNRANKED_FLAG);

			if (messageContent.contains(USE_VC_FLAG)) {
				event.getChannel().sendMessage(generateTeamsFromVoiceChannel(event, ranked, guild)).queue();
			} else {
				pollUsers(event, ranked);
			}
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {

		if (event.getUser().isBot()) {
			return;
		}

		if(!event.retrieveMessage().complete().getAuthor().isBot()) {
			return;
		}

		Guild guild = event.getGuild();

		if (event.getReactionEmote().isEmoji()) {

			String unicode = event.getReactionEmote().getAsCodepoints();

			Triplet<Boolean, Boolean, List<String>> lobbyInfo = lobbyInfoMap.get(event.getMessageId());

			switch (unicode.toUpperCase()) {
				case CHECK_UNICODE:
					if (lobbyInfo.getValue0()) { // if the poll is active
						List<String> userIds = lobbyInfo.getValue2();
						userIds.add(event.getUser().getId());
						Message message = event.retrieveMessage().complete();
						message.editMessage(editPollString(message.getContentRaw(), MemberUtil.getNamesWithRankWarnings(userIds, guild))).queue();
					}
					break;
				case START_UNICODE:
					if (lobbyInfo.getValue0()) { // if the poll is active
						lobbyInfo = lobbyInfo.setAt0(false); // not active
						lobbyInfoMap.put(event.getMessageId(), lobbyInfo);
						List<String> names = lobbyInfo.getValue2();
						event.getChannel().sendMessage(generateTeams(names, lobbyInfo.getValue1(), guild)).queue();
					}
					break;
				default:
					break;
			}
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {

		if (!event.retrieveMessage().complete().getAuthor().isBot()) {
			return;
		}

		Guild guild = event.getGuild();

		if (event.getReactionEmote().isEmoji()) {

			String unicode = event.getReactionEmote().getAsCodepoints();

			switch(unicode.toUpperCase()) {
				case CHECK_UNICODE:
					Triplet<Boolean, Boolean, List<String>> lobbyInfo = lobbyInfoMap.get(event.getMessageId());

					if (lobbyInfo.getValue0()) { // if the poll is active
						List<String> userIds = lobbyInfo.getValue2();
						userIds.remove(event.getUser().getId());
						Message message = event.retrieveMessage().complete();
						message.editMessage(editPollString(message.getContentRaw(), MemberUtil.getNamesWithRankWarnings(userIds, guild))).queue();
					}
					break;
				default:
					break;
			}
		}
	}

	public void pollUsers(MessageReceivedEvent event, Boolean ranked) {

		String poll = "Starting a custom game lobby!\n" +
				"Select " + CHECK_CODE + " to join the lobby. Deselect " + CHECK_CODE + " to leave the lobby.\n" +
				"Once ready, select " + START_CODE + " to finalize the lobby and generate random teams.\n";

		poll += getLobbyListString(new ArrayList<>());

		MessageChannel messageChannel = event.getChannel();

		Message message = messageChannel.sendMessage(poll).complete();

		message.addReaction(CHECK_UNICODE).queue();
		message.addReaction(START_UNICODE).queue();
		lobbyInfoMap.put(message.getId(), new Triplet<Boolean, Boolean, List<String>>(true, ranked, new ArrayList<>()));

	}

	public String getLobbyListString(List<String> names) {

		String listString = "";

		for (int i = 0; i < MAX_LOBBY_SIZE; i++) {
			if (i < names.size()) {
				listString += "> " + (i + 1) + ".\t" + names.get(i) + "\n";
			} else {
				listString += "> " + (i + 1) + ".\n";
			}
		}

		return listString;
	}

	public String editPollString(String poll, List<String> names) {
		return poll.substring(0, poll.indexOf("> 1.")) + getLobbyListString(names);
	}

	public String generateTeamsFromVoiceChannel(MessageReceivedEvent event, Boolean ranked, Guild guild) {

		String response = "";

		VoiceChannel voiceChannel = getVoiceChannelFromUserAndGuild(event.getAuthor(), event.getGuild());

		if (voiceChannel == null) {
			response = "You are not in a voice channel!";
		} else {
			List<String> usernames = new ArrayList<>();
			voiceChannel.getMembers().forEach(member -> usernames.add(member.getUser().getId()));

			response = generateTeams(usernames, ranked, guild);
		}

		return response;
	}

	public VoiceChannel getVoiceChannelFromUserAndGuild(User user, Guild guild) {

		// There doesn't seem to be a way to get the voice channel directly from the user,
		// so we must look through each voice channel in the server to find the user
		for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
			for (Member member : voiceChannel.getMembers()) {
				if (member.getUser().getId().equals(user.getId())) {
					return voiceChannel;
				}
			}
		}

		return null;
	}

	public String generateTeams(List<String> userIds, Boolean ranked, Guild guild) {

		if (userIds.isEmpty()) {
			return "You fool. You absolute buffoon. You cannot have randomized teams if no one joined the lobby to begin with. It is simply not possible.";
		}

		if (userIds.size() > MAX_LOBBY_SIZE) { // limit to max lobby size
			userIds = userIds.subList(0, MAX_LOBBY_SIZE);
		}

		List<String> team1, team2;

		if (ranked) { // Generate all balanced combinations, choose a random one

			List<Pair<List<String>, List<String>>> possibleTeams = getPossibleRankedTeams(userIds, guild);
			if (possibleTeams == null) {
				return "Cannot generate ranked teams!";
			}

			if (possibleTeams.isEmpty()) {
				return "There are no possible balanced matches with the current players!";
			}

			int index = (int) Math.random() * possibleTeams.size();

			Pair<List<String>, List<String>> teams = possibleTeams.get(index);

			team1 = teams.getValue0();
			team2 = teams.getValue1();

			// Shuffling the team orders after the fact to make it seem more random (lol)
			Collections.shuffle(team1);
			Collections.shuffle(team2);

		} else { // Simply shuffle user list and split into teams
			Collections.shuffle(userIds);

			team1 = userIds.subList(0, userIds.size() / 2);
			team2 = userIds.subList(userIds.size() / 2, userIds.size());
		}

		boolean team1Attackers = Math.random() >= 0.5;

		List<String> attackers = team1Attackers ? MemberUtil.getMentions(team1, guild) : MemberUtil.getMentions(team2, guild);
		List<String> defenders = team1Attackers ? MemberUtil.getMentions(team2, guild) : MemberUtil.getMentions(team1, guild);

		// Attackers
		String list = "**Attackers:**\n";
		for (String attacker : attackers) {
			list += "> " + attacker + "\n";
		}

		// Defenders
		list += "\n**Defenders:**\n";
		for (String defender : defenders) {
			list += "> " + defender + "\n";
		}

		return list;
	}

	private List<Pair<List<String>, List<String>>> getPossibleRankedTeams(List<String> users, Guild guild) {

		Map<String, Rank> userInfoMap = RankDataService.getRankData(guild);
		if (userInfoMap == null) {
			return null;
		}


		List<Pair<List<String>, List<String>>> possibleCombinations = new ArrayList<>();

		List<List<String>> team1Combinations = combinations(users, (int) Math.ceil(users.size() / 2.0));
		for (List<String> team1 : team1Combinations) {
			List<String> team2 = new ArrayList<>(users);
			team2.removeAll(team1);

			List<Rank> team1Ranks = new ArrayList<>();
			for (String teammateId : team1) {
				Rank rank = userInfoMap.get(teammateId);
				if (rank != null) {
					team1Ranks.add(rank);
				}
			}

			List<Rank> team2Ranks = new ArrayList<>();
			for (String teammateId : team2) {
				Rank rank = userInfoMap.get(teammateId);
				if (rank != null) {
					team2Ranks.add(rank);
				}
			}

			if (Rank.isFairMatch(team1Ranks, team2Ranks)) {
				possibleCombinations.add(new Pair<>(team1, team2));
			}
		}

		return possibleCombinations;
	}

	// Hey, I remembered something from discrete math...that's a surprise
	private List<List<String>> combinations(List<String> list, int k) {
		List<List<String>> combinations = new ArrayList<>();

		if (k <= 0) {
			return combinations;
		} else if (k == 1) {
			for (int i = 0; i < list.size(); i++) {
				List<String> singleElementList = new ArrayList<>();
				singleElementList.add(list.get(i));
				combinations.add(singleElementList);
			}
		} else {
			for (int i = 0; i <= list.size() - k; i++) {
				List<List<String>> sublistCombinations = combinations(list.subList(i + 1, list.size()), k - 1);
				for (List<String> combination : sublistCombinations) {
					combination.add(0, list.get(i));
					combinations.add(combination);
				}
			}
		}

		return combinations;
	}
}
