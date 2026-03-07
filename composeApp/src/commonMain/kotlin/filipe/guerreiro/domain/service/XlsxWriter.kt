package filipe.guerreiro.domain.service

enum class XlsxStyle(val id: Int) {
    NORMAL(0),
    HEADER(1),       // Azul Navy Fundo, Texto Branco Bold
    TITLE(2),        // Azul Navy Negrito
    PROFIT(3),       // Verde Texto
    LOSS(4),         // Vermelho Texto
    STRIPE(5),       // Fundo cinza claro
    STRIPE_PROFIT(6),// Fundo cinza claro + Verde Texto
    STRIPE_LOSS(7)   // Fundo cinza claro + Vermelho Texto
}

data class XlsxCell(
    val value: String,
    val style: XlsxStyle = XlsxStyle.NORMAL
)

/**
 * Minimal XLSX writer using a custom pure Kotlin ZipBuilder.
 * Generates a valid Office Open XML spreadsheet compatible with Excel and Google Sheets.
 * Now supports beautiful enterprise-level formatting.
 */
class XlsxWriter(
    private val sheetName: String,
    private val rows: List<List<XlsxCell>>
) {

    fun generateXlsxRawBytes(): ByteArray {
        val zip = ZipBuilder()

        zip.addEntry("[Content_Types].xml", contentTypes())
        zip.addEntry("_rels/.rels", rootRels())
        zip.addEntry("xl/workbook.xml", workbook())
        zip.addEntry("xl/_rels/workbook.xml.rels", workbookRels())
        zip.addEntry("xl/styles.xml", styles())
        zip.addEntry("xl/sharedStrings.xml", sharedStrings())
        zip.addEntry("xl/worksheets/sheet1.xml", worksheet())

        return zip.build()
    }

    // ---- Shared strings ----
    private val allStrings: List<String> by lazy {
        rows.flatten().map { it.value }.distinct()
    }

    private fun stringIndex(s: String) = allStrings.indexOf(s)

    private fun sharedStrings(): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"")
        append(" count=\"${allStrings.size}\" uniqueCount=\"${allStrings.size}\">")
        for (s in allStrings) {
            append("<si><t>${s.xmlEscape()}</t></si>")
        }
        append("</sst>")
    }

    // ---- Worksheet ----
    private fun worksheet(): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
        // Set column widths (approx 22 chars each to fit dates/currencies comfortably)
        val maxCols = rows.maxOfOrNull { it.size } ?: 1
        append("<cols>")
        for (i in 1..maxCols) append("<col min=\"$i\" max=\"$i\" width=\"22\" customWidth=\"1\"/>")
        append("</cols>")
        
        append("<sheetData>")
        rows.forEachIndexed { rowIndex, cells ->
            append("<row r=\"${rowIndex + 1}\">")
            cells.forEachIndexed { colIndex, cell ->
                val colLetter = (65 + colIndex).toChar()
                val ref = "$colLetter${rowIndex + 1}"
                
                // If cell is empty, we don't necessarily have to write string data to it,
                // but to preserve background color stylings for empty striped cells, we still write it.
                val si = stringIndex(cell.value)
                append("<c r=\"$ref\" t=\"s\" s=\"${cell.style.id}\"><v>$si</v></c>")
            }
            append("</row>")
        }
        append("</sheetData>")
        append("</worksheet>")
    }

    // ---- Styles ----
    // Based on App Theme:
    // Primary (Midnight Blue): FF192A56
    // Profit (Green 800): FF2E7D32
    // Error (Red 900): FFB71C1C
    // Surface Variant / Striping: FFDEE2E6
    private fun styles(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
          <fonts count="6">
            <font><sz val="11"/><name val="Calibri"/></font> <!-- 0: Normal -->
            <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font> <!-- 1: Header White Bold -->
            <font><b/><sz val="11"/><color rgb="FF192A56"/><name val="Calibri"/></font> <!-- 2: Title Midnight Blue Bold -->
            <font><sz val="11"/><color rgb="FF2E7D32"/><name val="Calibri"/></font> <!-- 3: Profit Green -->
            <font><sz val="11"/><color rgb="FFB71C1C"/><name val="Calibri"/></font> <!-- 4: Loss Red -->
            <font><b/><sz val="11"/><name val="Calibri"/></font> <!-- 5: Simple Bold -->
          </fonts>
          <fills count="4">
            <fill><patternFill patternType="none"/></fill> <!-- 0: None -->
            <fill><patternFill patternType="gray125"/></fill> <!-- 1: System reserved -->
            <fill><patternFill patternType="solid"><fgColor rgb="FF192A56"/></patternFill></fill> <!-- 2: Header Background (Midnight Blue) -->
            <fill><patternFill patternType="solid"><fgColor rgb="FFDEE2E6"/></patternFill></fill> <!-- 3: Stripe Background (Surface Variant) -->
          </fills>
          <borders count="2">
            <border><left/><right/><top/><bottom/><diagonal/></border> <!-- 0: No border -->
            <border> <!-- 1: Header Bottom Border -->
              <left/><right/><top/><bottom style="thin"><color rgb="FF192A56"/></bottom><diagonal/>
            </border>
          </borders>
          
          <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
          
          <cellXfs count="8">
            <!-- 0: NORMAL -->
            <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
            
            <!-- 1: HEADER (Midnight Blue bg, White bold text, bottom border) -->
            <xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1">
              <alignment vertical="center"/>
            </xf>
            
            <!-- 2: TITLE (Midnight Blue bold text) -->
            <xf numFmtId="0" fontId="2" fillId="0" borderId="0" xfId="0" applyFont="1"/>
            
            <!-- 3: PROFIT (Green text) -->
            <xf numFmtId="0" fontId="3" fillId="0" borderId="0" xfId="0" applyFont="1"/>
            
            <!-- 4: LOSS (Red text) -->
            <xf numFmtId="0" fontId="4" fillId="0" borderId="0" xfId="0" applyFont="1"/>
            
            <!-- 5: STRIPE (Grayish bg) -->
            <xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFill="1"/>
            
            <!-- 6: STRIPE_PROFIT (Grayish bg + Green text) -->
            <xf numFmtId="0" fontId="3" fillId="3" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
            
            <!-- 7: STRIPE_LOSS (Grayish bg + Red text) -->
            <xf numFmtId="0" fontId="4" fillId="3" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
          </cellXfs>
        </styleSheet>
    """.trimIndent()

    // ---- Boilerplate ----
    private fun workbook(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"
                  xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
          <sheets>
            <sheet name="${sheetName.xmlEscape()}" sheetId="1" r:id="rId1"/>
          </sheets>
        </workbook>
    """.trimIndent()

    private fun workbookRels(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1"
            Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet"
            Target="worksheets/sheet1.xml"/>
          <Relationship Id="rId2"
            Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles"
            Target="styles.xml"/>
          <Relationship Id="rId3"
            Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings"
            Target="sharedStrings.xml"/>
        </Relationships>
    """.trimIndent()

    private fun rootRels(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1"
            Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
            Target="xl/workbook.xml"/>
        </Relationships>
    """.trimIndent()

    private fun contentTypes(): String = """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Override PartName="/xl/workbook.xml"
            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
          <Override PartName="/xl/worksheets/sheet1.xml"
            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
          <Override PartName="/xl/sharedStrings.xml"
            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
          <Override PartName="/xl/styles.xml"
            ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
        </Types>
    """.trimIndent()

    private fun String.xmlEscape() = this
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")
}
