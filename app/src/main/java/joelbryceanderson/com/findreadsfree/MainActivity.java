package joelbryceanderson.com.findreadsfree;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    TextView mDescription;
    TextView mPageTitle;

    BottomNavigationView mBottomNav;

    ProgressBar mProgressBar;

    LinearLayout mContainer;

    String mReferralLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPageTitle = (TextView) findViewById(R.id.page_title);
        mDescription = (TextView) findViewById(R.id.book_description);
        mDescription.setMovementMethod(new ScrollingMovementMethod());

        mBottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        mBottomNav.setOnNavigationItemSelectedListener(onBottomNavSelected());

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mContainer = (LinearLayout) findViewById(R.id.main_container);

        loadPages();
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
        if (pages != null) {
            mFree = pages.get(0);
            mDiscounted = pages.get(1);
            mAudioBook = pages.get(2);

            showPage(mFree, getString(R.string.section_title_free));

        }
    }

    public void openReferralLink(View v) {
        if (mReferralLink != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mReferralLink));
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
            mDescription.setText(page.getMainText());
            mReferralLink = page.getRedirectionUrl();
            mPageTitle.setText(title);
            
            showViews();
        }
    }
}
