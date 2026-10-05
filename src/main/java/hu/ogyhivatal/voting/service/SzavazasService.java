package hu.ogyhivatal.voting.service;

import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.dto.SzavazatLekerdezesResponseDto;

public interface SzavazasService {

	SzavazasLetrehozasResponseDto letrehoz(SzavazasLetrehozasRequestDto request);

	SzavazatLekerdezesResponseDto szavazatLekerdez(String szavazasId, String kepviselo);

	SzavazasEredmenyResponseDto eredmenyLekerdez(String szavazasId);
}
