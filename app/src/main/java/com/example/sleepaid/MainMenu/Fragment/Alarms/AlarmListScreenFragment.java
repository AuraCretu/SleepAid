package com.example.sleepaid.MainMenu.Fragment.Alarms;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sleepaid.App;
import com.example.sleepaid.Database.AppDatabase;
import com.example.sleepaid.Model.SharedViewModel;
import com.example.sleepaid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AlarmListScreenFragment extends Fragment implements View.OnClickListener {
    private AppDatabase db;

    protected AlarmListFragment alarmListFragment;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_alarm_list_screen, container, false);
    }

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        db = AppDatabase.getDatabase(App.getContext());

        FloatingActionButton addAlarmButton = view.findViewById(R.id.addAlarmButton);
        addAlarmButton.setOnClickListener(this);

        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.alarmList);

        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomMenu = getView().findViewById(R.id.bottomMenu);

        NavigationUI.setupWithNavController(bottomMenu, navController);

        db.configurationDao()
                .loadAllByNames(new String[]{"supportNaps"})
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        napData -> {
                            if (napData.get(0).getValue().equals("No.")) {
                                bottomMenu.getMenu().removeItem(R.id.napAlarmsFragment);
                            }
                        },
                        Throwable::printStackTrace
                );
    }

    public void onClick(View view) {
        NavHostFragment.findNavController(this).navigate(R.id.configureAlarmAction);
    }
}