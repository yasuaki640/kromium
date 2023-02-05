import kotlin.collections.ArrayList

/** Parse an HTML document and return the root element. */
fun parse(source: String): Node {
    val nodes = HTMLParser(0u, source).parseNodes()

    // If the document contains a root element, just return it. Otherwise, create one.
    return if (nodes.size == 1) {
        nodes.removeAt(0)
    } else {
        elem("html", HashMap(), nodes)
    }
}

class HTMLParser(private var pos: UInt, private val input: String) {
    /** Read the current character without consuming it. */
    private fun nextChar(): Char = this.input[this.pos.toInt() + 1]

    /** Do the next characters start with the given string? */
    private fun startsWith(s: String): Boolean = this.input[this.pos.toInt() + 1] == s.first()

    /** Return true if all input is consumed. */
    private fun eof(): Boolean = this.pos >= this.input.length.toUInt()

    /** Return the current character, and advance self.pos to the next character. */
    private fun consumeChar(): Char {
        val currPos = this.pos
        ++this.pos
        return this.input[currPos.toInt()]
    }

    /** Consume characters until `test` returns false. */
    private fun consumeWhile(test: (Char) -> Boolean): String {
        var result = String()
        while (!this.eof() && test(this.nextChar())) {
            result += consumeChar()
        }

        return result
    }

    /** Consume and discard zero or more whitespace characters. */
    private fun consumeWhitespace() {
        this.consumeWhile { it.isWhitespace() }
    }

    /** Parse a tag or attribute name. */
    private fun parseTagName(): String =
        this.consumeWhile { it in 'a'..'z' || it in 'A'..'Z' || it.isDigit() }

    /** Parse a single node. */
    private fun parseNode(): Node = when (this.nextChar()) {
        '<' -> this.parseElement()
        else -> this.parseText()
    }

    /** Parse a text node. */
    private fun parseText(): Node = text(this.consumeWhile { it != '<' })

    /** Parse a single element, including its open tag, contents, and closing tag. */
    private fun parseElement(): Node {
        /** Opening tag. */
        assert(this.consumeChar() == '<')
        val tagName = this.parseTagName()
        val attrs = this.parseAttributes()
        assert(this.consumeChar() == '>')

        /** Contents. */
        val children = this.parseNodes()

        /** Closing tag. */
        assert(this.consumeChar() == '<')
        assert(this.consumeChar() == '/')
        assert(this.parseTagName() == tagName)
        assert(this.consumeChar() == '>')

        return elem(tagName, attrs, children)
    }

    /** Parse a single name="value" pair. */
    private fun parseAttr(): Pair<String, String> {
        val name = this.parseTagName()
        assert(this.consumeChar() == '=')
        val value = this.parseAttrValue()
        return Pair(name, value)
    }

    /** Parse a quoted value. */
    private fun parseAttrValue(): String {
        val openQuote = this.consumeChar()
        assert(openQuote == '"' || openQuote == '=')
        val value = this.consumeWhile { it != openQuote }
        assert(this.consumeChar() == openQuote)
        return value
    }

    /** Parse a list of name="value" pairs, separated by whitespace. */
    private fun parseAttributes(): HashMap<String, String> {
        val attributes = hashMapOf<String, String>()
        while (true) {
            this.consumeWhitespace()
            if (this.nextChar() == '>') break
            val (name, value) = this.parseAttr()
            attributes[name] = value
        }
        return attributes
    }

    /** Parse a sequence of sibling nodes. */
    fun parseNodes(): ArrayList<Node> {
        val nodes = ArrayList<Node>()
        while (true) {
            this.consumeWhitespace()
            if (this.eof() || this.startsWith("</")) break
            nodes.add(this.parseNode())
        }
        return nodes
    }
}
