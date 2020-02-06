package com.ora.android.eyecup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;

public class Global { //use for all common activity functionality //edit: combine with "Globals"
    private static final Global global = new Global();
    private AppCompatActivity[] navChain = null;
    private Global() { }
    public static Global Context(AppCompatActivity activity) { //call once in every activity to initialize a class variable //edit: make sure all activities call this
        if (activity == null)
            return null; //default for method misuse
        boolean activityExists = false;
        if (global.navChain == null)
            global.navChain = new AppCompatActivity[0];
        AppCompatActivity[] newChain = new AppCompatActivity[global.navChain.length + 1];
        for (int i = 0; i < global.navChain.length; i++)
            if (!global.navChain[i].equals(activity))
                newChain[i] = global.navChain[i];
            else {
                activityExists = true;
                break;
            }
        if (!activityExists) {
            newChain[global.navChain.length] = activity;
            global.navChain = newChain;
        }
        return global;
    }
    public static Context GetAppContext() {
//JLR
        if (global.navChain == null)
            global.navChain = new AppCompatActivity[0];
//end JLR
        if (global.navChain.length != 0)
            return global.navChain[0].getApplicationContext();

        return null; //default for method misuse
    }
    public void Back() { //call once in every activity just before "onBackPressed" is called (by overriding it)
        if (navChain != null && navChain.length != 0) {
            AppCompatActivity[] newChain = new AppCompatActivity[navChain.length - 1];
            for (int i = 0; i < newChain.length; i++)
                newChain[i] = navChain[i];
            navChain = newChain;
        }
    }
    public void Navigate(Class toActivityClass) {
        if (toActivityClass == null)
            return; //returned for method misuse

        try {
            GetCurrActivity().startActivity(new Intent(GetCurrActivity(), toActivityClass));
        } catch (NullPointerException e) {
            Log.e("Global:Navigate:Ex", e.toString());
            //todo handle
        }
    }
    public <T extends View> void InitInputView(T view, String[] inputs, final Function<Integer, Integer> output) { //pass a "ListView" for "view", and the "output" should return the previous item index in exchange for the current index; pass a "SeekBar" for "view", and the "output" can use the slider value; pass an empty array for "inputs" if "view" has no input to specify; return "null" in the "output" function if the control needs no output value
        if (view == null || inputs == null || output == null)
            return; //returned for method misuse
        if (view instanceof ListView) {
            final ListView listView = (ListView)view;

            //todo Warning:(67, 58) Argument 'GetCurrActivity()' might be null
            listView.setAdapter(new ArrayAdapter<>(GetCurrActivity(), android.R.layout.simple_list_item_1, inputs));
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    View lastItem = listView.getChildAt(output.apply(position));
                    if (lastItem != null)
                        lastItem.setBackgroundColor(Color.TRANSPARENT);
                    view.setBackgroundColor(Color.YELLOW); //color that appears when an item in the list is clicked
                }
            });
        }
        else if (view instanceof SeekBar) {
            ((SeekBar)view).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean b) { output.apply(progress); }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
        }
    }
    public <T extends View> T GetView(int id) {

        //todo Warning:(91, 16) Unchecked cast: 'android.view.View' to 'T'
        //todo does this work from fragment?
        //todo Warning findViewById may produce NullPointerException
        return (T)GetCurrActivity().findViewById(id);
    }
    public static Global GetGlobal() {
        return global;
    }
    public AppCompatActivity GetCurrActivity() {
        if (navChain != null && navChain.length != 0)
            return navChain[navChain.length - 1];
        return null; //default for method misuse
    }
}