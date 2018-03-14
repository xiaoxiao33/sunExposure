package com.example.xiaoxiaoouyang.sunexposure;

// CODE Modified from Code provided by Prof. Alshurafa - credit to him/his group for the majority
// of this file

import android.util.Log;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

/**
 * Handles saving CSV data to external memory
 * Created by William on 1/9/2018.
 */

public class CSVManager {

    private String fileName;
    private long sessionStart;

    CSVManager() {
        // Use current time to name the file
        Calendar c = Calendar.getInstance();
        fileName = "SunExposureData.csv";
    }

    /**
     * Gets the default directory for saving the data
     * @return File object representing the directory
     */
    private File getDirectory() {
        // Build file path
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirPath = baseDir + File.separator + "SunExposure" + File.separator;
        return new File(dirPath);
    }

    /**
     * Gets the file for saving the data
     * @param dir File representing the directory to save the data file in
     * @return File object representing teh data file
     */
    private File getDataFile(File dir) {
        String filePath = dir.getPath() + File.separator + fileName;
        return new File(filePath);
    }


    public String getDataAsString() {
        File dir = getDirectory();
        File dataFile = getDataFile(dir);


        if (!dataFile.exists()) return "";

        try {
            CSVReader reader = new CSVReader(new FileReader(dataFile));
            List<String[]> output = reader.readAll();

            StringBuilder builder = new StringBuilder();
            for (String[] line : output) {
                builder.append(line[0]);
                builder.append("  ");
                builder.append(line[1]);
                builder.append("  ");
                builder.append(line[2]);
                builder.append("  ");
                builder.append(line[3]);
                builder.append("\n");
            }
            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    void saveData(List<CSVRow> data) {
        Log.v("savedata", "in save data");
        File dir = getDirectory();
        File f = getDataFile(dir);

        CSVWriter writer;
        // File exists
        try {
            if (!dir.exists()) {
                //noinspection ResultOfMethodCallIgnored
                Log.v("savedata", "making directory");
                boolean success = dir.mkdirs();
                Log.v("savedata",  "bool: " + String.valueOf(success));
            }
            else {
                Log.v("savedata", "directory found");
            }
            if (f.exists() && !f.isDirectory()) {
                FileWriter mFileWriter = new FileWriter(f.getPath(), true);
                writer = new CSVWriter(mFileWriter);
            } else {
                if (!dir.exists()) {
                    Log.v("savedata", "dir does not exist");
                }
                else{
                    Log.v("savedata", "dir exists");
                }
                //noinspection ResultOfMethodCallIgnored
                f.createNewFile();
                writer = new CSVWriter(new FileWriter(f.getPath()));
                // Write header
                String[] header = {
                        "Time",
                        "Longitude",
                        "Latitude",
                        "UVI",
                        "Num GPS",
                        "wifi Perc",
                        "CellDbm",
                        "CellAsu",
                        "CellLevel"
                };

                writer.writeNext(header);
            }

            for (CSVRow r : data) {
                Log.v("Sensor", r.timestamp + " " + r.longitude + " " + r.latitude + " " + r.uvi + " " + r.numGPSSat  + " " + r.wifiPerc + " " + r.cellDbm);

                String[] row = {
                        String.valueOf(r.timestamp),
                        String.valueOf(r.longitude),
                        String.valueOf(r.latitude),
                        String.valueOf(r.uvi),
                        String.valueOf(r.numGPSSat),
                        String.valueOf(r.wifiPerc),
                        String.valueOf(r.cellDbm),
                        String.valueOf(r.cellAsu),
                        String.valueOf(r.cellLevel),
                };

                writer.writeNext(row);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
