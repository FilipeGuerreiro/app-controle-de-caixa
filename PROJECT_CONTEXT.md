# ControleDeCaixa — Contexto do Projeto

## Visão Geral
Aplicativo mobile multiplataforma (Android/iOS) para gestão de caixa simplificada, com foco em uso rápido, offline e intuitivo. O projeto usa Kotlin Multiplatform (KMP) para compartilhar lógica entre plataformas e Compose Multiplatform para a UI.

## Stack Principal
- Kotlin Multiplatform (KMP) com módulos compartilhados em `commonMain`.
- Compose Multiplatform + Material 3 para UI.
- Room KMP + BundledSQLiteDriver (local-first / offline-first).
- Koin para DI.
- Coroutines & Flow para assíncrono e reatividade.
- kotlinx-datetime (na prática, `kotlin.time.Instant` via Room Converters).

## Plataformas Suportadas
- Android e iOS (principais).
- Desktop/JVM para testes rápidos de lógica (via `compose.desktop`).

## Estrutura do Código (commonMain)
Base package: `filipe.guerreiro`

- `data/`
  - `local/`: Room Database, DAOs e repository local (`CashRepositoryImpl`).
  - `UserPreferences`: contrato de preferências de usuário.
- `domain/`
  - `model/`: entidades e tipos (`CashSession`, `Transaction`, etc.).
  - `repository/`: interfaces de domínio (`CashRepository`).
- `ui/`: telas e componentes (por feature: `home`, `opening`, `cash`, etc.).
- `core/`: navegação utilitária e fluxo de startup.
- `di/`: módulos Koin compartilhados.

## Módulos Koin (DI)
- `appModule` (common): ViewModels, Repository, DAOs e Database.
- `androidModule` + `androidDatabaseModule`: `AndroidUserPreferences` e Room builder com arquivo `caixa.db`.
- `iosModule` + `iosDatabaseModule`: `IosUserPreferences` e Room builder com arquivo `caixa_flavia.db`.

## Modelagem de Dados
### CashSession
- `openingTimeStamp`, `closingTimeStamp`, `initialAmount`, `status` (`OPEN`/`CLOSED`).
- Valores monetários são Long em centavos.

### Transaction
- Vinculada a uma sessão (`sessionId`).
- `type`: `INCOME` / `EXPENSE`.
- `amount` em centavos, `timestamp` e `description`.

### Saldo (SessionBalance)
Saldo não é persistido. É calculado via Flow:
`saldoAtual = initialAmount + Σ(entradas) - Σ(saídas)`

## Regras de Negócio Importantes
- Só pode existir um caixa aberto por vez.
  - `CashRepository.createSession` verifica sessão ativa.
- Valor sugerido de abertura vem da última sessão fechada:
  - `initial + totalIn - totalOut`.
- Observação reativa: use `Flow` para refletir mudanças do DB na UI.

## Persistência / Preferências
- Android: DataStore (key `user_registered`).
- iOS: NSUserDefaults (key `user_registered`).


## Convenções e Observações
- Formatação de moeda em `CurrencyExtensions.kt` (ex.: `R$ 12,50`).
- Use `toCents()` para normalizar input de valor.
- Room usa `Converters` para `Instant`, `TransactionType` e `CashStatusType`.
