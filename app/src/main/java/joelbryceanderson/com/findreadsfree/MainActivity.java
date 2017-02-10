package joelbryceanderson.com.findreadsfree;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import joelbryceanderson.com.findreadsfree.models.Page;
import joelbryceanderson.com.findreadsfree.services.BackendService;
import joelbryceanderson.com.findreadsfree.services.ServiceFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.toString();

    BackendService mBackendService;

    List<Page> mPages;

    int selectedPage = 0;

    TextSwitcher mDescription;
    TextSwitcher mPageTitle;
    BottomNavigationView mBottomNav;
    ImageView mBookCover;
    LinearLayout mContainer;
    AppCompatButton mButton;
    NestedScrollView mScrollView;
    SwipeRefreshLayout mSwipeRefresh;

    String mPurchaseLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageTitle = (TextSwitcher) findViewById(R.id.page_title);
        mDescription = (TextSwitcher) findViewById(R.id.book_description);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBookCover = (ImageView) findViewById(R.id.book_cover);
        mContainer = (LinearLayout) findViewById(R.id.main_container);
        mScrollView = (NestedScrollView) findViewById(R.id.description_scroller);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mButton = (AppCompatButton) findViewById(R.id.purchase_button);

        mBottomNav.setOnNavigationItemSelectedListener(onBottomNavSelected());

        mBackendService = ServiceFactory.createRetrofitService(
                BackendService.class, BackendService.SERVICE_ENDPOINT);

        initSwipeRefresh();
        initTextSwitchers();
        loadPages();
    }

    /**
     * Initialize reload on swipe down, colors for swipe view
     */
    private void initSwipeRefresh() {
        mSwipeRefresh.setOnRefreshListener(this::loadPages);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefresh.setRefreshing(true);
    }

    /**
     * Initialize animations for text views
     */
    private void initTextSwitchers() {
        mDescription.setFactory(() -> new TextView(this));
        mPageTitle.setFactory(() -> {
            TextView textView = new TextView(MainActivity.this);
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(Color.WHITE);
            return textView;
        });

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);

        mDescription.setInAnimation(in);
        mDescription.setOutAnimation(out);
        mPageTitle.setInAnimation(in);
        mPageTitle.setOutAnimation(out);
    }

    /**
     * Handle choosing of bottom nav menu items
     *
     * @return always true to select item
     */
    private BottomNavigationView.OnNavigationItemSelectedListener onBottomNavSelected() {
        return item -> {
            switch (item.getItemId()) {
                case R.id.action_free:
                    selectPage(0);
                    break;
                case R.id.action_promotion:
                    selectPage(1);
                    break;
                case R.id.action_audio_book:
                    selectPage(2);
                    break;
                case R.id.action_serial:
                    selectPage(3);
                    break;
            }
            return true;
        };
    }

    /**
     * Switches views to display data from selected page
     *
     * @param page the index of the page to select
     */
    private void selectPage(int page) {
        if (mPages.get(page) != null) {
            selectedPage = page;
            showPage(mPages.get(page));
        }
    }

    /**
     * Fetches data from the backend, displays on completion
     */
    private void loadPages() {
        mBackendService.getPages()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showData, throwable -> {
                    Log.e(TAG, throwable.getLocalizedMessage());
                    showErrorToast();
                });
    }

    /**
     * Displays data once load has finished
     *
     * @param pages list of all pages to display
     */
    private void showData(List<Page> pages) {
        if (pages != null && pages.size() >= 3) {
            mPages = pages;
            showPage(mPages.get(selectedPage));
            showViews();
        }
    }

    /**
     * Open currently selected link in device's browser
     *
     * @param v view parameter to pass as onClick listener
     */
    public void openPurchaseLink(View v) {
        if (mPurchaseLink != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPurchaseLink));
            startActivity(intent);
        }
    }

    /**
     * Stop refreshing, show main container and nav bar
     */
    private void showViews() {
        mSwipeRefresh.setRefreshing(false);
        mContainer.setVisibility(View.VISIBLE);
        mBottomNav.setVisibility(View.VISIBLE);
    }

    /**
     * Fills the views with data from the selected page
     *
     * @param page page to display in views
     */
    private void showPage(Page page) {
        if (page != null) {
            mScrollView.smoothScrollTo(0,0);
            mDescription.setText(page.getMainText());
            mPurchaseLink = page.getPurchaseUrl();
            mPageTitle.setText(page.getAppName());
            setButtonText();
            loadCover(page.getImageUrl());
        }
    }

    /**
     * Changes the currently displayed text on header button to correspond with tab
     */
    private void setButtonText() {
        if (selectedPage == 3) {
            mButton.setText(getString(R.string.read_more));
        } else {
            mButton.setText(getString(R.string.purchase));
        }
    }

    /**
     * Uses glide to load book cover from url into image view
     *
     * @param url from which to load cover image
     */
    private void loadCover(String url) {
        Glide.with(MainActivity.this)
                .load(url)
                .crossFade()
                .override(1000,1000)
                .fitCenter()
                .into(mBookCover);
    }

    /**
     * Stops refreshing and displays error message to user.
     */
    private void showErrorToast() {
        mSwipeRefresh.setRefreshing(false);
        Toast.makeText(MainActivity.this, R.string.error_text, Toast.LENGTH_LONG).show();
    }
}
