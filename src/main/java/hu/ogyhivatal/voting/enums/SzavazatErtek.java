package hu.ogyhivatal.voting.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;

@Schema(description = "i = igen; n = nem; t = tartózkodás")
public enum SzavazatErtek {

	IGEN("i"),
	NEM("n"),
	TARTAZKODAS("t");

	private final String kod;

	SzavazatErtek(String kod) {
		this.kod = kod;
	}

	@JsonValue
	public String getKod() {
		return kod;
	}

	@JsonCreator
	public static SzavazatErtek fromKod(String kod) {
		return Arrays.stream(values())
				.filter(value -> value.kod.equals(kod))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Érvénytelen szavazat: " + kod));
	}
}
