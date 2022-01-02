package com.hyapp.achat.bl.socket;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hyapp.achat.Config;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.gson.PeopleDeserializer;
import com.hyapp.achat.model.Resource;
import com.hyapp.achat.model.SortedList;

import java.util.Iterator;
import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PeopleApi {

    private static PeopleApi instance;

    public static PeopleApi singleton() {
        if (instance == null) {
            instance = new PeopleApi();
        }
        return instance;
    }

    private final MutableLiveData<Resource<SortedList<People>>> peopleLive;


    public PeopleApi() {
        peopleLive = new MutableLiveData<>();
    }

    public void listen(Socket socket) {
        socket.on(Config.ON_PEOPLE, onPeopleList);
        socket.on(Config.ON_USER_CAME, onUserCame);
        socket.on(Config.ON_USER_LEFT, onUserLeft);
    }

    public void requestPeople(Socket socket) {
        socket.emit(Config.ON_PEOPLE);
    }

    private final Emitter.Listener onPeopleList = args -> {
        List<People> people = new GsonBuilder()
                .registerTypeAdapter(People.class, new PeopleDeserializer())
                .create()
                .fromJson(args[0].toString(), new TypeToken<List<People>>() {
                }.getType());
        SortedList<People> peopleList = new SortedList<>(People::compare);
        peopleList.addAll(people);
        getPeopleLive().postValue(Resource.add(peopleList, Resource.INDEX_ALL));
    };

    private final Emitter.Listener onUserCame = args -> {
        People people = new GsonBuilder()
                .registerTypeAdapter(People.class, new PeopleDeserializer())
                .create().fromJson(args[0].toString(), People.class);
        Resource<SortedList<People>> value = getPeopleLive().getValue();
        if (value != null) {
            SortedList<People> peopleList = value.data;
            if (peopleList != null) {
                peopleList.add(people);
                int index = peopleList.indexOf(people);
                getPeopleLive().postValue(Resource.add(peopleList, index));
            }
        }
    };

    private final Emitter.Listener onUserLeft = args -> {
        String id = args[0].toString();
        Resource<SortedList<People>> value = getPeopleLive().getValue();
        if (value != null) {
            SortedList<People> peopleList = value.data;
            if (peopleList != null) {
                int index = remove(peopleList, id);
                if (index != -1) {
                    getPeopleLive().postValue(Resource.remove(peopleList, index));
                }
            }
        }
    };

    public MutableLiveData<Resource<SortedList<People>>> getPeopleLive() {
        return peopleLive;
    }

    private int remove(SortedList<People> peopleList, String id) {
        int i = 0;
        for (Iterator<People> iterator = peopleList.iterator(); iterator.hasNext(); i++) {
            if (iterator.next().getKey().getUid().equals(id)) {
                iterator.remove();
                return i;
            }
        }
        return -1;
    }
}
