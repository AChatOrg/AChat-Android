package com.hyapp.achat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hyapp.achat.model.ChatRepo
import com.hyapp.achat.model.UsersRoomsRepo
import com.hyapp.achat.model.entity.*
import com.hyapp.achat.model.objectbox.ContactDao
import com.hyapp.achat.model.Preferences
import com.hyapp.achat.model.objectbox.UserDao
import com.hyapp.achat.view.EventActivity
import com.hyapp.achat.viewmodel.service.SocketService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

@ExperimentalCoroutinesApi
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val _contactsLive = MutableLiveData<ContactList>()
    val contactsLive = _contactsLive as LiveData<ContactList>

    private val _usersLive = MutableLiveData<Resource<SortedList<User>>>()
    val usersLive = _usersLive as LiveData<Resource<SortedList<User>>>

    private val _roomsLive = MutableLiveData<Resource<SortedList<Room>>>()
    val roomsLive = _roomsLive as LiveData<Resource<SortedList<Room>>>

    private var stopTypingJob: Job? = null
    private var refreshOnlineTimeJob: Job? = null

    init {
        UserLive.value = UserDao.get(User.CURRENT_USER_ID)
        val context = getApplication<Application>().applicationContext
        SocketService.start(context, Preferences.instance().loginInfo)
        loadContacts()
        reloadUsers()
        reloadRooms()
        observeContacts()
        observeUsers()
    }

    private fun loadContacts() {
        _contactsLive.value = ContactList(ContactDao.all())
    }

    fun reloadUsers() {
        _usersLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRoomsRepo.requestUsers().collect { userList ->
                _usersLive.value = Resource.success(userList)
            }
        }
    }

    fun reloadRooms() {
        _roomsLive.value = Resource.loading(null)
        viewModelScope.launch {
            UsersRoomsRepo.requestRooms().collect { roomList ->
                _roomsLive.value = Resource.success(roomList)
            }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            ChatRepo.contactFlow.collect { pair ->
                when (pair.first) {
                    ChatRepo.CONTACT_PUT -> putContact(pair.second)
                    ChatRepo.CONTACT_UPDATE -> updateContact(pair.second)
                    ChatRepo.CONTACT_TYPING -> signalTyping(pair.second)
                }
            }
        }
    }

    private fun observeUsers() {
        viewModelScope.launch {
            launch {
                UsersRoomsRepo.flow.collect { pair ->
                    when (pair.first) {
                        UsersRoomsRepo.USER_CAME -> {
                            usersLive.value?.data?.let {
                                it.add(pair.second as User)
                                _usersLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.USER_LEFT -> {
                            usersLive.value?.data?.let {
                                it.remove(pair.second as User)
                                _usersLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.ROOM_CREATE -> {
                            roomsLive.value?.data?.let {
                                it.add(pair.second as Room)
                                _roomsLive.value = Resource.success(it)
                            }
                        }
                        UsersRoomsRepo.ROOM_DELETE -> {
                            roomsLive.value?.data?.let {
                                it.remove(pair.second as Room)
                                _roomsLive.value = Resource.success(it)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun putContact(contact: Contact) {
        val contactList = _contactsLive.value ?: ContactList()
        contactList.putFirst(contact)
        _contactsLive.value = contactList
    }

    private fun updateContact(contact: Contact) {
        val contactList = _contactsLive.value ?: ContactList()
        val updated = contactList.update(contact)
        if (updated) {
            _contactsLive.value = contactList
        }
    }

    private fun signalTyping(contact: Contact) {
        _contactsLive.value?.let { list ->
            var updated = list.update(contact.apply { isTyping = true })
            if (updated) {
                _contactsLive.value = list
            }
            stopTypingJob?.cancel()
            stopTypingJob = viewModelScope.launch(ioDispatcher) {
                delay(3000)
                ContactDao.get(contact.uid)?.let {
                    updated = list.update(it.apply { isTyping = false })
                    if (updated) {
                        _contactsLive.postValue(list)
                    }
                }
            }
        }
    }

    private fun refreshOnlineTimes() {
        _contactsLive.value?.let {
            it.refreshOnlineTimes()
            _contactsLive.value = it
        }
    }

    fun activityStarted() {
        if (EventActivity.startedActivities > 0) {
            ChatRepo.sendOnlineTime(true)
        }
        refreshOnlineTimeJob?.cancel()
        refreshOnlineTimeJob = viewModelScope.launch {
            while (true) {
                delay(60000)
                refreshOnlineTimes()
            }
        }
    }

    fun activityStopped() {
        if (EventActivity.startedActivities < 1) {
            ChatRepo.sendOnlineTime(false)
        }
        refreshOnlineTimeJob?.cancel()
    }
}