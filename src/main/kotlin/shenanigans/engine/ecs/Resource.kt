package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface Resource

class Resources {
    val _resources: HashMap<KClass<out Resource>, Resource> = hashMapOf()

    inline fun <reified T : Resource> get(): T {
        return getOpt()!!
    }

    inline fun <reified T : Resource> getOpt(): T? {
        val stored = _resources[T::class]

        return if (stored !== null) {
            stored as T
        } else {
            null
        }
    }

    inline fun <reified T : Resource> set(res: T) {
        _resources[T::class] = res
    }
}