package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.dto.KepviseloReszvetelAtlagResponseDto;
import hu.ogyhivatal.voting.dto.KulonlegesEljarasSzamDto;
import hu.ogyhivatal.voting.dto.KulonlegesEljarasokSzamaResponseDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasokResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.dto.SzavazatDto;
import hu.ogyhivatal.voting.dto.SzavazatLekerdezesResponseDto;
import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.entity.SzavazatEntity;
import hu.ogyhivatal.voting.enums.EljarasTipus;
import hu.ogyhivatal.voting.enums.EredmenyTipus;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SzavazasServiceImpl implements SzavazasService {

	private static final String OSSZES = "összes";
	private static final List<EljarasTipus> KULONLEGES_ELJARASOK = List.of(
			EljarasTipus.SURGOSSEGI,
			EljarasTipus.KIVETELES,
			EljarasTipus.SZABALYZATTOL_ELTERO);

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
		Idoszak idoszak = idoszak(kezdet, veg);
		List<SzavazasEntity> szavazasok = szavazasJpaRepository
				.findByIdopontGreaterThanEqualAndIdopontLessThanAndTipusNot(
						idoszak.kezdet(),
						idoszak.veg(),
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

	@Override
	@Transactional(readOnly = true)
	public KulonlegesEljarasokSzamaResponseDto kulonlegesEljarasokSzama(LocalDate kezdet, LocalDate veg) {
		Idoszak idoszak = idoszak(kezdet, veg);
		Map<EljarasTipus, Map<EredmenyTipus, Integer>> szamlalok = new EnumMap<>(EljarasTipus.class);
		szavazasJpaRepository
				.findByIdopontGreaterThanEqualAndIdopontLessThanAndEljarasIn(
						idoszak.kezdet(),
						idoszak.veg(),
						KULONLEGES_ELJARASOK)
				.forEach(szavazas -> szamlalok
						.computeIfAbsent(szavazas.getEljaras(), kulcs -> new EnumMap<>(EredmenyTipus.class))
						.merge(eredmenyCalculator.calculate(szavazas).getEredmeny(), 1, Integer::sum));

		List<KulonlegesEljarasSzamDto> sorok = new ArrayList<>();
		int osszesElfogadott = 0;
		int osszesElutasitott = 0;
		for (EljarasTipus eljaras : KULONLEGES_ELJARASOK) {
			int elfogadott = szam(szamlalok, eljaras, EredmenyTipus.ELFOGADOTT);
			int elutasitott = szam(szamlalok, eljaras, EredmenyTipus.ELUTASITOTT);
			sorok.add(new KulonlegesEljarasSzamDto(eljaras.getKod(), EredmenyTipus.ELFOGADOTT.getKod(), elfogadott));
			sorok.add(new KulonlegesEljarasSzamDto(eljaras.getKod(), EredmenyTipus.ELUTASITOTT.getKod(), elutasitott));
			osszesElfogadott += elfogadott;
			osszesElutasitott += elutasitott;
		}
		sorok.add(new KulonlegesEljarasSzamDto(OSSZES, EredmenyTipus.ELFOGADOTT.getKod(), osszesElfogadott));
		sorok.add(new KulonlegesEljarasSzamDto(OSSZES, EredmenyTipus.ELUTASITOTT.getKod(), osszesElutasitott));
		sorok.add(new KulonlegesEljarasSzamDto(OSSZES, OSSZES, osszesElfogadott + osszesElutasitott));
		return new KulonlegesEljarasokSzamaResponseDto(sorok);
	}

	private int szam(
			Map<EljarasTipus, Map<EredmenyTipus, Integer>> szamlalok,
			EljarasTipus eljaras,
			EredmenyTipus eredmeny) {
		return szamlalok.getOrDefault(eljaras, Map.of()).getOrDefault(eredmeny, 0);
	}

	private Idoszak idoszak(LocalDate kezdet, LocalDate veg) {
		if (veg.isBefore(kezdet)) {
			throw new ApplicationException(
					ErrorCode.IDOSZAK_ERVENYTELEN,
					HttpStatus.BAD_REQUEST,
					"Az időszak vége nem lehet korábbi, mint a kezdete.");
		}
		return new Idoszak(napKezdete(kezdet), napKezdete(veg.plusDays(1)));
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

	private record Idoszak(Instant kezdet, Instant veg) {
	}

	private SzavazasEntity findSzavazasOrThrow(String szavazasId) {
		return szavazasJpaRepository.findById(szavazasId)
				.orElseThrow(() -> new ApplicationException(
						ErrorCode.SZAVAZAS_NEM_TALALHATO,
						HttpStatus.NOT_FOUND,
						"Nincs szavazás a megadott azonosítóval."));
	}
}
