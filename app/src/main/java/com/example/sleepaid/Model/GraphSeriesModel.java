package com.example.sleepaid.Model;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.List;
import java.util.Optional;

@SuppressLint("NewApi")
public class GraphSeriesModel {
    private String dataType;

    private List<Double> data;
    private int[] translation;

    private LineGraphSeries<DataPoint> lineSeries;
    private PointsGraphSeries<DataPoint> pointsSeries;

    private String periodStart;
    private String periodEnd;

    public GraphSeriesModel(String dataType,
                            List<Double> data,
                            String periodStart,
                            String periodEnd,
                            int backgroundColor,
                            int lineColor,
                            int pointsColor) {
        this.dataType = dataType;

        this.data = data;
        this.setTranslation(data);

        this.periodStart = periodStart;
        this.periodEnd = periodEnd;

        this.setLineSeries(data, backgroundColor, lineColor);
        this.setPointsSeries(data, pointsColor);
    }

    public void update(List<Double> data,
                       String periodStart,
                       String periodEnd,
                       int backgroundColor,
                       int lineColor,
                       int pointsColor) {
        this.data = data;
        this.setTranslation(data);

        this.periodStart = periodStart;
        this.periodEnd = periodEnd;

        this.setLineSeries(data, backgroundColor, lineColor);
        this.setPointsSeries(data, pointsColor);
    }

    private void setTranslation(List<Double> data) {
        this.translation = new int[data.size()];

        if (this.dataType.equals("Bedtime")) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i) > 0 && data.get(i) < 12) {
                    translation[i] = 12;
                } else if (data.get(i) >= 12) {
                    translation[i] = -12;
                }
            }
        }
    }

    private void setLineSeries(List<Double> data,
                              int backgroundColor,
                              int lineColor) {
        this.lineSeries = this.createLineSeries(data, backgroundColor, lineColor);
    }

    private void setPointsSeries(List<Double> data,
                                int pointsColor) {
        this.pointsSeries = this.createPointsSeries(data, pointsColor);
    }

    public String getDataType() {
        return this.dataType;
    }

    public List<Double> getData() {
        return this.data;
    }

    public int getTranslation(int i) {
        return this.translation[i];
    }

    public LineGraphSeries<DataPoint> getLineSeries() {
        return this.lineSeries;
    }

    public PointsGraphSeries<DataPoint> getPointsSeries() {
        return this.pointsSeries;
    }

    public String getPeriod() {
        return this.periodStart + "-" + this.periodEnd;
    }

    private LineGraphSeries<DataPoint> createLineSeries(List<Double> data,
                                                        int backgroundColor,
                                                        int lineColor) {
        LineGraphSeries<DataPoint> lineSeries = new LineGraphSeries<>();

        for (int i = 0; i < data.size(); i++) {
            lineSeries.appendData(
                    new DataPoint(i, Math.max(0, data.get(i))),
                    true,
                    data.size()
            );
        }

        this.styleLineSeries(lineSeries, backgroundColor, lineColor);

        return lineSeries;
    }

    private void styleLineSeries(LineGraphSeries<DataPoint> lineSeries,
                                 int backgroundColor,
                                 int lineColor) {
        lineSeries.setDrawBackground(true);
        lineSeries.setDrawDataPoints(true);

        lineSeries.setBackgroundColor(backgroundColor);
        lineSeries.setColor(lineColor);
    }

    private PointsGraphSeries<DataPoint> createPointsSeries(List<Double> data,
                                                           int pointsColor) {
        PointsGraphSeries<DataPoint> pointsSeries = new PointsGraphSeries<>();

        for (int i = 0; i < data.size(); i++) {
            pointsSeries.appendData(
                    new DataPoint(i, Math.max(0, data.get(i))),
                    true,
                    data.size()
            );
        }

        this.stylePointsSeries(pointsSeries, pointsColor);

        return pointsSeries;
    }

    private void stylePointsSeries(PointsGraphSeries<DataPoint> pointsSeries,
                                   int pointsColor) {
        pointsSeries.setCustomShape(new PointsGraphSeries.CustomShape() {
            @Override
            public void draw(Canvas canvas, Paint paint, float x, float y, DataPointInterface dataPoint) {
                paint.setColor(pointsColor);
                paint.setTextSize(38);

                Optional<Double> point = data.stream()
                        .filter(d -> Math.max(0, d) == dataPoint.getY())
                        .findFirst();

                int index = data.indexOf(point.get());

                int hours = (int)(dataPoint.getY() + translation[index]);
                int minutes = (int) (((dataPoint.getY() + translation[index]) - hours) * 60);

                String text = getPointText(hours, minutes);

                canvas.drawText(text, x, y, paint);
            }
        });
    }

    private String getPointText(int hours, int minutes) {
        String text;

        switch (this.dataType) {
            case "Sleep duration":
                text = minutes == 0 ?
                        hours + "h" :
                        hours + "h" + minutes + "m";
                break;

            //"Wake-up time" or "Bedtime
            default:
                String delimiter = minutes < 10 ? ":0" : ":";
                text = hours + delimiter + minutes;
                break;
        }

        return text;
    }
}