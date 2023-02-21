data class Node(val children: ArrayList<Node>, val nodeType: NodeType)

sealed interface NodeType {
    val value: Any
}

data class Text(override val value: String) : NodeType

data class Element(override val value: ElementData) : NodeType

data class ElementData(val tagName: String, val attributes: HashMap<String, String>) {
    val id: String?
        get() = this.attributes["id"]

    val classes: HashSet<String>
        get() {
            val klazz = this.attributes["class"]
            return klazz?.split(' ')?.toHashSet() ?: HashSet()
        }
}

fun text(data: String): Node = Node(ArrayList(), Text(data))

fun elem(name: String, attrs: HashMap<String, String>, children: ArrayList<Node>): Node = Node(
    children,
    Element(
        ElementData(name, attrs)
    )
)
