package org.app.courseapp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CertificateDto {
    private Long id;
    private String title;
    private boolean verified;
    private LocalDate issuedAt;
    private String documentUrl;
}
