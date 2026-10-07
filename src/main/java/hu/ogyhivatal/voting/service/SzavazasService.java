package hu.ogyhivatal.voting.service;

import hu.ogyhivatal.voting.dto.KepviseloReszvetelAtlagResponseDto;
import hu.ogyhivatal.voting.dto.KulonlegesEljarasokSzamaResponseDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasokResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.dto.SzavazatLekerdezesResponseDto;

import java.time.LocalDate;

public interface SzavazasService {

	SzavazasLetrehozasResponseDto letrehoz(SzavazasLetrehozasRequestDto request);

	SzavazatLekerdezesResponseDto szavazatLekerdez(String szavazasId, String kepviselo);

	SzavazasEredmenyResponseDto eredmenyLekerdez(String szavazasId);

	NapiSzavazasokResponseDto napiSzavazasok(LocalDate nap);

	KepviseloReszvetelAtlagResponseDto kepviseloReszvetelAtlag(LocalDate kezdet, LocalDate veg);

	KulonlegesEljarasokSzamaResponseDto kulonlegesEljarasokSzama(LocalDate kezdet, LocalDate veg);
}
