package hu.ogyhivatal.voting.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
@Profile("local")
public class LocalBrowserLauncher {

	private final Environment environment;

	public LocalBrowserLauncher(Environment environment) {
		this.environment = environment;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openLocalPages() {
		String port = environment.getProperty("server.port", "8080");
		if (isEnabled("app.swagger.open-browser")) {
			open("http://localhost:" + port + "/swagger-ui/index.html");
		}
		if (isEnabled("app.h2.open-browser")) {
			open("http://localhost:" + port + "/h2-console");
		}
	}

	private boolean isEnabled(String property) {
		return Boolean.parseBoolean(environment.getProperty(property, "false"));
	}

	private void open(String url) {
		try {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				Desktop.getDesktop().browse(URI.create(url));
				return;
			}
			String os = System.getProperty("os.name", "").toLowerCase();
			if (os.contains("win")) {
				new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
			} else if (os.contains("mac")) {
				new ProcessBuilder("open", url).start();
			} else {
				new ProcessBuilder("xdg-open", url).start();
			}
		} catch (Exception ignored) {
		}
	}
}
