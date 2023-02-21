import kotlin.test.Test
import kotlin.test.assertEquals

internal class HTMLParserTest {
    @Test
    fun `Test HTML Parser is able to parse a simple html`() {
        val source =
            """
            <html>
                <body>
                    <h1>Title</h1>
                    <div id="main" class="test">
                        <p>Hello <em>world</em>!</p>
                    </div>
                </body>
            </html>
            """.trimIndent()

        val root = parse(source)
        assertEquals(root.nodeType, Element(ElementData("html", HashMap())))

        val body = root.children[0]
        assertEquals(body.nodeType, Element(ElementData("body", HashMap())))

        val text = body.children[0]
        assertEquals(
            text.nodeType,
            Element(ElementData("h1", hashMapOf()))
        )

        val div = body.children[1]
        assertEquals(
            div.nodeType,
            Element(
                ElementData(
                    "div",
                    hashMapOf(
                        Pair("id", "main"),
                        Pair("class", "test")
                    )
                )
            )
        )

        val p = div.children[0]
        assertEquals(p.nodeType, Element(ElementData("p", HashMap())))

        val helloTxt = p.children[0]
        assertEquals(helloTxt.nodeType, Text("Hello "))

        val em = p.children[1]
        assertEquals(em.nodeType, Element(ElementData("em", HashMap())))

        val worldTxt = em.children[0]
        assertEquals(worldTxt.nodeType, Text("world"))

        val exclamation = p.children[2]
        assertEquals(exclamation.nodeType, Text("!"))
    }
}
