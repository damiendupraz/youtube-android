package com.raziel73.youtubeuploader;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.espresso.core.deps.guava.collect.Lists;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.google.api.client.googleapis.media.MediaHttpUploader.UploadState.*;

/**
 * Created by damien on 2017/03/28.
 */

public class YoutubeUploader {

    private static final String VIDEO_FILE_FORMAT = "video/*";
    private static final List<String> SCOPES = Lists.newArrayList("https://www.googleapis.com/auth/youtube.upload");
    private static final String STORE = "yoauth";
    private static final int CLIENT = R.raw.client_secrets;

    private Activity activity = null;

    /**
     * Constructor
     * @param activity
     */
    public YoutubeUploader(Activity activity){
        this.activity = activity;
    }

    /**
     *
     * @param uri
     * @param title
     * @param description
     * @param tags
     * @param status
     * @param upload
     * @throws IOException
     */
    public  void upload(final Uri uri, final String title, final String description, final List<String> tags, final String status, final UploadCallback upload) throws IOException {

       Auth.authorize(SCOPES, STORE, CLIENT ,activity, new Auth.AuthCallback() {
            @Override
            public void onCredential(Credential cred) {

               YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, cred).setApplicationName("youtube-cmdline-uploadvideo-sample").build();

                // Add extra information to the video before uploading.
                Video videoObjectDefiningMetadata = new Video();

                // Set the video to be publicly visible. This is the default
                // setting. Other supporting settings are "unlisted" and "private."
                videoObjectDefiningMetadata.setStatus(new VideoStatus().setPrivacyStatus(status));

                // Most of the video's metadata is set on the VideoSnippet object.
                VideoSnippet snippet = new VideoSnippet();
                snippet.setTitle(title);
                snippet.setDescription(description);
                snippet.setTags(tags);
                videoObjectDefiningMetadata.setSnippet(snippet);

                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(getRealPathFromURI(activity,uri));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                InputStreamContent mediaContent = new InputStreamContent(VIDEO_FILE_FORMAT,fileInputStream);

                // Insert the video. The command sends three arguments. The first
                // specifies which information the API request is setting and which
                // information the API response should return. The second argument
                // is the video resource that contains metadata about the new video.
                // The third argument is the actual video content.
                YouTube.Videos.Insert videoInsert = null;
                try {
                    videoInsert = youtube.videos().insert("snippet,statistics,status", videoObjectDefiningMetadata, mediaContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Set the upload type and add an event listener.
                MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();

                // Indicate whether direct media upload is enabled. A value of
                // "True" indicates that direct media upload is enabled and that
                // the entire media content will be uploaded in a single request.
                // A value of "False," which is the default, indicates that the
                // request will use the resumable media upload protocol, which
                // supports the ability to resume an upload operation after a
                // network interruption or other transmission failure, saving
                // time and bandwidth in the event of network failures.
                uploader.setDirectUploadEnabled(false);

                MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
                    public void progressChanged(MediaHttpUploader uploader) throws IOException {
                        switch (uploader.getUploadState()) {
                            case INITIATION_STARTED:
                                //case not handled yet
                                break;
                            case INITIATION_COMPLETE:
                                //case not handled yet
                                break;
                            case MEDIA_IN_PROGRESS:
                                    upload.onProgress(uploader.getNumBytesUploaded());
                                break;
                            case MEDIA_COMPLETE:
                                //case not handled yet
                                break;
                            case NOT_STARTED:
                                //case not handled yet
                                break;
                        }
                    }
                };
                uploader.setProgressListener(progressListener);

                //Upload video on a thread
                final YouTube.Videos.Insert finalVideoInsert = videoInsert;
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            Video returnedVideo = finalVideoInsert.execute();
                            upload.onComplete(returnedVideo);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();


            }
        });

    }

    /**
     * Retrieve the real file path of a resource uri
     * @param context
     * @param contentUri
     * @return
     */
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Video.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    /**
     * Callback interface
     */
    public interface UploadCallback {

        void onProgress(long bytes);

        void onComplete(Video video);

    }

}
