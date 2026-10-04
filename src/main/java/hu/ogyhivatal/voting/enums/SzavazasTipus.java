package hu.ogyhivatal.voting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "j = jelenlét; e = egyszerű többségi szavazás; m = minősített többségi szavazás")
public enum SzavazasTipus {

	JELENLET("j"),
	EGYSZERU("e"),
	MINOSITETT("m");

	private final String kod;

	SzavazasTipus(String kod) {
		this.kod = kod;
	}

	@JsonValue
	public String getKod() {
		return kod;
	}

	@JsonCreator
	public static SzavazasTipus fromKod(String kod) {
		return Arrays.stream(values())
				.filter(value -> value.kod.equals(kod))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Érvénytelen szavazás típus: " + kod));
	}
}
