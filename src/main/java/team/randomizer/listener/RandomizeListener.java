package team.randomizer.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import team.randomizer.util.Pair;

public class RandomizeListener extends ListenerAdapter {
	
	private static final int MAX_LOBBY_SIZE = 10;
	
	private static final String CHECK_UNICODE = "U+2705";
	private static final String CHECK_CODE = ":white_check_mark:";
	private static final String START_UNICODE = "U+25B6";
	private static final String START_CODE = ":arrow_forward:";
	
	private Map<String, Pair<Boolean, List<String>>> lobbyInfoMap = new HashMap<>(); // message id -> (active flag, list of usernames)
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		
		if (event.getAuthor().isBot()) {
			return;
		}
		
		String messageContent = event.getMessage().getContentRaw(); 
		
		if (messageContent.startsWith("!randomize")) {
			
			if (messageContent.contains("-v")) {
				event.getChannel().sendMessage(generateTeamsFromVoiceChannel(event)).queue();
			} else {
				pollUsers(event);
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
				
		if (event.getReactionEmote().isEmoji()) {
			
			String unicode = event.getReactionEmote().getAsCodepoints();
			
			Pair<Boolean, List<String>> lobbyInfo = lobbyInfoMap.get(event.getMessageId());
			
			switch (unicode.toUpperCase()) {
				case CHECK_UNICODE:
					if (lobbyInfo.getLeft()) { // if the poll is active						
						List<String> names = lobbyInfo.getRight();
						names.add(event.getUser().getAsMention());
						Message message = event.retrieveMessage().complete();
						message.editMessage(editPollString(message.getContentRaw(), names)).queue();
					}
					
					break;
				case START_UNICODE:
					if (lobbyInfo.getLeft()) { // if the poll is active						
						lobbyInfo.setLeft(false); // not active
						List<String> names = lobbyInfo.getRight();
						event.getChannel().sendMessage(generateTeams(names)).queue();
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
		
		if (event.getReactionEmote().isEmoji()) {
			
			String unicode = event.getReactionEmote().getAsCodepoints();
			
			switch(unicode.toUpperCase()) {
				case CHECK_UNICODE:
					Pair<Boolean, List<String>> lobbyInfo = lobbyInfoMap.get(event.getMessageId());
					
					if (lobbyInfo.getLeft()) { // if the poll is active						
						List<String> names = lobbyInfo.getRight();
						names.remove(event.getUser().getAsMention());
						Message message = event.retrieveMessage().complete();
						message.editMessage(editPollString(message.getContentRaw(), names)).queue();
					}
					break;
				default:
					break;
			}
		}
	}
	
	public void pollUsers(MessageReceivedEvent event) {
		
		String poll = "Starting a custom game lobby!\n" + 
				"Select " + CHECK_CODE + " to join the lobby. Deselect " + CHECK_CODE + " to leave the lobby.\n" + 
				"Once ready, select " + START_CODE + " to finalize the lobby and generate random teams.\n";
		
		poll += getLobbyListString(new ArrayList<>());
		
		MessageChannel messageChannel = event.getChannel();
		messageChannel.sendMessage(poll).queue(message -> {
			message.addReaction(CHECK_UNICODE).queue();
			message.addReaction(START_UNICODE).queue();
			lobbyInfoMap.put(message.getId(), new Pair<Boolean, List<String>>(true, new ArrayList<>()));
		});
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
	
	public String generateTeamsFromVoiceChannel(MessageReceivedEvent event) {
		
		String response = "";
		
		VoiceChannel voiceChannel = getVoiceChannelFromUserAndGuild(event.getAuthor(), event.getGuild());
		
		if (voiceChannel == null) {
			response = "You are not in a voice channel!";
		} else {
			List<String> usernames = new ArrayList<>();
			voiceChannel.getMembers().forEach(member -> usernames.add(member.getUser().getAsMention())); 
			
			response = generateTeams(usernames);
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
	
	public String generateTeams(List<String> usernames) {
		
		if (usernames.isEmpty()) {
			return "You fool. You absolute buffoon. You cannot have randomized teams if no one joined the lobby to begin with. It is simply not possible.";
		}
		
		if (usernames.size() > MAX_LOBBY_SIZE) { // limit to max lobby size
			usernames = usernames.subList(0, MAX_LOBBY_SIZE);
		}
		
		Collections.shuffle(usernames);
		
		List<String> team1 = usernames.subList(0, usernames.size() / 2);
		List<String> team2 = usernames.subList(usernames.size() / 2, usernames.size());
		
		boolean team1Attackers = Math.random() >= 0.5;
		
		List<String> attackers = team1Attackers ? team1 : team2;
		List<String> defenders = team1Attackers ? team2 : team1;
		
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
	
}
