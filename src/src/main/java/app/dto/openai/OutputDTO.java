package app.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputDTO {
    @JsonProperty("type")
    String type;

    @JsonProperty("role")
    String role;

    @JsonProperty("content")
    List<ContentOpenAiDTO> content;
}
