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

		SzavazasEntity jelenlet = SzavazasEntity.builder()
				.id("TE1")
				.idopont(Instant.parse("2023-09-28T11:06:25Z"))
				.targy("Szavazás tárgya")
				.tipus(SzavazasTipus.JELENLET)
				.eljaras(EljarasTipus.NORMAL)
				.elnok("Kepviselo1")
				.build();
		jelenlet.addSzavazat(new SzavazatEntity("Kepviselo1", SzavazatErtek.IGEN));
		jelenlet.addSzavazat(new SzavazatEntity("Kepviselo2", SzavazatErtek.NEM));
		jelenlet.addSzavazat(new SzavazatEntity("Kepviselo3", SzavazatErtek.TARTAZKODAS));
		szavazasJpaRepository.save(jelenlet);

		SzavazasEntity egyszeru = SzavazasEntity.builder()
				.id("TE2")
				.idopont(Instant.parse("2023-09-28T14:30:00Z"))
				.targy("Egyszerű többségi szavazás")
				.tipus(SzavazasTipus.EGYSZERU)
				.eljaras(EljarasTipus.SURGOSSEGI)
				.elnok("Kepviselo1")
				.build();
		egyszeru.addSzavazat(new SzavazatEntity("Kepviselo1", SzavazatErtek.IGEN));
		egyszeru.addSzavazat(new SzavazatEntity("Kepviselo2", SzavazatErtek.IGEN));
		egyszeru.addSzavazat(new SzavazatEntity("Kepviselo3", SzavazatErtek.NEM));
		szavazasJpaRepository.save(egyszeru);
	}
}
