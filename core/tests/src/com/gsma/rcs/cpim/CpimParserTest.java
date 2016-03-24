/*******************************************************************************
 * Software Name : RCS IMS Stack Copyright (C) 2010 France Telecom S.A. Licensed under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package com.gsma.rcs.cpim;

import com.gsma.rcs.core.ims.service.im.chat.cpim.CpimMessage;
import com.gsma.rcs.core.ims.service.im.chat.cpim.CpimParser;

import android.test.AndroidTestCase;

public class CpimParserTest extends AndroidTestCase {

    private static final String sContentToParse = "CPIM sample: From: MR SANDERS <im:piglet@100akerwood.com>\n"
            + "To: Depressed Donkey<im:eeyore@100akerwood.com>\n"
            + "DateTime: 2000-12-13T13:40:00-08:00 Subject: the weather will be fine today\n"
            + "Content-type: text/plain\n"
            + "Content-ID: <1234567890@foo.com>\n"
            + "\n"
            + " Here is the text of my message.";




    public final void testCpimParserString() {
        CpimMessage msg = (new CpimParser(sContentToParse)).getCpimMessage();
        assertEquals(msg.getHeader("From"), "MR SANDERS <im:piglet@100akerwood.com>");
        assertEquals(msg.getHeader("To"), "Depressed Donkey<im:eeyore@100akerwood.com>");
        assertEquals(msg.getHeader("DateTime"), "2000-12-13T13:40:00-08:00");
        assertEquals(msg.getHeader("Subject"), "the weather will be fine today");
        assertEquals(msg.getContentHeader("Content-ID"), "<1234567890@foo.com>");
        assertEquals(msg.getContentType(), "text/plain");
        assertEquals(msg.getMessageContent(), "Here is the text of my message.");
    }

}
