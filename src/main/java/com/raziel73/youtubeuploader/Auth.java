package com.raziel73.youtubeuploader;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.FileDataStoreFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;


/**
 * Created by damien on 2017/03/24.
 */

public class Auth {

    /**
     * Define a global instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();


    public static void authorize(List<String> scopes, String credentialDatastore, int client, final Activity activity, final AuthCallback callback) throws IOException {

        // Load client secrets.
        Reader clientSecretReader = new InputStreamReader(activity.getResources().openRawResource(client));
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, clientSecretReader);

        // This creates the credentials datastore inside the application package (using Android/data/~)
        File f = new File( activity.getExternalFilesDir(null) + "/credentials/"+credentialDatastore);
        f.mkdirs();
        FileDataStoreFactory fileDataStoreFactory = new FileDataStoreFactory(f);
        DataStore<StoredCredential> datastore = fileDataStoreFactory.getDataStore(credentialDatastore);

        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, scopes).setCredentialDataStore(datastore)
                .build();


                LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8080).build();

                try {

                    Credential credential = flow.loadCredential("user");
                    if (credential != null   && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {

                        //pass credentials in callback
                        callback.onCredential(credential);

                    } else {
                        // open in browser
                        String redirectUri = receiver.getRedirectUri();
                        AuthorizationCodeRequestUrl authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri);
                        onAuthorization(authorizationUrl, activity);
                        // receive authorization code and exchange it for an access token
                        String code = receiver.waitForCode();
                        TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
                        // store credential and return it
                        credential = flow.createAndStoreCredential(response, "user");
                        //pass credentials in callback
                        callback.onCredential(credential);
                    }

                    receiver.stop();

             } catch (IOException e) {


                    e.printStackTrace();

             }

    }


    protected static void onAuthorization(AuthorizationCodeRequestUrl authorizationUrl,Activity activity) throws IOException {
        browse(authorizationUrl.build(), activity);
    }

    /**
     * Open the oauth URL in the device browser
     * @param url
     * @param activity
     */
    public static void browse(final String url, Activity activity) {

       Intent i = new Intent(Intent.ACTION_VIEW);
       i.setData(Uri.parse(url));
       activity.startActivity(i);

    }

    /**
     * Callback Interface
     */
    interface AuthCallback {

        void onCredential(Credential cred);

    }
}

