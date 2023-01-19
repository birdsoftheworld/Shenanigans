package shenanigans.engine.ui.api

import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node

class Fragment(val items: MutableList<Node> = mutableListOf()) : UIBuilder, ParentUIBuilder {
    override fun addChild(child: Node) {
        items.add(child)
    }

    override fun addChild(fragment: Fragment) {
        items.addAll(fragment.items)
    }

    override fun build(): Node {
        return if (items.size == 1) {
            items[0]
        } else {
            Box().apply {
                children = items
            }
        }
    }
}