package me.vanpetegem.accentor.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val _isOpen = MutableLiveData<Boolean>(false)
    val isOpen: LiveData<Boolean> = _isOpen

    private val _showQueue = MutableLiveData<Boolean>(false)
    val showQueue: LiveData<Boolean> = _showQueue

    fun toggleQueue() {
        _showQueue.value = !(_showQueue.value ?: false)
    }

    fun setOpen(isOpen: Boolean) {
        _isOpen.value = isOpen
    }
}
