package com.btsoft.opencvapp2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LibraryActivity extends Fragment {

    private static final String TAG = "LibraryActivity";

    private View view;
    private RecyclerViewAdapter adapter = new RecyclerViewAdapter();
    private RecyclerView library_view;

    public LibraryActivity() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.library_layout, container, false);
        initRecyclerView();
        return view;
    }

    private void initRecyclerView() {
        library_view = view.findViewById(R.id.recycler_view);
        library_view.setAdapter(adapter);
        library_view.setLayoutManager(new LinearLayoutManager(MainActivity.getContextOfApp()));
    }

}
