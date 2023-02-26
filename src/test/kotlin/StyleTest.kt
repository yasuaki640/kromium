import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

internal class StyleTest {
    @Test
    fun `is able to merge html and css to style tree`() {
        val html = domTree()
        val styleSheet = cssTree()

        val styleTree = styleTree(html, styleSheet)
        assertIs<StyledNode>(styleTree)

        assertEquals(
            styleTree.specifiedValues,
            mutableMapOf(
                "width" to Length(600f, Unit.Px),
                "padding" to Length(10f, Unit.Px),
                "border-width" to Length(1f, Unit.Px),
                "margin" to Keyword("auto"),
                "background" to ColorValue(Color(255u, 255u, 255u, 255u))
            )
        )

        val headStyle = styleTree.children[0]
        assertEquals(
            headStyle.specifiedValues,
            mutableMapOf<String, Value>(
                "display" to Keyword("none")
            )
        )

        // TODO クラスセレクタにspecifiedvaluesが入ってない、style.ktのクラスセレクタがうまいこと言ってない？
        // TODO そもそも計算値などをサポート指定内容だ
    }

    private fun cssTree(): StyleSheet {
        val css = File("src/test/resources/test.css").readText()
        return CSSParser(0u, css).parse(css)
    }

    private fun domTree(): Node {
        val html = File("src/test/resources/test.html").readText()
        return parse(html)
    }
}
