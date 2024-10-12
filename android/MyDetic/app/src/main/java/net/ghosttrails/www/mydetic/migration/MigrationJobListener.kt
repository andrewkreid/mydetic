package net.ghosttrails.www.mydetic.migration

/** Callbacks from the background migration job to the UI. */
interface MigrationJobListener {

    fun addLogEntry(logEntry : String)

    fun migrationComplete()

    /** Background job calls this to see if the migration has been cnacelled. */
    fun isCancelled() : Boolean
}