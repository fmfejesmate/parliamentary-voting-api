package hu.ogyhivatal.voting.repository;

import hu.ogyhivatal.voting.entity.SzavazasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface SzavazasJpaRepository extends JpaRepository<SzavazasEntity, String> {

	boolean existsByIdopont(Instant idopont);
}
