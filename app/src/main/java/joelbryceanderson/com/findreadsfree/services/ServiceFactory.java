package joelbryceanderson.com.findreadsfree.services;

import retrofit2.Retrofit;

/**
 * Created by JAnderson on 2/8/17.
 */

public class ServiceFactory {

    private ServiceFactory(){ }

    public static <T> T createRetrofitService(final Class<T> clazz, final String endPoint) {
        final Retrofit restAdapter = new Retrofit.Builder()
                .baseUrl(endPoint)
                .build();

        return restAdapter.create(clazz);
    }
}
