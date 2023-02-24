import java.io.File
import kotlin.test.Test
import kotlin.test.assertIs

internal class StyleTest {
    @Test
    fun `is able to merge html and css to style tree`() {
        val root = domTree()
        val styleSheet = cssTree()

        val styleTree = styleTree(root, styleSheet)
        assertIs<StyledNode>(styleTree)
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
