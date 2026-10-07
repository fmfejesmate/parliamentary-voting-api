package hu.ogyhivatal.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KulonlegesEljarasSzamDto {

	private String eljaras;
	private String eredmeny;
	private int szam;
}
