package hu.ogyhivatal.voting.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KepviseloReszvetelAtlagResponseDto {

	private BigDecimal atlag;
}
