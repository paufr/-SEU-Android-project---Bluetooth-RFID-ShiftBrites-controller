package com.seuproject.pau.seuprojectbluetoothle;

import android.widget.EditText;
import android.widget.ToggleButton;

/**
 * Created by Pau on 30/12/2017.
 */

public class DeviceParameters {

    private static final int DEFAULT_COLOR_VALUE = 0;
    private EditText id;
    private EditText red;
    private EditText green;
    private EditText blue;
    private ToggleButton access;

    public static final String DEFAULT_NO_ACCESS_STRING = "2550000000";

    public DeviceParameters(EditText id, EditText red, EditText green, EditText blue, ToggleButton access) {
        this.id = id;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.access = access;
    }

    public String getID() {
        return id.getText().toString();
    }

    public int getRed() {
        try {
            int value = Integer.parseInt(red.getText().toString());
            if (value < 0) value = 0;
            else if (value > 255) value = 255;
            return value;
        }
        catch (Exception e) {
            return DEFAULT_COLOR_VALUE;
        }
    }

    public int getGreen() {
        try {
            int value = Integer.parseInt(green.getText().toString());
            if (value < 0) value = 0;
            else if (value > 255) value = 255;
            return value;
        }
        catch (Exception e) {
            return DEFAULT_COLOR_VALUE;
        }
    }
    public int getBlue() {
        try {
            int value = Integer.parseInt(blue.getText().toString());
            if (value < 0) value = 0;
            else if (value > 255) value = 255;
            return value;
        }
        catch (Exception e) {
            return DEFAULT_COLOR_VALUE;
        }
    }

    public String getAccessString() {
        return  convertTo3DigitsString(getRed()) +
                convertTo3DigitsString(getGreen()) +
                convertTo3DigitsString(getBlue()) +
                getHasAccessString();
    }

    public static boolean isAccessParametersInfo(String data) {
        if (data.length() != 10) return false;
        for (int i = 0; i < data.length(); ++i) {
            try {
                Integer.parseInt(data.charAt(i) + "");
            }
            catch (Exception e) {
                return false;
            }
        }
        if ((Integer.parseInt(data.charAt(0) + "") > 2) ||
                (Integer.parseInt(data.charAt(3) + "") > 2) ||
                (Integer.parseInt(data.charAt(6) + "") > 2) ||
                (Integer.parseInt(data.charAt(9) + "") > 1)) return false;
        return true;
    }

    public boolean hasAccess() {
        return access.isChecked();
    }

    private String convertTo3DigitsString(int number){
        String num = number + "";
        while (num.length() < 3) {
            num = "0" + num;
        }
        return num;
    }

    private String getHasAccessString() {
        if (hasAccess()) return "1";
        else return "0";
    }


}
