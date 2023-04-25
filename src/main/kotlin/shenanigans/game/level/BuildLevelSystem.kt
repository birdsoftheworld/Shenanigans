package shenanigans.game.level

import org.joml.Vector2f
import org.joml.Vector3f
import shenanigans.engine.ecs.*
import shenanigans.engine.events.EventQueues
import shenanigans.engine.events.LocalEventQueue
import shenanigans.engine.graphics.api.component.Sprite
import shenanigans.engine.physics.Collider
import shenanigans.engine.util.Transform
import shenanigans.engine.util.shapes.Rectangle
import shenanigans.game.Followed
import shenanigans.game.level.block.*
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
        val playerSprite = Sprite(PlayerController.TEXTURE.getRegion(), PlayerController.SHAPE_BASE)
        //Shapes
        val floorShape = Rectangle(600f, 50f)

        //Oscillating Block
        insertBlock(
            lifecycle,
            OscillatingBlock(50f, Vector2f(100f, 500f), .01f),
            Vector3f(100f, 500f, 0.5f)
        )

        insertBlock(
            lifecycle,
            NormalBlock(floorShape),
            Vector3f(0f, 600f, 0.5f)
        )

        // slippery blocks
        insertBlock(
            lifecycle,
            SlipperyBlock(),
            Vector3f(200f, 600f, 1f)
        )
        insertBlock(
            lifecycle,
            SlipperyBlock(),
            Vector3f(250f, 600f, 1f)
        )
        insertBlock(
            lifecycle,
            SlipperyBlock(),
            Vector3f(300f, 600f, 1f)
        )
        //sticky block
        insertBlock(
            lifecycle,
            StickyBlock(),
            Vector3f(0f, 600f, 1f)
        )

        //sticky block
        insertBlock(
            lifecycle,
            StickyBlock(),
            Vector3f(0f, 600f, 1f)
        )

        //Player Respawn Block
        insertBlock(
            lifecycle,
            RespawnBlock(),
            Vector3f(100f, 500f, 0.5f)
        )

        val player = Player(
            PlayerProperties()
        )
        val playerTransform = Transform(
            Vector3f(100f, 0f, .9f)
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

        insertBlock(
            lifecycle,
            NormalBlock(floorShape),
            Vector3f(600f, 700f, 0.8f)
        )

        insertBlock(
            lifecycle,
            NormalBlock(floorShape),
            Vector3f(800f, 600f, 0.8f)
        )

        insertBlock(
            lifecycle,
            NormalBlock(floorShape),
            Vector3f(400f, 400f, 0.8f)
        )
    }
}