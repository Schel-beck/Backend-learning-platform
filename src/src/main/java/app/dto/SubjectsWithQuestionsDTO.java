package app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
@AllArgsConstructor
@Getter
@NoArgsConstructor
public class SubjectsWithQuestionsDTO {
    SubjectDTO subject;
    List<QuestionDTO> questions;
}
