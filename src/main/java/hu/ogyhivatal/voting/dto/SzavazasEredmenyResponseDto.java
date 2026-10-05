package hu.ogyhivatal.voting.dto;

import hu.ogyhivatal.voting.enums.EredmenyTipus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SzavazasEredmenyResponseDto {

	private EredmenyTipus eredmeny;
	private int kepviselokSzama;
	private int igenekSzama;
	private int nemekSzama;
	private int tartozkodasokSzama;
}
