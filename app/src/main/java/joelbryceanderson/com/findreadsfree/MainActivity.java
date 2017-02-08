package joelbryceanderson.com.findreadsfree;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import joelbryceanderson.com.findreadsfree.services.BackendService;
import joelbryceanderson.com.findreadsfree.services.ServiceFactory;

public class MainActivity extends AppCompatActivity {

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
                });
    }
}
