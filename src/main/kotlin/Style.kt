typealias PropertyMap = MutableMap<String, Value>

/** A node with associated style data. */
data class StyledNode(
    val node: Node, // pointer to a DOM node
    val specifiedValues: PropertyMap,
    val children: List<StyledNode>,
)

fun matches(elem: ElementData, selector: Selector): Boolean = when (selector) {
    is SimpleSelector -> matchesSimpleSelector(elem, selector)
}

fun matchesSimpleSelector(elem: ElementData, selector: SimpleSelector): Boolean {
    // Check type selector
    if (selector.tagName != elem.tagName) {
        return false
    }

    // Check ID selector
    if (selector.id != elem.id) {
        return false
    }

    // Check class selectors
    val elemClasses = elem.classes
    if (selector.klazz.any { !elemClasses.contains(it) }) {
        return false
    }

    // We didn't find any non-matching selector components.
    return true
}

data class MatchedRule(val specificity: Specificity, val rule: Rule)

/** If `rule` matches `elem`, return a `MatchedRule`. Otherwise return `None`. */
fun matchRule(elem: ElementData, rule: Rule): MatchedRule? =
    rule.selectors.find { matches(elem, it) }
        ?.let { MatchedRule(it.specificity(), rule) }

/** Find all CSS rules that match the given element. */
fun matchingRules(elem: ElementData, styleSheet: StyleSheet): ArrayList<MatchedRule> {
    val list = styleSheet.rules.mapNotNull { matchRule(elem, it) }
    return ArrayList(list.toMutableList())
}

fun specifiedValues(elem: ElementData, styleSheet: StyleSheet): PropertyMap {
    val values = mutableMapOf<String, Value>()
    val matchedRules = matchingRules(elem, styleSheet)

    // Go through the rules from lowest to highest specificity.
    matchedRules.sortBy { it.specificity }
    for (matchedRule in matchedRules) {
        for (declaration in matchedRule.rule.declarations) {
            values[declaration.name] = declaration.value.clone()
        }
    }
    return values
}

/**
 * Entry point of creating a styleNode tree.
 * Apply a stylesheet to an entire DOM tree, returning a StyledNode tree.
 */
fun styleTree(root: Node, styleSheet: StyleSheet): StyledNode = StyledNode(
    node = root,
    specifiedValues = when (val type = root.nodeType) {
        is Element -> specifiedValues(type.value, styleSheet)
        is Text -> mutableMapOf()
    },
    children = root.children.map { styleTree(it, styleSheet) }
)