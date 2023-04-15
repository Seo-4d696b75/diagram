package jp.seo.diagram.spherical

class Matrix private constructor(
    private val data: Array<Double>,
) {
    companion object {
        fun from(data: Array<Double>): Matrix {
            require(data.size == 9)
            return Matrix(data)
        }
    }

    operator fun get(i: Int, j: Int) = data[3 * i + j]

    operator fun plus(scalar: Double) = from(
        Array(9) { data[it] + scalar }
    )

    operator fun minus(scalar: Double) = from(
        Array(9) { data[it] - scalar }
    )

    operator fun times(scalar: Double) = from(
        Array(9) { data[it] * scalar }
    )

    operator fun div(scalar: Double) = from(
        Array(9) { data[it] / scalar }
    )

    operator fun times(v: Vector) = Vector(
        x = this[0,0] * v.x + this[0,1] * v.y + this[0,2] * v.z,
        y = this[1,0] * v.x + this[1,1] * v.y + this[1,2] * v.z,
        z = this[2,0] * v.x + this[2,1] * v.y + this[2,2] * v.z,
    )

    val det: Double
        get() = (
                this[0, 0] * this[1, 1] * this[2, 2]
                        + this[0, 1] * this[1, 2] * this[2, 0]
                        + this[0, 2] * this[1, 0] * this[2, 1]
                        - this[0, 2] * this[1, 1] * this[2, 0]
                        - this[0, 1] * this[1, 0] * this[2, 2]
                        - this[0, 0] * this[1, 2] * this[2, 1]
                )

    val inv: Matrix
        get() {
            val det = this.det
            require(!det.isZero)
            val a = from(
                arrayOf(
                    this[1, 1] * this[2, 2] - this[1, 2] * this[2, 1],
                    -this[0, 1] * this[2, 2] + this[0, 2] * this[2, 1],
                    this[0, 1] * this[1, 2] - this[0, 2] * this[1, 1],
                    -this[1, 0] * this[2, 2] + this[1, 2] * this[2, 0],
                    this[0, 0] * this[2, 2] - this[0, 2] * this[2, 0],
                    -this[0, 0] * this[1, 2] + this[0, 2] * this[1, 0],
                    this[1, 0] * this[2, 1] - this[1, 1] * this[2, 0],
                    -this[0, 0] * this[2, 1] + this[0, 1] * this[2, 0],
                    this[0, 0] * this[1, 1] - this[0, 1] * this[1, 0],
                )
            )
            return a / det
        }
}