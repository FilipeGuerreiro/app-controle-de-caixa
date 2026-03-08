package filipe.guerreiro.data.local

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection

/**
 * Migra bancos legados para o schema da versao 7 sem destruir dados locais.
 *
 * Estrategia:
 * - Criar tabelas/indices ausentes com IF NOT EXISTS.
 * - Adicionar colunas novas com valores padrao seguros (verifica via PRAGMA antes).
 * - Operacoes idempotentes para tolerar variacoes entre instalacoes antigas.
 *
 * NOTA: ALTER TABLE ... ADD COLUMN IF NOT EXISTS so existe no SQLite 3.35+.
 * Dispositivos Android antigos rodam versoes anteriores, entao usamos
 * PRAGMA table_info() para verificar a existencia antes do ALTER.
 */
private fun migrateToV7(connection: SQLiteConnection) {
    // Base: usuarios
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            name TEXT NOT NULL,
            businessName TEXT NOT NULL,
            remoteId TEXT
        )
        """.trimIndent()
    )
    connection.addColumnIfNotExists("users", "remoteId", "TEXT")

    // Caixa
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS cash_sessions (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            userId INTEGER NOT NULL,
            openingTimeStamp INTEGER NOT NULL,
            closingTimeStamp INTEGER,
            initialAmount INTEGER NOT NULL,
            dailyGoalAmount INTEGER,
            status TEXT NOT NULL DEFAULT 'OPEN',
            FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    connection.addColumnIfNotExists("cash_sessions", "dailyGoalAmount", "INTEGER")
    connection.addColumnIfNotExists("cash_sessions", "status", "TEXT NOT NULL DEFAULT 'OPEN'")
    connection.execSql("DROP INDEX IF EXISTS index_cash_sessions_userId")
    connection.execSql("CREATE INDEX IF NOT EXISTS index_cash_sessions_userId_openingTimeStamp ON cash_sessions(userId, openingTimeStamp)")

    // Categorias
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS categories (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            userId INTEGER NOT NULL,
            name TEXT NOT NULL,
            type TEXT NOT NULL,
            isActive INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    connection.addColumnIfNotExists("categories", "isActive", "INTEGER NOT NULL DEFAULT 1")
    connection.execSql("CREATE INDEX IF NOT EXISTS index_categories_userId ON categories(userId)")

    // Metodos de pagamento
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS payment_methods (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            userId INTEGER NOT NULL,
            name TEXT NOT NULL,
            isActive INTEGER NOT NULL DEFAULT 1,
            FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    connection.addColumnIfNotExists("payment_methods", "isActive", "INTEGER NOT NULL DEFAULT 1")
    connection.execSql("CREATE INDEX IF NOT EXISTS index_payment_methods_userId ON payment_methods(userId)")

    // Transacoes
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS transactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            sessionId INTEGER NOT NULL,
            categoryId INTEGER NOT NULL,
            paymentMethodId INTEGER NOT NULL,
            amount INTEGER NOT NULL,
            description TEXT NOT NULL,
            type TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            isDeleted INTEGER NOT NULL DEFAULT 0,
            FOREIGN KEY(sessionId) REFERENCES cash_sessions(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    connection.addColumnIfNotExists("transactions", "isDeleted", "INTEGER NOT NULL DEFAULT 0")
    connection.execSql("CREATE INDEX IF NOT EXISTS index_transactions_sessionId ON transactions(sessionId)")

    // Auditoria
    connection.execSql(
        """
        CREATE TABLE IF NOT EXISTS audit_logs (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            sessionId INTEGER NOT NULL,
            transactionId INTEGER NOT NULL,
            actionType TEXT NOT NULL,
            oldValue TEXT NOT NULL,
            newValue TEXT NOT NULL,
            reason TEXT NOT NULL,
            timestamp INTEGER NOT NULL,
            FOREIGN KEY(sessionId) REFERENCES cash_sessions(id) ON DELETE CASCADE,
            FOREIGN KEY(transactionId) REFERENCES transactions(id) ON DELETE CASCADE
        )
        """.trimIndent()
    )
    connection.execSql("CREATE INDEX IF NOT EXISTS index_audit_logs_sessionId ON audit_logs(sessionId)")
    connection.execSql("CREATE INDEX IF NOT EXISTS index_audit_logs_transactionId ON audit_logs(transactionId)")
}

private fun SQLiteConnection.execSql(sql: String) {
    prepare(sql).use { stmt ->
        stmt.step()
    }
}

/**
 * Verifica via PRAGMA table_info se a coluna ja existe antes de tentar ALTER TABLE.
 * Compativel com todas as versoes do SQLite.
 */
private fun SQLiteConnection.addColumnIfNotExists(
    table: String,
    column: String,
    definition: String,
) {
    val exists = prepare("PRAGMA table_info($table)").use { stmt ->
        var found = false
        while (stmt.step()) {
            // PRAGMA table_info retorna: cid, name, type, notnull, dflt_value, pk
            val colName = stmt.getText(1)
            if (colName.equals(column, ignoreCase = true)) {
                found = true
                break
            }
        }
        found
    }
    if (!exists) {
        execSql("ALTER TABLE $table ADD COLUMN $column $definition")
    }
}

private val MIGRATION_1_7 = object : Migration(1, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

private val MIGRATION_2_7 = object : Migration(2, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

private val MIGRATION_3_7 = object : Migration(3, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

private val MIGRATION_4_7 = object : Migration(4, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

private val MIGRATION_5_7 = object : Migration(5, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

private val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(connection: SQLiteConnection) = migrateToV7(connection)
}

val ALL_V7_MIGRATIONS = arrayOf(
    MIGRATION_1_7,
    MIGRATION_2_7,
    MIGRATION_3_7,
    MIGRATION_4_7,
    MIGRATION_5_7,
    MIGRATION_6_7,
)
