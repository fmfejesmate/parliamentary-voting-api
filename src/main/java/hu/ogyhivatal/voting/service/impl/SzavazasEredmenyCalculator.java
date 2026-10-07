package hu.ogyhivatal.voting.service.impl;

import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.entity.SzavazatEntity;
import hu.ogyhivatal.voting.enums.EredmenyTipus;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import hu.ogyhivatal.voting.enums.SzavazatErtek;
import hu.ogyhivatal.voting.repository.SzavazasJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SzavazasEredmenyCalculator {

	private final SzavazasJpaRepository szavazasJpaRepository;

	@Value("${app.kepviselok.osszesen:200}")
	private int osszesKepviselo;

	public SzavazasEredmenyResponseDto calculate(SzavazasEntity szavazas) {
		int igenek = count(szavazas, SzavazatErtek.IGEN);
		int nemek = count(szavazas, SzavazatErtek.NEM);
		int tartozkodasok = count(szavazas, SzavazatErtek.TARTAZKODAS);
		int kepviselokSzama = resolveKepviselokSzama(szavazas);
		EredmenyTipus eredmeny = resolveEredmeny(szavazas.getTipus(), igenek, kepviselokSzama);
		return new SzavazasEredmenyResponseDto(eredmeny, kepviselokSzama, igenek, nemek, tartozkodasok);
	}

	private int resolveKepviselokSzama(SzavazasEntity szavazas) {
		return switch (szavazas.getTipus()) {
			case JELENLET -> szavazas.getSzavazatok().size();
			case EGYSZERU -> jelenlevokSzama(szavazas);
			case MINOSITETT -> osszesKepviselo;
		};
	}

	private int jelenlevokSzama(SzavazasEntity szavazas) {
		return szavazasJpaRepository
				.findFirstByTipusAndIdopontBeforeOrderByIdopontDesc(SzavazasTipus.JELENLET, szavazas.getIdopont())
				.map(elozo -> elozo.getSzavazatok().size())
				.orElse(0);
	}

	private EredmenyTipus resolveEredmeny(SzavazasTipus tipus, int igenek, int kepviselokSzama) {
		if (tipus == SzavazasTipus.JELENLET) {
			return EredmenyTipus.ELFOGADOTT;
		}
		if (kepviselokSzama <= 0) {
			return EredmenyTipus.ELUTASITOTT;
		}
		return igenek > kepviselokSzama / 2.0
				? EredmenyTipus.ELFOGADOTT
				: EredmenyTipus.ELUTASITOTT;
	}

	private int count(SzavazasEntity szavazas, SzavazatErtek ertek) {
		return (int) szavazas.getSzavazatok().stream()
				.map(SzavazatEntity::getSzavazat)
				.filter(ertek::equals)
				.count();
	}
}
