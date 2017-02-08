package joelbryceanderson.com.findreadsfree;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import joelbryceanderson.com.findreadsfree.services.BackendService;
import joelbryceanderson.com.findreadsfree.services.ServiceFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static String TAG = MainActivity.class.toString();

    BackendService mBackendService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = (TextView) findViewById(R.id.book_description);
        tv.setMovementMethod(new ScrollingMovementMethod());

        loadPages();
    }

    private void loadPages() {
        mBackendService = ServiceFactory.createRetrofitService(
                BackendService.class, BackendService.SERVICE_ENDPOINT);

        mBackendService.getPages()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pagesList -> {
                    Log.e("Hey!", pagesList.get(0).getMainText());
                }, throwable -> {
                    Log.e(TAG, throwable.getLocalizedMessage());
                });
    }
}
