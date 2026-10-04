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

	private String createSzavazas() throws Exception {
		String response = mockMvc.perform(post("/szavazasok/szavazas")
						.contentType(MediaType.APPLICATION_JSON)
						.content(VALID_BODY))
				.andExpect(status().isCreated())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return JsonPath.read(response, "$.szavazasId");
	}
}
