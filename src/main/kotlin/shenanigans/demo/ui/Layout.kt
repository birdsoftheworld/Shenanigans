package shenanigans.demo.ui

import org.joml.Vector2f
import shenanigans.engine.ClientEngine
import shenanigans.engine.ecs.ResourcesView
import shenanigans.engine.ecs.utils.AddEntitiesSystem
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.init.SystemList
import shenanigans.engine.init.client.ClientEngineOptions
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.UIRendererComponent
import shenanigans.engine.ui.UISystem
import shenanigans.engine.ui.api.*
import shenanigans.engine.ui.elements.Box
import shenanigans.engine.ui.elements.Node

class Layout : UI {
    override fun render(): Fragment.() -> Unit {
        return {
            box {
                minSize = Vector2f(100f, 100f)

                color = Color(0.5f, 0.5f, 0.5f)
            }

            box {
                grow = 1f
                flexDirection = Box.FlexDirection.Column
                justifyContent = Box.JustifyContent.FlexStart

                fragment {
                    box {
                        color = Color(1f, 0f, 0f)
                        size = Vector2f(200f, 100f)

                        setMargin(Node.Edge.All, 15f)
                    }
                }

                fragment {
                    box {
                        color = Color(0f, 1f, 0f)
                        minSize = Vector2f(100f, 100f)
                        grow = 1f

                        flexDirection = Box.FlexDirection.Row
                        justifyContent = Box.JustifyContent.Center
                        alignItems = Box.Align.Center

                        text {
                            text = "Hello, world!"
                        }

                        text {
                            text = "Hello again, world!"
                        }
                    }

                    box {
                        color = Color(0f, 0f, 1f)
                        size = Vector2f(200f, 300f)
                        alignSelf = Box.Align.FlexEnd
                    }
                }
            }
        }
    }
}
