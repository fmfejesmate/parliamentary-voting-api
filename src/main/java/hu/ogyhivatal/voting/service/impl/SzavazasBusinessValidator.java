package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.exception.ApplicationException;
import hu.ogyhivatal.voting.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class SzavazasBusinessValidator {

	public void validate(SzavazasLetrehozasRequestDto request) {
		boolean elnokSzavazott = request.getSzavazatok().stream()
				.anyMatch(szavazat -> request.getElnok().equals(szavazat.getKepviselo()));
		if (!elnokSzavazott) {
			throw new ApplicationException(
					ErrorCode.SZAVAZAS_ELNOK_NINCS_SZAVAZAT,
					HttpStatus.BAD_REQUEST,
					"Az elnöknek szerepelnie kell a szavazatok között.");
		}

		Set<String> kepviselok = new HashSet<>();
		for (var szavazat : request.getSzavazatok()) {
			if (!kepviselok.add(szavazat.getKepviselo())) {
				throw new ApplicationException(
						ErrorCode.SZAVAZAS_KEPVISELO_TOBBSZOR_SZAVAZOTT,
						HttpStatus.BAD_REQUEST,
						"Egy képviselő csak egyszer szavazhat: " + szavazat.getKepviselo());
			}
		}
	}
}
