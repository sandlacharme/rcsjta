/*******************************************************************************
 * Software Name : RCS IMS Stack
 *
 * Copyright (C) 2010 France Telecom S.A.
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
 ******************************************************************************/

package com.gsma.rcs.core.ims.service.im.chat.cpim;

import com.gsma.rcs.utils.DateUtils;

import java.util.Hashtable;

/**
 * CPIM message
 * 
 * @author jexa7410
 */
public class CpimMessage {
    /**
     * MIME type
     */
    public static final String MIME_TYPE = "message/cpim";

    /**
     * Header "Content-type"
     */
    public static final String HEADER_CONTENT_TYPE = "Content-type";
    public static final String HEADER_CONTENT_TYPE2 = "Content-Type";

    /**
     * Header "From"
     */
    public static final String HEADER_FROM = "From";

    /**
     * Header "To"
     */
    public static final String HEADER_TO = "To";

    /**
     * Header "DateTime"
     */
    public static final String HEADER_DATETIME = "DateTime";

    /**
     * Header "NS"
     */
    public static final String HEADER_NS = "NS";

    /**
     * Header "Content-length"
     */
    public static final String HEADER_CONTENT_LENGTH = "Content-length";

    /**
     * Header "Content-Disposition"
     */
    public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

    /**
     * Message content
     */
    private String msgContent = null;

    /**
     * MIME headers
     */
    private Hashtable<String, String> headers = new Hashtable<>();

    /**
     * MIME content headers
     */
    private Hashtable<String, String> contentHeaders = new Hashtable<>();

    /**
     * Constructor
     * 
     * @param headers MIME headers
     * @param contentHeaders MIME content headers
     * @param msgContent Content
     */
    public CpimMessage(Hashtable<String, String> headers, Hashtable<String, String> contentHeaders,
            String msgContent) {
        this.headers = headers;
        this.contentHeaders = contentHeaders;
        this.msgContent = msgContent;
    }

    /**
     * Returns content type
     * 
     * @return Content type
     */
    public String getContentType() {
        String type = contentHeaders.get(CpimMessage.HEADER_CONTENT_TYPE);
        if (type == null) {
            return contentHeaders.get(CpimMessage.HEADER_CONTENT_TYPE2);
        }
        return type;
    }

    /**
     * Returns MIME header
     * 
     * @param name Header name
     * @return Header value
     */
    public String getHeader(String name) {
        return headers.get(name);
    }

    /**
     * Returns MIME content header
     * 
     * @param name Header name
     * @return Header value
     */
    public String getContentHeader(String name) {
        return contentHeaders.get(name);
    }

    /**
     * Returns message content
     * 
     * @return Content
     */
    public String getMessageContent() {
        return msgContent;
    }

    /**
     * Returns message timestamp sent
     * 
     * @return timestamp sent in payload
     */
    public long getTimestampSent() {
        String header = getHeader(CpimMessage.HEADER_DATETIME);
        if (header != null) {
            return DateUtils.decodeDate(header);
        }
        return 0;
    }
}
