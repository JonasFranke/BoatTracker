package de.jo.boattracker.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import androidx.core.content.FileProvider;
import de.jo.boattracker.main.MainActivity;

import java.io.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class GPX implements View.OnClickListener{

    private File file;
    private final File filesDir;
    private ArrayList<String> lines;

    public GPX(File filesDir) {
        assert filesDir != null;
        this.filesDir = filesDir;

        assert lines == null;
        createBoilerplate();
    }

    public void addLocation(Location location) {
        try {
            assert location != null;
            lines.add("         <trkpt lat=\"" + location.getLatitude() + "\" lon=\"" +location.getLongitude()  + "\" ele=\"" + location.getAltitude() + "\">");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ");//yyyy-MM-ddTHH:mm:ssZ
            lines.add("            <time>" + formatter.format(LocalDateTime.now()) + "</time>");
            lines.add("         </trkpt>");
        } catch (AssertionError e) {
            Log.w("LocationAdder", "Could not add location -> It's null");
        }

    }

    private void createBoilerplate() {
        lines = new ArrayList<>();
        lines.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        lines.add("<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" version=\"1.1\">");
        lines.add("   <trk>");
        lines.add("      <name>$name</name>");
        lines.add("      <cmt>Created with BoatTracker by JonasFranke</cmt>");
        lines.add("      <trkseg>");
    }

    public void write(Context context) {
        lines.add("      </trseg>");
        lines.add("   </trk>");
        lines.add("</gpx>");

        LocalDateTime date = LocalDateTime.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");

        final String FILENAME = dtf.format(date) + ".gpx";

        file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), FILENAME);

        final String TAG = "FileWriter";

        try {
            FileOutputStream fos = new FileOutputStream(file);
            assert lines.size() > 1;
            for (String line : lines) {
                fos.write((line + "\n").getBytes());
            }
            fos.close();
            Log.i(TAG, "Successfully wrote file");

            shareFile(FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file), context);

            if (filesDir.listFiles().length > 1) {
                for (File f : filesDir.listFiles()) {
                    Log.d(TAG, f.getPath());
                }
            }

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e);
        }

    }

    private void shareFile(Uri uri, Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        System.out.println("Write button pressed");
        write(MainActivity.getContext());
    }
}
