package hu.ogyhivatal.voting.config;

import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.entity.SzavazatEntity;
import hu.ogyhivatal.voting.enums.EljarasTipus;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import hu.ogyhivatal.voting.enums.SzavazatErtek;
import hu.ogyhivatal.voting.repository.SzavazasJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements ApplicationRunner {

	private final SzavazasJpaRepository szavazasJpaRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (szavazasJpaRepository.count() > 0) {
			return;
		}

		save("TE0", "2023-09-27T10:00:00Z", "Szavazás jelenlét előtt",
				SzavazasTipus.EGYSZERU, EljarasTipus.NORMAL, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN));

		save("TE1", "2023-09-28T11:06:25Z", "Szavazás tárgya",
				SzavazasTipus.JELENLET, EljarasTipus.NORMAL, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.NEM),
				szavazat("Kepviselo3", SzavazatErtek.TARTAZKODAS));

		save("TE2", "2023-09-28T14:30:00Z", "Egyszerű többségi szavazás",
				SzavazasTipus.EGYSZERU, EljarasTipus.SURGOSSEGI, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.IGEN),
				szavazat("Kepviselo3", SzavazatErtek.NEM));

		save("TE3", "2023-09-28T15:00:00Z", "Sürgősségi elutasított",
				SzavazasTipus.EGYSZERU, EljarasTipus.SURGOSSEGI, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.NEM),
				szavazat("Kepviselo2", SzavazatErtek.IGEN));

		save("TE4", "2023-09-28T15:30:00Z", "Kivételes elfogadott",
				SzavazasTipus.EGYSZERU, EljarasTipus.KIVETELES, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.IGEN),
				szavazat("Kepviselo3", SzavazatErtek.IGEN));

		save("TE5", "2023-09-28T16:00:00Z", "Kivételes elutasított",
				SzavazasTipus.EGYSZERU, EljarasTipus.KIVETELES, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo3", SzavazatErtek.NEM));

		save("TE6", "2023-09-28T16:30:00Z", "Szabályzattól eltérő elfogadott",
				SzavazasTipus.EGYSZERU, EljarasTipus.SZABALYZATTOL_ELTERO, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.IGEN),
				szavazat("Kepviselo3", SzavazatErtek.NEM));

		save("TE7", "2023-09-28T17:00:00Z", "Szabályzattól eltérő elutasított",
				SzavazasTipus.EGYSZERU, EljarasTipus.SZABALYZATTOL_ELTERO, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.NEM));

		save("TE8", "2023-09-28T17:30:00Z", "Minősített többség",
				SzavazasTipus.MINOSITETT, EljarasTipus.NORMAL, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.IGEN));

		save("TE9", "2023-09-29T09:00:00Z", "Másnapi jelenlét",
				SzavazasTipus.JELENLET, EljarasTipus.NORMAL, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo2", SzavazatErtek.IGEN),
				szavazat("Kepviselo4", SzavazatErtek.IGEN));

		save("TE10", "2023-09-29T10:00:00Z", "Másnapi egyszerű szavazás",
				SzavazasTipus.EGYSZERU, EljarasTipus.NORMAL, "Kepviselo1",
				szavazat("Kepviselo1", SzavazatErtek.IGEN),
				szavazat("Kepviselo4", SzavazatErtek.IGEN));
	}

	private void save(
			String id,
			String idopont,
			String targy,
			SzavazasTipus tipus,
			EljarasTipus eljaras,
			String elnok,
			SzavazatEntity... szavazatok) {
		SzavazasEntity szavazas = SzavazasEntity.builder()
				.id(id)
				.idopont(Instant.parse(idopont))
				.targy(targy)
				.tipus(tipus)
				.eljaras(eljaras)
				.elnok(elnok)
				.build();
		for (SzavazatEntity szavazat : szavazatok) {
			szavazas.addSzavazat(szavazat);
		}
		szavazasJpaRepository.save(szavazas);
	}

	private static SzavazatEntity szavazat(String kepviselo, SzavazatErtek ertek) {
		return new SzavazatEntity(kepviselo, ertek);
	}
}
