package kr.ac.dankook.ace.erp.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Getter
@Setter
@NoArgsConstructor
public class Approval {
    @Id
    private String requestId;
    private Integer requesterId;
    private String title;
    private String content;
    private List<Step> steps;
    private String finalStatus;
    private LocalDateTime createAt;
}
