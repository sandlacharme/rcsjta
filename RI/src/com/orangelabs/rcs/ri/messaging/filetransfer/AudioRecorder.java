/*******************************************************************************
 * Software Name : RCS IMS Stack
 * <p/>
 * Copyright (C) 2010-2016 Orange.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.orangelabs.rcs.ri.messaging.filetransfer;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.orangelabs.rcs.ri.utils.LogUtils;

import java.io.File;
import java.io.IOException;


public class AudioRecorder extends MediaRecorder {

    private static final String LOGTAG = LogUtils.getTag(AudioRecorder.class.getSimpleName());


    private static final String AUDIO_DIR = "/audiorecords/";
    private static final String FILE_EXTENSION = ".3gp";
    private static final String FILE_BODY = "recording";


    private final long mMaxAudioDuration;
    private final INotifyMaxDurationReached mListener;
    private String mOutputfile;
    private final MediaRecorder mCurrentRecord;

    public String getOutputFile() {
        return mOutputfile;
    }

    /**
     * Constructor
     *
     * @param maxAudioDuration maximum duration
     * @param listener callback
     */
    public AudioRecorder(long maxAudioDuration, INotifyMaxDurationReached listener) {
        mMaxAudioDuration = maxAudioDuration;
        mCurrentRecord = new MediaRecorder();
        mListener = listener;
    }

    private void init() {
        mCurrentRecord.setAudioSource(AudioSource.VOICE_COMMUNICATION);
        mCurrentRecord.setOutputFormat(OutputFormat.DEFAULT);
        mCurrentRecord.setAudioEncoder(AudioEncoder.DEFAULT);
        mOutputfile = randomFilename();
        if (LogUtils.isActive) {
            Log.w(LOGTAG, "File path and name " + mOutputfile);
        }
        mCurrentRecord.setOutputFile(mOutputfile);
        mCurrentRecord.setMaxDuration((int) mMaxAudioDuration); // 10 seconds
        mCurrentRecord.setOnInfoListener(new OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if (MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED == what) {
                    mListener.maxDurationReached();
                    mr.stop();
                    mr.reset();

                }
            }
        });
    }

    /**
     * Launches audio recording
     * @throws IOException
     */
    public void launchRecord() throws IOException {
        init();
        mCurrentRecord.prepare();
        mCurrentRecord.start();
    }

    /**
     * Stops audio recording
     */
    public void stopRecord() {
        mCurrentRecord.stop();
        mCurrentRecord.reset();
    }


    /**
     * Generates a random filename for audio file
     */
    private String randomFilename() {
        int lower = 1;
        int higher = 10000;
        int random = (int) (Math.random() * (higher - lower)) + lower;
        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath().concat(AUDIO_DIR));
        if (!f.exists()) {
            f.mkdir();
        }
        if (LogUtils.isActive) {
            Log.w(LOGTAG, "File path and name " + f.getPath());
        }
        return f.getPath() + FILE_BODY + random + FILE_EXTENSION;
    }

    /**
     * Interface who notify RI of en duration
     */
    public interface INotifyMaxDurationReached {
        void maxDurationReached();

        void NotifyDuration(int mmin, int msec);
    }
}
