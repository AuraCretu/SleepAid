package com.example.sleepaid.Fragment.SleepData;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.sleepaid.App;
import com.example.sleepaid.Database.AppDatabase;
import com.example.sleepaid.Database.SleepData.SleepData;
import com.example.sleepaid.Handler.DataHandler;
import com.example.sleepaid.Listener.OnSwipeTouchListener;
import com.example.sleepaid.Model.SharedViewModel;
import com.example.sleepaid.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;

import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;


public abstract class SleepDataGraphFragment extends Fragment {
    protected SleepDataFragment sleepDataFragment;

    private AppDatabase db;

    private SharedViewModel model;

    private GraphView graph;

    private int maxGraphSize;

    private int[] translation;

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        sleepDataFragment = (SleepDataFragment) getParentFragment().getParentFragment();
        sleepDataFragment.graphFragment = this;

        db = AppDatabase.getDatabase(App.getContext());

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        graph = view.findViewById(R.id.sleepDataGraph);

        graph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.white));
        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
        graph.getViewport().setBorderColor(getResources().getColor(R.color.white));

        graph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        graph.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.white));

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);

        //graph.getViewport().setDrawBorder(true);
        //graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
        graph.setOnTouchListener(new OnSwipeTouchListener(App.getContext()) {
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                if (sleepDataFragment.nextButton.getVisibility() == View.VISIBLE) {
                    sleepDataFragment.nextButton.performClick();
                }
            }

            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                sleepDataFragment.previousButton.performClick();
            }
        });

        loadGraph(sleepDataFragment.rangeMin, sleepDataFragment.rangeMax);
        loadTodaysData();
    }

    protected void loadGraph(ZonedDateTime min, ZonedDateTime max) {
        // for February in non-leap years we only have 4 weeks
        if (sleepDataFragment.rangeMin.getMonthValue() == 2 &&
                sleepDataFragment.rangeMin.getYear() % 4 != 0 &&
                model.getGraphViewType().equals("month")) {
            this.maxGraphSize = model.getGraphPeriodLength() - 1;
        } else {
            this.maxGraphSize = model.getGraphPeriodLength();
        }

        String period;

        if (DataHandler.getFormattedDate(sleepDataFragment.rangeMax)
                .equals(DataHandler.getFormattedDate(sleepDataFragment.today))) {
            sleepDataFragment.nextButton.setVisibility(View.INVISIBLE);

            period = "This " + model.getGraphViewType();
        }
        else {
            sleepDataFragment.nextButton.setVisibility(View.VISIBLE);

            switch (model.getGraphViewType()) {
                case "month":
                    period = DataHandler.getMonth(min);
                    break;

                case "year":
                    period = DataHandler.getYear(min);
                    break;

                //"week"
                default:
                    period = DataHandler.getFormattedDate(min) + " - " + DataHandler.getFormattedDate(max);
                    break;
            }
        }

        graph.removeAllSeries();

        TextView graphTitle = sleepDataFragment.getView().findViewById(R.id.graphTitle);
        graphTitle.setText(period);

        graph.getViewport().setMaxX(this.maxGraphSize - 1);
        graph.getGridLabelRenderer().setNumHorizontalLabels(this.maxGraphSize);

        switch (model.getGraphViewType()) {
            case "month":
                graph.getGridLabelRenderer().setLabelFormatter(sleepDataFragment.getMonthLabelFormatter(this.maxGraphSize));
                break;

            case "year":
                graph.getGridLabelRenderer().setLabelFormatter(sleepDataFragment.getYearLabelFormatter());
                break;

            //"week"
            default:
                graph.getGridLabelRenderer().setLabelFormatter(sleepDataFragment.getWeekLabelFormatter());
                break;
        }
    }

    protected void loadGoal(String goalName) {
        Bitmap goalIcon = BitmapFactory.decodeResource(getResources(), R.drawable.trophy_icon);

        if (model.getGoalMinLine(goalName) == null &&
                model.getGoalMaxLine(goalName) == null) {
            db.goalDao()
                    .loadAllByNames(new String[]{goalName})
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            goalData -> {
                                if (!goalData.isEmpty()) {
                                    model.setGoal(
                                            goalName,
                                            goalData.get(0).getValueMin(),
                                            goalData.get(0).getValueMax(),
                                            getResources().getColor(R.color.white),
                                            getResources().getColor(R.color.white)
                                    );

                                    graph.addSeries(model.getGoalMaxLine(goalName));
                                    graph.addSeries(model.getGoalMaxPoint(goalName));

                                    if (!model.getGoalMin(goalName).equals(model.getGoalMax(goalName))) {
                                        graph.addSeries(model.getGoalMinLine(goalName));
                                        graph.addSeries(model.getGoalMinPoint(goalName));
                                    }
                                }

                                loadFromDatabase(goalName);
                            },
                            Throwable::printStackTrace
                    );
        } else {
            graph.addSeries(model.getGoalMaxLine(goalName));
            graph.addSeries(model.getGoalMaxPoint(goalName));

            if (!model.getGoalMin(goalName).equals(model.getGoalMax(goalName))) {
                graph.addSeries(model.getGoalMinLine(goalName));
                graph.addSeries(model.getGoalMinPoint(goalName));
            }

            loadFromDatabase(goalName);
        }
    }

    private void loadFromDatabase(String name) {
        String periodStart = DataHandler.getSQLiteDate(sleepDataFragment.rangeMin);
        String periodEnd = DataHandler.getSQLiteDate(sleepDataFragment.rangeMax);

        if (model.getLineSeries(name, periodStart, periodEnd) == null) {
            db.sleepDataDao()
                    .loadAllByDateRangeAndType(
                            periodStart,
                            periodEnd,
                            name
                    )
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            sleepData -> {
                                this.setTranslation(name, sleepData);

                                List<Double> processedSleepData = this.processFromDatabase(sleepData);

                                int graphColor;

                                switch (name) {
                                    case "Wake-up time":
                                        graphColor = getResources().getColor(R.color.lightest_purple_sleep_transparent);
                                        break;

                                    case "Bedtime":
                                        graphColor = getResources().getColor(R.color.darkest_purple_sleep_transparent);
                                        break;

                                    //"Sleep duration"
                                    default:
                                        graphColor = getResources().getColor(R.color.purple_sleep_transparent);
                                        break;
                                }

                                model.setSeries(
                                        name,
                                        processedSleepData,
                                        periodStart,
                                        periodEnd,
                                        graphColor,
                                        getResources().getColor(R.color.white),
                                        getResources().getColor(R.color.white)
                                );

                                graph.getViewport().setMaxY(model.getMaxY(name, periodStart, periodEnd));

                                graph.addSeries(model.getLineSeries(name, periodStart, periodEnd));
                                //TODO add click on point and popup with value
                                //graph.addSeries(model.getPointsSeries(name, periodStart, periodEnd));
                            },
                            Throwable::printStackTrace
                    );
        } else {
            graph.getViewport().setMaxY(model.getMaxY(name, periodStart, periodEnd));

            graph.addSeries(model.getLineSeries(name, periodStart, periodEnd));
            //graph.addSeries(model.getPointsSeries(name, periodStart, periodEnd));
        }
    }

    private void setTranslation(String dataType, List<SleepData> sleepData) {
        if (!sleepData.isEmpty()) {
            List<String> sleepDataValues = sleepData
                    .stream()
                    .map(s -> s.getValue())
                    .collect(Collectors.toList());

            List<Double> sleepDataNumberValues = DataHandler.getDoublesFromTimes(sleepDataValues);

            this.translation = new int[sleepDataNumberValues.size()];

            if (dataType.equals("Bedtime")) {
                for (int i = 0; i < sleepDataNumberValues.size(); i++) {
                    if (sleepDataNumberValues.get(i) > 12 && sleepDataNumberValues.get(i) < 24) {
                        translation[i] = -12;
                    } else if (sleepDataNumberValues.get(i) >= 0) {
                        translation[i] = 12;
                    }
                }
            }
        }
    }

    private List<Double> processFromDatabase(List<SleepData> sleepData) {
        if (sleepData.isEmpty()) {
            List<Double> processedSleepData = new ArrayList(Arrays.asList(new Double[model.getGraphPeriodLength()]));
            Collections.fill(processedSleepData, -1.0);

            return processedSleepData;
        }

        switch (model.getGraphViewType()) {
            case "month":
                return this.processMonthData(sleepData);

            case "year":
                return this.processYearData(sleepData);

            //"week"
            default:
                return this.processWeekData(sleepData);
        }
    }

    private List<Double> processWeekData(List<SleepData> sleepData) {
        List<Double> processedSleepData = new ArrayList<>();

        ZonedDateTime day = sleepDataFragment.rangeMin;
        ZonedDateTime end = day.plusDays(6);

        while (!day.isAfter(end)) {
            String date = DataHandler.getSQLiteDate(day);

            Optional<SleepData> sleepDataForDay = sleepData
                    .stream()
                    .filter(s -> s.getDate().equals(date))
                    .findAny();

            if (sleepDataForDay.isPresent()) {
                double value = DataHandler.getDoubleFromTime(sleepDataForDay.get().getValue());
                value += translation[sleepData.indexOf(sleepDataForDay.get())];

                processedSleepData.add(value);
            } else {
                processedSleepData.add(-1.0);
            }

            day = day.plusDays(1);
        }

        return processedSleepData;
    }

    private List<Double> processMonthData(List<SleepData> sleepData) {
        List<Double> processedSleepData = new ArrayList<>();

        ZonedDateTime weekStart = sleepDataFragment.rangeMin;
        ZonedDateTime weekEnd = sleepDataFragment.rangeMin.plusDays(6);
        ZonedDateTime end = weekStart.withDayOfMonth(YearMonth.of(weekStart.getYear(), weekStart.getMonthValue()).lengthOfMonth());

        while (!weekEnd.isAfter(end)) {
            String startDate = DataHandler.getSQLiteDate(weekStart);
            String endDate = DataHandler.getSQLiteDate(weekEnd);

            List<SleepData> sleepDataForWeek = sleepData
                    .stream()
                    .filter(s -> s.getDate().compareTo(startDate) >= 0 &&
                            s.getDate().compareTo(endDate) <= 0)
                    .collect(Collectors.toList());

            if (!sleepDataForWeek.isEmpty()) {
                List<Double> valuesForWeek = sleepDataForWeek
                        .stream()
                        .map(s -> DataHandler.getDoubleFromTime(s.getValue()) +
                                translation[sleepData.indexOf(s)])
                        .collect(Collectors.toList());

                double weeklyAverage = valuesForWeek
                        .stream()
                        .mapToDouble(v -> v)
                        .average()
                        .getAsDouble();

                processedSleepData.add(weeklyAverage);
            } else {
                processedSleepData.add(-1.0);
            }

            weekStart = weekStart.plusDays(7);
            weekEnd = weekEnd.plusDays(7);
        }

        return processedSleepData;
    }

    private List<Double> processYearData(List<SleepData> sleepData) {
        List<Double> processedSleepData = new ArrayList<>();

        String[] months = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};

        for (String m : months) {
            List<SleepData> sleepDataForMonth = sleepData
                    .stream()
                    .filter(s -> s.getDate().contains("-" + m + "-"))
                    .collect(Collectors.toList());

            if (!sleepDataForMonth.isEmpty()) {
                List<Double> valuesForMonth = sleepDataForMonth
                        .stream()
                        .map(s -> DataHandler.getDoubleFromTime(s.getValue()) +
                                translation[sleepData.indexOf(s)])
                        .collect(Collectors.toList());

                double monthlyAverage = valuesForMonth
                        .stream()
                        .mapToDouble(v -> v)
                        .average()
                        .isPresent() ? valuesForMonth
                        .stream()
                        .mapToDouble(v -> v)
                        .average()
                        .getAsDouble() :
                        0.0;

                processedSleepData.add(monthlyAverage);
            } else {
                processedSleepData.add(-1.0);
            }
        }

        return processedSleepData;
    }

    protected void loadTodaysData() {}
}