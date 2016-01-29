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

import android.media.MediaPlayer;
import android.util.Log;

import com.orangelabs.rcs.ri.utils.LogUtils;

import java.io.IOException;

/**
 * Created by sandrine on 12/01/2016.
 */
public class AudioMediaPlayer extends MediaPlayer {

    private static final String LOGTAG = LogUtils.getTag(AudioRecorder.class.getSimpleName());

    AudioRecorder.INotifyMaxDurationReached mlistener;
    private int mmin;
    private int msec;
    Boolean mbReset = false;

    /**
     * Constructor
     * @param listener : used for display on RI duration of the file
     */

    public AudioMediaPlayer(AudioRecorder.INotifyMaxDurationReached listener)
    {
        super();
        mlistener=listener;

    }


    private void initPlayer (String outputfile)throws IOException
    {


            mbReset=false;
            setDataSource(outputfile);
            prepareAsync();

    }

    /**
     *
     * @param outputfile
     * @param bPlay : because this method is used even if there is no file playing just for getting duration for a record : bool true : play mode; false record mode
     */
    public void play (String outputfile, final boolean bPlay) throws IOException
    {
        initPlayer(outputfile);
        setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (bPlay) {
                    setVolume(1.0f, 1.0f);
                    mp.start();
                }
                int duration = getDuration();//ms
                if (LogUtils.isActive) {
                    Log.w(LOGTAG, "AudioDuration record onPrepared");
                }

                if (duration > 0) {
                    mmin = (int) ((duration / 1000) / 60) % 60;
                    msec = (int) ((duration / 1000) % 60);
                    mlistener.NotifyDuration(mmin, msec);
                }
                if (!bPlay) {
                    mp.reset();
                    mbReset = true;
                }
            }



    });

        setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.reset();
                mbReset=true;
                if (LogUtils.isActive) {
                    Log.w(LOGTAG, "AudioMediaPlayer OnCompletionListener ");
                }

            }
        });


    }

    /**
     *  Stop playing an audio file and reset the peripheral
     */
    public void stopPlay() {

            if(!mbReset) {
                stop();
                reset();
                Log.v("RCSMediaPlayer", "stopPlay");
            }


    }





}
