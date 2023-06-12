import javafx.animation.*
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Rectangle
import javafx.scene.shape.Shape
import javafx.scene.text.Font
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.util.Duration
import kotlin.math.floor

class View(private val model: Model, private val controller: Controller) : Pane(), IView {

    private val rootWidth = 800.0
    private val rootHeight = 600.0
    private val marginLeft = 120.0
    private val marginTop = 100.0
    private val cellSize = 70.0
    private val radius = 25.0
    private var currentPlayer = Player.NONE
    private var currentColumn = -1

    // Make Circle that can be dragged (adapted from lecture slide)
    data class DragInfo(var target : Circle? = null,
                        var anchorX: Double = 0.0,
                        var anchorY: Double = 0.0,
                        var initialX: Double = 0.0,
                        var initialY: Double = 0.0)
    var dragInfo = DragInfo()
    private fun makeCircle(x: Double, y: Double, col: Color) : Shape {
        val circle = Circle(x, y, radius, col).apply {
            addEventFilter(MouseEvent.MOUSE_PRESSED) {
                dragInfo = DragInfo(this, it.sceneX, it.sceneY, translateX, translateY)
            }
            addEventFilter(MouseEvent.MOUSE_DRAGGED) {
                translateX = dragInfo.initialX + it.sceneX - dragInfo.anchorX
            }
            addEventFilter(MouseEvent.MOUSE_RELEASED) {
                if (this.translateX > marginLeft  && this.translateX < rootWidth - marginLeft ) {
                    currentColumn = floor((this.translateX - marginLeft) / cellSize).toInt()
                    controller.dropCircle(currentColumn)
                } else {
                    moveBack()
                }
                dragInfo = DragInfo()
            }
        }
        return circle
    }
    private val circlePane = Pane().apply {
        addEventHandler(MouseEvent.MOUSE_DRAGGED) {
            if (dragInfo.target != null) {
                val currentX = dragInfo.target!!.translateX
                if (currentX < radius) {
                    dragInfo.target!!.translateX = radius
                } else if (currentX> rootWidth - radius) {
                    dragInfo.target!!.translateX = rootWidth - radius
                } else if (currentX > marginLeft  && currentX < rootWidth - marginLeft) {
                    dragInfo.target!!.translateX = currentX - (currentX - marginLeft) % cellSize + cellSize / 2
                }
            }
        }
    }

    // Player control text
    private val playerOne = Text("Player #1").apply {
        x = 10.0
        y = 25.0
        fill = Color.GRAY
        font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
        HBox.setHgrow(this, Priority.NEVER)
    }
    private val playerTwo = Text("Player #2").apply {
        x = rootWidth - 98.0
        y = 25.0
        fill = Color.GRAY
        font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
        HBox.setHgrow(this, Priority.NEVER)
    }

    // Start game button
    private val startRect = Rectangle(400.0, 50.0).apply {
        translateX = 200.0
        translateY = 40.0
        fill = Color.PALEGREEN
        onMouseClicked = EventHandler {
            controller.start()
        }
    }
    private val startText = Text("Click here to start game!").apply {
        translateX = 285.0
        translateY = 70.0
        font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
        onMouseClicked = EventHandler {
            controller.start()
        }
    }
    private val startButton = Pane(startRect, startText)

    // Make the grid of the game
    private fun drawGrid() : Shape {
        var grid: Shape = Rectangle(
            model.width * cellSize,
            model.height * cellSize
        )
        for (i in 0 until model.width) {
            for (j in 0 until model.height) {
                val circle = Circle(0.0, 0.0, radius)
                circle.apply {
                    translateX = cellSize * i + cellSize / 2
                    translateY = cellSize * j + cellSize / 2
                }
               grid = Shape.subtract(grid, circle)
            }
        }
        for (i in 1 until model.width) {
            val line = Line(i * cellSize, 0.0, i * cellSize, model.height * cellSize)
            grid = Shape.subtract(grid, line)
        }
        for (i in 1 until model.height) {
            val line = Line(0.0, i * cellSize, model.width * cellSize, i * cellSize)
            grid = Shape.subtract(grid, line)
        }
        grid.apply {
            translateX = marginLeft
            translateY = marginTop
            fill = Color.DEEPSKYBLUE
            stroke = Color.BLACK
        }
        return grid
    }

    // Circle move back animation
    private fun moveBack() {
        val currentCircle = circlePane.children[circlePane.children.size - 1]
        val endX = when (currentPlayer) {
            Player.ONE -> 55.0
            else -> rootWidth - 55.0
        }
        val animation = Timeline(
            KeyFrame(
                Duration.millis(800.0),
                KeyValue(currentCircle.translateXProperty(), endX, Interpolator.LINEAR)
            )
        )
        animation.play()
    }

    // Enlarge then reduce animation
    private fun scaleTransition(node: Node) : Timeline {
        return Timeline(
            KeyFrame(
                Duration.millis(2000.0),
                KeyValue(node.scaleXProperty(), 2.0),
                KeyValue(node.scaleYProperty(), 2.0)
            ),
            KeyFrame(
                Duration.millis(4000.0),
                KeyValue(node.scaleXProperty(), 1.0),
                KeyValue(node.scaleYProperty(), 1.0)
            )
        )
    }

    // Play enlarge then reduce animation
    private fun playAnimation(rect: Node, text: Node) {
        val rectAnimation = scaleTransition(rect)
        val textAnimation = scaleTransition(text)
        rectAnimation.play()
        textAnimation.play()
        textAnimation.onFinished = EventHandler {
            children.removeAt(children.size - 1)
            children.add(restartButton())
        }
    }

