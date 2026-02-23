package org.app.courseapp.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCuratorRequest {
    private String name;
    private String surname;
    private String phoneNumber;
    private String email;
}
