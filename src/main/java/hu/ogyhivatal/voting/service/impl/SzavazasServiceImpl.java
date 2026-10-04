package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.entity.SzavazatEntity;
import hu.ogyhivatal.voting.exception.ApplicationException;
import hu.ogyhivatal.voting.exception.ErrorCode;
import hu.ogyhivatal.voting.repository.SzavazasJpaRepository;
import hu.ogyhivatal.voting.service.SzavazasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SzavazasServiceImpl implements SzavazasService {

	private final SzavazasJpaRepository szavazasJpaRepository;
	private final SzavazasBusinessValidator businessValidator;
	private final SzavazasIdGenerator idGenerator;

	@Override
	public SzavazasLetrehozasResponseDto letrehoz(SzavazasLetrehozasRequestDto request) {
		businessValidator.validate(request);
		if (szavazasJpaRepository.existsByIdopont(request.getIdopont())) {
			throw new ApplicationException(
					ErrorCode.SZAVAZAS_IDOPONT_FOGLALT,
					HttpStatus.CONFLICT,
					"Erre az időpontra már van rögzített szavazás.");
		}

		String id = idGenerator.generate();
		SzavazasEntity entity = SzavazasEntity.builder()
				.id(id)
				.idopont(request.getIdopont())
				.targy(request.getTargy())
				.tipus(request.getTipus())
				.eljaras(request.getEljaras())
				.elnok(request.getElnok())
				.build();
		request.getSzavazatok().forEach(szavazat ->
				entity.addSzavazat(new SzavazatEntity(szavazat.getKepviselo(), szavazat.getSzavazat())));
		szavazasJpaRepository.save(entity);
		return new SzavazasLetrehozasResponseDto(id);
	}
}
