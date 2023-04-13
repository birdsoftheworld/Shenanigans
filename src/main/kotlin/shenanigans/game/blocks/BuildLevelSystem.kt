package shenanigans.game.blocks

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.game.Followed
import shenanigans.game.MousePlayer
import shenanigans.game.network.Synchronized
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerController
import shenanigans.game.player.PlayerProperties
import kotlin.reflect.KClass

class BuildLevelSystem : System {
    override fun executePhysics(
        resources: ResourcesView,
        eventQueues: EventQueues<LocalEventQueue>,
        query: (Iterable<KClass<out Component>>) -> QueryView,
        lifecycle: EntitiesLifecycle
    ) {
        //Sprites
        val oscillatingSprite = Sprites.oscillatingSprite
        val playerSprite = Sprites.playerSprite
        //Shapes
        val floorShape = Polygons.floorShape

        //Oscillating Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(100f, 500f,0.5f)
                ),
                oscillatingSprite,
                Collider(Polygons.oscillatingShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
                OscillatingBlock(50f, Vector2f(100f, 500f), .01f),
            )
        ))

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                Shape(floorShape, Color(1f, 0f, 0f)),
                Collider(Polygons.floorShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        // slippery blocks
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(200f, 600f,0.5f)
                ),
                Shape(Polygons.slipperyShape, Color(0f, 0.5f, 0.75f)),
                SlipperyBlock(),
                Collider(Polygons.slipperyShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(250f, 600f,0.5f)
                ),
                Shape(Polygons.slipperyShape, Color(0f, 0.5f, 0.75f)),
                SlipperyBlock(),
                Collider(Polygons.slipperyShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(300f, 600f,0.5f)
                ),
                Shape(Polygons.slipperyShape, Color(0f, 0.5f, 0.75f)),
                SlipperyBlock(),
                Collider(Polygons.slipperyShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                Shape(Polygons.stickyShape, Color(1f, 0.8f, 0f)),
                StickyBlock(),
                Collider(Polygons.stickyShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                Shape(Polygons.stickyShape, Color(1f, 0.8f, 0f)),
                StickyBlock(),
                Collider(Polygons.stickyShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //Player Respawn Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(100f, 500f,0.5f)
                ),
                Shape(Polygons.bigRect, Color(0f, 0f, 1f)),
                MousePlayer(false, Vector2f(0f,0f)),
                Collider(Polygons.bigRect, true),
                RespawnBlock(),
            )
        ))

        val player = Player(
            PlayerProperties()
        )
        val playerTransform = Transform(
            Vector3f(100f,0f,.9f)
        )

        //PLAYER
        lifecycle.add(
            sequenceOf(
                playerTransform,
                playerSprite,
                Collider(PlayerController.SHAPE_BASE, false, tracked = true),
                Player(
                    PlayerProperties()
                ),
                Followed {
                    val p = Vector3f(playerTransform.position)
                    p.x += PlayerController.SHAPE_BASE.width / 2
                    p.y += PlayerController.SHAPE_BASE.height / 2
                    if (player.crouching) {
                        p.y -= PlayerController.SHAPE_BASE.height - PlayerController.SHAPE_CROUCHED.height
                    }
                    p
                },
                Synchronized()
            )
        )

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(600f, 700f,0.8f)
                ),
                Shape(Polygons.floorShape, Color(1f, 0f, 0f)),
                Collider(Polygons.floorShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(800f, 600f,0.8f)
                ),
                Shape(Polygons.floorShape, Color(1f, 0f, 0f)),
                Collider(Polygons.floorShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(400f, 400f,0.8f)
                ),
                Shape(Polygons.wallShape, Color(0f, 1f, 1f)),
                Collider(Polygons.wallShape, true),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))
    }
}