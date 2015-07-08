package it.ialweb.poi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutionException;

import com.microsoft.windowsazure.mobileservices.MobileServiceException;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private MobileServiceClient mClient;
    private MobileServiceTable<Post> mPostTable;

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";

    public boolean bAuthenticating = false;
    public final Object mAuthenticationLock = new Object();

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
                Snackbar.make(findViewById(R.id.coordinator), "abcdefg", Snackbar.LENGTH_LONG).show();
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

    private void mockData() {
        Post post = new Post(2, "Hello!");
        mClient.getTable(Post.class).insert(post, new TableOperationCallback<Post>() {
            public void onCompleted(Post entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    System.out.println();
                } else {
                    System.out.println();
                }
            }
        });
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
        // Load the items from the Mobile Service
        refreshItemsFromTable();
    }

    private void refreshItemsFromTable() {

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

    public class PlaceHolder extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            createTable();

            RecyclerView recyclerView = new RecyclerView(getActivity());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                @Override
                public int getItemCount() {
                    return 30;
                }

                @Override
                public void onBindViewHolder(ViewHolder holder, int position) {
                    final ViewHolder fHolder = holder;
                    mClient.getTable(Post.class).lookUp(position, new TableOperationCallback<Post>() {
                        @Override
                        public void onCompleted(Post entity, Exception exception, ServiceFilterResponse response) {
                            if (entity != null)
                                ((TextView) fHolder.itemView).setText(entity.toString());
                        }
                    });

                }

                @Override
                public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
                    LayoutInflater layoutInflater = getActivity().getLayoutInflater();
                    View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
                    return new ViewHolder(view) {
                    };
                }
            });

            return recyclerView;
        }
    }
}