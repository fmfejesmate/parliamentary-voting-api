package hu.ogyhivatal.voting.dto;

import hu.ogyhivatal.voting.enums.EljarasTipus;
import hu.ogyhivatal.voting.enums.EredmenyTipus;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
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
public class NapiSzavazasDto {

	private Instant idopont;
	private String targy;
	private SzavazasTipus tipus;
	private EljarasTipus eljaras;
	private String elnok;
	private EredmenyTipus eredmeny;
	private int kepviselokSzama;
	private List<SzavazatDto> szavazatok;
}
