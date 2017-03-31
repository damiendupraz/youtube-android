# what is Youtube Uploader for Android ?
YoutubeUplaoder is a oauth 2.0 based video uploader for Android applications. It uploads video to the Youtube platform seamlessly after an oauth authentication has been done by the user.
Credentials are kept in the system to avoid repeated authentication.
The upload is done in a separated thread to avoid blocking the UI.

# dependencies ?
YoutubeUploader is already packed with all the necessary dependencies and should be self sufficient when integrated in an Android project.
A good practice would be to generate a .aar file from the sources before integrating it in a project.
A second step would be to make the aar package available through Maven or any other packages distribution platform for smooth integration with gradle.

# how to use ?

```java

            new YoutubeUploader(this).upload(videoUri,"My Title","My Description",new ArrayList<String>(), "unlisted",new YoutubeUploader.UploadCallback() {

                @Override
                public void onProgress(long bytes) {
                    //call back on upload progress with number og bytes already uploaded
                }

                @Override
                public void onComplete(Video video) {
                    //call back on video upload is completed, the Video object provides the video ID with the getId() methode
                }
            });