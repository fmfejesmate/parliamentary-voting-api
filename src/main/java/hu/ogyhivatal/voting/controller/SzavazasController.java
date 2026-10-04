package hu.ogyhivatal.voting.controller;

import hu.ogyhivatal.voting.dto.ErrorResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.service.SzavazasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/szavazasok")
@Tag(name = "Szavazások")
@RequiredArgsConstructor
public class SzavazasController {

	private final SzavazasService szavazasService;

	@PostMapping("/szavazas")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Szavazás adatainak rögzítése")
	@ApiResponse(responseCode = "201", description = "A szavazás rögzítve",
			content = @Content(schema = @Schema(implementation = SzavazasLetrehozasResponseDto.class)))
	@ApiResponse(responseCode = "400", description = "Érvénytelen kérés",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	@ApiResponse(responseCode = "409", description = "Az időpont foglalt",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	public SzavazasLetrehozasResponseDto letrehoz(@Valid @RequestBody SzavazasLetrehozasRequestDto request) {
		return szavazasService.letrehoz(request);
	}
}
