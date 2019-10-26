package com.example.twohavevideocompress.LibraryCompressVideo;

import android.content.Context;
import android.os.AsyncTask;

import com.iceteck.silicompressorr.SiliCompressor;

import java.net.URISyntaxException;

public class VideoCompressSilliCompressor extends AsyncTask<String, String, String> {

    Context mContext;
    AsyncResponse asyncResponse = null;

    public VideoCompressSilliCompressor(Context context, AsyncResponse asyncResponse) {
        mContext = context;
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... paths) {
        String filePath = null;
        try {
            filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1]);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return filePath;

    }

    @Override
    protected void onPostExecute(String compressedFilePath) {
        super.onPostExecute(compressedFilePath);
        asyncResponse.processFinish(compressedFilePath);
    }
}