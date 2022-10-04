package shenanigans.engine.graphics

//TBI: import shenanigans.engine.graphics.Texture
//TBI: import shenanigans.engine.graphics.Material

class TextItem constructor(private val text : String, fontFileName: String, final val numCols : Int, final val numRows : Int) /*: GameItem*/{//needs to super() gameItem when made

    private val VERTICES_PER_QUAD = 4
    //private val numCols : Int = col
    //private val numRows : Int = row
    //private val text : String = text

    //TBI: val texture = Texture(fontFileName)
    //TBI: this.setMesh(buidMesh(texture, numCols, numRows))



}

//fun getText(): String? {
    //return text
//}

//fun setText(text: String) {
    //this.text = text
    //val texture: Texture = this.getMesh().getMaterial().getTexture()
    //this.getMesh().deleteBuffers()
    //this.setMesh(buildMesh(texture, numCols, numRows))
//}

