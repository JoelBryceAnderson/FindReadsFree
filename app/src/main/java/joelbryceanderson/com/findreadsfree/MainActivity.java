package joelbryceanderson.com.findreadsfree;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextSwitcher;
import android.widget.TextView;

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

    Page mFree;
    Page mDiscounted;
    Page mAudioBook;

    TextSwitcher mDescription;
    TextSwitcher mPageTitle;
    BottomNavigationView mBottomNav;
    ProgressBar mProgressBar;
    ImageView mBookCover;
    LinearLayout mContainer;
    ScrollView mScrollView;

    String mPurchaseLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageTitle = (TextSwitcher) findViewById(R.id.page_title);
        mDescription = (TextSwitcher) findViewById(R.id.book_description);
        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mBookCover = (ImageView) findViewById(R.id.book_cover);
        mContainer = (LinearLayout) findViewById(R.id.main_container);
        mScrollView = (ScrollView) findViewById(R.id.description_scroller);

        mBottomNav.setOnNavigationItemSelectedListener(onBottomNavSelected());

        initTextSwitchers();
        loadPages();
    }

    private void initTextSwitchers() {
        mDescription.setFactory(() -> new TextView(this));
        mPageTitle.setFactory(() -> {
            TextView textView = new TextView(MainActivity.this);
            textView.setTextAppearance(MainActivity.this, android.R.style.TextAppearance);
            textView.setTextSize(20);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(Color.WHITE);
            return textView;
        });

        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);

        mDescription.setInAnimation(in);
        mDescription.setOutAnimation(out);
        mPageTitle.setInAnimation(in);
        mPageTitle.setOutAnimation(out);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onBottomNavSelected() {
        return item -> {
            switch (item.getItemId()) {
                case R.id.action_free:
                    showPage(mFree, getString(R.string.section_title_free));
                    break;
                case R.id.action_promotion:
                    showPage(mDiscounted, getString(R.string.section_title_discounted));
                    break;
                case R.id.action_audio_book:
                    showPage(mAudioBook, getString(R.string.section_title_audiobook));
                    break;
            }

            return true;
        };
    }

    private void loadPages() {
        mBackendService = ServiceFactory.createRetrofitService(
                BackendService.class, BackendService.SERVICE_ENDPOINT);

        mBackendService.getPages()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showData, throwable -> {
                    Log.e(TAG, throwable.getLocalizedMessage());
                });
    }

    private void showData(List<Page> pages) {
        if (pages != null && pages.size() >= 3) {
            mFree = pages.get(0);
            mDiscounted = pages.get(1);
            mAudioBook = pages.get(2);

            showPage(mFree, getString(R.string.section_title_free));

        }
    }

    public void openPurchaseLink(View v) {
        if (mPurchaseLink != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPurchaseLink));
            startActivity(intent);
        }
    }

    private void hideViews() {
        mContainer.setVisibility(View.GONE);
        mBottomNav.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void showViews() {
        mContainer.setVisibility(View.VISIBLE);
        mBottomNav.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
    }

    private void showPage(Page page, String title) {
        if (page != null) {
            mScrollView.smoothScrollTo(0,0);
            mDescription.setText(page.getMainText().replace(getString(R.string.alexa_string), "")); //check for "alexa string" in description, remove it if it exists
            mPurchaseLink = page.getPurchaseUrl();
            mPageTitle.setText(title);
            loadCover(page.getImageUrl());

            showViews();
        }
    }

    private void loadCover(String url) {
        Glide.with(MainActivity.this)
                .load(url)
                .crossFade(500)
                .override(1000,1000)
                .fitCenter()
                .into(mBookCover);
    }
}
