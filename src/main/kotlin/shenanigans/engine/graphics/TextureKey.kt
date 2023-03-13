package shenanigans.engine.graphics

class TextureKey(val name: String? = null) {
    override fun equals(other: Any?): Boolean {
        if (other is TextureKey) {
            if (this.name != null && other.name != null) {
                return this.name == other.name
            }
            return super.equals(other)
        }
        return false
    }

    override fun hashCode(): Int {
        return name?.hashCode() ?: super.hashCode()
    }
}