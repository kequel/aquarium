package pl.edu.pg.eti.kask.aquarium.chat;

import java.time.LocalTime;

/**
 * Chat message, the payload of CDI events fired when a message is sent.
 *
 * @param from login of the sender
 * @param to   login of the recipient, null for a message to everybody
 */
public record ChatMessage(String from, String to, String text, LocalTime time) {
}
