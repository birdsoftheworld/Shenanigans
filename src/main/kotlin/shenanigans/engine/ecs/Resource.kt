package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface Resource

/**
 * an ordered read-only view of several sets of resources
 */
class ResourcesView(private vararg val children: Resources) {
    inline fun <reified T : Resource> get(): T {
        return getOpt()!!
    }

    inline fun <reified T : Resource> getOpt(): T? {
        return getOptByClass(T::class) as T?
    }

    @PublishedApi
    internal fun getOptByClass(kclass: KClass<out Resource>): Resource? {
        return children.firstNotNullOfOrNull { it.getOptByClass(kclass) }
    }
}

class Resources {
    @PublishedApi internal val resources: HashMap<KClass<out Resource>, Resource> = hashMapOf()

    inline fun <reified T : Resource> get(): T {
        return getOpt()!!
    }

    inline fun <reified T : Resource> getOpt(): T? {
        return getOptByClass(T::class) as T?
    }

    @PublishedApi
    internal fun getOptByClass(kclass: KClass<out Resource>): Resource? {
        return resources[kclass]
    }

    inline fun <reified T : Resource> set(res: T) {
        resources[T::class] = res
    }
}