package pl.edu.pg.eti.kask.aquarium.chat;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.faces.push.Push;
import jakarta.faces.push.PushContext;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Delivers chat messages: observes the CDI events fired by {@link ChatView} and sends the message through
 * the WebSocket channel "chat" (opened by {@code <f:websocket>} in chat/chat.xhtml). Every open socket belongs
 * to one user (its login), so a message can be sent to chosen users only.
 */
@ApplicationScoped
public class ChatMessenger {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PushContext chat;
    private final UserService userService;

    @Inject
    public ChatMessenger(@Push(channel = "chat") PushContext chat, UserService userService) {
        this.chat = chat;
        this.userService = userService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected ChatMessenger() {
        this(null, null);
    }

    /** A message for everybody: sent to the sockets of all users. */
    void onMessageToEveryone(@Observes @ToEveryone ChatMessage message) {
        Set<String> everybody = userService.findAll().stream().map(User::getLogin).collect(Collectors.toSet());
        chat.send(toJson(message), everybody);
    }

    /** A private message: sent only to the recipient, and to the sender so it appears in the sender's chat too. */
    void onMessageToOneUser(@Observes @ToOneUser ChatMessage message) {
        chat.send(toJson(message), Set.of(message.from(), message.to()));
    }

    /** The map is sent to the browser as a JSON object. */
    private static Map<String, Object> toJson(ChatMessage message) {
        return Map.of(
                "from", message.from(),
                "to", message.to() != null ? message.to() : "",
                "text", message.text(),
                "time", message.time().format(TIME));
    }
}
