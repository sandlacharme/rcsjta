/*
 * Copyright (C) 2014 Sony Mobile Communications Inc.
 * All rights, including trade secret rights, reserved.
 */
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

package com.gsma.service.rcs.history;

import com.gsma.rcs.core.content.FileContent;
import com.gsma.rcs.core.content.MmContent;
import com.gsma.rcs.core.content.VideoContent;
import com.gsma.rcs.core.ims.protocol.PayloadException;
import com.gsma.rcs.core.ims.service.im.chat.ChatMessage;
import com.gsma.rcs.provider.LocalContentResolver;
import com.gsma.rcs.provider.contact.ContactProvider;
import com.gsma.rcs.provider.history.HistoryLogData;
import com.gsma.rcs.provider.history.HistoryProvider;
import com.gsma.rcs.provider.messaging.FileTransferData;
import com.gsma.rcs.provider.messaging.FileTransferProvider;
import com.gsma.rcs.provider.messaging.MessageData;
import com.gsma.rcs.provider.messaging.MessagingLog;
import com.gsma.rcs.provider.settings.RcsSettings;
import com.gsma.rcs.provider.settings.RcsSettingsProvider;
import com.gsma.rcs.provider.sharing.GeolocSharingData;
import com.gsma.rcs.provider.sharing.ImageSharingData;
import com.gsma.rcs.provider.sharing.RichCallHistory;
import com.gsma.rcs.provider.sharing.VideoSharingData;
import com.gsma.rcs.service.api.HistoryServiceImpl;
import com.gsma.rcs.utils.ContactUtilMockContext;
import com.gsma.services.rcs.Geoloc;
import com.gsma.services.rcs.RcsPermissionDeniedException;
import com.gsma.services.rcs.RcsService.Direction;
import com.gsma.services.rcs.chat.ChatLog;
import com.gsma.services.rcs.chat.ChatLog.Message;
import com.gsma.services.rcs.chat.ChatLog.Message.MimeType;
import com.gsma.services.rcs.contact.ContactId;
import com.gsma.services.rcs.contact.ContactUtil;
import com.gsma.services.rcs.filetransfer.FileTransfer;
import com.gsma.services.rcs.filetransfer.FileTransferLog;
import com.gsma.services.rcs.history.HistoryLog;
import com.gsma.services.rcs.history.HistoryUriBuilder;
import com.gsma.services.rcs.history.IHistoryService;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharing;
import com.gsma.services.rcs.sharing.geoloc.GeolocSharingLog;
import com.gsma.services.rcs.sharing.image.ImageSharing;
import com.gsma.services.rcs.sharing.image.ImageSharingLog;
import com.gsma.services.rcs.sharing.video.VideoSharing;
import com.gsma.services.rcs.sharing.video.VideoSharingLog;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import android.os.RemoteException;
import android.provider.BaseColumns;
import android.test.AndroidTestCase;
import android.test.IsolatedContext;
import android.test.mock.MockContentResolver;
import android.util.Log;



