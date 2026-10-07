package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.dto.KepviseloReszvetelAtlagResponseDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasokResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.dto.SzavazatDto;
import hu.ogyhivatal.voting.dto.SzavazatLekerdezesResponseDto;
import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.entity.SzavazatEntity;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import hu.ogyhivatal.voting.exception.ApplicationException;
import hu.ogyhivatal.voting.exception.ErrorCode;
import hu.ogyhivatal.voting.repository.SzavazasJpaRepository;
import hu.ogyhivatal.voting.service.SzavazasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SzavazasServiceImpl implements SzavazasService {

	private final SzavazasJpaRepository szavazasJpaRepository;
	private final SzavazasBusinessValidator businessValidator;
	private final SzavazasIdGenerator idGenerator;
	private final SzavazasEredmenyCalculator eredmenyCalculator;

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

	@Override
	@Transactional(readOnly = true)
	public SzavazatLekerdezesResponseDto szavazatLekerdez(String szavazasId, String kepviselo) {
		SzavazasEntity szavazas = findSzavazasOrThrow(szavazasId);
		return szavazas.getSzavazatok().stream()
				.filter(szavazat -> szavazat.getKepviselo().equals(kepviselo))
				.findFirst()
				.map(szavazat -> new SzavazatLekerdezesResponseDto(szavazat.getSzavazat()))
				.orElseThrow(() -> new ApplicationException(
						ErrorCode.KEPVISELO_SZAVAZAT_NEM_TALALHATO,
						HttpStatus.NOT_FOUND,
						"A képviselő ezen a szavazáson nem szavazott."));
	}

	@Override
	@Transactional(readOnly = true)
	public SzavazasEredmenyResponseDto eredmenyLekerdez(String szavazasId) {
		return eredmenyCalculator.calculate(findSzavazasOrThrow(szavazasId));
	}

	@Override
	@Transactional(readOnly = true)
	public NapiSzavazasokResponseDto napiSzavazasok(LocalDate nap) {
		var kezdet = napKezdete(nap);
		var veg = napKezdete(nap.plusDays(1));
		List<NapiSzavazasDto> szavazasok = szavazasJpaRepository
				.findByIdopontGreaterThanEqualAndIdopontLessThanOrderByIdopontAsc(kezdet, veg)
				.stream()
				.map(this::toNapiSzavazasDto)
				.toList();
		return new NapiSzavazasokResponseDto(szavazasok);
	}

	@Override
	@Transactional(readOnly = true)
	public KepviseloReszvetelAtlagResponseDto kepviseloReszvetelAtlag(LocalDate kezdet, LocalDate veg) {
		if (veg.isBefore(kezdet)) {
			throw new ApplicationException(
					ErrorCode.IDOSZAK_ERVENYTELEN,
					HttpStatus.BAD_REQUEST,
					"Az időszak vége nem lehet korábbi, mint a kezdete.");
		}
		List<SzavazasEntity> szavazasok = szavazasJpaRepository
				.findByIdopontGreaterThanEqualAndIdopontLessThanAndTipusNot(
						napKezdete(kezdet),
						napKezdete(veg.plusDays(1)),
						SzavazasTipus.JELENLET);
		long kepviselok = szavazasok.stream()
				.flatMap(szavazas -> szavazas.getSzavazatok().stream())
				.map(SzavazatEntity::getKepviselo)
				.distinct()
				.count();
		if (kepviselok == 0) {
			return new KepviseloReszvetelAtlagResponseDto(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
		}
		long reszvetelek = szavazasok.stream()
				.mapToLong(szavazas -> szavazas.getSzavazatok().size())
				.sum();
		BigDecimal atlag = BigDecimal.valueOf(reszvetelek)
				.divide(BigDecimal.valueOf(kepviselok), 2, RoundingMode.HALF_UP);
		return new KepviseloReszvetelAtlagResponseDto(atlag);
	}

	private Instant napKezdete(LocalDate nap) {
		return nap.atStartOfDay(ZoneOffset.UTC).toInstant();
	}

	private NapiSzavazasDto toNapiSzavazasDto(SzavazasEntity szavazas) {
		SzavazasEredmenyResponseDto eredmeny = eredmenyCalculator.calculate(szavazas);
		List<SzavazatDto> szavazatok = szavazas.getSzavazatok().stream()
				.map(szavazat -> new SzavazatDto(szavazat.getKepviselo(), szavazat.getSzavazat()))
				.toList();
		return new NapiSzavazasDto(
				szavazas.getIdopont(),
				szavazas.getTargy(),
				szavazas.getTipus(),
				szavazas.getEljaras(),
				szavazas.getElnok(),
				eredmeny.getEredmeny(),
				eredmeny.getKepviselokSzama(),
				szavazatok);
	}

	private SzavazasEntity findSzavazasOrThrow(String szavazasId) {
		return szavazasJpaRepository.findById(szavazasId)
				.orElseThrow(() -> new ApplicationException(
						ErrorCode.SZAVAZAS_NEM_TALALHATO,
						HttpStatus.NOT_FOUND,
						"Nincs szavazás a megadott azonosítóval."));
	}
}
