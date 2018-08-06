package com.example.xiaoxiaoouyang.sunexposure;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.app.IntentService;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;


import java.util.ArrayList;

public class ExposureDiagram extends AppCompatActivity {

    Context ctx;
    List<String> time_xaxis = new ArrayList<>();
    List<Double> uvi_yaxis = new ArrayList<>();
    List<String> dummy_list = new ArrayList<>();
    Map<Integer, List> xy_axis;
 //   XAxis xAxis = lineChart.getXAxis();
//    xAxis.setDrawGridLines(false);

    public Context getCtx() {
        return ctx;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exposure_diagram);

        try {
            xy_axis = readMHealthData();
            if (!xy_axis.containsKey(0)) {
                time_xaxis = xy_axis.get(1);
                uvi_yaxis = xy_axis.get(2);
            } else {
                Toast.makeText(ctx, "No data was found", Toast.LENGTH_LONG);
            }
        } catch (IOException e) {
            Toast.makeText(ctx, "No data was found", Toast.LENGTH_LONG);
            System.out.println("No data man");
            e.printStackTrace();
        }

        GraphView line_graph = (GraphView) findViewById(R.id.graph);
        DataPoint[] dataPoints = new DataPoint[time_xaxis.size()];
        for (int i = 0; i < time_xaxis.size(); i++) {
            dataPoints[i] = new DataPoint(i, uvi_yaxis.get(i));
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(dataPoints);
        line_graph.addSeries(series);

//        list1 = (List<String>) i.getSerializableExtra("TIME");
//        list2 = (List<String>) i.getSerializableExtra("UVI");

//        for (int i0 = 0; i0<list1.size(); i0++) {
//            System.out.println("HIIIII");
//            System.out.println(list1.get(i0));
//        }
//
//        for (int i2 = 0; i2<list2.size(); i2++) {
//            System.out.println("HIIIII222");
//            System.out.println(list2.get(i2));
//        }

//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                for (int i = 0 ; i < yEntrys.size(); ++i) {
//                    if (yEntrys.get(i).equals(value)) {
//                        return xEntrys.get(i);
//                    }
//                }
//                return null;
//            }
//        });

//        Double uvi = bundle.getDouble("UVI MEASURE");
//        Double timestamp = bundle.getDouble("TIME STAMP");



    }

//    public void FragmentOneClick(View view) {
//        Fragment myfragment;
//        myfragment = new FragmentOne();
//
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_switch, myfragment);
//        fragmentTransaction.commit();
//
//    }
//    public void FragmentTwoClick(View view) {
//        Fragment myfragment;
//        myfragment = new FragmentTwo();
//
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_switch, myfragment);
//        fragmentTransaction.commit();
//
//    }

//    public void FragmentThreeClick(View view) {
//        Fragment myfragment;
//        myfragment = new FragmentThree();
//
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_switch, myfragment);
//        fragmentTransaction.commit();
//
//    }

    public File getDirectory() {
        // Build file path
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String dirPath = baseDir + File.separator + "SunExposure" + File.separator;
        return new File(dirPath);
    }

    private String getDataFile(File dir) {
        String filePath = dir.getPath() + File.separator + "SunExposureData.csv";
        return  filePath;
    }


    public Map<Integer, List> readMHealthData() throws IOException {
        File dir = getDirectory();
        String filepath = getDataFile(dir);
        File mFile = new File(filepath);
        FileInputStream fis = null;
                try {
                    fis = new FileInputStream(mFile);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();
                    bufferedReader.readLine();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] tokens = line.split(",");
                        System.out.println(tokens[0] + "   " + tokens[1] + "   " + tokens[2] + "   " + tokens[3] + "   " + tokens[4] + "   " + tokens[5]);
                        time_xaxis.add(tokens[0]);
                        uvi_yaxis.add(tokens[4]);
                        System.out.println(tokens[0] + "   " + tokens[1] + "   " + tokens[2] + "   " + tokens[3] + "   " + tokens[4] + "   " + tokens[5]);
                    }
                    //return sb.toString();
                    xy_axis.put(1, time_xaxis);
                    xy_axis.put(2, uvi_yaxis);
                } catch (FileNotFoundException e) {
                    System.out.println("No data man");
                    xy_axis.put(0, dummy_list);
                }
        return xy_axis;
    }
    public void goBack(View view) {
        super.onBackPressed();
    }
}
