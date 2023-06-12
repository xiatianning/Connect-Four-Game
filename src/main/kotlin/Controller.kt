class Controller(var model: Model) {
    // Start the game
    fun start() {
        model.startGame()
    }

    // Restart the game
    fun restart() {
        model.restartGame()
    }

    // Drop piece at col
    fun dropCircle(col: Int) {
        model.dropPiece(col)
    }

    // Get the row the most current piece is placed
    fun getRow(col: Int) : Int{
        return model.grid.grid.getColumn(col).count { it.player == Player.NONE }
    }
}