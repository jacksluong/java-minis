import javafx.animation.FadeTransition;
import javafx.animation.FillTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;

/**
 * Created by Jacky on 6/23/18.
 * This visual program will solve the Towers of Hanoi problem for N rings, in which it moves a tower of rings to
 * another tower completely. The only rules are:
 * - Only the top ring of any tower can be moved.
 * - Each ring has to be smaller than the ring below it (unless it's the bottom one).
 */

public class TowersOfHanoi extends Application {
    /*
     * Project Stages
     * √ 1. Complete for N = 5 with arrays
     * √ 2. Complete for N = 5 (should work for any N) with JavaFX and then cap it at N = 8
     * √ 3. Add a sidebar of buttons from 3 to 8 that will animate the Towers of Hanoi problem solution for that N.
     * √ 4. Add a pause/play button, reset button, light/dark mode button, and toggle column lines button.
     */

    /***************** Completed, tested, tasted, perfected -animation- (using arrays) ******************/

    /*----- Global -----*/
    final int N = 8; // cap at 8 because anything more than 8 takes too long to animate
    final Pane display = new Pane();
    final Line[] lines = new Line[4];
    final Rectangle[] rectangles = new Rectangle[N]; // 0 -> N - 1: smallest to biggest
    final Button[] buttons = new Button[10];
    final SequentialTransition st = new SequentialTransition(); // queue of animations

