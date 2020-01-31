package com.ora.android.eyecup.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collection;

public class ProtocolRevisionParser {

    private ProtocolRevision mProtRev;
    private ProtocolRevEvent mProtRevEvt;
    private EventActivity mProtRevEvtAct;

    ProtocolRevisionParser() {
        mProtRev = new ProtocolRevision();
        mProtRevEvt = new ProtocolRevEvent();
        mProtRevEvtAct = new EventActivity();
    }

    ProtocolRevisionParser(String strJSON, ProtocolRevision protRev) {
        mProtRev = new ProtocolRevision();
        mProtRevEvt = new ProtocolRevEvent();
        mProtRevEvtAct = new EventActivity();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean ParseProtRev(String strJSON, ProtocolRevision protRev) {
        boolean bRet = false;
        Gson gson = new Gson();
        Collection collection = new ArrayList();
        collection.add("hello");
        collection.add(5);
        mProtRev = new ProtocolRevision();
//        collection.add(new ProtocolRevision("GREETINGS", "guest"));
        String json = gson.toJson(collection);
//        System.out.println("Using Gson.toJson() on a raw collection: " + json);
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();
        String message = gson.fromJson(array.get(0), String.class);
        int number = gson.fromJson(array.get(1), int.class);
        ProtocolRevision event = gson.fromJson(array.get(2), ProtocolRevision.class);
//        System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message, number, event);

        return bRet;
    }
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void main(String[] args) {
        Gson gson = new Gson();
        Collection collection = new ArrayList();
        collection.add("hello");
        collection.add(5);
//        collection.add(new ProtocolRevision("GREETINGS", "guest"));
        String json = gson.toJson(collection);
//        System.out.println("Using Gson.toJson() on a raw collection: " + json);
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(json).getAsJsonArray();
        String message = gson.fromJson(array.get(0), String.class);
        int number = gson.fromJson(array.get(1), int.class);
        ProtocolRevision event = gson.fromJson(array.get(2), ProtocolRevision.class);
//        System.out.printf("Using Gson.fromJson() to get: %s, %d, %s", message, number, event);
    }
}