package com.btsoft.opencvapp2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapter";

    private static ArrayList<String> bookNames = new ArrayList<>();
    private static ArrayList<String> authorNames = new ArrayList<>();
    private static ArrayList<String> releaseYears = new ArrayList<>();
    private static ArrayList<String> imagePaths = new ArrayList<>();

    private Bundle pdf_name = new Bundle();

    private int image_id;
    private Context aContext = MainActivity.getContextOfApp();
    public String line = "";
    private BufferedReader br = null;
    private String cvsSplitBy = ",";

    public RecyclerViewAdapter() {
        readCSV();
    }


    public void readCSV(){
        try {
            br = new BufferedReader((new InputStreamReader(aContext.getAssets().open("csvfile.csv"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            while ((line = br.readLine()) != null) {
                String[] satir = line.split(cvsSplitBy);
                bookNames.add(satir[0]);
                authorNames.add(satir[1]);
                releaseYears.add(satir[2]);
                imagePaths.add(satir[3]);
            }
        }   catch (IOException e) {
            e.printStackTrace();
        }   finally {
            if (br != null) {
                try {
                    br.close();
                }   catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.library_list_frame, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        Log.i(TAG, "onBindViewHolder: called");

        image_id = aContext.getResources().getIdentifier("@drawable/b" + imagePaths.get(position), null, aContext.getPackageName());

        holder.book_cover.setImageResource(image_id);
        holder.book_name.setText(String.valueOf(bookNames.get(position)));
        holder.author_name.setText(String.valueOf(authorNames.get(position)));
        holder.release_year.setText(String.valueOf(releaseYears.get(position)));

        holder.book_on_library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(aContext, PDFViewer.class);
                pdf_name.putString("name", imagePaths.get(position));
                intent.putExtras(pdf_name);
                aContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView book_name, author_name, release_year;
        ImageView book_cover;
        RelativeLayout book_on_library;

        public ViewHolder(View itemView){
            super(itemView);
            book_cover = itemView.findViewById(R.id.lib_book_image);
            book_name = itemView.findViewById(R.id.lib_book_name);
            author_name = itemView.findViewById(R.id.lib_author_name);
            release_year = itemView.findViewById(R.id.lib_release_year);
            book_on_library = itemView.findViewById(R.id.book_on_lib);
        }
    }
}
