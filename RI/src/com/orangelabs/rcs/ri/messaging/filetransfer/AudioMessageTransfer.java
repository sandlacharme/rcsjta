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


import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;
import android.widget.Toast;

import com.gsma.services.rcs.RcsServiceException;

import com.gsma.services.rcs.filetransfer.FileTransferServiceConfiguration;

import com.orangelabs.rcs.api.connection.ConnectionManager;
import com.orangelabs.rcs.api.connection.utils.RcsActivity;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.utils.LogUtils;

import java.io.File;
import java.io.IOException;


public class AudioMessageTransfer extends RcsActivity {
    private FileTransferServiceConfiguration mConfig;

    private static final String LOGTAG = LogUtils.getTag(AudioRecorder.class.getSimpleName());


  
    Button m_btnPlay;
    Button m_btnStop;
    Button m_btnRecordMsg;
    Button m_btnSelFile;

    TextView m_EditURI;
    TextView m_EditFileSize;
    TextView m_EditDuration;
    boolean m_BoolRecording = false;
    boolean m_BoolPlaying = false;
    RcsActivity m_app;


     static  AudioMediaPlayer mplayer;
    static   AudioRecorder mrecord ;



    private void initialize()
    {

        setContentView(R.layout.audio_msg_transfer_initiate);
        m_btnPlay = (Button) findViewById(R.id.buttonPlay);
        m_btnStop = (Button) findViewById(R.id.buttonStop);
        m_btnSelFile =(Button)findViewById(R.id.select_btn);
        m_btnRecordMsg =(Button)findViewById(R.id.select_record);
        m_EditDuration = (TextView) findViewById(R.id.duration);
        m_EditURI = (TextView) findViewById(R.id.uri);
        m_EditFileSize = (TextView)findViewById(R.id.size);
        m_btnStop.setEnabled(false);
        m_btnPlay.setEnabled(false);
        m_btnRecordMsg.setEnabled(true);

        try {
            mConfig = getFileTransferApi().getConfiguration();
            AudioRecorder.INotifyMaxDurationReached notifyMaxDurationReached = new  AudioRecorder.INotifyMaxDurationReached() {

                @Override
                public void maxDurationReached() {
                    m_btnStop.setEnabled(true);
                    m_btnRecordMsg.setEnabled(true);
                    m_btnPlay.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "End of Duration ", Toast.LENGTH_LONG).show();
                }

                @Override
                public void NotifyDuration(int min, int sec) {
                    m_EditDuration.setText("Duration : " +min+ "mins" + sec + "secs");

                }
            };
            mrecord = new AudioRecorder(mConfig.getMaxAudioMessageDuration(), notifyMaxDurationReached);
            mplayer = new AudioMediaPlayer(notifyMaxDurationReached);
        } catch (RcsServiceException e) {
            showExceptionThenExit(e);
            return;
        }
         startMonitorServices(ConnectionManager.RcsServiceName.FILE_TRANSFER);
        /**Register to API manager*/
        if (!isServiceConnected(ConnectionManager.RcsServiceName.FILE_TRANSFER)) {
            showMessageThenExit(R.string.label_service_not_available);
            return;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.audio_msg_transfer_initiate);

        initialize();
        m_app = this;

        m_btnRecordMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    m_btnStop.setEnabled(true);
                    m_btnPlay.setEnabled(false);
                    m_btnRecordMsg.setEnabled(false);
                    mrecord.launchRecord();
                    m_BoolRecording = true;
                    m_BoolPlaying = false;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }

        });

        m_btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_BoolRecording) {
                    mrecord.stopRecord();
                    try {
                        mplayer.play(mrecord.getOutputFile(), false);

                    }catch (IOException e)
                    {
                          e.printStackTrace();

                    }
                    m_BoolRecording = false;
                    m_EditURI.setText(mrecord.getOutputFile());

                    if (LogUtils.isActive) {
                        Log.w(LOGTAG, "RCS FIle Recorded" + mrecord.getOutputFile());
                    }
                    Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
                }
                if (m_BoolPlaying) {
                    mplayer.stopPlay();
                    m_BoolPlaying = false;
                    m_EditDuration.setText(" ");
                }
                m_btnRecordMsg.setEnabled(true);
                m_btnPlay.setEnabled(true);
                m_btnStop.setEnabled(true);
            }
        });

        m_btnSelFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File mPath = new File("/sdcard/audiorecords/");
                final String filePath;
                FileDialog fileDialog = new FileDialog(m_app, mPath);
                fileDialog.setFileEndsWith(".3gp");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) {
                        Log.d(getClass().getName(), "selected file " + file.toString());
                        m_EditURI.setText(file.toString());
                        m_EditFileSize.setText(file.length() / 1024 + "kbytes");

                    }
                });
                fileDialog.showDialog();
                //TODO Display duration
                m_btnRecordMsg.setEnabled(false);
                m_btnStop.setEnabled(true);
                m_btnPlay.setEnabled(true);
            }
        });


        m_btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_BoolPlaying) {
                    mplayer.stopPlay();
                    m_BoolPlaying = false;
                }
                try {
                    mplayer.play((String) m_EditURI.getText(), true);
                    m_BoolPlaying = true;
                    //TODO Display duration
                    m_btnRecordMsg.setEnabled(false);
                    m_btnStop.setEnabled(true);
                    m_btnPlay.setEnabled(true);

                } catch (Exception e) {
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mplayer.release();
        mrecord.release();


    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
