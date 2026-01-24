package org.app.courseapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type;
    private String username;
    private String authType;
}
