/*
 * Copyright 2025 Emerson Pinter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.pinter.mailcustomheaders;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetHeaders;
import jakarta.mail.internet.MimeMessage;

import java.io.InputStream;

public class CustomMimeMessage extends MimeMessage {
    private boolean messageIdUpdate = false;

    public CustomMimeMessage(Session session) {
        super(session);
    }

    public CustomMimeMessage(Session session, InputStream is) throws MessagingException {
        super(session, is);
    }

    @Override
    protected InternetHeaders createInternetHeaders(InputStream is) throws MessagingException {
        return new CustomInternetHeaders(is);
    }

    @Override
    protected void updateMessageID() throws MessagingException {
        if (messageIdUpdate) {
            super.updateMessageID();
        }
    }

    public void addHeaderSet(String after, String... keyValuePairs) {
        if (headers instanceof CustomInternetHeaders) {
            ((CustomInternetHeaders) headers).addHeaderSet(after, keyValuePairs);
        }
    }

    public boolean isMessageIdUpdate() {
        return messageIdUpdate;
    }

    public void setMessageIdUpdate(boolean messageIdUpdate) {
        this.messageIdUpdate = messageIdUpdate;
    }
}
