package com.example.iexam;

public class InputHandler {

    private int numLineRead;
    private boolean testFinished;
    private int testResult;

    //The correct answer to the given Snellen chart
    private String[][] answer =
                    {{"right"},
                    {"down", "right"},
                    {"left", "up", "down"},
                    {"right", "up", "right", "left"},
                    {"right", "up", "left", "right", "down"},
                    {"down", "left", "left", "up", "up"},
                    {"right", "left", "up", "up", "down", "right", "left"},
                    {"up", "left", "down", "up", "left", "down", "up", "left"},
                    {"left", "down", "up", "left", "right", "down", "up", "left"}};
    public final int TOTAL_LINES = 9;

    public InputHandler() {
        numLineRead = 0;
        testFinished = false;
        testResult = 0;
    }

    //Compare user input to the correct answer
    private void processInputPrivate(String[] input) {
        int wrongCount = 0;
        String[] correctAns = answer[numLineRead];
        for (int i = 0; i < correctAns.length; i++) {
            if (input.length <= i) {
                wrongCount ++;
                continue;
            }
            if (!input[i].equals(correctAns[i])) {
                wrongCount ++;
            }
        }

        //Cannot have more than half wrong answers
        if (wrongCount * 2 >= correctAns.length) {
            testFinished = true;
            testResult = numLineRead;
        } else if (numLineRead == TOTAL_LINES - 1) {
            testFinished = true;
            testResult = TOTAL_LINES;
        }
        numLineRead++;
    }

    //Assign a score according to number of lines read
    private String getTestResultPrivate() {
        switch (testResult) {
            case 0:
            case 1:
                return "20/200";
            case 2:
                return "20/100";
            case 3:
                return "20/80";
            case 4:
                return "20/63";
            case 5:
                return "20/50";
            case 6:
                return "20/40";
            case 7:
                return "20/32";
            case 8:
                return "20/25";
            case 9:
                return "20/20";
            default:
                return "wtf";
        }
    }

    public boolean isTestFinished() {
        return testFinished;
    }

    public void processInput(String[] input) {
        if (testFinished == false)
            processInputPrivate(input);
    }

    public String getTestResult() {
        if (testFinished == true)
            return getTestResultPrivate();
        else
            return "Test not finished yet!";
    }

}
