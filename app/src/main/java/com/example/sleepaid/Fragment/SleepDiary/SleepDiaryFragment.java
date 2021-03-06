package com.example.sleepaid.Fragment.SleepDiary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.sleepaid.Fragment.MainMenuFragment;
import com.example.sleepaid.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SleepDiaryFragment extends MainMenuFragment {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sleep_diary, container, false);
    }

    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager().findFragmentById(R.id.diary);

        NavController navController = navHostFragment.getNavController();
        BottomNavigationView bottomMenu = view.findViewById(R.id.sleepDiaryBottomMenu);

        NavGraph navGraph = navController.getNavInflater().inflate(R.navigation.sleep_diary_graph);

        if (getArguments() != null && getArguments().containsKey("DESTINATION")) {
            navGraph.setStartDestination(getArguments().getInt("DESTINATION"));
        } else {
            navGraph.setStartDestination(R.id.morningSleepDiaryFragment);
        }

        navController.setGraph(navGraph);
        NavigationUI.setupWithNavController(bottomMenu, navController);
    }
}