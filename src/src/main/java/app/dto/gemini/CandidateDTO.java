package app.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class CandidateDTO {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("content")
    ContentDTO content;
}
