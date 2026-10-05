package hu.ogyhivatal.voting.controller;

import com.jayway.jsonpath.JsonPath;
import hu.ogyhivatal.voting.repository.SzavazasJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SzavazasControllerTest {

	private static final String VALID_BODY = """
			{
			  "idopont": "2023-09-28T11:06:25Z",
			  "targy": "Szavazás tárgya",
			  "tipus": "j",
			  "eljaras": "n",
			  "elnok": "Kepviselo1",
			  "szavazatok": [
			    { "kepviselo": "Kepviselo1", "szavazat": "i" },
			    { "kepviselo": "Kepviselo2", "szavazat": "n" },
			    { "kepviselo": "Kepviselo3", "szavazat": "t" }
			  ]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SzavazasJpaRepository szavazasJpaRepository;

	@BeforeEach
	void clearStore() {
		szavazasJpaRepository.deleteAll();
	}

	@Test
	void validSzavazasIsSavedAndReturnsUrlFriendlyId() throws Exception {
		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.szavazasId").value(matchesPattern("^[A-Z]{2}[0-9]+$")));
	}

	@Test
	void missingRequiredFieldReturnsStructureError() throws Exception {
		String body = """
				{
				  "idopont": "2023-09-28T11:06:25Z",
				  "tipus": "j",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" }
				  ]
				}
				""";

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_INVALID_STRUCTURE"));
	}

	@Test
	void invalidTipusReturnsJsonError() throws Exception {
		String body = VALID_BODY.replace("\"j\"", "\"x\"");

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_INVALID_JSON"));
	}

	@Test
	void presidentWithoutVoteIsRejected() throws Exception {
		String body = """
				{
				  "idopont": "2023-09-28T11:06:25Z",
				  "targy": "Szavazás tárgya",
				  "tipus": "j",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo2", "szavazat": "n" }
				  ]
				}
				""";

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_ELNOK_NINCS_SZAVAZAT"));
	}

	@Test
	void duplicateRepresentativeIsRejected() throws Exception {
		String body = """
				{
				  "idopont": "2023-09-28T11:06:25Z",
				  "targy": "Szavazás tárgya",
				  "tipus": "j",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" },
				    { "kepviselo": "Kepviselo1", "szavazat": "n" }
				  ]
				}
				""";

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_KEPVISELO_TOBBSZOR_SZAVAZOTT"));
	}

	@Test
	void duplicateIdopontIsRejectedAndNothingIsDuplicated() throws Exception {
		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_IDOPONT_FOGLALT"));
	}

	@Test
	void failedValidationDoesNotPersist() throws Exception {
		String invalid = """
				{
				  "idopont": "2023-09-28T11:06:25Z",
				  "targy": "Szavazás tárgya",
				  "tipus": "j",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo2", "szavazat": "n" }
				  ]
				}
				""";

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(invalid))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated());
	}

	@Test
	void representativeVoteIsReturned() throws Exception {
		String szavazasId = createSzavazas();

		mockMvc.perform(get("/szavazasok/szavazat")
						.param("szavazas", szavazasId)
						.param("kepviselo", "Kepviselo1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.szavazat").value("i"));
	}

	@Test
	void unknownSzavazasReturnsNotFound() throws Exception {
		mockMvc.perform(get("/szavazasok/szavazat")
						.param("szavazas", "XX999")
						.param("kepviselo", "Kepviselo1"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_NEM_TALALHATO"));
	}

	@Test
	void representativeWithoutVoteReturnsNotFound() throws Exception {
		String szavazasId = createSzavazas();

		mockMvc.perform(get("/szavazasok/szavazat")
						.param("szavazas", szavazasId)
						.param("kepviselo", "Kepviselo99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("KEPVISELO_SZAVAZAT_NEM_TALALHATO"));
	}

	@Test
	void jelenletEredmenyIsAlwaysAccepted() throws Exception {
		String szavazasId = createSzavazas();

		mockMvc.perform(get("/szavazasok/eredmeny")
						.param("szavazas", szavazasId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eredmeny").value("F"))
				.andExpect(jsonPath("$.kepviselokSzama").value(3))
				.andExpect(jsonPath("$.igenekSzama").value(1))
				.andExpect(jsonPath("$.nemekSzama").value(1))
				.andExpect(jsonPath("$.tartozkodasokSzama").value(1));
	}

	@Test
	void egyszeruEredmenyUsesPreviousJelenletCount() throws Exception {
		createSzavazas();
		String egyszeruId = createSzavazas("""
				{
				  "idopont": "2023-09-28T14:30:00Z",
				  "targy": "Egyszerű szavazás",
				  "tipus": "e",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" },
				    { "kepviselo": "Kepviselo2", "szavazat": "i" },
				    { "kepviselo": "Kepviselo3", "szavazat": "n" }
				  ]
				}
				""");

		mockMvc.perform(get("/szavazasok/eredmeny")
						.param("szavazas", egyszeruId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eredmeny").value("F"))
				.andExpect(jsonPath("$.kepviselokSzama").value(3))
				.andExpect(jsonPath("$.igenekSzama").value(2))
				.andExpect(jsonPath("$.nemekSzama").value(1))
				.andExpect(jsonPath("$.tartozkodasokSzama").value(0));
	}

	@Test
	void minositettEredmenyUsesAllRepresentatives() throws Exception {
		String minositettId = createSzavazas("""
				{
				  "idopont": "2023-09-28T16:00:00Z",
				  "targy": "Minősített szavazás",
				  "tipus": "m",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" },
				    { "kepviselo": "Kepviselo2", "szavazat": "i" },
				    { "kepviselo": "Kepviselo3", "szavazat": "i" }
				  ]
				}
				""");

		mockMvc.perform(get("/szavazasok/eredmeny")
						.param("szavazas", minositettId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eredmeny").value("U"))
				.andExpect(jsonPath("$.kepviselokSzama").value(200))
				.andExpect(jsonPath("$.igenekSzama").value(3))
				.andExpect(jsonPath("$.nemekSzama").value(0))
				.andExpect(jsonPath("$.tartozkodasokSzama").value(0));
	}

	@Test
	void unknownSzavazasEredmenyReturnsNotFound() throws Exception {
		mockMvc.perform(get("/szavazasok/eredmeny")
						.param("szavazas", "XX999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.errorCode").value("SZAVAZAS_NEM_TALALHATO"));
	}

	@Test
	void egyszeruWithoutPreviousJelenletIsRejectedWithZeroPresent() throws Exception {
		String egyszeruId = createSzavazas("""
				{
				  "idopont": "2023-09-28T14:30:00Z",
				  "targy": "Egyszerű szavazás",
				  "tipus": "e",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" },
				    { "kepviselo": "Kepviselo2", "szavazat": "n" }
				  ]
				}
				""");

		mockMvc.perform(get("/szavazasok/eredmeny")
						.param("szavazas", egyszeruId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.eredmeny").value("U"))
				.andExpect(jsonPath("$.kepviselokSzama").value(0))
				.andExpect(jsonPath("$.igenekSzama").value(1))
				.andExpect(jsonPath("$.nemekSzama").value(1))
				.andExpect(jsonPath("$.tartozkodasokSzama").value(0));
	}

	@Test
	void napiSzavazasokReturnsVotesForDayOrderedByIdopont() throws Exception {
		createSzavazas();
		createSzavazas("""
				{
				  "idopont": "2023-09-28T14:30:00Z",
				  "targy": "Egyszerű szavazás",
				  "tipus": "e",
				  "eljaras": "s",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" },
				    { "kepviselo": "Kepviselo2", "szavazat": "i" },
				    { "kepviselo": "Kepviselo3", "szavazat": "n" }
				  ]
				}
				""");
		createSzavazas("""
				{
				  "idopont": "2023-09-29T10:00:00Z",
				  "targy": "Másik nap",
				  "tipus": "j",
				  "eljaras": "n",
				  "elnok": "Kepviselo1",
				  "szavazatok": [
				    { "kepviselo": "Kepviselo1", "szavazat": "i" }
				  ]
				}
				""");

		mockMvc.perform(get("/szavazasok/napi-szavazasok")
						.param("nap", "2023-09-28"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.szavazasok.length()").value(2))
				.andExpect(jsonPath("$.szavazasok[0].idopont").value("2023-09-28T11:06:25Z"))
				.andExpect(jsonPath("$.szavazasok[0].tipus").value("j"))
				.andExpect(jsonPath("$.szavazasok[0].eredmeny").value("F"))
				.andExpect(jsonPath("$.szavazasok[0].kepviselokSzama").value(3))
				.andExpect(jsonPath("$.szavazasok[0].szavazatok.length()").value(3))
				.andExpect(jsonPath("$.szavazasok[1].idopont").value("2023-09-28T14:30:00Z"))
				.andExpect(jsonPath("$.szavazasok[1].tipus").value("e"))
				.andExpect(jsonPath("$.szavazasok[1].eljaras").value("s"))
				.andExpect(jsonPath("$.szavazasok[1].eredmeny").value("F"))
				.andExpect(jsonPath("$.szavazasok[1].kepviselokSzama").value(3));
	}

	@Test
	void napiSzavazasokReturnsEmptyListWhenNoVotes() throws Exception {
		mockMvc.perform(get("/szavazasok/napi-szavazasok")
						.param("nap", "2020-01-01"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.szavazasok.length()").value(0));
	}

	private String createSzavazas() throws Exception {
		return createSzavazas(VALID_BODY);
	}

	private String createSzavazas(String body) throws Exception {
		String response = mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(response, "$.szavazasId");
	}
}
