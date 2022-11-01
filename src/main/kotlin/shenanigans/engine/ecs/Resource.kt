package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface Resource

class Resources {
    @PublishedApi internal val resources: HashMap<KClass<out Resource>, Resource> = hashMapOf()

    inline fun <reified T : Resource> get(): T {
        return getOpt()!!
    }

    inline fun <reified T : Resource> getOpt(): T? {
        val stored = resources[T::class]

        return if (stored !== null) {
            stored as T
        } else {
            null
        }
    }

    inline fun <reified T : Resource> set(res: T) {
        resources[T::class] = res
    }
}