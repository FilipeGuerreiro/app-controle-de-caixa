# Contexto do Projeto: Controle de Caixa (Microempreendedor)

## 1. Visão Geral
Este é um aplicativo multiplataforma (Android/KMP) focado em gestão financeira simplificada para microempreendedores individuais que trabalham com vendas rápidas na rua (ambulantes, food trucks, feirantes).

## 2. Stack Tecnológica
* **Linguagem:** Kotlin.
* **Framework:** Kotlin Multiplatform (KMP) + Jetpack Compose (UI).
* **Arquitetura:** MVVM com Clean Architecture (camadas separadas de Domain, Data e UI).
* **Banco de Dados:** Room ou SQLDelight (Local-first, funciona offline).

## 3. Glossário do Domínio
* **Entrada:** Dinheiro que entra (Venda).
* **Saída:** Dinheiro que sai (Compra de insumos, Gelo, Carvão).
* **Fechamento:** Resumo final do dia.