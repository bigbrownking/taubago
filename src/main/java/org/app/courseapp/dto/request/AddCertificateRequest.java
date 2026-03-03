package org.app.courseapp.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AddCertificateRequest {
    private String title;
    private LocalDate issuedAt;
}
