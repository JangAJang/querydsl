package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(of = {"name", "ageField"})
public class UserDto {

    private String name;
    private String ageField;

    @QueryProjection
    public UserDto(String name, String ageField) {
        this.name = name;
        this.ageField = ageField;
    }


}
