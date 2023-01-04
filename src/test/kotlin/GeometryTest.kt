import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class GeometryTest {

    @Nested
    internal class Vec2Test {

        @ParameterizedTest
        @CsvSource(
            "LEFT, L, DOWN",
            "UP, R, RIGHT",
        )
        fun whenRotatedByTurnShouldPointToExpectedDirection(initialDirection: String, turn: String, expectedRotation: String) {
            val vec = Vec2.unit(Direction2.of(initialDirection))
            val rotated = vec * Rotation2.rotate(turn.first())
            assertEquals(Direction2.of(expectedRotation), rotated.toDirection2())
        }

        @ParameterizedTest
        @CsvSource(
            "LEFT, 90, DOWN",
            "LEFT, -180, RIGHT",
            "UP, -90, RIGHT",
        )
        fun whenRotatedByAngleShouldPointToExpectedDirection(initialDirection: String, turn: String, expectedRotation: String) {
            val vec = Vec2.unit(Direction2.of(initialDirection))
            val rotated = vec * Rotation2.rotate(turn.toInt())
            assertEquals(Direction2.of(expectedRotation), rotated.toDirection2())
        }

    }

    @Nested
    internal class Vec3Test {

        @ParameterizedTest
        @CsvSource(
            "UP, 90, BACK",  //BACK means behind the screen
            "DOWN, -90, BACK",
            "UP, 180, DOWN",
            "FRONT, -180, BACK",
            "RIGHT, 90, RIGHT",
        )
        fun whenRotatedByXAngleShouldPointToExpectedDirection(initialDirection: String, angle: String, expectedRotation: String) {
            val vec = Vec3.unit(Direction3.of(initialDirection))
            val rotated = vec * Rotation3.rotateX(angle.toInt())
            assertEquals(Direction3.of(expectedRotation), rotated.toDirection3())
        }

        @ParameterizedTest
        @CsvSource(
            "RIGHT, 90, BACK",  //BACK means behind the screen
            "RIGHT, -90, FRONT",
            "FRONT, -180, BACK",
            "UP, -180, UP",
        )
        fun whenRotatedByYAngleShouldPointToExpectedDirection(initialDirection: String, angle: String, expectedRotation: String) {
            val vec = Vec3.unit(Direction3.of(initialDirection))
            val rotated = vec * Rotation3.rotateY(angle.toInt())
            assertEquals(Direction3.of(expectedRotation), rotated.toDirection3())
        }

        @ParameterizedTest
        @CsvSource(
            "RIGHT, 90, UP",
            "RIGHT, -90, DOWN",
            "LEFT, -180, RIGHT",
            "BACK, -180, BACK",//BACK means behind the screen
        )
        fun whenRotatedByZAngleShouldPointToExpectedDirection(initialDirection: String, angle: String, expectedRotation: String) {
            val vec = Vec3.unit(Direction3.of(initialDirection))
            val rotated = vec * Rotation3.rotateZ(angle.toInt())
            assertEquals(Direction3.of(expectedRotation), rotated.toDirection3())
        }

    }


    @Nested
    internal class Rotation3Test {

        @Test
        fun whenRotatedByXAngleShouldPointToExpectedDirection(
        ) {
            val rot = Rotation3.rotateX(90) * Rotation3.rotateY(90)
            val rotated = Direction3.BACK.toVec3() * rot
            assertEquals(Direction3.DOWN, rotated.toDirection3())
        }
    }

}