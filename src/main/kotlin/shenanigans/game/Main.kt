package shenanigans.game

import org.joml.Vector2f
import shenanigans.engine.Engine
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.physics.CollisionSystem
import shenanigans.engine.physics.DeltaTime
import shenanigans.engine.scene.Scene
import shenanigans.engine.ui.Button
import shenanigans.engine.ui.ButtonSystem
import shenanigans.engine.util.Transform
import shenanigans.engine.util.isPointInside
import shenanigans.engine.window.Key
import shenanigans.engine.window.MouseButtonAction
import shenanigans.engine.window.events.KeyboardState
import shenanigans.engine.window.events.MouseButtonEvent
import shenanigans.engine.window.events.MousePositionEvent
import shenanigans.engine.window.events.MouseState
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import kotlin.math.round
import kotlin.reflect.KClass

fun main() {
    Engine(testScene()).run()
}

fun testScene(): Scene {
    val scene = Scene()

    // NOTE: in the future, this will not be the recommended way to populate a scene
    //       instead, the engine will have a facility for running systems once
    //       which will be used with a canonical "AddEntities" system
    scene.runSystems(Resources(), listOf(AddTestEntities()))

    scene.defaultSystems.add(MouseMovementSystem())
    scene.defaultSystems.add(PlayerController())
    scene.defaultSystems.add(CollisionSystem())
    scene.defaultSystems.add(ButtonSystem())

    return scene
}

class MousePlayer(var grabbed : Boolean, var dragOffset : Vector2f) : Component{fun grab(){this.grabbed=true}fun drop(){this.grabbed=false}}


class AddTestEntities : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        val shape = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 50f), Vector2f(600f, 50f), Vector2f(600f, 0f)
            ), Color(1f, 0f, 0f)
        )


        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        val shape2 = Shape(
            arrayOf(
                Vector2f(0f, 0f), Vector2f(0f, 30f), Vector2f(30f, 30f), Vector2f(30f, 0f)
            ), Color(0f, 0f, 1f)
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 500f),
                ),
                shape2,
                Collider(shape2, false),
                Player(500f),
            )
        )

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(600f, 700f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(800f, 600f)
                ),
                shape,
                Collider(shape, true),
                MousePlayer(false, Vector2f(0f,0f)),
                )
        ))
    }
}

class MouseMovementSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return setOf(MousePlayer::class, Transform::class)
    }

    override fun execute(resources: Resources, entities: Sequence<EntityView>, lifecycle: EntitiesLifecycle) {
        resources.get<EventQueue>().iterate<MousePositionEvent>().forEach { event ->
            entities.forEach { entity ->
                val mousePlayer = entity.component<MousePlayer>().get()
                val transform = entity.component<Transform>().get()
                if(mousePlayer.grabbed){
                    transform.position.set(event.position.x() + mousePlayer.dragOffset.x(), event.position.y() + mousePlayer.dragOffset.y())
                    entity.component<Transform>().mutate()
                }
            }
        }

        resources.get<EventQueue>().iterate<MouseButtonEvent>().forEach { event ->
            entities.forEach { entity ->
                val transform = entity.component<Transform>().get()
                val mousePosition = resources.get<MouseState>().position()
                val mousePlayer = entity.component<MousePlayer>().get()
                if(event.action == MouseButtonAction.PRESS && entity.component<Shape>().get().isPointInside(mousePosition, transform)){
                    mousePlayer.dragOffset.x = transform.position.x - mousePosition.x()
                    mousePlayer.dragOffset.y = transform.position.y - mousePosition.y()
                    mousePlayer.grab()
                }
                if(event.action == MouseButtonAction.RELEASE){
                    mousePlayer.drop()
                    transform.position.x = round(transform.position.x/50)*50
                    transform.position.y = round(transform.position.y/50)*50
                }
            }
        }
    }
}

