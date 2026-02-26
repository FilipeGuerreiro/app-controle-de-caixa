package filipe.guerreiro.domain.service

expect class ShareManager() {
    fun shareCsvFile(filename: String, content: String)
}
