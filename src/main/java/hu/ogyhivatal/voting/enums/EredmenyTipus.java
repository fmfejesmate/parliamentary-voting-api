package hu.ogyhivatal.voting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

import java.util.Arrays;

@Schema(description = "F = elfogadott; U = elutasított")
@AllArgsConstructor
public enum EredmenyTipus {

	ELFOGADOTT("F"),
	ELUTASITOTT("U");

	private final String kod;

	@JsonValue
	public String getKod() {
		return kod;
	}

	@JsonCreator
	public static EredmenyTipus fromKod(String kod) {
		return Arrays.stream(values())
				.filter(value -> value.kod.equals(kod))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Érvénytelen eredmény: " + kod));
	}
}
