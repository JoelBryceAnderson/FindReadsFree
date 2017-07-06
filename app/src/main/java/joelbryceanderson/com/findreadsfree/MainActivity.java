package joelbryceanderson.com.findreadsfree;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
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

    private static final String ACTION_FREE = "joelbryceanderson.com.findreadsfree.FREE";
    private static final String ACTION_PROMOTION = "joelbryceanderson.com.findreadsfree.PROMOTION";
    private static final String ACTION_AUDIOBOOK = "joelbryceanderson.com.findreadsfree.AUDIOBOOK";
    private static final String ACTION_SERIAL = "joelbryceanderson.com.findreadsfree.SERIAL";

    BackendService mBackendService;

    List<Page> mPages;

    int mSelectedPage = 0;

    TextSwitcher mDescription;
    TextSwitcher mPageTitle;
    BottomNavigationView mBottomNav;
    ImageView mBookCover;
    LinearLayout mContainer;
    AppCompatButton mPurchaseButton;
    AppCompatButton mReferralButton;
    NestedScrollView mScrollView;
    SwipeRefreshLayout mSwipeRefresh;

    String mPurchaseLink;
    String mReferralLink;

    @IdRes
    int[] mMenuIDs = new int[]{
            R.id.action_free,
            R.id.action_promotion,
            R.id.action_audio_book,
            R.id.action_serial
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageTitle = findViewById(R.id.page_title);
        mDescription = findViewById(R.id.book_description);
        mBottomNav = findViewById(R.id.bottom_navigation);
        mBookCover = findViewById(R.id.book_cover);
        mContainer = findViewById(R.id.main_container);
        mScrollView = findViewById(R.id.description_scroller);
        mSwipeRefresh = findViewById(R.id.swipe_refresh_layout);
        mPurchaseButton = findViewById(R.id.purchase_button);
        mReferralButton = findViewById(R.id.referral_button);

        mBottomNav.setOnNavigationItemSelectedListener(onBottomNavSelected());
        mPurchaseButton.setOnClickListener(onPurchaseButtonClicked());
        mReferralButton.setOnClickListener(onReferralButtonClicked());

        mBackendService = ServiceFactory.createRetrofitService(
                BackendService.class, BackendService.SERVICE_ENDPOINT);

        handleShortcuts();
        initSwipeRefresh();
        initTextSwitchers();
        loadPages();
    }

    /**
     * Handle Android 7.1 App shortcuts after pages have loaded
     */
    private void handleShortcuts() {
        switch (getIntent().getAction()) {
            case ACTION_PROMOTION:
                mSelectedPage = 1;
                break;
            case ACTION_AUDIOBOOK:
                mSelectedPage = 2;
                break;
            case ACTION_SERIAL:
                mSelectedPage = 3;
                break;
            default:
                mSelectedPage = 0;
                break;
        }
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
        if (mSelectedPage == page) {
            mScrollView.smoothScrollTo(0,0);
        } else if (page < mPages.size() && mPages.get(page) != null) {
            mSelectedPage = page;
            showPage(mPages.get(page));
        } else {
            showErrorToast();
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
            showPage(mPages.get(mSelectedPage));
            selectMenuItem();
            showViews();
        }
    }

    /**
     * On click listener to open links.
     * Checks if link is null before attempting to open.
     *
     * @return On click listener to assign to link button
     */
    public View.OnClickListener onPurchaseButtonClicked() {
        return view -> {
            if (mPurchaseLink != null) {
                openLink(mPurchaseLink);
            }
        };
    }

    /**
     * On click listener to open links.
     * Checks if link is null before attempting to open.
     *
     * @return On click listener to assign to link button
     */
    public View.OnClickListener onReferralButtonClicked() {
        return view -> {
            if (mReferralLink != null) {
                openLink(mReferralLink);
            }
        };
    }


    /**
     * Opens link in browser, safely checking if link is malformed
     *
     * @param link to open in browser
     */
    private void openLink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, e.getLocalizedMessage());
            Toast.makeText(MainActivity.this, R.string.error_text, Toast.LENGTH_LONG).show();
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
            mPageTitle.setText(page.getAppName());
            mDescription.setText(page.getMainText());

            mPurchaseLink = page.getPurchaseUrl();

            mScrollView.scrollTo(0,0);
            setPurchaseButtonText();
            updateReferralButton(page);
            loadCover(page.getImageUrl());
        }
    }

    private void updateReferralButton(Page page) {
        if (page.getBountytext() != null && page.getBountylink() != null) {
            mReferralButton.setVisibility(View.VISIBLE);
            mReferralButton.setText(page.getBountytext());
            mReferralLink = page.getBountylink();
        } else {
            mReferralButton.setVisibility(View.GONE);
        }
    }

    /**
     * Changes the currently displayed text on header button to correspond with tab
     */
    private void setPurchaseButtonText() {
        if (mSelectedPage == 3) {
            mPurchaseButton.setText(getString(R.string.read_more));
        } else {
            mPurchaseButton.setText(getString(R.string.purchase));
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

    private void selectMenuItem() {
        mBottomNav.setOnNavigationItemSelectedListener(null);
        mBottomNav.setSelectedItemId(mMenuIDs[mSelectedPage]);
        mBottomNav.setOnNavigationItemSelectedListener(onBottomNavSelected());
    }

    /**
     * Stops refreshing and displays error message to user.
     */
    private void showErrorToast() {
        mSwipeRefresh.setRefreshing(false);
        Toast.makeText(MainActivity.this, R.string.error_loading_text, Toast.LENGTH_LONG).show();
    }
}
