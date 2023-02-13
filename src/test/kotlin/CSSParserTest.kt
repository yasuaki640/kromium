import kotlin.test.Test
import kotlin.test.assertEquals

internal class CSSParserTest {
    @Test
    fun `CSS Parser is able to parse tag selector`() {
        val source = "h1, h2, h3 { margin: auto; color: #cc0000; }"

        val parser = CSSParser(0u, source)
        val rules = parser.parseRules()

        val rule = rules[0]
        assertEquals(
            rule.selectors,
            arrayListOf<Selector>(
                SimpleSelector("h1", null, ArrayList()),
                SimpleSelector("h2", null, ArrayList()),
                SimpleSelector("h3", null, ArrayList()),
            )
        )

        assertEquals(
            rule.declarations,
            arrayListOf(
                Declaration("margin", Keyword("auto")),
                Declaration(
                    "color",
                    ColorValue(
                        Color(204u, 0u, 0u, 255u)
                    )
                ),
            )
        )
    }

    @Test
    fun `CSS Parser is able to parse class selector`() {
        val source = "div.note { margin-bottom: 20px; padding: 10px; }"

        val parser = CSSParser(0u, source)
        val rules = parser.parseRules()

        val rule = rules[0]
        assertEquals(
            rule.selectors,
            arrayListOf<Selector>(
                SimpleSelector("div", null, arrayListOf("note"))
            )
        )

        assertEquals(
            rule.declarations,
            arrayListOf(
                Declaration("margin-bottom", Length(20.0f, Unit.Px)),
                Declaration("padding", Length(10.0f, Unit.Px))
            )
        )
    }

    @Test
    fun `CSS Parser is able to parse id selector`() {
        val source = "#answer { display: none; }"

        val parser = CSSParser(0u, source)
        val rules = parser.parseRules()

        val rule = rules[0]
        assertEquals(
            rule.selectors,
            arrayListOf<Selector>(
                SimpleSelector(null, "answer", ArrayList())
            )
        )

        assertEquals(
            rule.declarations,
            arrayListOf(
                Declaration("display", Keyword("none")),
            )
        )
    }
}
