package shenanigans.game.Blocks

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Rectangle
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
        val shapes = Shapes()
        val sprites = Sprites()

        //Sprites
        val oscillatingSprite = sprites.oscillatingSprite
        val teleportarASprite = sprites.teleportarASprite
        val teleportarBSprite = sprites.teleportarBSprite
        val spikeSprite = sprites.spikeSprite
        val trampolineSprite = sprites.trampolineSprite
        val playerSprite = sprites.playerSprite
        //Shapes
        val oscillatingShape = shapes.oscillatingShape
        val teleportShape = shapes.teleportShape
        val spikeShape = shapes.spikeShape
        val floorShape = shapes.floorShape
        val wallShape = shapes.wallShape
        val playerShape = shapes.playerShape
        val slipperyShape = shapes.slipperyShape
        val stickyShape = shapes.stickyShape
        val trampolineShape = shapes.trampolineShape
        //Oscillating Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(100f, 500f,0.5f)
                ),
                oscillatingSprite,
                Collider(oscillatingShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
                OscillatingBlock(50f, Vector2f(100f, 500f), .01f),
            )
        ))

        //Tp Blocks
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(50f, 500f,0.5f)
//                ),
//                teleportarASprite,
//                Collider(teleportShape.polygon, true, true, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                TeleporterBlock(0)
//            )
//        ))
//
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(600f, 500f,0.5f)
//                ),
//                teleportarBSprite,
//                Collider(teleportShape.polygon, true, true, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                TeleporterBlock(1),
//            )
//        ))
//
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(600f, 600f,0.5f)
//                ),
//                teleportarASprite,
//                Collider(teleportShape.polygon, true, true, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                TeleporterBlock(2),
//            )
//        ))
//
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(50f, 200f,0.5f)
//                ),
//                teleportarBSprite,
//                Collider(teleportShape.polygon, true, true, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                TeleporterBlock(3),
//            )
//        ))


        //scaryBlock
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(100f, 500f,0.5f)
//                ),
//                spikeSprite,
//                Collider(spikeShape.polygon, true, false, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                SpikeBlock(),
//            )
//        ))

        //springBlock
//        lifecycle.add((
//            sequenceOf(
//                Transform(
//                    Vector3f(300f, 700f,0.5f)
//                ),
//                trampolineSprite,
//                Collider(trampolineShape.polygon, true, false, tracked = true),
//                MousePlayer(false, Vector2f(0f,0f)),
//                TrampolineBlock(),
//            )
//        ))



        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                floorShape,
                Collider(floorShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

//        slipppery blocks
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(200f, 600f,0.5f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(250f, 600f,0.5f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(300f, 600f,0.5f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector3f(0f, 600f,0.5f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //Player Respawn Block
        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(100f, 500f,0.5f)
                ),
                playerShape,
                MousePlayer(false, Vector2f(0f,0f)),
                Collider(playerShape.polygon, true, true),
                RespawnBlock(),
            )
        ))

        val player = Player(
            PlayerProperties()
        )
        val playerTransform =
            Transform(
                Vector3f(100f,-100f,.9f)
            )
        //PLAYER
        lifecycle.add(
            sequenceOf(
                playerTransform,
                playerSprite,
                Collider(playerShape.polygon, false, tracked = true),
                Player(
                    PlayerProperties()
                ),
                MousePlayer(false, Vector2f(0f,0f)),
                Followed {
                    val p = playerTransform.position
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
                floorShape,
                Collider(floorShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))

        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(800f, 600f,0.8f)
                ),
                floorShape,
                Collider(floorShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))


        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector3f(400f, 400f,0.8f)
                ),
                wallShape,
                Collider(wallShape.polygon, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))
    }
}