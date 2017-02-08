package joelbryceanderson.com.findreadsfree.services;


import java.util.List;

import io.reactivex.Observable;
import joelbryceanderson.com.findreadsfree.models.Page;
import retrofit2.http.GET;

/**
 * Created by JAnderson on 2/8/17.
 */

public interface BackendService {

    String SERVICE_ENDPOINT = "http://s3.amazonaws.com";

    @GET("/feed1/t5.js")
    Observable<List<Page>> getPages();
}
