package team.randomizer;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import team.randomizer.listener.GetRankListener;
import team.randomizer.listener.HelpListener;
import team.randomizer.listener.RandomizeListener;
import team.randomizer.listener.SetRankListener;
import team.randomizer.util.ApplicationUtil;

public class Main {

	public static void main(String[] arguments) throws Exception {

		String botToken = ApplicationUtil.getResource("bot.token");
		JDA api = JDABuilder.createDefault(botToken).build();

		// Setup listeners
		api.addEventListener(new RandomizeListener());
		api.addEventListener(new SetRankListener());
		api.addEventListener(new GetRankListener());
		api.addEventListener(new HelpListener());
	}

}