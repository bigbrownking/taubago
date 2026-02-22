package org.app.courseapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expert")
@Tag(name = "Expert Management", description = "APIs for expert management operations")
public class ExpertController {

}
