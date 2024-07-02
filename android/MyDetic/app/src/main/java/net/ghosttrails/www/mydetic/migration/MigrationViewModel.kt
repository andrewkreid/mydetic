package net.ghosttrails.www.mydetic.migration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import net.ghosttrails.www.mydetic.MyDeticConfig
import java.util.Calendar

class MigrationViewModel : ViewModel() {

    val fromApi = mutableStateOf(MyDeticConfig.DS_RESTAPI)
    val toApi = mutableStateOf(MyDeticConfig.DS_FIREBASE)
    val overwriteExisting : MutableState<Boolean> = mutableStateOf(false)
    val fromDate = mutableStateOf(Calendar.getInstance())
    val toDate = mutableStateOf(Calendar.getInstance())

    fun getApiList(): List<String> {
        val apiList = mutableListOf<String>()
        apiList.add(MyDeticConfig.DS_RESTAPI)
        apiList.add(MyDeticConfig.DS_FIREBASE)
        apiList.add(MyDeticConfig.DS_INRAM)

        return apiList
    }
}