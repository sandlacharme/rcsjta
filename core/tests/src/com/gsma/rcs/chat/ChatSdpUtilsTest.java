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

package com.gsma.rcs.chat;

import com.gsma.rcs.core.ims.protocol.sdp.MediaDescription;
import com.gsma.rcs.core.ims.protocol.sdp.SdpParser;

import android.test.AndroidTestCase;

import java.util.Vector;

public class ChatSdpUtilsTest extends AndroidTestCase {

    private String mLocalSocketProtocol;

    private String mAcceptedTypes;

    private String mWrappedTypes;

    private String localSetup;

    private String localMsrpPath;

    protected void setUp() throws Exception {
        super.setUp();
        mLocalSocketProtocol = "TCP/MSRP";
        localMsrpPath = "msrp://10.29.67.37:20000/1391503972255;tcp";
        localSetup = "actpass";
        mAcceptedTypes = "message/cpim application/im-iscomposing+xml";
        mWrappedTypes = "text/plain message/imdn+xml application/vnd.gsma.rcspushlocation+xml application/vnd.gsma.rcs-ft-http+xml";
    }

    public void testbuildChatSDP() {
        // Parse the remote SDP part
        String sdp = "v=0 \n"
                + "o=- 3600492772 3600492772 IN IP4 10.29.67.37 \n"
                + "s=- \n"
                + "c=IN IP4 10.29.67.37 \n"
                + "t=0 0 \n"
                + "m=message 20000 TCP/MSRP \n"
                + "a=path:msrp://10.29.67.37:20000/1391503972255;tcp\n"
                + "a=setup:actpass\n"
                + "a=accept-types:message/cpim application/im-iscomposing+xml\n"
                + "a=accept-wrapped-types:text/plain message/imdn+xml application/vnd.gsma.rcspushlocation+xml application/vnd.gsma.rcs-ft-http+xml\n"
                + "a=sendrecv";
        SdpParser parser = new SdpParser(sdp.getBytes());
        Vector<MediaDescription> media = parser.getMediaDescriptions();
        MediaDescription mediaDesc = media.elementAt(0);
        assertEquals(mediaDesc.getMediaAttribute("setup").getValue(), localSetup);
        assertEquals(mediaDesc.getMediaAttribute("accept-types").getValue(), mAcceptedTypes);
        assertEquals(mediaDesc.getMediaAttribute("accept-wrapped-types").getValue(), mWrappedTypes);
        assertEquals(mediaDesc.getMediaAttribute("path").getValue(), localMsrpPath);
        assertEquals(mediaDesc.mPort, 20000);
        assertEquals(mediaDesc.mProtocol, mLocalSocketProtocol);
    }
}
