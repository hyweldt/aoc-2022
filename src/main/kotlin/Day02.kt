fun main() {
    val x = Utils.readResource("02.txt")
    var score = 0
    for (round in x.lines()) {
        if (round.trim().isNotEmpty()) {
            score += scoreRound(round)
        }
    }
    println(score)
}

enum class OpponentShape {
    ROCK, PAPER, SCISSORS;
    companion object {
        fun fromString(s: String): OpponentShape {
            return when (s) {
                "A" -> ROCK
                "B" -> PAPER
                "C" -> SCISSORS
                else -> throw Error("Invalid opponent shape $s")
            }
        }
    }
}

enum class PlayerShape {
    ROCK, PAPER, SCISSORS;
    companion object {
        fun fromString(s: String): PlayerShape {
            return when (s) {
                "X" -> ROCK
                "Y" -> PAPER
                "Z" -> SCISSORS
                else -> throw Error("Invalid player shape $s")
            }
        }
    }

}

enum class Outcome {
    LOSE, WIN, DRAW;

    companion object {
        fun fromString(s: String): Outcome {
            return when (s) {
                "X" -> LOSE
                "Y" -> DRAW
                "Z" -> WIN
                else -> throw Error("Invalid player shape $s")
            }
        }
    }
}

val OUTCOME_SCORES = mapOf(
    Outcome.LOSE to 0, Outcome.DRAW to 3, Outcome.WIN to 6
)

val SHAPE_SCORES = mapOf(
   PlayerShape.ROCK to 1, PlayerShape.PAPER to 2, PlayerShape.SCISSORS to 3
)

fun findShapeForOutcome(opponentShape: OpponentShape, outcome: Outcome): PlayerShape {
    return when (opponentShape) {
        OpponentShape.ROCK -> when (outcome) {
            Outcome.LOSE -> PlayerShape.SCISSORS
            Outcome.DRAW -> PlayerShape.ROCK
            Outcome.WIN -> PlayerShape.PAPER
        }
        OpponentShape.SCISSORS -> when (outcome) {
            Outcome.LOSE -> PlayerShape.PAPER
            Outcome.DRAW -> PlayerShape.SCISSORS
            Outcome.WIN -> PlayerShape.ROCK
        }
        OpponentShape.PAPER -> when (outcome) {
            Outcome.LOSE -> PlayerShape.ROCK
            Outcome.DRAW -> PlayerShape.PAPER
            Outcome.WIN -> PlayerShape.SCISSORS
        }
    }
}

fun scoreWinLoss(opponentShape: String, myShape: String): Int {
    if (
        opponentShape == "A" && myShape == "X"
        || opponentShape == "B" && myShape == "Y"
        || opponentShape == "C" && myShape == "Z"
    ) {
        //draw
        return 3
    } else if (
        opponentShape == "A" && myShape == "Y"
        || opponentShape == "B" && myShape == "Z"
        || opponentShape == "C" && myShape == "X"
    ) {
        return 6
    }
    return 0
}

fun scoreRound(s: String): Int {
    val (opponentShapeString, myShapeString) = s.split(' ')
    val opponentShape = OpponentShape.fromString(opponentShapeString)
    val outcome = Outcome.fromString(myShapeString)
    val myShape = findShapeForOutcome(opponentShape, outcome)
    return OUTCOME_SCORES[outcome]!! + SHAPE_SCORES[myShape]!!
}