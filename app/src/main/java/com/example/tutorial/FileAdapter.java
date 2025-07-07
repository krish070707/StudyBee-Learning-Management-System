package com.example.tutorial;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Map;

//handles a list of strings
public class FileAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final ArrayList<String> taskList;//gets the caption
    private final Map<String, String> urlMap;//maps task name to file url

    //parameterized constructor
    public FileAdapter(Context context, ArrayList<String> taskList, Map<String, String> urlMap) {
        super(context, 0, taskList);//calls parent constructor
        this.context = context;
        this.taskList = taskList;
        this.urlMap = urlMap;
    }

    //This method returns the layout for each row (task) in the list.
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        String taskName = taskList.get(position);//get task name at this position

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        TextView taskTitle = convertView.findViewById(R.id.taskTitle);
        Button previewBtn = convertView.findViewById(R.id.previewBtn);
        Button downloadBtn = convertView.findViewById(R.id.downloadBtn);

        taskTitle.setText(taskName);

        String url = urlMap.get(taskName);//gets the url associated with the task name

        //to preview a file
        previewBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context.getApplicationContext(), PreviewActivity.class);
            intent.putExtra("pdf_url", url);
            context.startActivity(intent);
        });


        //to download a file
        downloadBtn.setOnClickListener(v -> {
            Uri uri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setTitle("Downloading...")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "file_" + System.currentTimeMillis() + ".pdf");

            ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request);
            Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show();
        });

        return convertView;
    }
}