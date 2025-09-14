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
import jakarta.mail.internet.InternetHeaders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CustomInternetHeaders extends InternetHeaders {
    private final List<String> CUSTOM_HEADER_ORDER = new ArrayList<String>() {{
        add("return-path");
        add("received");
        add("authentication-results");
        add("dkim-signature");
        add("received-spf");
        add("arc-authentication-results");
        add("arc-message-signature");
        add("arc-seal");
    }};

    public CustomInternetHeaders(InputStream is) throws MessagingException {
        super(is);
    }

    /**
     * Add a set of headers
     *
     * @param after         the header to search, the set will be added AFTER the specified
     * @param keyValuePairs a list of key-value pairs
     */
    public void addHeaderSet(String after, String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("A list of key-value pair is expected");
        }
        int idx = -1;
        if (after != null) {
            idx = CUSTOM_HEADER_ORDER.indexOf(after.toLowerCase());
        }

        if (idx > 0) {
            int offset = 0;
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i) == null || headers.get(i).getName() == null) {
                    continue;
                }
                for (int j = idx; j >= 0; j--) {
                    if (headers.get(i).getName().toLowerCase().equalsIgnoreCase(CUSTOM_HEADER_ORDER.get(j))) {
                        offset = i + 1;
                        break;
                    }
                }
            }

            for (int i = keyValuePairs.length - 2; i >= 0; ) {
                String n = keyValuePairs[i];
                String v = keyValuePairs[i + 1];
                headers.add(offset, new InternetHeader(n, v));
                i -= 2;
            }
        } else {
            for (int i = 0; i < keyValuePairs.length; ) {
                String k = keyValuePairs[i];
                String v = keyValuePairs[i + 1];
                super.addHeader(k, v);
                i += 2;
            }
        }

    }

    private void addHeaderCustomOrdered(String name, String value) {
        int idx = CUSTOM_HEADER_ORDER.indexOf(name.toLowerCase());

        int offset = 0;
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i) == null || headers.get(i).getName() == null) {
                continue;
            }
            for (int j = (idx > 0 ? idx - 1 : 0); j >= 0; j--) { //find the header before
                if (headers.get(i).getName().equalsIgnoreCase(CUSTOM_HEADER_ORDER.get(j))) {
                    offset = i + 1;
                    break;
                }
            }
        }
        headers.add(offset, new InternetHeader(name, value));
    }

    @Override
    public void addHeader(String name, String value) {
        if (name == null) {
            return;
        }
        int idx = CUSTOM_HEADER_ORDER.indexOf(name.toLowerCase());

        if (!headers.isEmpty() && idx >= 0
                && !name.equalsIgnoreCase("return-path")
                && !name.equalsIgnoreCase("received")) {
            addHeaderCustomOrdered(name, value);
        } else {
            super.addHeader(name, value);
        }
    }
}
