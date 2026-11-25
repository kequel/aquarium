package pl.edu.pg.eti.kask.aquarium.chat;

import jakarta.enterprise.event.Event;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.security.CurrentUser;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.io.Serializable;
import java.time.LocalTime;
import java.util.List;

/**
 * Backing bean of the chat view (chat/chat.xhtml). Sending a message (an AJAX request) only fires a CDI event,
 * with the qualifier telling whether it is for everybody or for one user. Delivering the message
 * is done by {@link ChatMessenger}, which observes these events. View scoped: the chosen recipient stays
 * selected between messages.
 */
@ViewScoped
@Named
public class ChatView implements Serializable {

    private final Event<ChatMessage> toEveryone;
    private final Event<ChatMessage> toOneUser;
    private final UserService userService;
    private final CurrentUser currentUser;

    /** Login of the recipient, empty for everybody. */
    private String recipient;

    private String text;

    @Inject
    public ChatView(@ToEveryone Event<ChatMessage> toEveryone, @ToOneUser Event<ChatMessage> toOneUser,
                    UserService userService, CurrentUser currentUser) {
        this.toEveryone = toEveryone;
        this.toOneUser = toOneUser;
        this.userService = userService;
        this.currentUser = currentUser;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected ChatView() {
        this(null, null, null, null);
    }

    public void send() {
        if (recipient == null || recipient.isBlank()) {
            toEveryone.fire(new ChatMessage(currentUser.getName(), null, text, LocalTime.now()));
        } else {
            toOneUser.fire(new ChatMessage(currentUser.getName(), recipient, text, LocalTime.now()));
        }
        text = null;
    }

    /** Options of the recipient list: all users except the logged-in one. */
    public List<String> getUsers() {
        return userService.findAll().stream()
                .map(User::getLogin)
                .filter(login -> !login.equals(currentUser.getName()))
                .toList();
    }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
