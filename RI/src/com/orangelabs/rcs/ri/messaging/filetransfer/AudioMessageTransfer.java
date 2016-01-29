package com.orangelabs.rcs.ri.messaging.filetransfer.multi;

import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gsma.services.rcs.RcsServiceException;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.FileTransferService;
import com.gsma.services.rcs.filetransfer.FileTransferServiceConfiguration;
import com.gsma.services.rcs.filetransfer.GroupFileTransferListener;
import com.gsma.services.rcs.filetransfer.OneToOneFileTransferListener;
import com.orangelabs.rcs.api.connection.ConnectionManager;
import com.orangelabs.rcs.api.connection.utils.RcsActivity;
import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.messaging.filetransfer.FileDialog;
import com.orangelabs.rcs.ri.messaging.filetransfer.FileTransferDAO;
import com.orangelabs.rcs.ri.messaging.filetransfer.Observer;
import com.orangelabs.rcs.ri.messaging.filetransfer.RCSMediaPlayer;
import com.orangelabs.rcs.ri.messaging.filetransfer.RCSRecorder;
import com.orangelabs.rcs.ri.messaging.filetransfer.ReceiveFileTransfer;
import com.orangelabs.rcs.ri.utils.LogUtils;

import java.io.File;

/**
 * Created by sandrine on 20/01/2016.
 */
public class AudioMessageTransfer extends RcsActivity {
    private FileTransferServiceConfiguration mConfig;


    Button m_btnRecord;
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
    Observer m_obs;
    private final Handler mHandler = new Handler();

    private FileTransfer mFileTransfer;

    private FileTransferDAO mFtDao;

    private boolean mGroupFileTransfer = false;

    private String mTransferId;

    private Button mPauseBtn;

    private Button mResumeBtn;

    private DialogInterface.OnClickListener mDeclineBtnListener;

    private DialogInterface.OnClickListener mAcceptBtnListener;

    private android.view.View.OnClickListener mBtnPauseListener;

    private android.view.View.OnClickListener mBtnResumeListener;

    private OneToOneFileTransferListener mFileTransferListener;

    private GroupFileTransferListener mGroupFtListener;

    private FileTransferService mFileTransferService;

    private ProgressBar mProgressBar;

    private static final String LOGTAG = LogUtils.getTag(ReceiveFileTransfer.class.getSimpleName());

    private static final String VCARD_MIME_TYPE = "text/x-vcard";

    private static final String BUNDLE_FTDAO_ID = "ftdao";

    final static  RCSMediaPlayer player = new RCSMediaPlayer();
    static   RCSRecorder record ;



    private void initialize()
    {


        setContentView(R.layout.audio_msg_transfer_initiate);
        Log.w("RCS", "Path " + Environment.getExternalStorageDirectory().getAbsolutePath());
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
            record = new RCSRecorder(mConfig.getMaxAudioMessageDuration());

        } catch (RcsServiceException e) {
            showExceptionThenExit(e);
            return;
        }
        m_obs=new Observer(this,player,record);
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
        m_app=this;

        m_btnRecordMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    m_btnStop.setEnabled(true);
                    m_btnPlay.setEnabled(false);
                    m_btnRecordMsg.setEnabled(false);
                    record.launchRecord();
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
                    record.stopRecord();
                    player.play(record.getOutputFile(), false);
                    m_BoolRecording = false;
                    m_EditURI.setText(record.getOutputFile());
                    Log.v("RCS FIle Rcorded",record.getOutputFile());
                    Toast.makeText(getApplicationContext(), "Audio recorded successfully", Toast.LENGTH_LONG).show();
                }
                if (m_BoolPlaying) {
                    player.stopPlay();
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
                        m_EditFileSize.setText(file.length()/1024 + "kbytes");

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
                    player.stopPlay();
                    m_BoolPlaying = false;
                }
                try {
                    player.play((String) m_EditURI.getText(), true);
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

    public  void setDuration(int min, int sec)
    {
        m_EditDuration.setText("Duration : " +min+ "mins" + sec + "secs");

    }
    public void endRecordingDuration() {
        m_btnStop.setEnabled(true);
        m_btnRecordMsg.setEnabled(true);
        m_btnPlay.setEnabled(true);
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
