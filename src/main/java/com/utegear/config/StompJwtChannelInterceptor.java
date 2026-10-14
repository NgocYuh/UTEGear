package com.utegear.config;

import com.utegear.security.CustomUserDetailsService;
import com.utegear.security.JwtService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class StompJwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public StompJwtChannelInterceptor(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new MessagingException("Không đọc được STOMP frame");
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            validateSubscription(accessor);
            return message;
        }
        if (accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new MessagingException("Thiếu JWT cho kết nối STOMP");
        }
        try {
            String token = authorization.substring(7).trim();
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.extractSubject(token));
            if (!jwtService.isValid(token, userDetails)) {
                throw new MessagingException("JWT STOMP không hợp lệ");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(authentication);
            return message;
        } catch (RuntimeException exception) {
            if (exception instanceof MessagingException messagingException) {
                throw messagingException;
            }
            throw new MessagingException("JWT STOMP không hợp lệ", exception);
        }
    }

    private void validateSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (accessor.getUser() == null) {
            throw new MessagingException("Kết nối STOMP chưa được xác thực");
        }
        if ("/topic/admin/orders".equals(destination)) {
            if (!(accessor.getUser() instanceof UsernamePasswordAuthenticationToken authentication)
                    || authentication.getAuthorities().stream()
                            .noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))) {
                throw new MessagingException("Không có quyền subscribe topic quản trị");
            }
            return;
        }
        if (!"/user/queue/orders".equals(destination)
                && !"/user/queue/notifications".equals(destination)) {
            throw new MessagingException("STOMP destination không được phép");
        }
    }
}
