package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.exception.ApplicationException;
import hu.ogyhivatal.voting.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class SzavazasIdGenerator {

	private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private final JdbcTemplate jdbcTemplate;
	private final SecureRandom random = new SecureRandom();

	public String generate() {
		Long sorszam = jdbcTemplate.queryForObject("SELECT NEXT VALUE FOR szavazas_id_seq", Long.class);
		if (sorszam == null) {
			throw new ApplicationException(
					ErrorCode.SZAVAZAS_ID_GENERALASI_HIBA,
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Nem sikerült szavazás azonosítót előállítani.");
		}
		return randomLetters(2) + sorszam;
	}

	private String randomLetters(int count) {
		StringBuilder builder = new StringBuilder(count);
		for (int i = 0; i < count; i++) {
			builder.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
		}
		return builder.toString();
	}
}
