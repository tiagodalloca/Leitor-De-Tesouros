package masterplan.leitordetesouros; /**
 * Created by u15194 on 07/06/2017.
 */

import com.loopj.android.http.*;

public class SergioRestClient {
    private static final String BASE_URL = "http://slmm.com.br/cotuca/restCotuca17.php";

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(RequestParams params, AsyncHttpResponseHandler responseHandler) {
        post("", params, responseHandler);
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}