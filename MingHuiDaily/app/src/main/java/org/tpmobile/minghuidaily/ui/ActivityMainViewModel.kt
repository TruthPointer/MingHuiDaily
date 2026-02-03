package org.tpmobile.minghuidaily.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.tpmobile.minghuidaily.data.TaskInfo
import org.tpmobile.minghuidaily.util.ext.CoroutineViewModel

class ActivityMainViewModel: CoroutineViewModel() {

    private val _taskInfo = MutableLiveData<TaskInfo>()

    fun setProgress(taskInfo: TaskInfo){
        _taskInfo.value = taskInfo
    }

    fun getProgress(): MutableLiveData<TaskInfo> {
        return _taskInfo
    }

    class Factory() : ViewModelProvider.NewInstanceFactory() {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityMainViewModel() as T
        }
    }
}