package nazar.cybulskij.testdirection.network;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by nazar on 15.10.15.
 */


public interface DirectionService {



    @GET("/json")
    void getDirection(@Query("origin") String from,
                      @Query("destination") String to,
                      @Query("mode") String mode,
                      @Query("key") String key,
                      @Query("sensor") Boolean sensor,
                      @Query("language") String language,
             Callback<Response> callback);



    @GET("/json")
    void getMatrixDirection(@Query("origins") String from,
                      @Query("destinations") String to,
                      @Query("mode") String mode,
                      @Query("key") String key,
                      @Query("sensor") Boolean sensor,
                      @Query("language") String language,
                      Callback<Response> callback);




}
