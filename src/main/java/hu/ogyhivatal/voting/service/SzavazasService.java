package hu.ogyhivatal.voting.service;

import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;

public interface SzavazasService {

	SzavazasLetrehozasResponseDto letrehoz(SzavazasLetrehozasRequestDto request);
}
