package hu.ogyhivatal.voting.dto;

import hu.ogyhivatal.voting.enums.EljarasTipus;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SzavazasLetrehozasRequestDto {

	@NotNull
	private Instant idopont;

	@NotBlank
	private String targy;

	@NotNull
	private SzavazasTipus tipus;

	private EljarasTipus eljaras;

	@NotBlank
	private String elnok;

	@NotNull
	private List<@Valid SzavazatDto> szavazatok;
}
