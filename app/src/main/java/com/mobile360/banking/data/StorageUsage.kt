package com.mobile360.banking.data

import java.io.File

// CR-009: pure storage measuring / cache clearing helpers (no Android context needed).
data class StorageInfo(val database: Long, val cache: Long, val other: Long) {
    val total: Long get() = database + cache + other

    /** Percentages of database, cache, other; they add up to 100 (all 0 when empty). */
    fun percentages(): Triple<Int, Int, Int> {
        if (total <= 0L) return Triple(0, 0, 0)
        val db = (database * 100 / total).toInt()
        val ca = (cache * 100 / total).toInt()
        return Triple(db, ca, 100 - db - ca)
    }
}

fun dirSize(file: File?): Long {
    if (file == null || !file.exists()) return 0L
    if (file.isFile) return file.length()
    return file.listFiles()?.sumOf { dirSize(it) } ?: 0L
}

/** Size of the database file plus its -wal, -shm and -journal sidecars. */
fun databaseSize(dbFile: File): Long =
    listOf("", "-wal", "-shm", "-journal").sumOf { dirSize(File(dbFile.path + it)) }

fun measureStorage(dbFile: File, cacheDir: File?, otherDirs: List<File?>): StorageInfo =
    StorageInfo(databaseSize(dbFile), dirSize(cacheDir), otherDirs.sumOf { dirSize(it) })

/** Deletes only the children of [cacheDir]; returns false if something could not be deleted. */
fun clearDirContents(cacheDir: File?): Boolean {
    if (cacheDir == null || !cacheDir.exists()) return true
    return cacheDir.listFiles()?.map { it.deleteRecursively() }?.all { it } ?: true
}

fun formatBytes(bytes: Long): String = when {
    bytes < 1024L -> "$bytes B"
    bytes < 1024L * 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
    else -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024.0 * 1024))
}
