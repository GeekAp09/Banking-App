package com.mobile360.banking

import com.mobile360.banking.data.StorageInfo
import com.mobile360.banking.data.clearDirContents
import com.mobile360.banking.data.formatBytes
import com.mobile360.banking.data.measureStorage
import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// CR-009: storage breakdown and clear cache
class Cr009StorageCacheTest {
    private fun tmp(): File = Files.createTempDirectory("cr009").toFile()

    @Test
    fun measuresCategoriesIncludingSidecars() {
        val root = tmp()
        val db = File(root, "banking.db").apply { writeBytes(ByteArray(100)) }
        File(root, "banking.db-wal").writeBytes(ByteArray(50))
        val cache = File(root, "cache").apply { mkdirs() }
        File(cache, "a").writeBytes(ByteArray(30))
        val other = File(root, "ext").apply { mkdirs() }
        File(other, "b").writeBytes(ByteArray(20))
        val info = measureStorage(db, cache, listOf(other))
        assertEquals(StorageInfo(150, 30, 20), info)
        assertEquals(200, info.total)
    }

    @Test
    fun clearEmptiesOnlyCache() {
        val root = tmp()
        val db = File(root, "banking.db").apply { writeBytes(ByteArray(10)) }
        val cache = File(root, "cache").apply { mkdirs() }
        File(cache, "sub").mkdirs()
        File(cache, "sub/x").writeBytes(ByteArray(5))
        assertTrue(clearDirContents(cache))
        assertEquals(0, cache.listFiles()!!.size)
        assertTrue(db.exists())
        assertEquals(0, measureStorage(db, cache, emptyList()).cache)
        assertTrue(clearDirContents(cache))
    }

    @Test
    fun percentagesAddTo100() {
        val p = StorageInfo(1, 1, 1).percentages()
        assertEquals(100, p.first + p.second + p.third)
        assertEquals(Triple(0, 0, 0), StorageInfo(0, 0, 0).percentages())
        assertEquals("0 B", formatBytes(0))
    }
}
