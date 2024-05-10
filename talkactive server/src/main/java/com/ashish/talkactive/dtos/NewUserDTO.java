package com.ashish.talkactive.dtos;

import com.ashish.talkactive.models.Status;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class NewUserDTO {

    @Email
    @NotEmpty
    private String email;
    private String name;
    private String password;
    private String profileImage;
    private Status status;

    public NewUserDTO(String name, String email, String password, String profileImage) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.profileImage = profileImage;
    }
}
