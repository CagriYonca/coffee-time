package com.btsoft.opencvapp2;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

public class PDFViewer extends AppCompatActivity {

    public static PDFView pdfView;
    private Bundle pdf_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        makeFullScreen();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pdf_viewer);

        pdf_name = getIntent().getExtras();
        pdfView = findViewById(R.id.pdf_view_frame);
        pdfView.fromAsset("bookPDF_db/p" + pdf_name.getString("name") + ".pdf").load();
    }

    public void makeFullScreen(){

        this.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
