package team.randomizer.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ApplicationUtil {

	private static final String CONFIG_FILENAME = "config.properties";
	private static Properties config = new Properties();

	static {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try (InputStream configResourceStream = loader.getResourceAsStream(CONFIG_FILENAME)) {
			config.load(configResourceStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getResource(String title) {
		return config.getProperty(title);
	}
}