import java.io.IOException;
import java.net.URI;
import java.sql.SQLDataException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class HistoryLogTest extends AndroidTestCase {

    private static final String REMOTE_CONTACT_NUMBER = "+46123456789";

    private static final String SELECTION_EMPTY = "";

    private static final String SELECTION_NOT_EMPTY = HistoryLog.CONTACT + "='"
            + REMOTE_CONTACT_NUMBER + "'";

    private static final String FILE_TRANSFER_ID = "FtId" + System.currentTimeMillis();

    private static final String MESSAGE_ID = "MsgId" + System.currentTimeMillis();

    private static final String IMAGE_SHARING_ID = "ImageSharingId" + System.currentTimeMillis();

    private static final String VIDEO_SHARING_ID = "VideoSharingId" + System.currentTimeMillis();

    private static final String GEOLOC_SHARING_ID = "GeolocSharingId" + System.currentTimeMillis();

    private static final String IMAGE_FILE_NAME = "image1.jpg";

    private static final String VIDEO_FILE_NAME = "video1.mpg";

    private static final String IMAGE_URI = "content://file/image1.jpg";

    private static final String VIDEO_URI = "content://file/video1.mpg";

    private static final long IMAGE_FILE_SIZE = 123456;

    private static final long VIDEO_FILE_SIZE = 1234567;

    private static final String THUMBNAIL_FILE_NAME = "thumbnail1.jpg";

    private static final String THUMBNAIL_URI = "content://file/thumbnail1.jpg";

    private static final long THUMBNAIL_FILE_SIZE = 1234;

    private static final String VIDEO_ENCODING = "video/";

    private static MyContentProvider sMyContentProvider = new MyContentProvider();

    private static final MmContent CONTENT = new FileContent(Uri.parse(IMAGE_URI), IMAGE_FILE_SIZE,
            IMAGE_FILE_NAME);

    private static final MmContent THUMBNAIL = new FileContent(Uri.parse(THUMBNAIL_URI),
            THUMBNAIL_FILE_SIZE, THUMBNAIL_FILE_NAME);

    private static final VideoContent VIDEO_CONTENT = new VideoContent(Uri.parse(VIDEO_URI),
            VIDEO_ENCODING, VIDEO_FILE_SIZE, VIDEO_FILE_NAME);

    private static final String TXT = "Hello";

    private static final String DISPLAY_NAME = "display";

    private static final int EXTERNAL_PROVIDER_ID = 10;

    private static final int INVALID_EXTERNAL_PROVIDER_ID = 1;

    public static final String EXTERNAL_AUTHORITY = "com.gsma.services.rcs.provider.externaltest";

    public static final Uri EXTERNAL_URI = Uri.parse("content://" + EXTERNAL_AUTHORITY);

    public static final String EXTERNAL_TABLE = "mytable";

    public static final String EXTERNAL_TABLE_CREATE = "create table if not exists "
            + EXTERNAL_TABLE + " ( _id integer, myid text, mycontent text, mytimestamp integer )";

    public static final String EXTERNAL_DATABASE_NAME = "my.db";

    private static final String[] PROJECTION = new String[] {
            HistoryLog.PROVIDER_ID, HistoryLog.BASECOLUMN_ID, HistoryLog.ID
    };

    private static final String SELECTION_WITH_MESSAGE_ID = HistoryLog.ID + "='" + MESSAGE_ID + "'";

    private static final String SELECTION_ID = HistoryLog.ID + "=?";

    private static final String[] SELECTION_ARGS = new String[] {
        MESSAGE_ID
    };

    private static final String SORT_TIMESTAMP_ASC = HistoryLog.TIMESTAMP + " ASC";

    private static final String SORT_TIMESTAMP_DESC = HistoryLog.TIMESTAMP + " DESC";

    private static IHistoryService sHistoryService;

    private MessagingLog mMessagingLog;

    private static LocalContentResolver sLocalContentResolver;

    private RichCallHistory mRichCallHistory;

    private Random mRandom = new Random();

    private long mTimestamp;

    private long mTimestampSent;

    private ContactUtil mContactUtil;
    String mStrMsgAuthority;
    String mStrFTAuthority;
    String mStrImgAuthority;
    String mStrVideoAuthority;
    String mStrGeolocAuthority;
    String mStrHistoryAuthority;

    static class MyContentProvider extends ContentProvider {
        private boolean mQueried = false;

        MyContentProvider() {
        }

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                String sortOrder) throws NullPointerException {
            Log.i("HISTORY", "query external");
            Context cont = getContext();
            if (cont == null) {
                throw new NullPointerException("Cont is NULL");
            }
            SQLiteDatabase db = getContext().openOrCreateDatabase(EXTERNAL_DATABASE_NAME,
                    Context.MODE_PRIVATE, null);
            if (db == null) {
                throw new NullPointerException("DataBase is NULL" + EXTERNAL_DATABASE_NAME);
            }
            Cursor c = db.query(EXTERNAL_TABLE, projection, null, null, null, null, null);
            if (c == null) {
                throw new NullPointerException("Cursor is NULL");

            }
            return c;

        }

        public void setQueried(boolean bQueried) {
            mQueried = bQueried;
        }

        @Override
        public String getType( Uri uri) {
            return null;
        }

        @Override
        public Uri insert( Uri uri, ContentValues values) {
            return null;
        }

        @Override
        public int delete( Uri uri, String selection,
                 String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update( Uri uri,  ContentValues values,
                String selection,String[] selectionArgs) {
            return 0;
        }
    }

    ContentResolver getContentResolver()
    {
        ContentResolver realResolver = getContext().getContentResolver();
        if (realResolver == null) {
            throw new SQLException("mockResolver is NULL");
        }
        return realResolver;
    }

    MockContentResolver getMokResolver()
    {
        MockContentResolver mockResolver = new MockContentResolver();
        if (mockResolver == null) {
            throw new SQLException("mockResolver is NULL");
        }
        return mockResolver;
    }

    ContentProvider getContentProvider(ContentResolver resolver,Uri uri)
    {
        ContentProvider provider=resolver.acquireContentProviderClient(uri).getLocalContentProvider();
        if (provider == null) {
            throw new SQLException("mockResolver is NULL");
        }
        return provider;
    }

    protected void setUp() throws Exception {
        super.setUp();
        IsolatedContext iContext = null;
        ContentResolver mRealResolver = null;
        MockContentResolver mockResolver = null;
        ContentProvider provider;
        mRealResolver=getContentResolver();

            mockResolver = new MockContentResolver();

            iContext = new IsolatedContext(mockResolver, super.getContext());
            mStrMsgAuthority = MessageData.CONTENT_URI.getAuthority();
            mStrFTAuthority = FileTransferData.CONTENT_URI.getAuthority();
            mStrImgAuthority = ImageSharingData.CONTENT_URI.getAuthority();
            mStrVideoAuthority = VideoSharingData.CONTENT_URI.getAuthority();
            mStrGeolocAuthority = GeolocSharingData.CONTENT_URI.getAuthority();
            mStrHistoryAuthority = HistoryLogData.CONTENT_URI.getAuthority();
            if (mStrMsgAuthority.isEmpty() || mStrFTAuthority.isEmpty()
                    || mStrImgAuthority.isEmpty() || mStrVideoAuthority.isEmpty()
                    || mStrGeolocAuthority.isEmpty() || mStrHistoryAuthority.isEmpty()) {
                throw new IllegalStateException("Authority is Empty");
            }


        mockResolver=getMokResolver();

            provider = getContentProvider(mRealResolver, Message.CONTENT_URI);

            mockResolver.addProvider(mStrMsgAuthority, provider);

            provider = getContentProvider(mRealResolver, FileTransferLog.CONTENT_URI);

            mockResolver.addProvider(mStrFTAuthority, provider);

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(ImageSharingLog.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrImgAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(VideoSharingLog.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrVideoAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(GeolocSharingLog.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrGeolocAuthority, Objects.requireNonNull(provider));

            HistoryProvider historyProvider = (HistoryProvider) Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(HistoryLog.CONTENT_URI))
                    .getLocalContentProvider();

            mockResolver.addProvider(mStrHistoryAuthority, Objects.requireNonNull(historyProvider));
            mockResolver.addProvider(EXTERNAL_AUTHORITY, sMyContentProvider);
            historyProvider.registerInternalProviders();

            sMyContentProvider.attachInfo(iContext, null);
            sMyContentProvider.setQueried(false);

            setContext(iContext);

            if (sLocalContentResolver == null) {
                sLocalContentResolver = new LocalContentResolver(mockResolver);
            }
            mMessagingLog = MessagingLog.getInstance(sLocalContentResolver,
                    RcsSettings.getInstance(sLocalContentResolver));
            RichCallHistory.getInstance(sLocalContentResolver);
            mRichCallHistory = RichCallHistory.getInstance();
            mContactUtil = ContactUtil.getInstance(new ContactUtilMockContext(
                    new ContactUtilMockContext(getContext())));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(MessageData.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrMsgAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(FileTransferData.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrFTAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(ImageSharingData.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrImgAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(VideoSharingData.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrVideoAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(GeolocSharingData.CONTENT_URI))
                    .getLocalContentProvider();
            mockResolver.addProvider(mStrGeolocAuthority, Objects.requireNonNull(provider));

            provider = Objects.requireNonNull(
                    mRealResolver.acquireContentProviderClient(HistoryLogData.CONTENT_URI))
                    .getLocalContentProvider();

            mockResolver.addProvider(mStrHistoryAuthority, Objects.requireNonNull(provider));

            assert sLocalContentResolver != null;
            sLocalContentResolver.delete(MessageData.CONTENT_URI, null, null);
            sLocalContentResolver.delete(ImageSharingData.CONTENT_URI, null, null);
            sLocalContentResolver.delete(VideoSharingData.CONTENT_URI, null, null);
            sLocalContentResolver.delete(GeolocSharingData.CONTENT_URI, null, null);


                SQLiteDatabase dbft = getContext().openOrCreateDatabase(
                        FileTransferProvider.DATABASE_NAME, Context.MODE_PRIVATE, null);
                dbft.delete(FileTransferProvider.TABLE, null, null);
                dbft.close();


            sHistoryService = new HistoryServiceImpl(getContext());

            getContext().deleteDatabase(EXTERNAL_DATABASE_NAME);
            SQLiteDatabase db = getContext().openOrCreateDatabase(EXTERNAL_DATABASE_NAME,
                    Context.MODE_PRIVATE, null);
            db.execSQL(EXTERNAL_TABLE_CREATE);
            db.delete(EXTERNAL_TABLE, null, null);
            db.close();
            mTimestamp = mRandom.nextLong();
            mTimestampSent = mRandom.nextLong();


    }// setup

    public Context getContext() {
        return super.getContext();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private ContactId getRemoteContact() throws RcsPermissionDeniedException {
        return mContactUtil.formatContact(REMOTE_CONTACT_NUMBER);
    }

    private void addOutgoingFileTransferSharing() throws RcsPermissionDeniedException {
        mMessagingLog.addOneToOneFileTransfer(FILE_TRANSFER_ID, getRemoteContact(),
                Direction.INCOMING, CONTENT, THUMBNAIL, FileTransfer.State.INVITED,
                FileTransfer.ReasonCode.UNSPECIFIED, mTimestamp++, mTimestampSent++,
                FileTransferLog.UNKNOWN_EXPIRATION, FileTransferLog.UNKNOWN_EXPIRATION);
    }

    private void addOutgoingOneToOneChatMessages(String... ids)
            throws RcsPermissionDeniedException, PayloadException, IOException {
        if (ids.length == 0) {
            ChatMessage msg = new ChatMessage(MESSAGE_ID, getRemoteContact(), TXT,
                    MimeType.TEXT_MESSAGE, mTimestamp++, mTimestampSent++, DISPLAY_NAME);
            mMessagingLog.addOutgoingOneToOneChatMessage(msg, Message.Content.Status.SENT,
                    Message.Content.ReasonCode.UNSPECIFIED, 0);
        }
        for (String id : ids) {
            ChatMessage msg = new ChatMessage(id, getRemoteContact(), TXT, MimeType.TEXT_MESSAGE,
                    mTimestamp++, mTimestampSent++, DISPLAY_NAME);
            mMessagingLog.addOutgoingOneToOneChatMessage(msg, Message.Content.Status.SENT,
                    Message.Content.ReasonCode.UNSPECIFIED, 0);
        }
    }

    private void addOutgoingImageSharing() throws RcsPermissionDeniedException {
        mRichCallHistory.addImageSharing(IMAGE_SHARING_ID, getRemoteContact(), Direction.INCOMING,
                CONTENT, ImageSharing.State.ACCEPTING, ImageSharing.ReasonCode.UNSPECIFIED,
                mTimestamp++);
    }

    private void addOutgoingVideoSharing() throws RcsPermissionDeniedException {
        mRichCallHistory.addVideoSharing(VIDEO_SHARING_ID, getRemoteContact(), Direction.INCOMING,
                VIDEO_CONTENT, VideoSharing.State.ACCEPTING, VideoSharing.ReasonCode.UNSPECIFIED,
                mTimestamp++);
    }

    private void addOutgoingGeolocSharing() throws RcsPermissionDeniedException {
        mRichCallHistory.addOutgoingGeolocSharing(getRemoteContact(), GEOLOC_SHARING_ID,
                new Geoloc("test", 0, 0, 0, 0), GeolocSharing.State.TRANSFERRED,
                GeolocSharing.ReasonCode.UNSPECIFIED, mTimestamp++);
    }

    private void addItems() throws RcsPermissionDeniedException, PayloadException, IOException {
        addOutgoingOneToOneChatMessages();
        addOutgoingFileTransferSharing();
        addOutgoingImageSharing();
        addOutgoingVideoSharing();
        addOutgoingGeolocSharing();
    }

    private Uri getUriWithAllInternalProviders() {
        return createHistoryUri(ChatLog.Message.HISTORYLOG_MEMBER_ID,
                FileTransferData.HISTORYLOG_MEMBER_ID, ImageSharingData.HISTORYLOG_MEMBER_ID,
                VideoSharingData.HISTORYLOG_MEMBER_ID, GeolocSharingData.HISTORYLOG_MEMBER_ID);
    }

    private void verifyHistoryLogEntries(Cursor cursor) {
        boolean messageEncountered = false;
        boolean fileTransferEncountered = false;
        boolean imageSharingEncountered = false;
        boolean videoSharingEncountered = false;
        boolean geolocSharingEncountered = false;

        while (cursor.moveToNext()) {
            int providerId = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryLog.PROVIDER_ID));
            String readUniqueId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));

            if (ChatLog.Message.HISTORYLOG_MEMBER_ID == providerId) {
                assertEquals(MESSAGE_ID, readUniqueId);
                messageEncountered = true;

            } else if (FileTransferData.HISTORYLOG_MEMBER_ID == providerId) {
                assertEquals(FILE_TRANSFER_ID, readUniqueId);
                fileTransferEncountered = true;

            } else if (ImageSharingData.HISTORYLOG_MEMBER_ID == providerId) {
                assertEquals(IMAGE_SHARING_ID, readUniqueId);
                imageSharingEncountered = true;

            } else if (GeolocSharingData.HISTORYLOG_MEMBER_ID == providerId) {
                assertEquals(GEOLOC_SHARING_ID, readUniqueId);
                geolocSharingEncountered = true;

            } else {
                assertEquals(VideoSharingData.HISTORYLOG_MEMBER_ID, providerId);
                assertEquals(VIDEO_SHARING_ID, readUniqueId);
                videoSharingEncountered = true;
            }
        }
        assertTrue(messageEncountered);
        assertTrue(fileTransferEncountered);
        assertTrue(imageSharingEncountered);
        assertTrue(videoSharingEncountered);
        assertTrue(geolocSharingEncountered);
    }

    private void verifyChatLogEntry(Cursor cursor) {
        while (cursor.moveToNext()) {
            int providerId = cursor.getInt(cursor.getColumnIndexOrThrow(HistoryLog.PROVIDER_ID));
            String readUniqueId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));

            assertEquals(ChatLog.Message.HISTORYLOG_MEMBER_ID, providerId);
            assertEquals(MESSAGE_ID, readUniqueId);
        }
    }

    private static Uri createHistoryUri(int... providerIds) {
        HistoryUriBuilder uriBuilder = new HistoryUriBuilder(HistoryLog.CONTENT_URI);

        for (int providerId : providerIds) {
            uriBuilder.appendProvider(providerId);
        }

        return uriBuilder.build();
    }

    private static Map<String, String> getExternalColumnMapping() {
        Map<String, String> columnMapping = new HashMap<>();
        columnMapping.put(HistoryLog.PROVIDER_ID, String.valueOf(EXTERNAL_PROVIDER_ID));
        columnMapping.put(HistoryLog.BASECOLUMN_ID, BaseColumns._ID);
        columnMapping.put(HistoryLog.ID, "myid");
        columnMapping.put(HistoryLog.CONTENT, "mycontent");
        columnMapping.put(HistoryLog.TIMESTAMP, "mytimestamp");
        return columnMapping;
    }

    public void testQueryHistoryLogProviderWithoutProjection() throws RcsPermissionDeniedException,
            PayloadException, IOException, SQLDataException {
        addItems();
        Uri historyUri = getUriWithAllInternalProviders();
        try {

            Cursor cursor = getContext().getContentResolver().query(historyUri, null, null, null,
                    null);
            if (cursor == null) {
                throw new NullPointerException();
            }
            assertEquals(5, cursor.getCount());
            verifyHistoryLogEntries(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testQueryHistoryLogProviderWithProjection() throws RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems();
        Uri historyUri = getUriWithAllInternalProviders();
        Cursor cursor = getContext().getContentResolver().query(historyUri, PROJECTION, null, null,
                null);
        assertNotNull(cursor);
        assertEquals(5, cursor.getCount());
        verifyHistoryLogEntries(cursor);
    }

    public void testQueryHistoryLogProviderWithSelection() throws RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems();
        Uri historyUri = getUriWithAllInternalProviders();
        Cursor cursor = getContext().getContentResolver().query(historyUri, null,
                SELECTION_WITH_MESSAGE_ID, null, null);
        assert cursor != null;
        assertEquals(1, cursor.getCount());
        verifyChatLogEntry(cursor);
    }

    public void testQueryHistoryLogProviderWithSelectionArgs() throws RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems();
        Uri historyUri = getUriWithAllInternalProviders();
        try {

            Cursor cursor = getContext().getContentResolver().query(historyUri, null, SELECTION_ID,
                    SELECTION_ARGS, null);
            if (cursor == null) {
                throw new NullPointerException("Cursor is null");
            }
            assertEquals(1, cursor.getCount());
            verifyChatLogEntry(cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testQueryHistoryLogProviderWithSelectionEmpty()
            throws RcsPermissionDeniedException, PayloadException, IOException {
        addItems();
        Uri historyUri = getUriWithAllInternalProviders();
        try {
            Cursor cursor = getContext().getContentResolver().query(historyUri, null,
                    SELECTION_EMPTY, null, null);
            if (cursor == null) {
                throw new NullPointerException("Cursor is null");
            }
            assertEquals(5, cursor.getCount());
            verifyHistoryLogEntries(cursor);
        } catch (Exception ignore) {
        }

    }

    public void testQueryHistoryLogProviderWithSelectionNotEmpty()
            throws RcsPermissionDeniedException, PayloadException, IOException {
        addItems();
        Cursor cursor = null;
        Uri historyUri = getUriWithAllInternalProviders();
        try {

            cursor = getContext().getContentResolver().query(historyUri, null, SELECTION_NOT_EMPTY,
                    null, null);
            if (cursor == null) {
                throw new NullPointerException("Cursor is null");
            }
            assertEquals(5, cursor.getCount());
            verifyHistoryLogEntries(cursor);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

    public void testQueryHistoryLogProviderWithSort() throws RcsPermissionDeniedException,
            PayloadException, IOException {
        addOutgoingOneToOneChatMessages();
        addOutgoingFileTransferSharing();
        Cursor cursor;
        try {
            Uri historyUri = getUriWithAllInternalProviders();
            if (historyUri == null) {
                throw new NullPointerException("Cursor is null");
            }
            cursor = getContext().getContentResolver().query(historyUri, null, null, null,
                    SORT_TIMESTAMP_ASC);
            if (cursor == null) {
                throw new NullPointerException("Cursor is null");
            }
            assertNotNull(cursor);
            assertEquals(2, cursor.getCount());
            cursor.moveToNext();
            String firstAscId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));
            cursor.moveToNext();
            String secondAscId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));
            cursor = getContext().getContentResolver().query(historyUri, null, null, null,
                    SORT_TIMESTAMP_DESC);
            if (cursor == null) {
                throw new NullPointerException("Cursor is null");
            }
            assertEquals(2, cursor.getCount());
            cursor.moveToNext();
            String firstDescId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));
            cursor.moveToNext();
            String secondDescId = cursor.getString(cursor.getColumnIndexOrThrow(HistoryLog.ID));
            assertEquals(firstAscId, secondDescId);
            assertEquals(secondAscId, firstDescId);
            assertTrue(!firstAscId.equals(secondAscId));
        } catch (Exception ignore) {
        }
    }

    public void testRegisterInvalidExtraHistoryLogMember_badproviderid()
            throws RcsPermissionDeniedException, PayloadException, IOException {
        addItems();
        Map<String, String> columnMapping = new HashMap<>();
        columnMapping.put(HistoryLog.PROVIDER_ID, String.valueOf(INVALID_EXTERNAL_PROVIDER_ID));

        Uri database = Uri.fromFile(getContext().getDatabasePath(EXTERNAL_DATABASE_NAME));
        try {
            sHistoryService.registerExtraHistoryLogMember(INVALID_EXTERNAL_PROVIDER_ID,
                    EXTERNAL_URI, database, EXTERNAL_TABLE, columnMapping);
            fail();
        } catch (Exception ignore) {
        }
    }

    public void testRegisterExtraHistoryLogMemberWithForbiddenDatabases()
            throws RcsPermissionDeniedException, PayloadException, IOException {
        addItems();
        Map<String, String> columnMapping = new HashMap<>();
        columnMapping.put(HistoryLog.PROVIDER_ID, String.valueOf(EXTERNAL_PROVIDER_ID));

        try {
            sHistoryService.registerExtraHistoryLogMember(EXTERNAL_PROVIDER_ID, EXTERNAL_URI,
                    Uri.fromFile(getContext().getDatabasePath(RcsSettingsProvider.DATABASE_NAME)),
                    EXTERNAL_TABLE, columnMapping);
            fail();
        } catch (Exception ignore) {
        }

        try {
            sHistoryService.registerExtraHistoryLogMember(EXTERNAL_PROVIDER_ID, EXTERNAL_URI,
                    Uri.fromFile(getContext().getDatabasePath(ContactProvider.DATABASE_NAME)),
                    EXTERNAL_TABLE, columnMapping);
            fail();
        } catch (Exception ignore) {
        }
    }

    public void testRegisterExtraHistoryLogMemberWithMappingNull()
            throws RcsPermissionDeniedException, PayloadException, IOException {
        addItems();

        Uri database = Uri.fromFile(getContext().getDatabasePath(EXTERNAL_DATABASE_NAME));
        try {
            sHistoryService.registerExtraHistoryLogMember(EXTERNAL_PROVIDER_ID, EXTERNAL_URI,
                    database, EXTERNAL_TABLE, null);
            fail();
        } catch (Exception ignore) {
        }
    }

    public void testUnregisterInvalidExtraHistoryLogMember() {
        try {
            sHistoryService.unregisterExtraHistoryLogMember(INVALID_EXTERNAL_PROVIDER_ID);
            fail();
        } catch (Exception ignore) {
        }
    }

    public void testSQLInjection_selection() throws RemoteException, RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems(); // 5
        ContentResolver cResolver;
        Cursor curs;
        try {
            cResolver = getContext().getContentResolver();
            if (cResolver == null) {
                throw new NullPointerException();
            }
            curs = cResolver.query(getUriWithAllInternalProviders(), null, null, null, null);
            if (curs == null) {
                throw new NullPointerException();
            }
            assertEquals(5, curs.getCount());

            try {
                curs = cResolver.query(createHistoryUri(1), null, "_id = 1;DROP TABLE MESSAGE;",
                        null, null);
                if (curs == null) {
                    throw new NullPointerException();
                }
                curs.close();
                fail();
            } catch (Exception e) {
                addOutgoingOneToOneChatMessages("AnotherOne"); // 1
                curs = cResolver.query(getUriWithAllInternalProviders(), null, null, null, null);
                if (curs == null) {
                    throw new NullPointerException();
                }

                assertEquals(6, curs.getCount());
            }
        } catch (Exception ignore) {
        }
    }

    public void testSQLInjection_sort() throws RemoteException, RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems(); // 5
        ContentResolver cResolver;
        Cursor curs;
        try {
            cResolver = getContext().getContentResolver();
            if (cResolver == null) {
                throw new NullPointerException();
            }
            curs = cResolver
                    .query(createHistoryUri(1), null, null, null, "_id;DROP TABLE MESSAGE;");
            if (curs == null) {
                throw new NullPointerException();
            }
            curs.close();
            curs = cResolver.query(getUriWithAllInternalProviders(), null, null, null, null);
            if (curs == null) {
                throw new NullPointerException();
            }
            addOutgoingOneToOneChatMessages("AnotherOne"); // 1
            assertEquals(6, curs.getCount());
        } catch (Exception ignore) {
        }
    }

    public void testSQLInjection_tablename() throws RemoteException, RcsPermissionDeniedException,
            PayloadException, IOException {
        addItems(); // 5
        ContentResolver contResolv;
        Cursor cursor = null;
        try {
            contResolv = getContext().getContentResolver();

            if (contResolv == null) {
                throw new NullPointerException();
            }
            cursor = contResolv.query(getUriWithAllInternalProviders(), null, null, null, null);
            if (cursor == null) {
                throw new NullPointerException();
            }
            assertEquals(5, cursor.getCount());

            Uri database = Uri.fromFile(getContext().getDatabasePath(EXTERNAL_DATABASE_NAME));
            sHistoryService.registerExtraHistoryLogMember(EXTERNAL_PROVIDER_ID, EXTERNAL_URI,
                    database, EXTERNAL_TABLE + ";DROP TABLE MESSAGE;", getExternalColumnMapping());

            Uri uriProvider = getUriWithAllInternalProviders();
            if (uriProvider == null) {
                throw new NullPointerException();
            }
            addOutgoingOneToOneChatMessages("YetAnotherOne"); // 1
            cursor = contResolv.query(uriProvider, null, null, null, null);
            if (cursor == null) {
                throw new NullPointerException();
            }
            assertEquals(6, cursor.getCount());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

    }

}
