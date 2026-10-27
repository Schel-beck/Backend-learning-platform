package app.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiResponseDTO {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("candidates")
    private List<CandidateDTO> candidates;
}
