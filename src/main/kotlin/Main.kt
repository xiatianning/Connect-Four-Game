import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

class Main : Application() {
    override fun start(stage: Stage) {

        // Create and initialize the model
        val model = Model()

        // Create the Controller, and pass it with the model
        // The controller will handle input and pass requests to the model
        val controller = Controller(model)

        // Create the views, and pass them with the model and controller
        // The views will register themselves and handle displaying the data from the model
        val root = View(model, controller)

        stage.apply {
            scene = Scene(root, 800.0, 600.0)
            title = "CS349 - A3 Connect Four - t37xia"
            isResizable = false
        }
        stage.show()
    }
}