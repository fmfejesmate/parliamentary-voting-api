package hu.ogyhivatal.voting.dto;

import hu.ogyhivatal.voting.enums.SzavazatErtek;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SzavazatDto {

	@NotBlank
	private String kepviselo;

	@NotNull
	private SzavazatErtek szavazat;
}
