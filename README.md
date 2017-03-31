# what is Youtube Uploader for Android ?
YoutubeUplaoder is a oauth 2.0 based video uploader for Android applications. It uploads video to the Youtube platform seamlessly after an oauth authentication has been done by the user.
Credentials are kept in the system to avoid repeated authentication.
The upload is done in a separated thread to avoid blocking the UI.

# dependencies ?
YoutubeUploader is already packed with all the necessary dependencies and should be self sufficient when integrated in an Android project.
A good practice would be to generate a .aar file from the sources before integrating it in a project.
A second step would be to make the aar package available through Maven or any other packages distribution platform for smooth integration with gradle.

# how to use ?

It is first essential to create a client ID from the  Google API console to give access to the Youtube API.
The client ID exported to JSON format must be copy/pasted inside the following file : res/raw/client_secret.json

Then the Youtube Uploader for Android should be used as follow

Ccreate a new YoutubeUploader object by passing the current Activity (this) object to the constructor then call the upload() method on that object.
Parameters of the upload method are as follow :
- a video Uri (resource Uri provided by an Intent most of the time)
- a video title
- a video description
- a set of tags to add to the video meta-data, it is an ArrayList of Strings
- a visibility status (public, private, unlisted)
- implementation of a callback interface with 2 methods onProgress and onComplete

```java

            //
            new YoutubeUploader(this).upload(videoUri,"My Title","My Description",new ArrayList<String>(), "unlisted",new YoutubeUploader.UploadCallback() {

                @Override
                public void onProgress(long bytes) {
                    //call back on upload progress with number og bytes already uploaded
                }

                @Override
                public void onComplete(Video video) {
                    //call back on video upload is completed, the Video object provides the video ID with the getId() method
                }
            });
```

# ongoing development
- test and code coverage
- distribution through maven
- devices compatibility
- factorize the code and make it even more generic
- comment the code