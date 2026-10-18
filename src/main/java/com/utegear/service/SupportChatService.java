package com.utegear.service;

import com.utegear.dto.support.SupportChatDtos;
import com.utegear.entity.Role;
import com.utegear.entity.Store;
import com.utegear.entity.SupportConversation;
import com.utegear.entity.SupportConversationStatus;
import com.utegear.entity.SupportMessage;
import com.utegear.entity.User;
import com.utegear.exception.ApiException;
import com.utegear.repository.StoreRepository;
import com.utegear.repository.SupportConversationRepository;
import com.utegear.repository.SupportMessageRepository;
import com.utegear.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportChatService {
    private final SupportConversationRepository conversations;
    private final SupportMessageRepository messages;
    private final StoreRepository stores;
    private final UserRepository users;
    private final SupportChatRealtimePublisher realtime;
    public SupportChatService(SupportConversationRepository conversations, SupportMessageRepository messages,
            StoreRepository stores, UserRepository users, SupportChatRealtimePublisher realtime) {
        this.conversations = conversations; this.messages = messages; this.stores = stores; this.users = users; this.realtime = realtime;
    }

    @Transactional
    public SupportChatDtos.ConversationView start(User customer, Long storeId) {
        requireRole(customer, Role.CUSTOMER, "Chỉ khách hàng có thể mở cuộc hội thoại hỗ trợ");
        Store store = stores.findByIdAndActiveTrue(storeId).orElseThrow(() -> missing("STORE_NOT_FOUND", "Không tìm thấy cửa hàng đang hoạt động"));
        SupportConversation conversation = conversations.findByCustomerIdAndStoreIdAndStatus(customer.getId(), storeId, SupportConversationStatus.OPEN)
                .orElseGet(() -> conversations.save(new SupportConversation(customer, store,
                        users.findEnabledByAssignedStoreIdAndRoleName(storeId, Role.EMPLOYEE).stream().findFirst().orElse(null))));
        if (conversation.getEmployee() != null) realtime.publishAssignedAfterCommit(conversation);
        return SupportChatDtos.ConversationView.from(conversation);
    }

    @Transactional(readOnly = true)
    public List<SupportChatDtos.ConversationView> listForCustomer(User customer) {
        requireRole(customer, Role.CUSTOMER, "Chỉ khách hàng có thể xem danh sách hỗ trợ");
        return conversations.findAllByCustomerIdOrderByUpdatedAtDesc(customer.getId()).stream().map(SupportChatDtos.ConversationView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SupportChatDtos.ConversationView> listForEmployee(User employee) {
        if (has(employee, Role.ADMIN)) {
            return conversations.findAllByOrderByUpdatedAtDesc().stream().map(SupportChatDtos.ConversationView::from).toList();
        }
        requireRole(employee, Role.EMPLOYEE, "Yêu cầu vai trò nhân viên");
        List<SupportConversation> result = new ArrayList<>(conversations.findAllByEmployeeIdOrderByUpdatedAtDesc(employee.getId()));
        if (employee.getAssignedStore() != null) result.addAll(conversations.findAllByStoreIdAndEmployeeIsNullAndStatusOrderByUpdatedAtDesc(employee.getAssignedStore().getId(), SupportConversationStatus.OPEN));
        return result.stream().map(SupportChatDtos.ConversationView::from).toList();
    }

    @Transactional(readOnly = true)
    public SupportChatDtos.ConversationDetail detail(User actor, Long id) {
        SupportConversation conversation = require(id); canRead(actor, conversation);
        return new SupportChatDtos.ConversationDetail(SupportChatDtos.ConversationView.from(conversation),
                messages.findAllByConversationIdOrderBySentAtAsc(id).stream().map(SupportChatDtos.MessageView::from).toList());
    }

    @Transactional
    public SupportChatDtos.ConversationView claim(User employee, Long id) {
        requireRole(employee, Role.EMPLOYEE, "Yêu cầu vai trò nhân viên");
        SupportConversation conversation = conversations.findLockedById(id).orElseThrow(() -> missing("SUPPORT_CONVERSATION_NOT_FOUND", "Không tìm thấy cuộc hội thoại"));
        employeeStore(employee, conversation);
        if (conversation.getStatus() != SupportConversationStatus.OPEN) throw invalid("Cuộc hội thoại đã đóng");
        if (conversation.getEmployee() != null && !conversation.getEmployee().getId().equals(employee.getId())) throw denied("Cuộc hội thoại đã được nhân viên khác nhận");
        conversation.assignEmployee(employee); conversation.touch();
        SupportConversation saved = conversations.save(conversation); realtime.publishAssignedAfterCommit(saved);
        return SupportChatDtos.ConversationView.from(saved);
    }

    @Transactional
    public SupportChatDtos.MessageView send(User actor, Long id, String content) {
        if (content == null || content.isBlank() || content.trim().length() > 2000) {
            throw invalid("Nội dung tin nhắn không được để trống và tối đa 2000 ký tự");
        }
        SupportConversation conversation = conversations.findLockedById(id).orElseThrow(() -> missing("SUPPORT_CONVERSATION_NOT_FOUND", "Không tìm thấy cuộc hội thoại"));
        canSend(actor, conversation);
        if (conversation.getStatus() != SupportConversationStatus.OPEN) throw invalid("Không thể gửi tin nhắn cho cuộc hội thoại đã đóng");
        SupportMessage message = messages.save(new SupportMessage(conversation, actor, content.trim()));
        conversation.touch(); conversations.save(conversation); realtime.publishMessageAfterCommit(message);
        return SupportChatDtos.MessageView.from(message);
    }

    @Transactional
    public SupportChatDtos.ConversationView close(User actor, Long id) {
        SupportConversation conversation = conversations.findLockedById(id).orElseThrow(() -> missing("SUPPORT_CONVERSATION_NOT_FOUND", "Không tìm thấy cuộc hội thoại"));
        canRead(actor, conversation); conversation.close();
        return SupportChatDtos.ConversationView.from(conversations.save(conversation));
    }

    private SupportConversation require(Long id) { return conversations.findById(id).orElseThrow(() -> missing("SUPPORT_CONVERSATION_NOT_FOUND", "Không tìm thấy cuộc hội thoại")); }
    private void canRead(User actor, SupportConversation value) {
        if (has(actor, Role.ADMIN) || value.getCustomer().getId().equals(actor.getId()) || (has(actor, Role.EMPLOYEE) && value.getEmployee() != null && value.getEmployee().getId().equals(actor.getId()))) return;
        if (has(actor, Role.EMPLOYEE) && value.getEmployee() == null) { employeeStore(actor, value); return; }
        throw denied("Bạn không có quyền xem cuộc hội thoại này");
    }
    private void canSend(User actor, SupportConversation value) {
        if (value.getCustomer().getId().equals(actor.getId()) || (has(actor, Role.EMPLOYEE) && value.getEmployee() != null && value.getEmployee().getId().equals(actor.getId()))) return;
        throw denied("Bạn không có quyền gửi tin nhắn trong cuộc hội thoại này");
    }
    private void employeeStore(User employee, SupportConversation value) {
        if (employee.getAssignedStore() == null || !employee.getAssignedStore().getId().equals(value.getStore().getId())) throw denied("Nhân viên chỉ được xử lý hội thoại của cửa hàng được phân công");
    }
    private void requireRole(User user, String role, String message) { if (!has(user, role)) throw denied(message); }
    private boolean has(User user, String role) { return user.getRoles().stream().anyMatch(item -> role.equals(item.getName())); }
    private ApiException invalid(String message) { return new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message); }
    private ApiException denied(String message) { return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message); }
    private ApiException missing(String code, String message) { return new ApiException(HttpStatus.NOT_FOUND, code, message); }
}
