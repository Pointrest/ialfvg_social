package it.ialweb.poi;

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
import android.widget.TextView;
import com.microsoft.windowsazure.mobileservices.*;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import java.net.MalformedURLException;

import it.ialweb.poi.it.ialweb.poi.models.Post;

public class MainActivity extends AppCompatActivity {

	private TabLayout tabLayout;
	private ViewPager viewPager;
	private MobileServiceClient mClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setupAMC();

		mockData();

		setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

		tabLayout = (TabLayout) findViewById(R.id.tabLayout);
		viewPager = (ViewPager) findViewById(R.id.pager);

		FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

			private int[] titles = new int[] { R.string.Timeline, R.string.Users, R.string.MyProfile };

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

	private void setupAMC() {
		try {
			mClient = new MobileServiceClient(
                    "https://baassi.azure-mobile.net/",
                    "QsyouuxvWGyryvCCVrCIvEvJCvgNQh42",
                    this
            );
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private void mockData() {
		Post post = new Post("1", "Hello!");
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

	public class PlaceHolder extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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