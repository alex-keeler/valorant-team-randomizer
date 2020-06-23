package team.randomizer;

import java.io.FileInputStream;
import java.util.Properties;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import team.randomizer.listener.RandomizeListener;

public class Main {
	
	public static void main(String[] arguments) throws Exception {
		
		String configPath = Thread.currentThread().getContextClassLoader().getResource("config.properties").getPath();
		Properties config = new Properties();
		config.load(new FileInputStream(configPath));
		
		String botToken = config.getProperty("bot.token");
		JDA api = JDABuilder.createDefault(botToken).build();
	    
		// Setup listeners
		api.addEventListener(new RandomizeListener());
	}
	
}