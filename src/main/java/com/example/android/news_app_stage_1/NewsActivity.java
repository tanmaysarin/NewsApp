    package com.example.android.news_app_stage_1;

    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;

    import android.content.Context;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.net.ConnectivityManager;
    import android.net.NetworkInfo;
    import android.net.Uri;
    import android.app.LoaderManager;
    import android.content.Loader;
    import android.preference.PreferenceManager;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.AdapterView;
    import android.widget.ListView;
    import android.widget.ProgressBar;
    import android.widget.TextView;

    import java.util.ArrayList;
    import java.util.List;




    public class NewsActivity extends AppCompatActivity
            implements LoaderManager.LoaderCallbacks<List<News>> {

        private static final String LOG_TAG = NewsActivity.class.getName();

        // constant values for complete URL and appropriate keys
        private final static String REQUEST_URL = BuildConfig.GUARDIAN_REQUEST_URL;
        private final static String PAGE_SIZE = BuildConfig.PAGE_SIZE;
        private final static String TAGS = BuildConfig.TAGS;
        private final static String AUTHOR = BuildConfig.AUTHOR;
        private final static String SECTION = BuildConfig.SECTION;
        private final static String QUERY = BuildConfig.QUERY;
        private final static String ORDER_BY = BuildConfig.ORDER_BY;
        private final static String API_KEY = BuildConfig.API_KEY;
        private final static String MY_API_KEY = BuildConfig.THE_GUARDIAN_API_KEY;

        private static final int NEWS_LOADER_ID = 1;

        private NewsAdapter mAdapter;

        /** TextView that is displayed when the list is empty */
        private TextView mEmptyStateTextView;

        @Override
        protected void onCreate(Bundle savedInstanceState)  {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.news_activity);

            // Find a reference to the {@link ListView} in the layout
            ListView newsListView = (ListView) findViewById(R.id.list);

            mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
            newsListView.setEmptyView(mEmptyStateTextView);

            mAdapter = new NewsAdapter(this, new ArrayList<News>());

            // Set the adapter on the {@link ListView}
            // so the list can be populated in the user interface
            newsListView.setAdapter(mAdapter);

            newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    // Find the current earthquake that was clicked on
                    News currentNewsArticle = mAdapter.getItem(position);

                    // Convert the String URL into a URI object (to pass into the Intent constructor)
                    Uri newsUri = Uri.parse(currentNewsArticle.getUrl());

                    // Create a new intent to view the earthquake URI
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                    // Send the intent to launch a new activity
                    startActivity(websiteIntent);
                }
            });

            // Get a reference to the ConnectivityManager to check state of network connectivity
            ConnectivityManager connMgr = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);

            // Get details on the currently active default data network
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // If there is a network connection, fetch data
            if (networkInfo != null && networkInfo.isConnected()) {
                // Get a reference to the LoaderManager, in order to interact with loaders.
                LoaderManager loaderManager = getLoaderManager();

                // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                // because this activity implements the LoaderCallbacks interface).
                loaderManager.initLoader(NEWS_LOADER_ID, null, this);
            } else {
                // Otherwise, display error
                // First, hide loading indicator so error message will be visible
                View loadingIndicator = findViewById(R.id.loading_indicator);
                loadingIndicator.setVisibility(View.GONE);

                // Update empty state with no connection error message
                mEmptyStateTextView.setText(R.string.no_internet_connection);
            }
        }

        @Override
        public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
            String minArticles = sharedPrefs.getString(
                    getString(R.string.settings_min_articles_key),
                    getString(R.string.settings_min_articles_default));

            String section = sharedPrefs.getString(
                    getString(R.string.settings_section_key),
                    getString(R.string.settings_section_default));

            String searchContent = sharedPrefs.getString(
                    getString(R.string.settings_search_content_TF),
                    getString(R.string.settings_search_content_default));

            String orderBy = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default));

            // parse breaks apart the URI string that's passed into its parameter
            Uri baseUri = Uri.parse(REQUEST_URL);

            // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
            Uri.Builder uriBuilder = baseUri.buildUpon();

            // Append query parameter and its value. For example, the `page-size=10`
            uriBuilder.appendQueryParameter(PAGE_SIZE, minArticles);
            uriBuilder.appendQueryParameter(TAGS, AUTHOR);
            uriBuilder.appendQueryParameter(SECTION, section);
            uriBuilder.appendQueryParameter(QUERY, searchContent);
            uriBuilder.appendQueryParameter(ORDER_BY, orderBy);
            uriBuilder.appendQueryParameter(API_KEY, MY_API_KEY);

            // Return the completed uri
            return new NewsLoader (this, uriBuilder.toString());
        }

        @Override
        public void onLoadFinished(Loader<List<News>> loader, List<News> NewsItems) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // Set empty state text to display "No NewsItems found."
            mEmptyStateTextView.setText(R.string.no_news_articles);

            mAdapter.clear();

            if (NewsItems != null && !NewsItems.isEmpty()) {
                mAdapter.addAll(NewsItems);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<News>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }

        @Override
        // This method initialize the contents of the Activity's options menu
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater ().inflate ( R.menu.main, menu );
            return true;
        }

        @Override
        // This method is called whenever an item in the options menu is selected.
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId ();
            if (id == R.id.action_settings) {
                Intent settingsIntent = new Intent ( this, SettingsActivity.class );
                startActivity ( settingsIntent );
                return true;
            }
            return super.onOptionsItemSelected ( item );
        }
    }