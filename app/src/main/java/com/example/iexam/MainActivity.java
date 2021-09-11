package com.example.iexam;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArHandler arHandler;
    private InputHandler inputHandler;
    private Button placeButton, recordButton;

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkIsSupportedDevice();
        setContentView(R.layout.activity_main);

        arHandler = new ArHandler(this);
        arHandler.getArScene().setOnClickListener((view) -> showIntroWindow(view));

        placeButton = findViewById(R.id.placeButton);
        recordButton = findViewById(R.id.recordButton);

        Toast.makeText(
                getApplicationContext(),
                "Tap anywhere to see the intro window.",
                Toast.LENGTH_LONG).show();
    }

    //Check if the device supports AR features
    private void checkIsSupportedDevice() {
        String openGlVersionString = ((ActivityManager)
                Objects.requireNonNull(this.getSystemService(Context.ACTIVITY_SERVICE)))
                .getDeviceConfigurationInfo().getGlEsVersion();

        if (Double.parseDouble(openGlVersionString) < 3.0) {
            Log.e(MainActivity.class.getSimpleName(), "AR requires OpenGL ES 3.0 later");
            Toast.makeText(
                    getApplicationContext(),
                    "AR requires OpenGL ES 3.0 or later",
                    Toast.LENGTH_LONG).show();
            this.finish();
        }
    }

    private void showIntroWindow(View view) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.intro_window, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener((myview, event) -> {
            popupWindow.dismiss();
            placeButton.setVisibility(View.VISIBLE);
            placeButton.setOnClickListener(views -> {
                inputHandler = new InputHandler();
                arHandler.addSnellenChart();
                Toast.makeText(
                        getApplicationContext(),
                        "Now press the record button, and then read the " +
                                "first line of Snellen chart out loud.",
                        Toast.LENGTH_LONG).show();
            });
            recordButton.setVisibility(View.VISIBLE);
            recordButton.setOnClickListener(views -> {
                if (arHandler.isChartLive())
                    startVoiceRecognition();
                    arHandler.getArScene().getScene().removeOnUpdateListener(arHandler);
            });
            arHandler.deleteSnellenChart();   //bug!
            return true;});
        arHandler.getArScene().setOnClickListener(null);

        //A very minor bug: must have these two lines of code labelled "bug!"
        //otherwise the snellen chart will somehow have the wrong dimension
        arHandler.addSnellenChart();   //bug!
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        //Send user voice input to onActivityResult
        try {
            startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(
                    getApplicationContext(),
                    "You need to first download \"Voice Search\" app from store",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE &&
                resultCode == RESULT_OK && data != null) {
            ArrayList<String> input =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            //Test if at least one of the matches contain valid input
            boolean validInput = false;
            outerloop:
            for (int i = 0; i < input.size(); i++) {
                String[] tokens = input.get(i).split(" ");
                for (int j = 0; j < tokens.length; j++) {
                    String nextWord = tokens[j];
                    if (!nextWord.equals("up") && !nextWord.equals("down") &&
                            !nextWord.equals("left") && !nextWord.equals("right"))
                        continue outerloop;
                }
                inputHandler.processInput(tokens);
                validInput = true;
                break;
            }

            //Handles invalid user voice input
            if (validInput == false) {
                Toast.makeText(
                        getApplicationContext(),
                        "Your input is invalid. Make sure your input only " +
                                "contains \"up\", \"down\", \"left\", or \"right\". " +
                                "Please try again by pressing the record button.",
                        Toast.LENGTH_LONG).show();

            //Handles valid user voice input
            } else {
                if (inputHandler.isTestFinished() == true) {
                    String score = inputHandler.getTestResult();
                    Toast.makeText(
                            getApplicationContext(),
                            "The test is finished. Your score is: " + score + ". " +
                                    "Please feel free to try the test again.",
                            Toast.LENGTH_LONG).show();
                    arHandler.deleteSnellenChart();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Now press the record button again, " +
                                    "and then read the next line of Snellen chart.",
                            Toast.LENGTH_LONG).show();
                    arHandler.getArScene().getScene().removeOnUpdateListener(arHandler);
                }
            }
        }
    }

}
