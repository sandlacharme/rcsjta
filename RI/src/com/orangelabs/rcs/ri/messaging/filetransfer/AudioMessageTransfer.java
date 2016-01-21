package com.orangelabs.rcs.ri.messaging.filetransfer.multi;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gsma.services.rcs.RcsServiceException;
import com.gsma.services.rcs.filetransfer.FileTransferServiceConfiguration;
import com.orangelabs.rcs.api.connection.ConnectionManager;
import com.orangelabs.rcs.api.connection.utils.RcsActivity;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.messaging.filetransfer.FileDialog;
import com.orangelabs.rcs.ri.messaging.filetransfer.Observer;
import com.orangelabs.rcs.ri.messaging.filetransfer.RCSMediaPlayer;
import com.orangelabs.rcs.ri.messaging.filetransfer.RCSRecorder;

import java.io.File;

/**
 * Created by sandrine on 20/01/2016.
 */
public class AudioMessageTransfer extends RcsActivity {
    private FileTransferServiceConfiguration mConfig;


    Button brecord;
    Button bplay;
    Button bstop;
    EditText chrono;
    boolean recording = false;
    boolean playing = false;
    RcsActivity m_app;
    Observer m_obs;



    final static  RCSMediaPlayer player = new RCSMediaPlayer();
    static   RCSRecorder record ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.filetransfer_service_config);
        m_app=this;
        /* Register to API connection manager */
        if (!isServiceConnected(ConnectionManager.RcsServiceName.FILE_TRANSFER)) {
            showMessageThenExit(R.string.label_service_not_available);
            return;
        }
        try {
            mConfig = getFileTransferApi().getConfiguration();
            record = new RCSRecorder(mConfig.getMaxAudioMessageDuration());

        } catch (RcsServiceException e) {
            showExceptionThenExit(e);
            return;
        }
        startMonitorServices(ConnectionManager.RcsServiceName.FILE_TRANSFER);


        m_obs=new Observer(this,player,record);
        setContentView(R.layout.audio_message_transfer);
        Log.w("RCS", "Path " + Environment.getExternalStorageDirectory().getAbsolutePath());
        //recorder = record;
        bplay = (Button) findViewById(R.id.buttonPlay);
        bstop = (Button) findViewById(R.id.buttonStop);
        brecord = (Button) findViewById(R.id.buttonRecord);
        chrono = (EditText) findViewById(R.id.chronometer);
        bstop.setEnabled(false);


        brecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    chrono.setText("");
                    bstop.setEnabled(true);
                    bplay.setEnabled(false);
                    brecord.setEnabled(false);
                    record.launchRecord();
                    recording = true;
                    playing = false;
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
            }

        });

        bstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (recording) {
                    record.stopRecord();
                    player.play(record.getOutputFile(), false);
                    recording = false;
                    Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
                }
                if (playing) {
                    player.stopPlay();
                    playing = false;
                    chrono.setText("");
                }

                brecord.setEnabled(true);
                bplay.setEnabled(true);
                bstop.setEnabled(true);
            }
        });

            bplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(playing)
                {
                    player.stopPlay();
                    playing=false;
                }
                File mPath = new File("/sdcard/audiorecords/");
                final String filePath;
                FileDialog fileDialog = new FileDialog(m_app, mPath);
                fileDialog.setFileEndsWith(".3gp");
                fileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                    public void fileSelected(File file) {
                        Log.d(getClass().getName(), "selected file " + file.toString());
                        try {
                            player.play(file.toString(), true);
                            playing = true;

                        } catch (Exception e) {
                        }
                    }
                });
                fileDialog.showDialog();
                //TODO Display duration
                brecord.setEnabled(true);
                bstop.setEnabled(true);
                bplay.setEnabled(true);
            }
        });

    }


    public  void setDuration(int min, int sec)
    {
        chrono.setText("Duration : " +min+ "mins" + sec + "secs");

    }
    public void endRecordingDuration() {
        bstop.setEnabled(true);
        brecord.setEnabled(true);
        bplay.setEnabled(true);
        Toast.makeText(getApplicationContext(), "End of Duration ", Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
        record.release();
        m_obs.detach();

    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}
