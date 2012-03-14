package com.chaos.sleepcry.busecretary.loading;

import com.chaos.sleepcry.busecretary.R;
import com.chaos.sleepcry.busecretary.append.AppendActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

public class LoadingActivity extends Activity{
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.loading);
		ProgressBar pb = (ProgressBar)findViewById(R.id.loadong_pb);
		pb.setMax(100);
		pb.setProgress(0);
		//TODO:add actual initialization here
		//maybe a timer or a new thread to do this
		for(int i=0;i<100;i++){
			pb.setProgress(i);	
		}
		Intent intent = new Intent(this,AppendActivity.class);
		startActivity(intent);
	}
	public void endloading(View v){
		Intent intent = new Intent(this,AppendActivity.class);
		startActivity(intent);
	}
}
