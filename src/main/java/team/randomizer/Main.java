package team.randomizer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import team.randomizer.listener.RandomizeListener;

public class Main {
	
	public static final String BOT_TOKEN = "NzE3OTY1OTIxMjY4MDA3MDI0.XvFwog.Wm7TaARi70Dqy7atBAo78vI4DOQ";
	
	public static void main(String[] arguments) throws Exception {
	    JDA api = JDABuilder.createDefault(BOT_TOKEN).build();
	    
	    // Setup listeners
	    api.addEventListener(new RandomizeListener());
	}
	
}