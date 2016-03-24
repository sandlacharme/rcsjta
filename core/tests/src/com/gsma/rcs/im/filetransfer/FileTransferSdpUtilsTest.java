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

package com.gsma.rcs.im.filetransfer;

import com.gsma.rcs.core.ims.protocol.sdp.MediaAttribute;
import com.gsma.rcs.core.ims.protocol.sdp.MediaDescription;
import com.gsma.rcs.core.ims.protocol.sdp.SdpParser;
import com.gsma.rcs.utils.logger.Logger;

import junit.framework.TestCase;

import java.util.Vector;

public class FileTransferSdpUtilsTest extends TestCase {

    private static Logger sLogger = Logger.getLogger(FileTransferSdpUtilsTest.class.getName());

    public void testbuildFtSDP() {

        int maxSize = 15728640;
        int localMsrpPort = 20000;
        String localSocketProtocol = "TCP/MSRP";
        String localMsrpPath = "msrp://10.29.67.37:20000/1391518338240;tcp";
        String localSetup = "actpass";
        String acceptedTypes = "image/jpeg";
        String fileTransferID = "1391518338244";
        String fileSelector = "name:\"phototmp_3_1_1_1.jpg\" type:image/jpegize:195490";

        String sdp = "\n" + "v=0 \n" + "o=- 3600507138 3600507138 IN IP4 10.29.67.37 \n" + "s=- \n"
                + "c=IN IP4 10.29.67.37 \n" + "t=0 0\n" + "m=message 20000 TCP/MSRP\n"
                + "a=accept-types:image/jpeg \n" + "a=file-transfer-id:1391518338244\n"
                + "a=file-disposition:attachment \n"
                + "a=file-selector:name:\"phototmp_3_1_1_1.jpg\" type:image/jpegize:195490 \n"
                + "a=setup:actpass \n" + "a=path:msrp://10.29.67.37:20000/1391518338240;tcp\n"
                + "a=sendonly\n" + "a=max-size:15728640";

        sLogger.info("SDP " + sdp);
        // Parse the remote SDP part
        SdpParser parser = new SdpParser(sdp.getBytes());
        Vector<MediaDescription> media = parser.getMediaDescriptions();
        MediaDescription mediaDesc = media.elementAt(0);
        for (MediaDescription mediaDescription : media) {
            sLogger.info(media.toString());
            for (MediaAttribute attribute : mediaDescription.mMediaAttributes) {
                sLogger.info("attribute: (name=" + attribute.getName() + ") (value="
                        + attribute.getValue() + ")");
            }
        }
        assertEquals(mediaDesc.getMediaAttribute("setup").getValue().trim(), localSetup);
        assertEquals(mediaDesc.getMediaAttribute("file-transfer-id").getValue().trim(),
                fileTransferID);
        assertEquals(mediaDesc.getMediaAttribute("file-disposition").getValue().trim(),
                "attachment");

        assertEquals(mediaDesc.getMediaAttribute("max-size").getValue().trim(), "" + maxSize);
        assertEquals(mediaDesc.getMediaAttribute("accept-types").getValue().trim(), acceptedTypes);
        assertEquals(mediaDesc.getMediaAttribute("path").getValue().trim(), localMsrpPath);
        assertEquals(mediaDesc.getMediaAttribute("file-selector").getValue().trim(), fileSelector);

        assertEquals(mediaDesc.mPort, localMsrpPort);
        assertEquals(mediaDesc.mProtocol, localSocketProtocol);
    }

}