    @Override
    public void start(Stage primaryStage) {

        /*----- Initialization -----*/

        // Set display size
        display.setPrefWidth(150 + 90 * N);
        display.setPrefHeight(70 + 32 * N);
        // Set control panel size
        GridPane control = new GridPane();
        control.setMinWidth(200);
        control.setMinHeight(50 + 32 * N);
        control.setMaxHeight(50 + 32 * N);
        // Create buttons
        for (int i = 0; i <= 2; i++) for (int j = 0; j <= 1; j++)
            control.add((buttons[i * 2 + j] = new Button("Run " + (i * 2 + j + 3))), j, i);
        control.add((buttons[6] = new Button("Pause")), 0, 3);
        control.add((buttons[7] = new Button("Reset")), 1, 3);
        control.add((buttons[8] = new Button("Light")), 0, 4);
        control.add((buttons[9] = new Button("Lines")), 1, 4);
        // Create slider
        Slider slider = new Slider(0.5, 7.5, 1);
        Text text = new Text("Animation Speed: ");
        GridPane.setHalignment(text, HPos.RIGHT);
        control.add(text, 0, 5);
        control.add(slider, 1, 5);
        // Create lines
        Line
                l1 = lines[0] = new Line(38 + 15 * N, 53, 38 + 15 * N, 50 + 32 * N),
                l2 = lines[1] = new Line(73 + 45 * N, 53, 73 + 45 * N, 50 + 32 * N),
                l3 = lines[2] = new Line(108 + 75 * N, 53, 108 + 75 * N, 50 + 32 * N),
                l4 = lines[3] = new Line(15, 51 + 32 * N, 135 + 90 * N, 51 + 32 * N);
        // Set up HBox
        HBox hbox = new HBox(control, display);
        hbox.setStyle("-fx-background-color: black;");



        /*----- Formatting -----*/

        // Format control panel
        control.setStyle("-fx-background-color: lightblue; -fx-background-radius: 25;");
        HBox.setMargin(control, new Insets(10));
        int rows = 6, columns = 2;
        for (int i = 0; i < rows; i++) { // number of rows
            RowConstraints r = new RowConstraints();
            r.setPercentHeight(100 / rows);
            control.getRowConstraints().add(r);
        }
        for (int i = 0; i < columns; i++) { // number of columns
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(100 / columns);
            control.getColumnConstraints().add(c);
        }
        // Format controls and give them their functions
        for (Button b : buttons) {
            GridPane.setValignment(b, VPos.CENTER);
            GridPane.setHalignment(b, HPos.CENTER);
            b.setPrefWidth(56);
        }
        buttons[0].setOnAction(e -> simulate(3));
        buttons[1].setOnAction(e -> simulate(4));
        buttons[2].setOnAction(e -> simulate(5));
        buttons[3].setOnAction(e -> simulate(6));
        buttons[4].setOnAction(e -> simulate(7));
        buttons[5].setOnAction(e -> simulate(8));
        buttons[6].setDisable(true); ////////////////////////////////////////////////////////////// play / pause
        buttons[6].setStyle("-fx-background-color: #ff7f7f;");
        buttons[6].setOnAction(e -> {
            if (buttons[6].getText().equals("Pause")) {
                buttons[6].setText("Play");
                buttons[6].setStyle("-fx-background-color: #7fff7f;");
                st.pause();
            } else {
                buttons[6].setText("Pause");
                buttons[6].setStyle("-fx-background-color: #ff7f7f;");
                st.play();
            }
        });
        buttons[7].setDisable(true); ////////////////////////////////////////////////////////////// reset
        buttons[7].setStyle("-fx-background-color: darkred; -fx-text-fill: white;");
        buttons[7].setOnAction(e -> stop());
        buttons[8].setStyle("-fx-background-color: white; -fx-text-fill: black;"); //////////////// dark/light
        buttons[8].setOnAction(e -> {
            if (buttons[8].getText().equals("Light")) {
                hbox.setStyle("-fx-background-color: #fff282;");
                control.setStyle("-fx-background-color: #00214c; -fx-background-radius: 25;");
                l4.setStyle("-fx-stroke: black; -fx-stroke-width: 2;");
                buttons[8].setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white;");
                buttons[8].setText("Dark");
                text.setFill(Color.WHITE);
            } else {
                hbox.setStyle("-fx-background-color: black;");
                control.setStyle("-fx-background-color: lightblue; -fx-background-radius: 25;");
                l4.setStyle("-fx-stroke: white; -fx-stroke-width: 2;");
                buttons[8].setStyle("-fx-background-color: white; -fx-text-fill: black;");
                buttons[8].setText("Light");
                text.setFill(Color.BLACK);
            }
        });
        buttons[9].setOnAction(e -> { ///////////////////////////////////////////////////////////// toggle lines
            if (l1.getOpacity() == 1) {
                l1.setOpacity(0);
                l2.setOpacity(0);
                l3.setOpacity(0);
            } else {
                l1.setOpacity(1);
                l2.setOpacity(1);
                l3.setOpacity(1);
            }
        });
        slider.valueProperty().addListener((observable, oldValue, newValue) -> st.setRate((double) newValue));
        st.setOnFinished(e -> buttons[6].setDisable(true));
        // Format lines
        l1.setStyle("-fx-stroke-width: 4; -fx-stroke: grey;");
        l2.setStyle("-fx-stroke-width: 4; -fx-stroke: grey;");
        l3.setStyle("-fx-stroke-width: 4; -fx-stroke: grey;");
        l4.setStyle("-fx-stroke-width: 2; -fx-stroke: white;");
        display.getChildren().addAll(l1, l2, l3, l4);
        // Create and format rectangles
        for (int i = 0; i < N; i++) { ///////////////////////////////////////////////////////////// rectangles
            rectangles[i] = new Rectangle(0, 0, 50 + 30 * i, 30);
            rectangles[i].setStyle("-fx-fill: rgb(140, 31, 242);" +
                    "-fx-arc-height: 12;" +
                    "-fx-arc-width: 12;");
        }



        /*----- Begin -----*/
        Scene scene = new Scene(hbox, 380 + 90 * N, 70 + 32 * N, Color.BLACK);
        primaryStage.setTitle("The Towers of Hanoi");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void simulate(int n) {
        /*----- Resetting 1 -----*/
        stop();
        buttons[6].setDisable(false);
        buttons[7].setDisable(false);



        /*----- Import rectangles -----*/
        Rectangle[][] towers = new Rectangle[3][n];
        for (int i = 0; i < n; i++) {
            rectangles[i].setTranslateX(148 - 15 * (i + 1));
            rectangles[i].setTranslateY(305 - 32 * (n - i));
        }
        towers[0] = Arrays.copyOf(rectangles, n);
        for (Rectangle r : towers[0]) display.getChildren().add(r);



        /*----- Create animations -----*/
        st.getChildren().clear();
        st.getChildren().add(new FadeTransition(Duration.millis(100), display)); // for eyes to ready up
        recur(n, n, 2, towers);

        st.play();
    }

    public void recur(int n, int tower, int correctIndex, Rectangle[][] towers) {
        Rectangle thisR = rectangles[tower - 1]; // for easier referencing

        // Locate the index of the current tower in focus
        int currentIndex;
        if (correctIndex == 0)
            if (contains(n, 1, thisR, towers)) currentIndex = 1; else currentIndex = 2;
        else if (correctIndex == 1)
            if (contains(n, 2, thisR, towers)) currentIndex = 2; else currentIndex = 0;
        else
            if (contains(n, 0, thisR, towers)) currentIndex = 0; else currentIndex = 1;

        // Move everything on top of the tower base off
        if (tower > 1) recur(n, tower - 1, 3 - currentIndex - correctIndex, towers);

        // Locate the level of the tower base and where it's to be moved (throw an error if something is wrong)
        int currentLevel = n - 1, openIndex = n - 1;
        while (towers[currentIndex][currentLevel] != thisR) currentLevel--;
        while (towers[correctIndex][openIndex] != null) openIndex--;

        // Conduct transfer
        towers[correctIndex][openIndex] = thisR;
        towers[currentIndex][currentLevel] = null;

        /*----- Add animation -----*/
//        int timeFactor = n <= 4 ? 1 : (n - 3); //////////////////////////////////////////////////// animation
        // color
        FillTransition f1 = new FillTransition(Duration.millis(150), thisR,
                Color.rgb(140, 31, 242), Color.rgb(91,19,182));
        // lift
        TranslateTransition t1 = new TranslateTransition(
                Duration.millis(150 * (currentLevel + 1)), thisR);
        t1.setByY(-32 * (currentLevel + 1 + N - n));
        // move
        TranslateTransition t2 = new TranslateTransition(
                Duration.millis(350 * Math.abs(currentIndex - correctIndex)), thisR);
        t2.setByX((correctIndex - currentIndex) * (35 + 30 * N));
        // lower
        TranslateTransition t3 = new TranslateTransition(
                Duration.millis(150 * (openIndex + 1)), thisR);
        t3.setByY(32 * (openIndex + 1 + N - n));
        // color
        FillTransition f2 = new FillTransition(Duration.millis(150), thisR,
                Color.rgb(91,19,182), Color.rgb(140, 31, 242));
        st.getChildren().addAll(f1, t1, t2, t3, f2);

        // Move everything back on top of the tower base, as it should be (completing the tower)
        if (tower > 1) recur(n, tower - 1, correctIndex, towers);
    }

    public boolean contains(int n, int index, Rectangle r, Rectangle[][] towers) {
        for (int i = 0; i < n; i++) if (towers[index][i] == r) return true;
        return false;
    }

    public void stop() {
        st.stop();
        st.getChildren().clear();
        display.getChildren().removeAll(rectangles);
        buttons[6].setDisable(true);
        buttons[7].setDisable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }

    /***************** Completed, tested, tasted, perfected -algorithm- (using arrays) ******************/
    /*

    static final int N = 6;
    static int[][] towers = new int[3][N]; // biggest numbers first

    public static void main(String[] args) {
        // init
        for (int i = 0; i < N;) towers[0][i] = N - i++; // original tower at first index
        recur(N, 1); // final tower will be at index 1, but it doesn't matter if it's 1 or 2
        print();
    }

    public static void recur(int tower, int correctIndex) {
        // Locate the index of the current tower in focus
        int currentIndex;
        if (correctIndex == 0)
            if (contains(1, tower)) currentIndex = 1; else currentIndex = 2;
        else if (correctIndex == 1)
            if (contains(2, tower)) currentIndex = 2; else currentIndex = 0;
        else
        if (contains(0, tower)) currentIndex = 0; else currentIndex = 1;

        // Move everything on top of the tower base off
        if (tower > 1) recur(tower - 1, 3 - currentIndex - correctIndex);

        // Locate the level of the tower base and where it's to be moved (throw an error if something is wrong)
        int currentLevel = N - 1, openIndex = 0;
        while (towers[currentIndex][currentLevel] != tower) currentLevel--;
        while (towers[correctIndex][openIndex] != 0) openIndex++;

        // Conduct transfer
        towers[correctIndex][openIndex] = tower;
        towers[currentIndex][currentLevel] = 0;

        // Move everything back on top of the tower base, as it should be (completing the tower)
        if (tower > 1) recur(tower - 1, correctIndex);
    }

    public static boolean contains(int index, int value) {
        for (int i = 0; i < N; i++) if (towers[index][i] == value) return true;
        return false;
    }

    public static void print() {
        for (int i = N - 1; i >= 0; i--) {
            for (int j = 0; j <= 2; j++) {
                if (towers[j][i] != 0)
                    System.out.print(" " + towers[j][i] + " ");
                else if (i == 0)
                    System.out.print(" X ");
                else
                    System.out.print("   ");
            }
            System.out.println();
        }
        System.out.println("---------");
    }
    */

}
