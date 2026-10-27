package app.dto.openai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContentOpenAiDTO {
    @JsonProperty("type")
    String type;

    @JsonProperty("text")
    String text;
}
