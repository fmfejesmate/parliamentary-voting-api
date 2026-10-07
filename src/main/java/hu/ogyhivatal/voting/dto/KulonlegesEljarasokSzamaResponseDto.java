package hu.ogyhivatal.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KulonlegesEljarasokSzamaResponseDto {

	private List<KulonlegesEljarasSzamDto> szavazasok;
}
