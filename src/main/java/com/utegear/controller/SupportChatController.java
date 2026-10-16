package com.utegear.controller;

import com.utegear.dto.support.SupportChatDtos;
import com.utegear.security.CustomUserDetails;
import com.utegear.service.SupportChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class SupportChatController {
    private final SupportChatService service;
    public SupportChatController(SupportChatService service) { this.service = service; }
    @PostMapping("/conversations") @ResponseStatus(HttpStatus.CREATED)
    public SupportChatDtos.ConversationView start(@AuthenticationPrincipal CustomUserDetails principal, @Valid @RequestBody SupportChatDtos.StartConversationRequest request) { return service.start(principal.getUser(), request.storeId()); }
    @GetMapping("/conversations")
    public List<SupportChatDtos.ConversationView> list(@AuthenticationPrincipal CustomUserDetails principal) { return service.listForCustomer(principal.getUser()); }
    @GetMapping("/conversations/{id}")
    public SupportChatDtos.ConversationDetail detail(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) { return service.detail(principal.getUser(), id); }
    @PostMapping("/conversations/{id}/messages") @ResponseStatus(HttpStatus.CREATED)
    public SupportChatDtos.MessageView send(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id, @Valid @RequestBody SupportChatDtos.SendMessageRequest request) { return service.send(principal.getUser(), id, request.content()); }
    @PutMapping("/conversations/{id}/close")
    public SupportChatDtos.ConversationView close(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) { return service.close(principal.getUser(), id); }
    @GetMapping("/employee/conversations")
    public List<SupportChatDtos.ConversationView> listEmployee(@AuthenticationPrincipal CustomUserDetails principal) { return service.listForEmployee(principal.getUser()); }
    @PutMapping("/employee/conversations/{id}/claim")
    public SupportChatDtos.ConversationView claim(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) { return service.claim(principal.getUser(), id); }
}
