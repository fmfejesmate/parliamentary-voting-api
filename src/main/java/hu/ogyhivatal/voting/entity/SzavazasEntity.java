package hu.ogyhivatal.voting.entity;

import hu.ogyhivatal.voting.enums.EljarasTipus;
import hu.ogyhivatal.voting.enums.SzavazasTipus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "szavazas")
public class SzavazasEntity {

	@Id
	private String id;

	@Column(nullable = false, unique = true)
	private Instant idopont;

	@Column(nullable = false)
	private String targy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SzavazasTipus tipus;

	@Enumerated(EnumType.STRING)
	private EljarasTipus eljaras;

	@Column(nullable = false)
	private String elnok;

	@Builder.Default
	@OneToMany(mappedBy = "szavazas", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<SzavazatEntity> szavazatok = new ArrayList<>();

	public void addSzavazat(SzavazatEntity szavazat) {
		szavazat.setSzavazas(this);
		szavazatok.add(szavazat);
	}
}
