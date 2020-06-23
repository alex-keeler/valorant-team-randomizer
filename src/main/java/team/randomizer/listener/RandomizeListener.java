package team.randomizer.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class RandomizeListener extends ListenerAdapter {
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
    	
        if (event.getAuthor().isBot()) {
        	return;
        }
        
        String messageContent = event.getMessage().getContentRaw(); 
        
        if (messageContent.startsWith("!randomize"))
        {
        	
        	String response = "";
        	
        	VoiceChannel voiceChannel = getVoiceChannelFromUserAndGuild(event.getAuthor(), event.getGuild());
        	
        	if (voiceChannel == null) {
        		response = "You are not in a voice channel!";
        	} else {        		
        		List<String> usernames = new ArrayList<>();
        		voiceChannel.getMembers().forEach(member -> usernames.add(member.getUser().getAsMention())); 
        		
        		response = generateTeams(usernames);
        	}
        	
        	
            MessageChannel messageChannel = event.getChannel();
            messageChannel.sendMessage(response).queue();
        }
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
