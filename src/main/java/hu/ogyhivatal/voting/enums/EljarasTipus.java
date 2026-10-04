package hu.ogyhivatal.voting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "n = normál; s = sürgősségi; k = kivételes; e = szabályzattól eltérő")
public enum EljarasTipus {

	NORMAL("n"),
	SURGOSSEGI("s"),
	KIVETELES("k"),
	SZABALYZATTOL_ELTERO("e");

	private final String kod;

	EljarasTipus(String kod) {
		this.kod = kod;
	}

	@JsonValue
	public String getKod() {
		return kod;
	}

	@JsonCreator
	public static EljarasTipus fromKod(String kod) {
		return Arrays.stream(values())
				.filter(value -> value.kod.equals(kod))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Érvénytelen eljárás: " + kod));
	}
}
