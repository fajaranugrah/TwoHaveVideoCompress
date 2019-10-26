package com.example.twohavevideocompress.LibraryCompressVideo;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;

import com.iceteck.silicompressorr.SiliCompressor;

import java.net.URISyntaxException;

public class VideoCompressSilliCompressor2 extends AsyncTask<String, String, String> {

    Context mContext;
    AsyncResponse asyncResponse = null;
    MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

    public VideoCompressSilliCompressor2(Context context, AsyncResponse asyncResponse) {
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
            //rotation
            mediaMetadataRetriever.setDataSource(paths[0]);
            int width = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int rotation = Integer.valueOf(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));

            //default or original value
            int valueWidth;
            int valueHeight;
            int valueBit = 450000;

            //width
            if (rotation == 0) {
                valueWidth = width;
            } else {
                valueWidth = 0;
            }

            //height
            if (rotation == 0){
                valueHeight = height;
            } else {
                valueHeight = 0;
            }

            filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1],
                    valueWidth,
                    valueHeight,
                    valueBit);
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