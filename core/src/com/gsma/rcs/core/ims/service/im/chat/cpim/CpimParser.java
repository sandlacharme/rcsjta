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
     * CRLF constant
     */
    private static final String CRLF = "n";


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
         * CPIM sample: From: MR SANDERS <im:piglet@100akerwood.com> To: Depressed Donkey
         * <im:eeyore@100akerwood.com> DateTime: 2000-12-13T13:40:00-08:00 Subject: the weather will
         * be fine today Content-type: text/plain Content-ID: <1234567890@foo.com> Here is the text
         * of my message.
         */
        /* Read message headers */
        int begin = 0;
        int end = data.indexOf(":", 0);
        String block2 = data.substring(begin, end);
        if(block2.equals("CPIM sample"))
        {
            int point = data.indexOf(":", end+1);
            block2 = data.substring(end+1, point);

        }
        StringTokenizer lines = new StringTokenizer(block2.concat(":"), CRLF);
        Hashtable<String, String> headers = new Hashtable<>();
        while (lines.hasMoreTokens()) {
            String token = lines.nextToken();
            CpimHeader hd = CpimHeader.parseHeader(token);
            headers.put(hd.getName(), hd.getValue());
        }

        /* Read the MIME-encapsulated content header */
        begin = end + 4;
        end = data.indexOf(CRLF, begin);
        String block3 = data.substring(begin, end);
        lines = new StringTokenizer(block3, CRLF);
        Hashtable<String, String> contentHeaders = new Hashtable<String, String>();
        while (lines.hasMoreTokens()) {
            String token = lines.nextToken();
            CpimHeader hd = CpimHeader.parseHeader(token);
            contentHeaders.put(hd.getName(), hd.getValue());
        }

        /* Read the message content */
        begin = end + 4;
        String content = data.substring(begin);

        mMessage = new CpimMessage(headers, contentHeaders, content);
    }
}
