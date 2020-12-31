package com.eme22.serverproxy;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadTask implements Runnable, BufferFile {

    private static final int BUFFER_SIZE = 1024;
    //private boolean publishContentOn;
    // Connectivity variables
    private InputStream is;
    private BufferedInputStream bis;
    private File file;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private final String URL;
    private final String[] HEADERSH;
    private final String[] HEADERSD;
    private final Context context;
    private final ProgressUpdate progressUpdate;

    public DownloadTask(String URL, String[] HEADERSH,String[] HEADERSD, Context context, ProgressUpdate progressUpdate) {
        this.URL = URL;
        this.HEADERSH = HEADERSH;
        this.HEADERSD = HEADERSD;
        this.context = context;
        this.progressUpdate = progressUpdate;
    }

    private boolean establishConnection(String url) {
        try {
            Headers.Builder headers = new Headers.Builder();
            for (int i = 0; i < HEADERSH.length ; i++) {
                headers.add(HEADERSH[i], HEADERSD[i]);
            }
            OkHttpClient client = new OkHttpClient.Builder().build();
            file = new File(context.getExternalCacheDir(),"CACHE.mp4");
            Request request = new Request.Builder().url(url).headers(headers.build()).build();
            Response response = client.newCall(request).execute();

                //Log.d("DOWNLOAD:", "ResumeWHeaders: "
                //        + this.resumeWHeaders);
                //if (this.resumeWHeaders) {
                //    connection.setRequestProperty("Range",
                //            "bytes=" + (file.length()) + "-");
               // } else
            if (file.exists()) {
                    // First time establishing connection with yt service
                    file.delete();
                    file.createNewFile();
            }
                /* Define InputStreams to read from the URLConnection. */
            if (response.body() != null) {
                is = response.body().byteStream();
            }
            else return false;
            bis = new BufferedInputStream(is);

                // Either we have created a new file or we have an existing
                // append to the file output buffer...
                fos = new FileOutputStream(file, true);
                bos = new BufferedOutputStream(fos);

                Log.d("DOWNLOAD:", "EST_CONN: SUCCESS");
                return true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("DOWNLOAD:", "EST_CONN: EXCEPTION");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void closeConnection() {
        try {
            if (bis != null)
                bis.close();
            if (bos != null)
                bos.close();
            if (fos != null)
                fos.close();
            if (is != null)
                is.close();
            bis = null;
            bos = null;
            fos = null;
            is = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void preInit(){
        Log.d("DOWNLOAD:", "AsyncTask: Task started.");
        this.is = null;
        this.bis = null;
        this.file = null;
        this.fos = null;
        this.bos = null;
    }

    private void downloadProcess() {
        try {
            if (this.establishConnection(URL)) {
                byte[] data = new byte[BUFFER_SIZE];
                int j;
                // While loop streams until there is nothing left in the socket
                progressUpdate.update();
                while ((j = bis.read(data)) != -1) {
                        // We will write to the file if we have closed connection
                        // for energy issues or if the execution of this task
                        // does not taking into account 'energy awareness issues'
                        fos.write(data, 0, j);

                }
                Log.d("DOWNLOAD:", "DOWN_PROCESS: SUCCESS");
            }
            else {
                Log.d("DOWNLOAD:",
                        "Not acquiring mp4 file! Problem with the connection...");
            }
        } catch (IOException e) {
            this.closeConnection();
        } catch (Exception e) {
            Log.d("DOWNLOAD:", "" + e.getMessage());
        }
    }

    void postInit(){
        Log.d("DOWNLOAD:", "Task finished.");
        this.closeConnection();
    }



    @Override
    public void run() {
        preInit();
        downloadProcess();
        postInit();
    }

    @Override
    public File getFile() {
        return null;
    }

    @Override
    public Long getContentLength() {
        return null;
    }

    @Override
    public long getEstimatedSize() {
        return 0;
    }

    @Override
    public boolean isWorkDone() {
        return false;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onResume() {

    }


    public interface ProgressUpdate{
        void update();
    }

}
