class StyleSheet(val rules: List<Rule>)

class Rule(val selectors: ArrayList<Selector>, val declarations: ArrayList<Declaration>)

sealed interface Selector {
    fun specificity(): Specificity {
        // http://www.w3.org/TR/selectors/#specificity
        val simple = this
        if (simple !is SimpleSelector) {
            throw Exception("No other selector is supported except the simple selector.")
        }

        val a = simple.id?.length ?: 0
        val b = simple.klazz.size
        val c = simple.tagName?.length ?: 0
        return Specificity(a, b, c)
    }
}

data class Specificity(val a: Int, val b: Int, val c: Int) : Comparable<Specificity> {
    override fun compareTo(other: Specificity): Int = when {
        a != other.a -> a - other.a
        b != other.a -> b - other.b
        else -> c - other.c
    }
}

data class SimpleSelector(
    var tagName: String?,
    var id: String?,
    val klazz: ArrayList<String>,
) : Selector

data class Declaration(val name: String, val value: Value)

sealed interface Value : Cloneable {
    /** Return the size of a length in px, or zero for non-lengths. */
    fun toPx(): Float = when (this) {
        is Length -> this.f
        else -> 0.0F
    }

    public override fun clone(): Value
}

data class Keyword(val value: String) : Value {
    override fun clone(): Keyword = this.copy()
}

data class Length(val f: Float, val unit: Unit) : Value {
    override fun clone(): Length = this.copy()
}

data class ColorValue(val color: Color) : Value {
    override fun clone(): ColorValue = this.copy()
}

enum class Unit {
    Px,
}

data class Color(
    val r: UInt,
    val g: UInt,
    val b: UInt,
    val a: UInt,
)

class CSSParser(private var pos: UInt, private val input: String) {
    /** Parse a whole CSS stylesheet. */
    fun parse(source: String): StyleSheet {
        val parser = CSSParser(0u, source)
        return StyleSheet(parser.parseRules())
    }

    /** Parse a list of rule sets, separated by optional whitespace. */
    fun parseRules(): ArrayList<Rule> {
        val rules = ArrayList<Rule>()
        while (true) {
            this.consumeWhitespace()
            if (this.eof()) break
            rules.add(this.parseRule())
        }
        return rules
    }

    /** Parse a rule set: `<selectors> { <declarations> }`. */
    private fun parseRule(): Rule = Rule(
        this.parseSelectors(),
        this.parseDeclarations()
    )

    /** Parse a comma-separated list of selectors. */
    private fun parseSelectors(): ArrayList<Selector> {
        val selectors = ArrayList<Selector>()
        while (true) {
            selectors.add(this.parseSimpleSelector())
            this.consumeWhitespace()
            when (val c = this.nextChar()) {
                ',' -> {
                    this.consumeChar()
                    this.consumeWhitespace()
                }

                '{' -> break

                else -> throw Exception("Unexpected character $c in selector list")
            }
        }

        // Return selectors with highest specificity first, for use in matching.
        selectors.sortBy { it.specificity() }
        return selectors
    }

    /** Parse one simple selector, e.g.: `type#id.class1.class2.class3` */
    private fun parseSimpleSelector(): SimpleSelector {
        val selector = SimpleSelector(null, null, ArrayList())
        while (!this.eof()) {
            val c = this.nextChar()
            when {
                c == '#' -> {
                    this.consumeChar()
                    selector.id = this.parseIdentifier()
                }

                c == '.' -> {
                    this.consumeChar()
                    this.parseIdentifier().let { selector.klazz.add(it) }
                }

                c == '*' -> {
                    // universal selector
                    this.consumeChar()
                }

                validIdentifierChar(c) -> {
                    selector.tagName = this.parseIdentifier()
                }

                else -> break
            }
        }
        return selector
    }

    private fun parseIdentifier(): String = this.consumeWhile(::validIdentifierChar)

    private fun parseDeclarations(): ArrayList<Declaration> {
        assert(this.consumeChar() == '{')
        val declarations = ArrayList<Declaration>()
        while (true) {
            this.consumeWhitespace()
            if (this.nextChar() == '}') {
                this.consumeChar()
                break
            }
            declarations.add(this.parseDeclaration())
        }
        return declarations
    }

    private fun parseDeclaration(): Declaration {
        val propertyName = this.parseIdentifier()
        this.consumeWhitespace()
        assert(this.consumeChar() == ':')
        this.consumeWhitespace()
        val value = this.paresValue()
        this.consumeWhitespace()
        assert(this.consumeChar() == ';')

        return Declaration(propertyName, value)
    }

    /** Methods for parsing values: */
    private fun paresValue(): Value = when (this.nextChar()) {
        in '0'..'9' -> this.parseLength()
        '#' -> this.parseColor()
        else -> Keyword(this.parseIdentifier())
    }

    private fun parseLength(): Value = Length(this.parseFloat(), this.parseUnit())

    private fun parseFloat(): Float {
        val s = this.consumeWhile {
            when (it) {
                in '0'..'9', '.' -> true
                else -> false
            }
        }
        return s.toFloat()
    }

    private fun parseUnit(): Unit = when (this.parseIdentifier().lowercase()) {
        "px" -> Unit.Px
        else -> throw Exception("unrecognized unit")
    }

    private fun parseColor(): Value {
        assert(this.consumeChar() == '#')
        return ColorValue(
            Color(
                this.parseHexPair(),
                this.parseHexPair(),
                this.parseHexPair(),
                255u
            )
        )
    }

    private fun parseHexPair(): UInt {
        val s = this.input.substring(this.pos.toInt(), this.pos.toInt() + 2)
        this.pos += 2u
        return s.toUInt(16)
    }

    /** Consume and discard zero or more whitespace characters. */
    private fun consumeWhitespace() {
        this.consumeWhile { it.isWhitespace() }
    }

    /** Consume characters until `test` returns false. */
    private fun consumeWhile(test: (Char) -> Boolean): String {
        var result = String()
        while (!this.eof() && test(this.nextChar())) {
            result += consumeChar()
        }

        return result
    }

    /** Return the current character, and advance self.pos to the next character. */
    private fun consumeChar(): Char {
        val currPos = this.pos
        ++this.pos
        return this.input[currPos.toInt()]
    }

    /** Read the current character without consuming it. */
    private fun nextChar(): Char = this.input[this.pos.toInt()]

    /** Return true if all input is consumed. */
    private fun eof(): Boolean = this.pos >= this.input.length.toUInt()

    private fun validIdentifierChar(c: Char): Boolean = when (c) {
        in 'a'..'z', in 'A'..'Z', in '0'..'9', '-', '_' -> true
        else -> false
    }
}
