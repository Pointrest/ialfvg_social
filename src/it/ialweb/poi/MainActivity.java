package it.ialweb.poi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

import it.ialweb.poi.it.ialweb.poi.models.Post;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutionException;

import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.notifications.NotificationsManager;
import com.microsoft.windowsazure.notifications.NotificationsManager;
public class MainActivity extends AppCompatActivity implements NewPostDialog.Callback {

    public static final String SENDER_ID = "baassi-999";
    public static MobileServiceClient mClientMSC;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MobileServiceClient mClient;
    private MobileServiceTable<Post> mPostTable;
    private List<Post> mPosts = new ArrayList<Post>();


    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";

    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            setupAMC();
            // Authenticate passing false to load the current token cache if available.
            authenticate(false);
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("Error creating the Mobile Service. " +
                    "Verify the URL"), "Error");
        }

        NotificationsManager.handleNotifications(this, SENDER_ID, MyHandler.class);

        setupGui();
    }

    private void setupGui() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.pager);

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            private int[] titles = new int[]{R.string.Timeline, R.string.Users, R.string.MyProfile};

            @Override
            public int getCount() {
                return titles.length;
            }

            @Override
            public Fragment getItem(int position) {
                return new PlaceHolder();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getResources().getString(titles[position]);
            }
        };
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        findViewById(R.id.fabBtn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new NewPostDialog().show(getSupportFragmentManager(), "WHAT_GOES_HERE?");
                //Snackbar.make(findViewById(R.id.coordinator), "abcdefg", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void setupAMC() throws MalformedURLException {
            mClient = new MobileServiceClient(
                    "https://baassi.azure-mobile.net/",
                    "QsyouuxvWGyryvCCVrCIvEvJCvgNQh42",
                    this
            );
    }

    private void cacheUserToken(MobileServiceUser user)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        editor.commit();

        mUserId = user.getUserId();
    }

    private boolean loadUserTokenCache(MobileServiceClient client)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, "undefined");
        if (userId.equals("undefined"))
            return false;
        String token = prefs.getString(TOKENPREF, "undefined");
        if (token.equals("undefined"))
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        client.setCurrentUser(user);
        mUserId = userId;
        return true;
    }

    /**
     * Detects if authentication is in progress and waits for it to complete.
     * Returns true if authentication was detected as in progress. False otherwise.
     */
    public boolean detectAndWaitForAuthentication()
    {
        boolean detected = false;
        synchronized(mAuthenticationLock)
        {
            do
            {
                if (bAuthenticating == true)
                    detected = true;
                try
                {
                    mAuthenticationLock.wait(1000);
                }
                catch(InterruptedException e)
                {}
            }
            while(bAuthenticating == true);
        }
        if (bAuthenticating == true)
            return true;

        return detected;
    }

    /**
     * Waits for authentication to complete then adds or updates the token
     * in the X-ZUMO-AUTH request header.
     *
     * @param request
     *            The request that receives the updated token.
     */
    private void waitAndUpdateRequestToken(ServiceFilterRequest request)
    {
        MobileServiceUser user = null;
        if (detectAndWaitForAuthentication())
        {
            user = mClient.getCurrentUser();
            if (user != null)
            {
                request.removeHeader("X-ZUMO-AUTH");
                request.addHeader("X-ZUMO-AUTH", user.getAuthenticationToken());
            }
        }
    }

    /**
     * Authenticates with the desired login provider. Also caches the token.
     *
     * If a local token cache is detected, the token cache is used instead of an actual
     * login unless bRefresh is set to true forcing a refresh.
     *
     * @param bRefreshCache
     *            Indicates whether to force a token refresh.
     */
    private void authenticate(boolean bRefreshCache) {

        bAuthenticating = true;

        if (bRefreshCache || !loadUserTokenCache(mClient))
        {
            // New login using the provider and update the token cache.
            mClient.login(MobileServiceAuthenticationProvider.Google,
                    new UserAuthenticationCallback() {
                        @Override
                        public void onCompleted(MobileServiceUser user,
                                                Exception exception, ServiceFilterResponse response) {

                            synchronized (mAuthenticationLock) {
                                if (exception == null) {
                                    cacheUserToken(mClient.getCurrentUser());
                                    createTable();
                                } else {
                                    createAndShowDialog(exception.getMessage(), "Login Error");
                                }
                                bAuthenticating = false;
                                mAuthenticationLock.notifyAll();
                            }
                        }
                    });
        }
        else
        {
            // Other threads may be blocked waiting to be notified when
            // authentication is complete.
            synchronized(mAuthenticationLock)
            {
                bAuthenticating = false;
                mAuthenticationLock.notifyAll();
            }
            createTable();
        }
    }

    private void createAndShowDialog(String format, String success) {
        //create dialog
    }

    private void createAndShowDialog(Exception e, String success) {
        //create dialog
    }

    private void createTable() {
        // Get the Mobile Service Table instance to use
        mPostTable = mClient.getTable(Post.class);
        refreshItemsFromTable();
    }

    private void refreshItemsFromTable() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final MobileServiceList<Post> result = mPostTable.where().execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mPosts = result;
                        }
                    });
                } catch (Exception exception) {
                    createAndShowDialog(exception, "Error");
                }
                return null;
            }
        }.execute();
    }

    public String getItem(int position) {
        //mClient.getTable(Post.class).lookUp(id, callback);
        if (mPosts.size() > position)
            return mPosts.get(position).getText();
        return "";
    }

    @Override
    public void onPost(String text) {
        Post post = new Post(mUserId, text);
        mClient.getTable(Post.class).insert(post, new TableOperationCallback<Post>() {
            public void onCompleted(Post entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    //TODO: Fai qualcosa qua
                    System.out.println();
                } else {
                    System.out.println();
                }
            }
        });
    }

    /**
     * The RefreshTokenCacheFilter class filters responses for HTTP status code 401.
     * When 401 is encountered, the filter calls the authenticate method on the
     * UI thread. Out going requests and retries are blocked during authentication.
     * Once authentication is complete, the token cache is updated and
     * any blocked request will receive the X-ZUMO-AUTH header added or updated to
     * that request.
     */
    private class RefreshTokenCacheFilter implements ServiceFilter {

        AtomicBoolean mAtomicAuthenticatingFlag = new AtomicBoolean();

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(
                final ServiceFilterRequest request,
                final NextServiceFilterCallback nextServiceFilterCallback
        )
        {
            // In this example, if authentication is already in progress we block the request
            // until authentication is complete to avoid unnecessary authentications as
            // a result of HTTP status code 401.
            // If authentication was detected, add the token to the request.
            waitAndUpdateRequestToken(request);

            // Send the request down the filter chain
            // retrying up to 5 times on 401 response codes.
            ListenableFuture<ServiceFilterResponse> future = null;
            ServiceFilterResponse response = null;
            int responseCode = 401;
            for (int i = 0; (i < 5 ) && (responseCode == 401); i++)
            {
                future = nextServiceFilterCallback.onNext(request);
                try {
                    response = future.get();
                    responseCode = response.getStatus().getStatusCode();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    if (e.getCause().getClass() == MobileServiceException.class)
                    {
                        MobileServiceException mEx = (MobileServiceException) e.getCause();
                        responseCode = mEx.getResponse().getStatus().getStatusCode();
                        if (responseCode == 401)
                        {
                            // Two simultaneous requests from independent threads could get HTTP status 401.
                            // Protecting against that right here so multiple authentication requests are
                            // not setup to run on the UI thread.
                            // We only want to authenticate once. Requests should just wait and retry
                            // with the new token.
                            if (mAtomicAuthenticatingFlag.compareAndSet(false, true))
                            {
                                // Authenticate on UI thread
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // Force a token refresh during authentication.
                                        authenticate(true);
                                    }
                                });
                            }

                            // Wait for authentication to complete then update the token in the request.
                            waitAndUpdateRequestToken(request);
                            mAtomicAuthenticatingFlag.set(false);
                        }
                    }
                }
            }
            return future;
        }
    }

}