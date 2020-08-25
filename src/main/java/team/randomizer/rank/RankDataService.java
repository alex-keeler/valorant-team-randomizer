package team.randomizer.rank;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import net.dv8tion.jda.api.entities.Guild;
import team.randomizer.util.ApplicationUtil;

public class RankDataService {

	private static final String RANKS_JSON_FILENAME = "ranks.json";
	private static final String RANKS_JSON_FILEPATH;

	private static final File ranksFile;

	static {
		String dataDirectory = ApplicationUtil.getResource("data.directory");
		RANKS_JSON_FILEPATH = String.format("%s/%s", dataDirectory, RANKS_JSON_FILENAME);
		ranksFile = new File(String.format("%s/%s", dataDirectory, RANKS_JSON_FILENAME));
	}

	public static Map<String, Rank> getRankData(Guild guild) {

		if (!ranksFile.exists()) {
			try {
				ranksFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}

		Map<String, Rank> rankMap = new HashMap<>();

		JSONParser parser = new JSONParser();

		JSONObject guildsObj;
		try {
			if (ranksFile.length() > 0) {
				guildsObj = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(RANKS_JSON_FILEPATH)));
			} else {
				guildsObj = null;
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}

		if (guildsObj == null) { // file is empty
			System.out.println("DEBUG: Rank file is empty");
			return rankMap;
		}

		JSONArray guildsList = (JSONArray) guildsObj.get("guilds");

		JSONObject guildObj = null;
		for (Object guildVar : guildsList) {
			JSONObject guildJson = (JSONObject) guildVar;
			if (guild.getId().equals(guildJson.get("guild_id"))) {
				guildObj = guildJson;
				break;
			}
		}

		if (guildObj == null) { // guild not found
			System.out.println("DEBUG: Guild not found");
			return rankMap;
		}

		JSONArray usersList = (JSONArray) guildObj.get("users");
		for (Object user : usersList) {
			JSONObject userObj = (JSONObject) user;

			String userId = (String) userObj.get("user_id");
			Boolean manualEntryFlag = (Boolean) userObj.get("manual");
			Rank rank;

			if (manualEntryFlag) {
				String rankStr = (String) userObj.get("rank");
				rank = Rank.valueOf(rankStr);
			} else {
				// TODO: Implement once Valorant API is available
				rank = Rank.UNRATED; // dummy value
			}

			rankMap.put(userId, rank);
		}

		return rankMap;
	}

	@SuppressWarnings("unchecked")
	public static boolean updateRankEntryManual(String userId, Rank rank, Guild guild) {

		if (!ranksFile.exists()) {
			try {
				ranksFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}

		JSONParser parser = new JSONParser();

		JSONObject guildsObj;
		try {
			if (ranksFile.length() > 0) {
				guildsObj = (JSONObject) parser.parse(new InputStreamReader(new FileInputStream(RANKS_JSON_FILEPATH)));
			} else {
				guildsObj = null;
			}
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return false;
		}

		if (guildsObj == null) {
			guildsObj = new JSONObject();
		}

		JSONArray guildsList = (JSONArray) guildsObj.get("guilds");
		JSONObject guildObj = null;
		if (guildsList == null) {
			guildsList = new JSONArray();
			guildsObj.put("guilds", guildsList);
		} else {
			for (Object guildVar : guildsList) {
				JSONObject guildJson = (JSONObject) guildVar;
				if (guild.getId().equals(guildJson.get("guild_id"))) {
					guildObj = guildJson;
					break;
				}
			}
		}

		if (guildObj == null) { // guild not found
			System.out.printf("Creating guild: {guild_id: %s}\n", guild.getId());
			guildObj = new JSONObject();
			guildObj.put("guild_id", guild.getId());
			guildsList.add(guildObj);
		}

		JSONArray usersList = (JSONArray) guildObj.get("users");


		if (usersList == null) {
			usersList = new JSONArray();
			guildObj.put("users", usersList);
		}

		boolean userFound = false;
		for (Object user : usersList) {

			JSONObject userObj = (JSONObject) user;
			String id = (String) userObj.get("user_id");

			if (userId.equals(id)) { // edit
				System.out.printf("Updating user: {id: %s, manual: %s, rank: %s}\n", userId, true, rank.toString());
				userFound = true;
				userObj.put("manual", true);
				userObj.put("rank", rank.toString());
			}

		}

		if (!userFound) { // add
			System.out.printf("Creating user: {id: %s, manual: %s, rank: %s}\n", userId, true, rank.toString());
			JSONObject userObj = new JSONObject();
			userObj.put("user_id", userId);
			userObj.put("manual", true);
			userObj.put("rank", rank.toString());
			usersList.add(userObj);
		}

		try {
			Writer writer = new FileWriter(RANKS_JSON_FILEPATH);
			guildsObj.writeJSONString(writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	public static void updateRankEntryLink(String userMention, String riotAccount) {
		// TODO: Implement once Valorant API is available
	}

}
