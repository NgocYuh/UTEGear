package com.utegear.controller;

import com.utegear.dto.consultant.ConsultantDtos.*;
import com.utegear.service.GearConsultantService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/consultant")
public class GearConsultantController {

    private final GearConsultantService service;

    public GearConsultantController(GearConsultantService service) {
        this.service = service;
    }

    @GetMapping("/quick-prompts")
    public List<QuickPrompt> getQuickPrompts() {
        return service.getQuickPrompts();
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return service.consult(request);
    }
}

