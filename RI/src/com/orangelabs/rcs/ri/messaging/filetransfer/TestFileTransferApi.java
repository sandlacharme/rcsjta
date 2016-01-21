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

package com.orangelabs.rcs.ri.messaging.filetransfer;

import com.orangelabs.rcs.ri.R;
import com.orangelabs.rcs.ri.messaging.filetransfer.multi.AudioMessageTransfer;

import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * File transfer API
 * 
 * @author Jean-Marc AUFFRET
 */
public class TestFileTransferApi extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        /* Set items */
        String[] items = {
                getString(R.string.menu_transfer_file),
                getString(R.string.menu_transfer_audio_msg),
                getString(R.string.menu_file_transfer_log),
                getString(R.string.menu_file_transfer_config)
        };
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        switch (position) {
            case 0:
                startActivity(new Intent(this, InitiateFileTransfer.class));
                break;

            case 1:
                // TODO
                startActivity(new Intent(this, AudioMessageTransfer.class));
                break;

            case 2:
                startActivity(new Intent(this, FileTransferList.class));
                break;

            case 3:
                startActivity(new Intent(this, FileTransferServiceConfigActivity.class));
                break;
        }
    }
}
