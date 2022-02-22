package com.example.sleepaid.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sleepaid.R;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SleepDurationGraphFragment extends SleepDataGraphFragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_duration_graph, container, false);
    }

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void loadGraph(String period) {
        super.loadGraph(period);

        db.sleepDataDao()
                .loadAllByDateRangeAndType(
                        sleepDataFragment.graphRangeMin,
                        sleepDataFragment.graphRangeMax,
                        "duration"
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sleepData -> {
                            List<Double> processedSleepData = processFromDatabase(sleepData);

                            LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<>();
                            PointsGraphSeries<DataPoint> pointsGraphSeries = new PointsGraphSeries<>();

                            int maxGraphSize = model.getGraphPeriodLength();

                            for (int i = 0; i < Math.min(maxGraphSize, processedSleepData.size()); i++) {
                                lineGraphSeries.appendData(
                                        new DataPoint(i, processedSleepData.get(i)),
                                        true,
                                        maxGraphSize
                                );

                                if (processedSleepData.get(i) != 0) {
                                    pointsGraphSeries.appendData(
                                            new DataPoint(Math.max(0, i - 0.15), processedSleepData.get(i) + 0.5),
                                            true,
                                            maxGraphSize
                                    );
                                }
                            }

                            model.setSleepDurationLineSeries(
                                    lineGraphSeries,
                                    getResources().getColor(R.color.white),
                                    getResources().getColor(R.color.white)
                            );

                            model.setSleepDurationPointsSeries(
                                    pointsGraphSeries,
                                    getResources().getColor(R.color.white)
                            );

                            graph.getViewport().setMaxY(Collections.max(processedSleepData) + 1);

                            graph.addSeries(model.getSleepDurationLineSeries());
                            graph.addSeries(model.getSleepDurationPointsSeries());
                        },
                        Throwable::printStackTrace
                );
    }
}