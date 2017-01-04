package com.samanlan.slidevalidation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import com.samanlan.lib_slidevalidation.SlideListener;
import com.samanlan.lib_slidevalidation.SlideValidationView;

public class MainActivity extends AppCompatActivity {

    SeekBar seekBar;
    SlideValidationView slideValidationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                slideValidationView.restore();
            }
        });
        slideValidationView = (SlideValidationView) findViewById(R.id.yzm);
        slideValidationView.setListener(new SlideListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "验证成功", Toast.LENGTH_SHORT).show();
                seekBar.setProgress(0);
            }

            @Override
            public void onFail() {
                Toast.makeText(MainActivity.this, "验证失败", Toast.LENGTH_SHORT).show();
                seekBar.setProgress(0);
            }
        });
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                System.out.println("当前进度"+progress);
                slideValidationView.setOffsetX(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                slideValidationView.deal();
            }
        });
    }
}
