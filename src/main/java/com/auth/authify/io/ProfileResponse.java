package com.auth.authify.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class ProfileResponse {

    private String userId;
    private String name;
    private String email;
    private Boolean isAccountVerified;

}
