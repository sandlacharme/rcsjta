/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
 * Copyright (C) 2014 Sony Mobile Communications Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * NOTE: This file has been modified by Sony Mobile Communications Inc.
 * Modifications are licensed under the License.
 ******************************************************************************/

package com.gsma.rcs.core.ims.service.im.chat.cpim;

import static com.gsma.rcs.utils.StringUtils.UTF8;

import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 * CPIM parser (see RFC3862)
 * 
 * @author jexa7410
 */
public class CpimParser {

    /**
     * CPIM message
     */
    private CpimMessage mMessage = null;

    /**
     * Constructor
     *
     * @param data Input data
     */
    public CpimParser(byte data[]) {
        parse(new String(data, UTF8));
    }

    /**
     * Constructor
     *
     * @param data Input data
     */
    public CpimParser(String data) {
        parse(data);
    }

    /***
     * Returns the CPIM message
     *
     * @return CPIM message
     */
    public CpimMessage getCpimMessage() {
        return mMessage;
    }

    /**
     * Parse message/CPIM document
     *
     * @param data Input data
     */
    private void parse(String data) {
        /*
         * "CPIM sample: From: MR SANDERS <im:piglet@100akerwood.com>\n" +
         * "To: Depressed Donkey<im:eeyore@100akerwood.com> \n" +
         * "DateTime: 2000-12-13T13:40:00-08:00 Subject: the weather will be fine today \n" +
         * "Content-type: text/plain \n" + "Content-ID: <1234567890@foo.com>\n" + "\n" +
         * " Here is the text of my message.";
         */

        String content = "";
        Hashtable<String, String> headers = new Hashtable<>();
        Hashtable<String, String> contentHeaders = new Hashtable<>();
        StringTokenizer lines = new StringTokenizer(data, "\n");
        while (lines.hasMoreTokens()) {

            // CpimHeader hd = CpimHeader.parseHeader(token.concat(":"));
            content = lines.nextToken().trim();
            StringTokenizer l = new StringTokenizer(content, ":");
            while (l.hasMoreTokens()) {
                String token = l.nextToken().trim();
                if ("CPIM sample".equals(token)) {
                    // String strContent = token.substring()
                    token = l.nextToken().trim();
                }
                if ("DateTime".equals(token)) {
                    int posSubject = content.indexOf("Subject:");
                    String strContentSubject = (content
                            .substring((posSubject + "Subject:".length() + 1))).trim();
                    String strContentDate = content.substring(token.length() + 1, posSubject)
                            .trim();
                    headers.put(token, strContentDate);
                    headers.put("Subject", strContentSubject);
                }

                if (("From".equals(token)) || ("To".equals(token))
                        || ("Content-type".equals(token)) || ("Content-ID".equals(token))
                        || ("Subject".equals(token))) {
                    int start = content.indexOf(token);
                    String strContent = (content.substring((start + token.length() + 1))).trim();
                    if ("Content-type".equals(token) || "Content-ID".equals(token)) {
                        contentHeaders.put(token, strContent);
                    } else {
                        headers.put(token, strContent);
                    }
                }
            }
        }

        if (!headers.isEmpty() && !contentHeaders.isEmpty()) {
            mMessage = new CpimMessage(headers, contentHeaders, content);
        }

    }
}
