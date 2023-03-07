package shenanigans.game.Blocks

import org.joml.Vector2f
import shenanigans.engine.ecs.*
import shenanigans.engine.graphics.api.Color
import shenanigans.engine.graphics.api.component.Shape
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.game.MousePlayer
import shenanigans.game.network.Sendable
import shenanigans.game.player.Player
import shenanigans.game.player.PlayerProperties
import kotlin.reflect.KClass

class BuildLevelSystem : System {
    override fun query(): Iterable<KClass<out Component>> {
        return emptySet()
    }

    override fun execute(resources: ResourcesView, entities: EntitiesView, lifecycle: EntitiesLifecycle) {
        val shapes = Shapes()
        val sprites = Sprites()

        //Sprites
        val oscillatingSprite = sprites.oscillatingSprite
        val teleportarASprite = sprites.teleportarASprite
        val teleportarBSprite = sprites.teleportarBSprite
        val spikeSprite = sprites.spikeSprite
        val trampolineSprite = sprites.trampolineSprite
        val respawnSprite = sprites.respawnSprite
        val playerSprite = sprites.playerSprite
        //Shapes
        val oscillatingShape = shapes.oscillatingShape
        val teleportShape = shapes.teleportShape
        val spikeShape = shapes.scaryShape
        val floorShape = shapes.floorShape
        val wallShape = shapes.wallShape
        val playerShape = shapes.playerShape
        val slipperyShape = shapes.slipperyShape
        val stickyShape = shapes.stickyShape
        val trampolineShape = shapes.springShape
        //Oscillating Block
        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(100f, 500f)
                    ),
                    oscillatingSprite,
                    Collider(oscillatingShape, true, false),
                    MousePlayer(false, Vector2f(0f,0f)),
                    OscillatingBlock(50f, Vector2f(100f, 500f), .01f),
                )
                ))

        //Tp Blocks
        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(50f, 500f)
                    ),
                    teleportarASprite,
                    Collider(teleportShape, true, true, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    TeleporterBlock(0),
                )
                ))

        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(600f, 500f)
                    ),
                    teleportarBSprite,
                    Collider(teleportShape, true, true, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    TeleporterBlock(1),
                )
                ))

        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(600f, 600f)
                    ),
                    teleportarASprite,
                    Collider(teleportShape, true, true, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    TeleporterBlock(2),
                )
                ))

        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(50f, 200f)
                    ),
                    teleportarBSprite,
                    Collider(teleportShape, true, true, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    TeleporterBlock(3),
                )
                ))


        //scaryBlock
        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(100f, 500f)
                    ),
                    spikeSprite,
                    Collider(spikeShape, true, false, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    SpikeBlock(),
                )
                ))

        //springBlock
        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(300f, 700f)
                    ),
                    trampolineSprite,
                    Collider(trampolineShape, true, false, tracked = true),
                    MousePlayer(false, Vector2f(0f,0f)),
                    TrampolineBlock(),
                )
                ))



        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                floorShape,
                Collider(floorShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //slipppery blocks
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(250f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(300f, 600f)
                ),
                slipperyShape,
                SlipperyBlock(),
                Collider(slipperyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )
        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        //sticky block
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(0f, 600f)
                ),
                stickyShape,
                StickyBlock(),
                Collider(stickyShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        )

        val nullShape = Shape(
            arrayOf(
                Vector2f(0f, 0f),
                Vector2f(0f, 30f),
                Vector2f(30f, 30f),
                Vector2f(30f, 0f)
            ), Color(.5f, .5f, .5f)
        )
        //Player Respawn Block
        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(100f, 500f)
                    ),
                    playerShape,
                    MousePlayer(false, Vector2f(0f,0f)),
                    Collider(nullShape, true, true),
                    RespawnBlock(),
                )
                ))


        //PLAYER
        lifecycle.add(
            sequenceOf(
                Transform(
                    Vector2f(200f, 500f),
                ),
                playerSprite,
                Collider(playerShape, false, tracked = true),
                Player(
                    PlayerProperties()
                ),
                Sendable(),
            )
        )



        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(600f, 700f)
                    ),
                    floorShape,
                    Collider(floorShape, true, false),
                    MousePlayer(false, Vector2f(0f,0f)),
                )
                ))

        lifecycle.add((
                sequenceOf(
                    Transform(
                        Vector2f(800f, 600f)
                    ),
                    floorShape,
                    Collider(floorShape, true, false),
                    MousePlayer(false, Vector2f(0f,0f)),
                )
                ))


        lifecycle.add((
            sequenceOf(
                Transform(
                    Vector2f(400f, 400f)
                ),
                wallShape,
                Collider(wallShape, true, false),
                MousePlayer(false, Vector2f(0f,0f)),
            )
        ))
    }
}