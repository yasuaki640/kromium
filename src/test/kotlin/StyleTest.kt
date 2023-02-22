import kotlin.test.Test
import kotlin.test.assertEquals

internal class StyleTest {
    @Test
    fun `is able to merge html and css to style tree`() {
        val root = Node(
            arrayListOf(
                Node(
                    arrayListOf(
                        Node(
                            arrayListOf(Node(arrayListOf(), Text("Title"))),
                            Element(ElementData("h1", hashMapOf()))
                        ),
                        Node(
                            arrayListOf(
                                Node(
                                    arrayListOf(
                                        Node(arrayListOf(),Text("Hello ")),
                                        Node(
                                            arrayListOf(Node(arrayListOf(), Text("world"))),
                                            Element(ElementData("em", hashMapOf()))
                                        ),
                                        Node(arrayListOf(), Text("!"))
                                    ),
                                    Element(ElementData("p", hashMapOf()))
                                )
                            ), Element(ElementData("div", hashMapOf("id" to "main", "class" to "test")))
                        )
                    ), Element(ElementData("body", hashMapOf()))
                )
            ), Element(ElementData("html", hashMapOf()))
        )

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
