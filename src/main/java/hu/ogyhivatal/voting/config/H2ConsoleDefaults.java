package hu.ogyhivatal.voting.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Component
public class H2ConsoleDefaults implements BeanPostProcessor {

	private final Environment environment;

	public H2ConsoleDefaults(Environment environment) {
		this.environment = environment;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (bean instanceof ServletRegistrationBean<?> registration && "h2Console".equals(beanName)) {
			configureDefaultJdbcUrl(registration);
		}
		return bean;
	}

	private void configureDefaultJdbcUrl(ServletRegistrationBean<?> registration) {
		try {
			Path dir = Path.of(System.getProperty("java.io.tmpdir"), "parliamentary-voting-api-h2");
			Files.createDirectories(dir);
			String url = environment.getProperty("spring.datasource.url", "jdbc:h2:mem:szavazasok");
			String user = environment.getProperty("spring.datasource.username", "sa");
			Properties props = new Properties();
			props.setProperty("0", "Generic H2 (Embedded)|org.h2.Driver|" + url + "|" + user);
			try (OutputStream out = Files.newOutputStream(dir.resolve(".h2.server.properties"))) {
				props.store(out, "H2 Console defaults");
			}
			registration.addInitParameter("properties", dir.toAbsolutePath().toString());
		} catch (IOException ignored) {
		}
	}
}
