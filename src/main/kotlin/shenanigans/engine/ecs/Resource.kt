package shenanigans.engine.ecs

import kotlin.reflect.KClass

interface Resource

class MutableResourcesView<T>(protected val map: LinkedHashMap<T, Resources>) : ResourcesView(*map.values.toTypedArray()) {
    /**
     * set an existing resource to the new value
     * @throws NoSuchElementException if there is no existing resource matching the given resource
     */
    inline fun <reified U : Resource> setExisting(value: U) {
        getContainerByResourceClass(U::class).set(value)
    }

    @PublishedApi
    internal fun getContainerByResourceClass(c: KClass<*>) : Resources {
        return children.first { it.resources[c] != null }
    }

    /**
     * set a new or existing resource at the given target domain
     * @throws IllegalArgumentException if the domain doesn't exist
     */
    fun setAt(value: Resource, target: T) {
        requireNotNull(map[target]).set(value)
    }
}

/**
 * an ordered read-only view of several sets of resources
 */
open class ResourcesView(protected vararg val children: Resources) {
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