package dev.pinter.mailcustomheaders;

import jakarta.mail.Header;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CustomMimeMessageTest {
    private static final Logger logger = LoggerFactory.getLogger(CustomMimeMessageTest.class);

    @Test
    void shouldBeAddedAfterReceivedInOrder() throws IOException, MessagingException {
        MimeMessage mimeMessage = createMessage();
        addHeaderTest(5, mimeMessage, "Authentication-Results", "authentication-results-value");
        addHeaderTest(6, mimeMessage, "DKIM-Signature", "dkim-value");
        addHeaderTest(7, mimeMessage, "Received-SPF", "spf-value");
        assertEquals(5, getHeaderPosition(mimeMessage, "Authentication-Results", "authentication-results-value"));
        assertEquals(6, getHeaderPosition(mimeMessage, "DKIM-Signature", "dkim-value"));
        assertEquals(7, getHeaderPosition(mimeMessage, "Received-SPF", "spf-value"));
    }

    @Test
    void shouldBeAddedAfterReceivedInReverseOrder() throws IOException, MessagingException {
        MimeMessage mimeMessage = createMessage();
        addHeaderTest(5, mimeMessage, "Received-SPF", "spf-value");
        addHeaderTest(5, mimeMessage, "DKIM-Signature", "dkim-value");
        addHeaderTest(5, mimeMessage, "Authentication-Results", "authentication-results-value");
        String headerLines = headersToString(mimeMessage);
        String errorText = "** Error:\n" + headerLines.replaceAll("(?m)^", "**\t\t");
        assertEquals(5,
                getHeaderPosition(mimeMessage, "Authentication-Results", "authentication-results-value"),
                errorText);
        assertEquals(6,
                getHeaderPosition(mimeMessage, "DKIM-Signature", "dkim-value"),
                errorText);
        assertEquals(7,
                getHeaderPosition(mimeMessage, "Received-SPF", "spf-value"),
                errorText);
    }

    @Test
    void shouldBeAddedAfterReceivedHeaderSet() throws IOException, MessagingException {
        CustomMimeMessage mimeMessage = createMessage();

        mimeMessage.addHeaderSet("received-spf", //lowercase!
                "ARC-Authentication-Results", "arc-results-0",
                "ARC-Message-Signature", "arc-message-sig-0",
                "ARC-Seal", "arc-seal-0");
        String headerLines = headersToString(mimeMessage);
        String errorText = "** Error:\n" + headerLines.replaceAll("(?m)^", "**\t\t");
        assertEquals(5, getHeaderPosition(mimeMessage, "ARC-Authentication-Results", "arc-results-0"),
                errorText);
        assertEquals(6, getHeaderPosition(mimeMessage, "ARC-Message-Signature", "arc-message-sig-0"),
                errorText);
        assertEquals(7, getHeaderPosition(mimeMessage, "ARC-Seal", "arc-seal-0"),
                errorText);
    }

    @Test
    void shouldBeEqual() throws MessagingException, IOException {
        CustomMimeMessage mimeMessage = createMessage();
        mimeMessage.saveChanges();
        assertEquals("<f0e54312-6303-4e0e-b166-58a8a7d06474@domain.com.br>", mimeMessage.getMessageID());
    }

    @Test
    void shouldChangeIfMessageIdUpdateIsEnabled() throws MessagingException, IOException {
        CustomMimeMessage mimeMessage = createMessage();
        mimeMessage.setMessageIdUpdate(true);
        mimeMessage.saveChanges();
        assertNotEquals("<f0e54312-6303-4e0e-b166-58a8a7d06474@domain.com.br>", mimeMessage.getMessageID());
    }

    private void addHeaderTest(int expectedPosition, MimeMessage mimeMessage, String name, String value) throws IOException, MessagingException {
        mimeMessage.addHeader(name, value);
        mimeMessage.saveChanges();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        mimeMessage.writeTo(os);

        String headerLines = headersToString(mimeMessage);
        String errorText = "** Error:\n" + headerLines.replaceAll("(?m)^", "**\t\t");

        int i = 0;
        Enumeration<Header> allHeaders = mimeMessage.getAllHeaders();
        while (allHeaders.hasMoreElements()) {
            Header header = allHeaders.nextElement();
            if (header.getName().equals(name)) {
                assertEquals(expectedPosition, i, errorText);
                assertEquals(value, header.getValue(), errorText);
            }
            i++;
        }
    }


    private CustomMimeMessage createMessage() throws MessagingException, IOException {
        String msg = String.join("\n", Files.readAllLines(Paths.get("src/test/resources/mail.msg")));
        Session session = Session.getInstance(new Properties());
        return new CustomMimeMessage(session, new ByteArrayInputStream(msg.getBytes(StandardCharsets.UTF_8)));
    }

    private int getHeaderPosition(MimeMessage mimeMessage, String name, String value) throws MessagingException {
        int pos = 0;
        Enumeration<Header> allHeaders = mimeMessage.getAllHeaders();
        while (allHeaders.hasMoreElements()) {
            Header header = allHeaders.nextElement();
            if (header.getName().equals(name) && header.getValue() != null && header.getValue().equals(value)) {
                return pos;
            }
            pos++;
        }
        return 0;
    }

    private String headersToString(MimeMessage mimeMessage) throws MessagingException {
        StringBuilder headerLines = new StringBuilder();
        Enumeration<String> headerLinesIterator = mimeMessage.getAllHeaderLines();
        while (headerLinesIterator.hasMoreElements()) {
            headerLines.append(headerLinesIterator.nextElement()).append("\n");
        }
        return headerLines.toString();
    }
}
