package hu.ogyhivatal.voting.controller;

import hu.ogyhivatal.voting.dto.ErrorResponseDto;
import hu.ogyhivatal.voting.dto.KepviseloReszvetelAtlagResponseDto;
import hu.ogyhivatal.voting.dto.NapiSzavazasokResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasEredmenyResponseDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasRequestDto;
import hu.ogyhivatal.voting.dto.SzavazasLetrehozasResponseDto;
import hu.ogyhivatal.voting.dto.SzavazatLekerdezesResponseDto;
import hu.ogyhivatal.voting.service.SzavazasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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

	@GetMapping("/szavazat")
	@Operation(summary = "Képviselő szavazatának lekérdezése")
	@ApiResponse(responseCode = "200", description = "A képviselő leadott szavazata",
			content = @Content(schema = @Schema(implementation = SzavazatLekerdezesResponseDto.class)))
	@ApiResponse(responseCode = "404", description = "Nincs ilyen szavazás vagy a képviselő nem szavazott",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	public SzavazatLekerdezesResponseDto szavazat(
			@Parameter(description = "A szavazás azonosítója", required = true)
			@RequestParam("szavazas") String szavazas,
			@Parameter(description = "A képviselő azonosítója", required = true)
			@RequestParam("kepviselo") String kepviselo) {
		return szavazasService.szavazatLekerdez(szavazas, kepviselo);
	}

	@GetMapping("/eredmeny")
	@Operation(summary = "Szavazás eredményének lekérdezése")
	@ApiResponse(responseCode = "200", description = "A szavazás eredménye",
			content = @Content(schema = @Schema(implementation = SzavazasEredmenyResponseDto.class)))
	@ApiResponse(responseCode = "404", description = "Nincs ilyen szavazás",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	public SzavazasEredmenyResponseDto eredmeny(
			@Parameter(description = "A szavazás azonosítója", required = true)
			@RequestParam("szavazas") String szavazas) {
		return szavazasService.eredmenyLekerdez(szavazas);
	}

	@GetMapping("/napi-szavazasok")
	@Operation(summary = "Adott nap szavazásainak és eredményeinek lekérdezése")
	@ApiResponse(responseCode = "200", description = "A nap szavazásai",
			content = @Content(schema = @Schema(implementation = NapiSzavazasokResponseDto.class)))
	public NapiSzavazasokResponseDto napiSzavazasok(
			@Parameter(description = "A nap (ISO dátum, pl. 2023-09-28)", required = true, example = "2023-09-28")
			@RequestParam("nap")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate nap) {
		return szavazasService.napiSzavazasok(nap);
	}

	@GetMapping("/kepviselo-reszvetel-atlag")
	@Operation(summary = "Képviselők átlagos részvétele egy időszakban")
	@ApiResponse(responseCode = "200", description = "Átlagos részvétel, jelenléti szavazások nélkül",
			content = @Content(schema = @Schema(implementation = KepviseloReszvetelAtlagResponseDto.class)))
	@ApiResponse(responseCode = "400", description = "Érvénytelen időszak",
			content = @Content(schema = @Schema(implementation = ErrorResponseDto.class)))
	public KepviseloReszvetelAtlagResponseDto kepviseloReszvetelAtlag(
			@Parameter(description = "Időszak kezdete (ISO dátum)", required = true, example = "2023-09-28")
			@RequestParam("kezdet")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate kezdet,
			@Parameter(description = "Időszak vége (ISO dátum, a nap beleszámít)", required = true, example = "2023-09-28")
			@RequestParam("veg")
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
			LocalDate veg) {
		return szavazasService.kepviseloReszvetelAtlag(kezdet, veg);
	}
}
