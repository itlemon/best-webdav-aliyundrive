package cn.itlemon.best.web.dav.aliyun;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import cn.hutool.json.JSONUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author itlemon <lemon_jiang@aliyun.com>
 * Created on 2022-03-30
 */
public class MainTest {

    @Test
    public void testFileList() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, "{\"drive_id\":\"1917514\","
                + "\"parent_file_id\":\"623751c63e281df2cbc845be8025306a9f592798\",\"limit\":100,\"all\":false,"
                + "\"url_expire_sec\":1600,\"image_thumbnail_process\":\"image/resize,w_400/format,jpeg\","
                + "\"image_url_process\":\"image/resize,w_1920/format,jpeg\","
                + "\"video_thumbnail_process\":\"video/snapshot,t_1000,f_jpg,ar_auto,w_300\",\"fields\":\"*\","
                + "\"order_by\":\"updated_at\",\"order_direction\":\"DESC\"}");
        Request request = new Request.Builder()
                .url("https://api.aliyundrive.com/adrive/v3/file/list?jsonmask=next_marker%2Citems(name%2Cfile_id%2Cdrive_id%2Ctype%2Csize%2Ccreated_at%2Cupdated_at%2Ccategory%2Cfile_extension%2Cparent_file_id%2Cmime_type%2Cstarred%2Cthumbnail%2Curl%2Cstreams_info%2Ccontent_hash%2Cuser_tags%2Cuser_meta%2Ctrashed%2Cvideo_media_metadata%2Cvideo_preview_metadata%2Csync_meta%2Csync_device_flag%2Csync_flag)")
                .method("POST", body)
                .addHeader("Accept", "application/json, text/plain, */*")
                .addHeader("Authorization", "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJjZmY3ZmJjYTI1ZDA0MWRmYTNhYzNmMjIwOTYzN2IxMyIsImN1c3RvbUpzb24iOiJ7XCJjbGllbnRJZFwiOlwiMjVkelgzdmJZcWt0Vnh5WFwiLFwiZG9tYWluSWRcIjpcImJqMjlcIixcInNjb3BlXCI6W1wiRFJJVkUuQUxMXCIsXCJTSEFSRS5BTExcIixcIkZJTEUuQUxMXCIsXCJVU0VSLkFMTFwiLFwiVklFVy5BTExcIixcIlNUT1JBR0UuQUxMXCIsXCJTVE9SQUdFRklMRS5MSVNUXCIsXCJCQVRDSFwiLFwiT0FVVEguQUxMXCIsXCJJTUFHRS5BTExcIixcIklOVklURS5BTExcIixcIkFDQ09VTlQuQUxMXCJdLFwicm9sZVwiOlwidXNlclwiLFwicmVmXCI6XCJodHRwczovL3d3dy5hbGl5dW5kcml2ZS5jb20vXCIsXCJkZXZpY2VfaWRcIjpcImU0MGIyNGY3YTEyZjQwZjFiN2VlYzVlZTgzYjVmMGNlXCJ9IiwiZXhwIjoxNjQ4NjM0NjcxLCJpYXQiOjE2NDg2Mjc0MTF9.UxdRgialJO6N3m2uwEW6EUffgOB2FR1124p9tBZvYQAxNqh2aAtcWdqKBJQnPmHa8JznY4kcTH2VSvOeJt2SzQUP_Sy1deNAZNIHwp7JIKhb1I8OdUgIeD3KwnNcbrstxisDSIZWB2fMQ_RrN5VxuWZEqk9b9bdq-lWdU1vebac")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/99.0.4844.83 Safari/537.36")
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(JSONUtil.parseObj(new String(response.body().bytes())));
    }

    public void testFileUpload() {

    }

}
