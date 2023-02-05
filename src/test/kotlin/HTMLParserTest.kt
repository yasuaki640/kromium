import kotlin.test.Test
import kotlin.test.assertIs

internal class HTMLParserTest {
    @Test
    fun `Test HTML Parser is able to parse a simple html`() {
        val source = """<html> <body> <h1>Title</h1> <div id="main" class="test"> <p>Hello <em>world</em>!</p> </div> </body></html>"""

        val result = parse(source)
        assertIs<Node>(result)

    }
}