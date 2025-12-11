package kr.ac.dankook.ace.erp.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Step {
    private Integer step;
    private Integer approverId;
    private String status;
}
