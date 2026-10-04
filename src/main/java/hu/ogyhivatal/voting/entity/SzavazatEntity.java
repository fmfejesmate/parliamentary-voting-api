package hu.ogyhivatal.voting.entity;

import hu.ogyhivatal.voting.enums.SzavazatErtek;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "szavazat")
public class SzavazatEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String kepviselo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SzavazatErtek szavazat;

	@ManyToOne(optional = false)
	@JoinColumn(name = "szavazas_id", nullable = false)
	private SzavazasEntity szavazas;

	public SzavazatEntity(String kepviselo, SzavazatErtek szavazat) {
		this.kepviselo = kepviselo;
		this.szavazat = szavazat;
	}
}
