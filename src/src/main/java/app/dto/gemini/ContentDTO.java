package app.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;
@Getter
public class ContentDTO {
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("parts")
    List<PartDTO> parts;
}
