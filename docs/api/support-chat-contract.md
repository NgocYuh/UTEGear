# Support chat contract

Task: `CHAT-BE-01`. All routes require a JWT. A customer starts one open conversation per store; a staff member can only claim and answer conversations for the store assigned to that employee account.

## HTTP API

| Method | Route | Role | Purpose |
|---|---|---|---|
| POST | `/api/support/conversations` | CUSTOMER | Start or return the current open conversation for `{ "storeId": 1 }`. |
| GET | `/api/support/conversations` | CUSTOMER | List the caller's conversations. |
| GET | `/api/support/conversations/{id}` | Customer participant, assigned EMPLOYEE, ADMIN | Conversation and messages. |
| POST | `/api/support/conversations/{id}/messages` | Customer participant or assigned EMPLOYEE | Send `{ "content": "..." }`, maximum 2,000 characters. |
| PUT | `/api/support/conversations/{id}/close` | A participant or ADMIN | Close the conversation. |
| GET | `/api/support/employee/conversations` | EMPLOYEE | Assigned conversations plus unassigned open conversations of that employee's store. |
| PUT | `/api/support/employee/conversations/{id}/claim` | EMPLOYEE | Claim an unassigned open conversation for the employee's assigned store. |
| PUT | `/api/admin/users/{id}/assigned-store` | ADMIN | Assign an EMPLOYEE to `{ "storeId": 1 }`; `storeId: null` clears the assignment. |

`EMPLOYEE` is a role in addition to `ADMIN` and `CUSTOMER`. Removing `EMPLOYEE` from an account automatically clears its assigned store.

## Realtime

Authenticated STOMP clients subscribe to `/user/queue/support`. The server emits the established `RealtimeEvent` envelope with:

- `SUPPORT_MESSAGE_CREATED`: a `MessageView` for the customer and the assigned employee.
- `SUPPORT_CONVERSATION_ASSIGNED`: a `ConversationView` for the employee.

The client must fetch the conversation detail again after reconnecting; WebSocket events are notifications, not the source of truth.
