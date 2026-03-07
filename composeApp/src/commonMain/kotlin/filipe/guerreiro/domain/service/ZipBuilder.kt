package filipe.guerreiro.domain.service

/**
 * A minimal, pure Kotlin builder for creating uncompressed ZIP files (Store method).
 * It writes data directly into a ByteArray, which is sufficient for generating
 * small XLSX documents without external compression libraries.
 */
class ZipBuilder {
    private val entries = mutableListOf<ZipEntry>()

    private class ZipEntry(
        val name: String,
        val content: ByteArray,
        var crc32: UInt = 0u,
        var offset: Int = 0
    )

    fun addEntry(name: String, content: String) {
        addEntry(name, content.encodeToByteArray())
    }

    fun addEntry(name: String, content: ByteArray) {
        val entry = ZipEntry(name, content)
        entry.crc32 = calculateCrc32(content)
        entries.add(entry)
    }

    fun build(): ByteArray {
        var totalSize = 0
        
        // Calculate offsets and total size
        var currentOffset = 0
        for (entry in entries) {
            entry.offset = currentOffset
            val headerSize = 30 + entry.name.encodeToByteArray().size
            currentOffset += headerSize + entry.content.size
            totalSize += headerSize + entry.content.size
        }
        
        val centralDirectoryOffset = currentOffset
        
        // Calculate central directory size
        for (entry in entries) {
            val cdSize = 46 + entry.name.encodeToByteArray().size
            totalSize += cdSize
            currentOffset += cdSize
        }
        
        // End of central directory record
        totalSize += 22

        val buffer = ByteArray(totalSize)
        var pos = 0

        // 1. Write Local File Headers and Data
        for (entry in entries) {
            val nameBytes = entry.name.encodeToByteArray()
            
            // Signature
            buffer.writeIntLittleEndian(pos, 0x04034b50); pos += 4
            // Version needed to extract (10 = 1.0)
            buffer.writeShortLittleEndian(pos, 10); pos += 2
            // General purpose bit flag
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Compression method (0 = stored)
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Last mod file time & date (dummy)
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // CRC-32
            buffer.writeIntLittleEndian(pos, entry.crc32.toInt()); pos += 4
            // Compressed size
            buffer.writeIntLittleEndian(pos, entry.content.size); pos += 4
            // Uncompressed size
            buffer.writeIntLittleEndian(pos, entry.content.size); pos += 4
            // File name length
            buffer.writeShortLittleEndian(pos, nameBytes.size); pos += 2
            // Extra field length
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            
            // File name
            nameBytes.copyInto(buffer, pos)
            pos += nameBytes.size
            
            // File data
            entry.content.copyInto(buffer, pos)
            pos += entry.content.size
        }

        // 2. Write Central Directory
        val centralDirectorySize = pos - centralDirectoryOffset
        for (entry in entries) {
            val nameBytes = entry.name.encodeToByteArray()
            
            // Signature
            buffer.writeIntLittleEndian(pos, 0x02014b50); pos += 4
            // Version made by
            buffer.writeShortLittleEndian(pos, 20); pos += 2
            // Version needed to extract
            buffer.writeShortLittleEndian(pos, 10); pos += 2
            // General purpose
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Compression method (0 = stored)
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Last mod time & date
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // CRC-32
            buffer.writeIntLittleEndian(pos, entry.crc32.toInt()); pos += 4
            // Compressed size
            buffer.writeIntLittleEndian(pos, entry.content.size); pos += 4
            // Uncompressed size
            buffer.writeIntLittleEndian(pos, entry.content.size); pos += 4
            // File name length
            buffer.writeShortLittleEndian(pos, nameBytes.size); pos += 2
            // Extra field length
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // File comment length
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Disk number start
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // Internal file attributes
            buffer.writeShortLittleEndian(pos, 0); pos += 2
            // External file attributes
            buffer.writeIntLittleEndian(pos, 0); pos += 4
            // Relative offset of local header
            buffer.writeIntLittleEndian(pos, entry.offset); pos += 4
            
            // File name
            nameBytes.copyInto(buffer, pos)
            pos += nameBytes.size
        }

        val actualCdSize = pos - centralDirectoryOffset

        // 3. End of Central Directory Record
        // Signature
        buffer.writeIntLittleEndian(pos, 0x06054b50); pos += 4
        // Number of this disk
        buffer.writeShortLittleEndian(pos, 0); pos += 2
        // Disk where CD starts
        buffer.writeShortLittleEndian(pos, 0); pos += 2
        // Number of CD records on this disk
        buffer.writeShortLittleEndian(pos, entries.size); pos += 2
        // Total number of CD records
        buffer.writeShortLittleEndian(pos, entries.size); pos += 2
        // Size of CD
        buffer.writeIntLittleEndian(pos, actualCdSize); pos += 4
        // Offset of CD w.r.t starting disk
        buffer.writeIntLittleEndian(pos, centralDirectoryOffset); pos += 4
        // ZIP file comment length
        buffer.writeShortLittleEndian(pos, 0)
        
        return buffer
    }

    private fun ByteArray.writeIntLittleEndian(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
        this[offset + 2] = ((value ushr 16) and 0xFF).toByte()
        this[offset + 3] = ((value ushr 24) and 0xFF).toByte()
    }

    private fun ByteArray.writeShortLittleEndian(offset: Int, value: Int) {
        this[offset] = (value and 0xFF).toByte()
        this[offset + 1] = ((value ushr 8) and 0xFF).toByte()
    }

    private fun calculateCrc32(data: ByteArray): UInt {
        var crc = 0xFFFFFFFFu
        for (b in data) {
            crc = crc xor (b.toUInt() and 0xFFu)
            for (j in 0..7) {
                crc = if ((crc and 1u) != 0u) {
                    (crc shr 1) xor 0xEDB88320u
                } else {
                    crc shr 1
                }
            }
        }
        return crc xor 0xFFFFFFFFu
    }
}
