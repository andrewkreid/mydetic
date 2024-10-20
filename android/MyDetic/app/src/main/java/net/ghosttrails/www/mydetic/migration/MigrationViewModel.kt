package net.ghosttrails.www.mydetic.migration

import android.app.Application
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ghosttrails.www.mydetic.MemoryAppState
import net.ghosttrails.www.mydetic.MyDeticConfig
import net.ghosttrails.www.mydetic.api.FirebaseMemoryApi
import net.ghosttrails.www.mydetic.api.MemoryApi.SingleMemoryGetListener
import net.ghosttrails.www.mydetic.api.MemoryApi.SingleMemoryPutListener
import net.ghosttrails.www.mydetic.api.MemoryData
import net.ghosttrails.www.mydetic.api.RestfulMemoryApi
import net.ghosttrails.www.mydetic.exceptions.MyDeticException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar

class MigrationViewModel(application: Application) : MigrationJobListener,
    SingleMemoryGetListener, SingleMemoryPutListener,
    AndroidViewModel(
        application
    ) {

    val fromApi = mutableStateOf(MyDeticConfig.DS_RESTAPI)
    val toApi = mutableStateOf(MyDeticConfig.DS_FIREBASE)
    val overwriteExisting: MutableState<Boolean> = mutableStateOf(false)
    val fromDate = mutableStateOf(Calendar.getInstance().timeInMillis)
    val toDate = mutableStateOf(Calendar.getInstance().timeInMillis)
    val buttonText = mutableStateOf("Start migration")
    val logEntries = mutableStateListOf<String>()

    var restApi : RestfulMemoryApi? = null
    var fireBaseApi : FirebaseMemoryApi? = null
    var currentDate : Instant = Instant.now()
    var endDate : Instant = Instant.now()

    var isMigrating = false

    fun getApiList(): List<String> {
        val apiList = mutableListOf<String>()
        apiList.add(MyDeticConfig.DS_RESTAPI)
        apiList.add(MyDeticConfig.DS_FIREBASE)
        apiList.add(MyDeticConfig.DS_INRAM)

        return apiList
    }

    fun startMigrationClicked() {
        if (isMigrating) {
            buttonText.value = "Start migration"
        } else {
            // TODO : start the migration
            viewModelScope.launch(Dispatchers.IO) { startMigration() }
            buttonText.value = "Cancel migration"
        }
        isMigrating = !isMigrating
    }

    override fun addLogEntry(logEntry: String) {
        logEntries.add(0, logEntry)
    }

    fun clearLogEntries() {
        logEntries.clear()
    }

    override fun migrationComplete() {
        isMigrating = false
        buttonText.value = "Start migration"
    }

    override fun isCancelled(): Boolean {
        return !isMigrating
    }

    fun startMigration() {
        currentDate = Instant.ofEpochMilli(fromDate.value)
        endDate = Instant.ofEpochMilli(toDate.value)
        val config = MemoryAppState.getInstance().config
        restApi = RestfulMemoryApi(getApplication(), config)
        fireBaseApi = FirebaseMemoryApi()
        clearLogEntries()
        addLogEntry("Start Migration")

        migrateNextDate()
//        while(currentDate.isBefore(endDate)) {
//            if (isCancelled()) {
//                addLogEntry("Cancelled")
//                break
//            }
//            addLogEntry("migrating " + currentDate.toString())
//            Thread.sleep(1000)
//            currentDate = currentDate.plus(1, ChronoUnit.DAYS)
//        }
//        addLogEntry("Migration complete")
//        migrationComplete()
//        return
    }

    override fun onApiGetError(exception: MyDeticException?) {
        addLogEntry("GET ERROR: " + exception?.message)
        currentDate = currentDate.plus(1, ChronoUnit.DAYS)
        migrateNextDate()
    }

    override fun onApiGetResponse(memory: MemoryData?) {
        val memoryText = memory?.memoryText ?: "";
        addLogEntry("Got Memory "
                + memory?.memoryDate?.format(DateTimeFormatter.BASIC_ISO_DATE)
                + " "
                + memoryText.substring(0, Math.min(memoryText.length, 10)))
        fireBaseApi?.putMemory(MemoryAppState.getInstance().config.userName, memory, this)
    }

    override fun onApiPutResponse(memory: MemoryData?) {
        addLogEntry("PUT " + memory?.memoryDate?.format(DateTimeFormatter.BASIC_ISO_DATE))
        currentDate = currentDate.plus(1, ChronoUnit.DAYS)
        migrateNextDate()
    }

    override fun onApiPutError(exception: MyDeticException?) {
        addLogEntry("PUT FAIL " + exception?.message)
        currentDate = currentDate.plus(1, ChronoUnit.DAYS)
        migrateNextDate()
    }

    fun migrateNextDate() {
        if (currentDate.isBefore(endDate)) {
            if (isCancelled()) {
                addLogEntry("Cancelled")
                migrationComplete()
            }
            val localDate = currentDate.atZone(ZoneId.systemDefault()).toLocalDate()
            restApi!!.getMemory(MemoryAppState.getInstance().config.userName, localDate, this)
        } else {
            migrationComplete()
        }
    }
}