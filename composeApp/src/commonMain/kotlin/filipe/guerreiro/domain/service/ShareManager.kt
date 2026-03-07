package filipe.guerreiro.domain.service

expect class ShareManager() {
    fun shareXlsxFile(filename: String, content: ByteArray)
}
