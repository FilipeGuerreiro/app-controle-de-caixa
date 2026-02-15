# Diretrizes de Tema e Estilo (Design System)

## 1. Filosofia Visual: "Confiança & Energia"
O app foge do padrão "verde planilha" para adotar uma identidade moderna de Fintech, garantindo legibilidade extrema sob luz solar.

## 2. Paleta de Cores (Material Design 3)

### A. Cores Estruturais (Sistema)
* **Primary (Azul Profundo - #005AC1):**
    * *Uso:* TopAppBar, BottomNavigation, Botões de Confirmação ("Salvar"), Checkboxes ativos.
    * *Significado:* Institucional, Segurança, Sistema Operacional.
* **Background/Surface (Branco/Off-white):**
    * *Uso:* Fundo de telas e cartões. Deve ser limpo para alto contraste.

### B. Cores de Ação (Call-to-Action)
* **Tertiary (Laranja Terracota - #984816):**
    * *Uso EXCLUSIVO:* Floating Action Button (FAB) "Nova Venda", Botões de destaque urgente, Ícones de comida.
    * *Significado:* Apetite, Urgência, Ação Principal.

### C. Cores Semânticas (Dados Financeiros)
**IMPORTANTE:** Não use a cor Primary (Azul) para representar dinheiro. Use as extensões do tema.

* **Profit (Verde Customizado - #006C4C):**
    * *Acesso no Código:* `MaterialTheme.financial.profit`
    * *Uso:* Textos de valores positivos (ex: "+ R$ 15,00"), Ícones de seta para cima.
* **Error/Loss (Vermelho - #BA1A1A):**
    * *Acesso no Código:* `MaterialTheme.colorScheme.error`
    * *Uso:* Textos de valores negativos, Despesas, Alertas de erro.

## 3. Tipografia
Utilize a escala padrão do Material 3, mas priorize pesos **Medium** ou **Bold** para valores monetários para melhorar a leitura rápida.