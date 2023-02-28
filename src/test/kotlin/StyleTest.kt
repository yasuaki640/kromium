import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

internal class StyleTest {
    @Test
    fun `is able to merge html and css to style tree`() {
        val html = domTree()
        val styleSheet = cssTree()

        val styleTree = styleTree(html, styleSheet)

        assertEquals(
            hashMapOf(
                "padding" to Length(40f, Unit.Px),
                "background-color" to ColorValue(Color(54u, 79u, 107u, 255u))
            ),
            styleTree.specifiedValues
        )

        val inner1 = styleTree.children[0]
        assertEquals(
            hashMapOf(
                "padding" to Length(40f, Unit.Px),
                "background-color" to ColorValue(Color(63u, 193u, 201u, 255u))
            ),
            inner1.specifiedValues
        )

        val inner2 = inner1.children[0]
        assertEquals(
            hashMapOf(
                "padding" to Length(40f, Unit.Px),
                "background-color" to ColorValue(Color(245u, 245u, 245u, 255u))
            ),
            inner2.specifiedValues
        )

        val inner3 = inner2.children[0]
        assertEquals(
            hashMapOf(
                "padding" to Length(40f, Unit.Px),
                "background-color" to ColorValue(Color(252u, 81u, 133u, 255u))
            ),
            inner3.specifiedValues
        )
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