    // Display win message
    private fun winMessage(player: Player) : Pane {
        val color = when (player) {
            Player.ONE -> Color.RED
            else -> Color.YELLOW
        }
        val winRect = Circle(0.0, 0.0, 100.0, color).apply {
            translateX = rootWidth / 2
            translateY = rootHeight / 2
        }
        val text = when (player) {
            Player.ONE -> "Player #1 won!!!"
            else -> "Player #2 won!!!"
        }
        val winText = Text(text).apply {
            translateX = rootWidth / 2 - 75.0
            translateY = rootHeight / 2 + 10.0
            font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
        }
        playAnimation(winRect, winText)
        return Pane(winRect, winText)
    }

    // Display draw message
    private fun drawMessage() : Pane {
        val drawRect = Circle(0.0, 0.0, 100.0, Color.GRAY).apply {
            translateX = rootWidth / 2
            translateY = rootHeight / 2
        }
        val drawText = Text("Draw").apply {
            translateX = rootWidth / 2 - 24.0
            translateY = rootHeight / 2 + 10.0
            font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
        }
        playAnimation(drawRect, drawText)
        return Pane(drawRect, drawText)
    }

    // Restart game button
    private fun restartButton() : Pane {
        val restartRect = Rectangle(400.0, 50.0).apply {
            translateX = 0.0
            translateY = 40.0
            fill = Color.PALEGREEN
            onMouseClicked = EventHandler {
                restart()
            }
        }
        val restartText = Text("Click here to start game again!").apply {
            translateX = 58.0
            translateY = 70.0
            font = Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 18.0)
            onMouseClicked = EventHandler {
                restart()
            }
        }
        val restartButton = Pane(restartRect, restartText)
        val animation = Timeline(
            KeyFrame(
                Duration.millis(5000.0),
                KeyValue(restartButton.translateXProperty(), 400.0, Interpolator.LINEAR)
            )
        ).apply {
            cycleCount = Animation.INDEFINITE
            isAutoReverse = true
        }
        animation.play()
        return restartButton
    }

    // Clear one circle animation
    private fun clearCircle(index: Int) {
        val circle = circlePane.children[index]
        val animation = Timeline(
            KeyFrame(
                Duration.millis(500.0),
                KeyValue(circle.translateYProperty(), -100.0, Interpolator.EASE_IN)
            )
        )
        animation.play()
        animation.onFinished = EventHandler {
            if (index - 1 >= 0) {
                clearCircle(index - 1)
            } else {
                controller.restart()
            }
        }
    }

    // Clear grid animation
    private fun clearGrid() {
        clearCircle(circlePane.children.size - 1)
    }

    // Restart game
    private fun restart() {
        children.removeAt(children.size - 1)
        clearGrid()
    }

    override fun updateView() {
        children.clear()
        circlePane.children.clear()
        children.addAll(circlePane, playerOne, playerTwo, startButton, drawGrid())
    }

    init {
        // Set background color
        background = Background(
            BackgroundFill(
                Color.rgb(240, 240, 240),
                CornerRadii(0.0),
                Insets.EMPTY
            )
        )
        // Add listeners to model properties
        model.onGameStart.addListener { _, _, new ->
            if (new) {
                children.remove(startButton)
            }
        }
        model.onNextPlayer.addListener { _, _, new ->
            currentPlayer = new
            playerOne.fill = when (new) {
                Player.ONE -> Color.BLACK
                else -> Color.GRAY
            }
            playerTwo.fill = when (new) {
                Player.TWO -> Color.BLACK
                else -> Color.GRAY
            }
            if (new == Player.ONE) {
                circlePane.children.add(
                    makeCircle(0.0, 0.0, Color.RED).apply {
                        translateX = 55.0
                        translateY = 65.0
                    }
                )
            } else if (new == Player.TWO) {
                circlePane.children.add(
                    makeCircle(0.0, 0.0, Color.YELLOW).apply {
                        translateX = rootWidth - 55.0
                        translateY = 65.0
                    }
                )
            }
        }
        model.onPieceDropped.addListener { _, _, new ->
            val currentCircle = circlePane.children[circlePane.children.size - 1]
            if (new != null) {
                circlePane.children.remove(currentCircle)
                val currentColor = when (currentPlayer) {
                    Player.ONE -> Color.RED
                    else -> Color.YELLOW
                }
                val drawable = Circle(0.0, 0.0, radius, currentColor).apply {
                    translateX = currentCircle.translateX
                    translateY = currentCircle.translateY
                }
                val row = controller.getRow(currentColumn)
                val endY = marginTop + cellSize * row + cellSize / 2
                val animation = Timeline(
                    KeyFrame(
                        Duration.millis(100.0 + row * 100.0),
                        KeyValue(drawable.translateYProperty(), endY, Interpolator.EASE_IN)
                    )
                )
                animation.play()
                circlePane.children.add(drawable)
            } else {
                moveBack()
            }
        }
        model.onGameWin.addListener { _, _, new ->
            if (new != Player.NONE) {
                children.add(winMessage(new))
                playerOne.fill = Color.GRAY
                playerTwo.fill = Color.GRAY
            }
        }
        model.onGameDraw.addListener { _, _, new ->
            if (new) {
                children.add(drawMessage())
                playerOne.fill = Color.GRAY
                playerTwo.fill = Color.GRAY
            }
        }
        // Register with the model
        model.addView(this)
    }
}