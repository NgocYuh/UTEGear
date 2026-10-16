package com.utegear.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.utegear.entity.Role;
import com.utegear.entity.User;
import com.utegear.security.CustomUserDetails;
import com.utegear.security.CustomUserDetailsService;
import com.utegear.security.JwtService;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

class StompJwtChannelInterceptorTest {

    @Test
    void authenticatesConnectFrameFromBearerToken() {
        JwtService jwtService = mock(JwtService.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        CustomUserDetails details = details();
        when(jwtService.extractSubject("valid-token")).thenReturn("owner@example.test");
        when(users.loadUserByUsername("owner@example.test")).thenReturn(details);
        when(jwtService.isValid("valid-token", details)).thenReturn(true);
        StompJwtChannelInterceptor interceptor = new StompJwtChannelInterceptor(jwtService, users);
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid-token");
        AtomicReference<java.security.Principal> sessionUser = new AtomicReference<>();
        accessor.setUserChangeCallback(sessionUser::set);
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        assertThat(result.getHeaders().get(SimpMessageHeaderAccessor.USER_HEADER))
                .isInstanceOf(java.security.Principal.class)
                .extracting(value -> ((java.security.Principal) value).getName())
                .isEqualTo("owner@example.test");
        assertThat(sessionUser.get()).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    }

    @Test
    void rejectsConnectFrameWithoutToken() {
        StompJwtChannelInterceptor interceptor = new StompJwtChannelInterceptor(
                mock(JwtService.class), mock(CustomUserDetailsService.class));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void rejectsWildcardAllowedOrigin() {
        assertThatThrownBy(() -> new WebSocketConfig(
                mock(StompJwtChannelInterceptor.class), "http://localhost:8080,*"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void customerCannotSubscribeAdminOrderTopic() {
        StompJwtChannelInterceptor interceptor = new StompJwtChannelInterceptor(
                mock(JwtService.class), mock(CustomUserDetailsService.class));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        CustomUserDetails customer = details(Role.CUSTOMER);
        accessor.setUser(new UsernamePasswordAuthenticationToken(customer, null, customer.getAuthorities()));
        accessor.setDestination("/topic/admin/orders");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(MessagingException.class);
    }

    @Test
    void adminCanSubscribeAdminOrderTopic() {
        StompJwtChannelInterceptor interceptor = new StompJwtChannelInterceptor(
                mock(JwtService.class), mock(CustomUserDetailsService.class));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        CustomUserDetails admin = details(Role.ADMIN);
        accessor.setUser(new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));
        accessor.setDestination("/topic/admin/orders");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThat(interceptor.preSend(message, mock(MessageChannel.class))).isSameAs(message);
    }

    @Test
    void authenticatedEmployeeCanSubscribeSupportQueue() {
        StompJwtChannelInterceptor interceptor = new StompJwtChannelInterceptor(
                mock(JwtService.class), mock(CustomUserDetailsService.class));
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        CustomUserDetails employee = details(Role.EMPLOYEE);
        accessor.setUser(new UsernamePasswordAuthenticationToken(employee, null, employee.getAuthorities()));
        accessor.setDestination("/user/queue/support");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThat(interceptor.preSend(message, mock(MessageChannel.class))).isSameAs(message);
    }

    private CustomUserDetails details() {
        return details(Role.CUSTOMER);
    }

    private CustomUserDetails details(String role) {
        User user = new User("Owner", "owner@example.test", null, "hash");
        user.getRoles().addAll(List.of(new Role(role)));
        return new CustomUserDetails(user);
    }
}
