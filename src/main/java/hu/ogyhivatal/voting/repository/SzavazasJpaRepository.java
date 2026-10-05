package hu.ogyhivatal.voting.repository;

import hu.ogyhivatal.voting.entity.SzavazasEntity;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SzavazasJpaRepository extends JpaRepository<SzavazasEntity, String> {

	boolean existsByIdopont(Instant idopont);

	Optional<SzavazasEntity> findFirstByTipusAndIdopontBeforeOrderByIdopontDesc(
			SzavazasTipus tipus,
			Instant idopont);

	List<SzavazasEntity> findByIdopontGreaterThanEqualAndIdopontLessThanOrderByIdopontAsc(
			Instant kezdet,
			Instant veg);
}
