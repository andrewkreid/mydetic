package net.ghosttrails.www.mydetic.migration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.ghosttrails.www.mydetic.MyDeticConfig
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Calendar

class MigrationViewModel : MigrationJobListener, ViewModel() {

    val fromApi = mutableStateOf(MyDeticConfig.DS_RESTAPI)
    val toApi = mutableStateOf(MyDeticConfig.DS_FIREBASE)
    val overwriteExisting : MutableState<Boolean> = mutableStateOf(false)
    val fromDate = mutableStateOf(Calendar.getInstance().timeInMillis)
    val toDate = mutableStateOf(Calendar.getInstance().timeInMillis)
    val buttonText = mutableStateOf("Start migration")
    val logEntries = mutableStateListOf<String>()

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
        logEntries.add(logEntry)
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
        var currentDate = Instant.ofEpochMilli(fromDate.value)
        val endDate = Instant.ofEpochMilli(toDate.value)
        clearLogEntries()
        addLogEntry("Start Migration")
        while(currentDate.isBefore(endDate)) {
            if (isCancelled()) {
                addLogEntry("Cancelled")
                break
            }
            addLogEntry("migrating " + currentDate.toString())
            Thread.sleep(1000)
            currentDate = currentDate.plus(1, ChronoUnit.DAYS)
        }
        addLogEntry("Migration complete")
        migrationComplete()
        return
    }
}