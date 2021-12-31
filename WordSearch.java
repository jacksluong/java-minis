import visuals.FeedbackManager;
import visuals.PopupPrompt;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by Jacky on 1/29/2019.
 * This visual program proceeds to search for a target string in a grid of squares containing anywhere from 1 to 3
 * letters. There are different options for how the program will search for consecutive letters to form the
 * target word, including:
 * - Row major order (consecutive tiles of rows are treated as contiguous, meaning a string is not limited to one row)
 * - Diagonal (diagonally consecutive tiles in any direction)
 * - Snake in only cardinal directions (any chain of cardinally adjacent tiles without using one more than once)
 * - Snake in any direction (any chain of adjacent tiles without using one more than once)
 */

public class WordSearch extends Application {
    
    public final int PARSE_ANIMATION_BASE_SPEED = 250;
    
    Trail trail; // for animating the path
    Tile[][] tiles;
    String target;
    FeedbackManager feedbackManager;
    int tilesSetUp = 0;
    boolean allSingles = true; // condition whether all tiles contain only one letter, to signal for a better search
    // alg.
    
    Scene scene;
    VBox panel;
    Stage stage;
    SequentialTransition animatePath = new SequentialTransition();
    CheckBox showParse = new CheckBox("Show parse");
    
    @Override
    public void start(Stage primaryStage) {
        Rectangle shade = new Rectangle(600, 400);
        shade.setStyle("-fx-fill: black; -fx-opacity: 0.6");
        PopupPrompt popup = new PopupPrompt("Setup", new String[]{"Rows", "Columns"});
        StackPane prompter = new StackPane(shade, popup);
        Pane whole = new Pane(prompter);
        feedbackManager = new FeedbackManager(whole, 115, 0);
        showParse.setFont(new Font(9));
        scene = new Scene(whole, 600, 400);
        stage = primaryStage;
        
        popup.setAlert(0, input -> Integer.parseInt(input) <= 0 ? "Must have a positive number of rows." : "");
        popup.setAlert(1, input -> Integer.parseInt(input) <= 0 ? "Must have a positive number of columns." : "");
        popup.setQuitVisible(false);
        
        whole.setOnKeyPressed(key -> {
            if (whole.getChildren().contains(prompter) &&
                    key.getCode() == KeyCode.ENTER && popup.noEmptyFields()) popup.triggerSubmit();
        });
        
        popup.setSubmitAction(begin -> {
            String[] answers = popup.getAnswers();
            tiles = new Tile[Integer.parseInt(answers[0])][Integer.parseInt(answers[1])];
            feedbackManager.setMaxWidth(45 * tiles[0].length);
            feedbackManager.setLimit(5);
            whole.getChildren().remove(prompter);
            whole.getChildren().add(showParse);
            for (int row = 0; row < tiles.length; row++) {
                for (int column = 0; column < tiles[0].length; column++) {
                    Tile newTile = new Tile(row, column);
                    tiles[row][column] = newTile;
                    whole.getChildren().add(newTile);
                }
            }
            
            primaryStage.setWidth(150 + tiles[0].length * 50);
            primaryStage.setHeight((40 + tiles.length * 50) > 350 ? 40 + tiles.length * 50 : 350);
            showParse.setTranslateX(10);
            showParse.setTranslateY((40 + tiles.length * 50) > 310 ? -10 + tiles.length * 50 : 310);
            
            Text instructions = new Text("Set up the board.\nLetters only.");
            panel = new VBox(instructions);
            panel.setSpacing(20);
            panel.setPrefWidth(90);
            panel.setTranslateX(10);
            panel.setTranslateY(10);
            panel.setAlignment(Pos.CENTER);
            whole.getChildren().add(panel);
            Button rowMajor = new Button("Row Major"),
                    diagonal = new Button("Diagonal"),
                    snakeCardinal = new Button("Snake 4D"),
                    snakeAll = new Button("Snake 8D");
            Text target = new Text("\"\"");
            target.setWrappingWidth(90);
            rowMajor.setAlignment(Pos.CENTER);
            diagonal.setAlignment(Pos.CENTER);
            snakeCardinal.setAlignment(Pos.CENTER);
            snakeAll.setAlignment(Pos.CENTER);
            rowMajor.setOnAction(clicked -> search(0));
            diagonal.setOnAction(clicked -> search(1));
            snakeCardinal.setOnAction(clicked -> search(2));
            snakeAll.setOnAction(clicked -> search(3));
            
            tiles[0][0].fillTrans(Color.LIGHTGRAY, Color.GRAY);
            
            initiateSetup(whole, prompter, popup, new Node[]{rowMajor, diagonal, snakeCardinal, snakeAll, target});
            
        });
        
        
        primaryStage.setScene(scene);
        primaryStage.show();
        // add JavaFX afterwards
    }
    
    public void initiateSetup(Pane whole, StackPane prompter, PopupPrompt popup, Node[] panelNodes) {
        allSingles = true;
        scene.setOnKeyPressed(key -> {
            Tile current = tiles[tilesSetUp / tiles[0].length][tilesSetUp % tiles[0].length];
            if (key.getCode() == KeyCode.ENTER) {
                if (!current.getText().isEmpty()) {
                    current.fillTrans(Color.GRAY, Color.LIGHTBLUE);
                    if (current.getText().length() > 1) allSingles = false;
                    tilesSetUp++;
                    if (tilesSetUp < tiles.length * tiles[0].length)
                        tiles[tilesSetUp / tiles[0].length][tilesSetUp % tiles[0].length].fillTrans(Color.LIGHTGRAY,
                                Color.GRAY);
                    else {
                        ((Text) panel.getChildren().get(0)).setText("T: change target\n    text (below)\nR: reset " +
                                "board");
                        panel.getChildren().addAll(panelNodes);
                        
                        scene.setOnKeyPressed(newKey -> {
                            if (!whole.getChildren().contains(prompter)) {
                                switch (newKey.getCode()) {
                                    case T:
                                        if (popup.getSize() > 1) popup.deletePrompt(0);
                                        popup.clearFields();
                                        popup.setPrompt(0, "Target");
                                        popup.setAlert(0, answer -> answer.isEmpty() ? "String required." : "");
                                        popup.setSubmitAction(submitted -> {
                                            ((Text) panel.getChildren().get(5)).setText("\"" + popup.getAnswers()[0]
                                                    .toUpperCase().replaceAll("[^A-Z]", "") + "\"");
                                            whole.getChildren().remove(prompter);
                                            whole.getChildren().add(showParse);
                                            
                                            stage.setWidth(150 + tiles[0].length * 50);
                                            stage.setHeight((40 + tiles.length * 50) > 350 ? 40 + tiles.length * 50 :
                                                    350);
                                        });
                                        whole.getChildren().add(prompter);
                                        whole.getChildren().remove(showParse);
                                        popup.requestFocus();
                                        stage.setWidth(600);
                                        stage.setHeight(400);
                                        break;
                                    case R:
                                        ((Text) panel.getChildren().get(0)).setText("Set up the board.\nLetters only.");
                                        panel.getChildren().removeAll(panelNodes);
                                        for (Tile[] row : tiles)
                                            for (Tile tile : row)
                                                tile.reset();
                                        tilesSetUp = 0;
                                        tiles[0][0].fillTrans(Color.LIGHTGRAY, Color.GRAY);
                                        initiateSetup(whole, prompter, popup, panelNodes);
                                        break;
                                    case DIGIT0:
                                        ((Button) panelNodes[0]).fire();
                                        break;
                                    case DIGIT1:
                                        ((Button) panelNodes[1]).fire();
                                        break;
                                    case DIGIT2:
                                        ((Button) panelNodes[2]).fire();
                                        break;
                                    case DIGIT3:
                                        ((Button) panelNodes[3]).fire();
                                }
                            }
                        });
                    }
                } else current.fillTrans(Color.RED, Color.GRAY);
            } else if (key.getCode().isLetterKey()) {
                current.appendChar(key.getCode().getName());
            } else if (key.getCode() == KeyCode.BACK_SPACE) {
                current.removeChar();
            }
        });
    }
    
    public void search(int mode) {
        animatePath.getChildren().clear();
        target = ((Text) panel.getChildren().get(5)).getText();
        target = target.substring(1, target.length() - 1);
        if (target.isEmpty()) {
            feedbackManager.addMessage("No target given!");
            animateError();
            return;
        }
        trail = new Trail(target.length());
        for (Tile[] row : tiles)
            for (Tile tile : row) {
                tile.backgroundColor.setFill(Color.LIGHTBLUE);
            }
        switch (mode) {
            case 0:
                rowMajor();
                break;
            case 1:
                diagonal();
                break;
            case 2:
                snake(false);
                break;
            case 3:
                snake(true);
                break;
        }
    }
    
    /*---------------------------------------------*/
    /**------------------ Search -----------------**/
    /*---------------------------------------------*/
    public void rowMajor() {
        String all = "";
        String[] oneD = new String[tiles.length * tiles[0].length];
        int index;
        for (int i = 0; i < tiles.length; i++)
            for (int j = 0; j < tiles[0].length; j++) {
                all += tiles[i][j].getText();
                oneD[i * tiles[0].length + j] = tiles[i][j].getText();
            }
        
        if (allSingles) {
            feedbackManager.addMessage("This algorithm doesn't not require parsing.");
            if ((index = all.indexOf(target)) != -1) {
                for (int i = 0; i < target.length(); i++)
                    addToTrail(tiles[(index + i) / tiles.length][(index + i) % tiles.length]);
                animatePathFound();
            } else
                animateError();
        } else {
            index = 0;
            int stringIndex, currentIndex;
            while (index < oneD.length) {
                stringIndex = 0;
                currentIndex = 0;
                while (target.length() >= stringIndex + oneD[index + currentIndex].length() &&
                        target.substring(stringIndex,
                                stringIndex + oneD[index + currentIndex].length()).equals(oneD[index + currentIndex])) {
                    addToTrail(tiles[(index + currentIndex) / tiles[0].length][(index + currentIndex) % tiles[0].length]);
                    stringIndex += oneD[index + currentIndex++].length();
                    if (stringIndex == target.length()) { // found
                        animatePathFound();
                        return;
                    }
                }
                
                do {
                    all = all.replaceFirst(oneD[index], "");
                    index++;
                    if (all.length() < target.length()) {
                        animateError();
                        return;
                    }
                    // so the beginning of all is always a potential occurrence of target
                } while (index < oneD.length && !all.substring(0, target.length()).equals(target));
                clearTrail();
            }
            
            animateError();
            
        }
        
    }
    
    public void diagonal() {
        if (allSingles && tiles.length >= target.length() && tiles[0].length >= target.length()) {
            
            int viableColumns = tiles[0].length - target.length() + 1,
                    viableRows = tiles.length - target.length() + 1;
            for (int row = 0; row < viableRows * 2; row++) {
                for (int column = 0; column < viableColumns * 2; column++) {
                    
                    int index = 0;
                    
                    if (row < viableRows) { // good to look up
                        if (column < viableColumns) { // good to look left
                            for (int i = 0; i < target.length(); i++) {
                                int currentRow = target.length() + row - i - 1;
                                int currentColumn = target.length() + column - i - 1;
                                if (!tiles[currentRow][currentColumn].getText().equals("" + target.charAt(i))) {
                                    clearTrail();
                                    break;
                                } else {
                                    addToTrail(tiles[currentRow][currentColumn]);
                                    index += tiles[currentRow][currentColumn].getText().length();
                                    if (index == target.length()) {
                                        animatePathFound();
                                        return;
                                    }
                                }
                            }
                        } else { // good to look right
                            for (int i = 0; i < target.length(); i++) {
                                int currentRow = target.length() + row - i - 1;
                                int currentColumn = column - viableColumns + i;
                                if (!tiles[currentRow][currentColumn].getText().equals("" + target.charAt(i))) {
                                    clearTrail();
                                    break;
                                } else {
                                    addToTrail(tiles[currentRow][currentColumn]);
                                    index += tiles[currentRow][currentColumn].getText().length();
                                    if (index == target.length()) {
                                        animatePathFound();
                                        return;
                                    }
                                }
                            }
                        }
                    } else { // good to look down
                        if (column < viableColumns) { // good to look left
                            for (int i = 0; i < target.length(); i++) {
                                int currentRow = row - viableRows + i;
                                int currentColumn = target.length() + column - i - 1;
                                if (!tiles[currentRow][currentColumn].getText().equals("" + target.charAt(i))) {
                                    clearTrail();
                                    break;
                                } else {
                                    addToTrail(tiles[currentRow][currentColumn]);
                                    index += tiles[currentRow][currentColumn].getText().length();
                                    if (index == target.length()) {
                                        animatePathFound();
                                        return;
                                    }
                                }
                            }
                        } else { // good to look right
                            for (int i = 0; i < target.length(); i++) {
                                int currentRow = row - viableRows + i;
                                int currentColumn = column - viableColumns + i;
                                if (!tiles[currentRow][currentColumn].getText().equals("" + target.charAt(i))) {
                                    clearTrail();
                                    break;
                                } else {
                                    addToTrail(tiles[currentRow][currentColumn]);
                                    index += tiles[currentRow][currentColumn].getText().length();
                                    if (index == target.length()) {
                                        animatePathFound();
                                        return;
                                    }
                                }
                            }
                        }
                        
                    }
                    
                }
            }
        } else if (!allSingles && tiles.length * 3 >= target.length() && tiles[0].length * 3 >= target.length()) {
            // have to tackle it a different way
            for (int row = 0; row < tiles.length; row++) {
                for (int column = 0; column < tiles[0].length; column++) {
                    
                    if (row > 0 && (row + 1) * 3 >= target.length()) { // good to look up
                        if (column > 0 && (column + 1) * 3 >= target.length()) { // good to look left
                            int index = 0;
                            for (int i = 0; i < target.length(); i++) {
                                if (tiles[row - i][column - i].getText().length() <= target.length() - index &&
                                        tiles[row - i][column - i].getText().equals(
                                                target.substring(index,
                                                        index + tiles[row - i][column - i].getText().length()))) {
                                    addToTrail(tiles[row - i][column - i]);
                                    index += tiles[row - i][column - i].getText().length();
                                } else {
                                    clearTrail();
                                    break;
                                }
                                
                                if (index == target.length()) {
                                    animatePathFound();
                                    return;
                                }
                                
                                if (row - i == 0 || column - i == 0) {
                                    clearTrail();
                                    break;
                                }
                            }
                        }
                        
                        if (column <= tiles[0].length - (target.length() + 2) / 3) { // good to look right
                            int index = 0;
                            for (int i = 0; i < target.length(); i++) {
                                if (tiles[row - i][column + i].getText().length() <= target.length() - index &&
                                        tiles[row - i][column + i].getText().equals(
                                                target.substring(index,
                                                        index + tiles[row - i][column + i].getText().length()))) {
                                    addToTrail(tiles[row - i][column + i]);
                                    index += tiles[row - i][column + i].getText().length();
                                } else {
                                    clearTrail();
                                    break;
                                }
                                
                                if (index == target.length()) {
                                    animatePathFound();
                                    return;
                                }
                                
                                if (row - i == 0 || column + i == tiles[0].length - 1) {
                                    clearTrail();
                                    break;
                                }
                            }
                        }
                    }
                    if (row <= tiles.length - (target.length() + 2) / 3) { // good to look down
                        if (column > 0 && (column + 1) * 3 >= target.length()) { // good to look left
                            int index = 0;
                            for (int i = 0; i < target.length(); i++) {
                                if (tiles[row + i][column - i].getText().length() <= target.length() - index &&
                                        tiles[row + i][column - i].getText().equals(
                                                target.substring(index,
                                                        index + tiles[row + i][column - i].getText().length()))) {
                                    addToTrail(tiles[row + i][column - i]);
                                    index += tiles[row + i][column - i].getText().length();
                                } else {
                                    clearTrail();
                                    break;
                                }
                                
                                if (index == target.length()) {
                                    animatePathFound();
                                    return;
                                }
                                
                                if (row + i == tiles.length - 1 || column - i == 0) {
                                    clearTrail();
                                    break;
                                }
                            }
                        }
                        
                        if (column <= tiles[0].length - (target.length() + 2) / 3) { // good to look right
                            int index = 0;
                            for (int i = 0; i < target.length(); i++) {
                                if (tiles[row + i][column + i].getText().length() <= target.length() - index &&
                                        tiles[row + i][column + i].getText().equals(
                                                target.substring(index,
                                                        index + tiles[row + i][column + i].getText().length()))) {
                                    addToTrail(tiles[row + i][column + i]);
                                    index += tiles[row + i][column + i].getText().length();
                                } else {
                                    clearTrail();
                                    break;
                                }
                                
                                if (index == target.length()) {
                                    animatePathFound();
                                    return;
                                }
                                
                                if (row + i == tiles.length - 1 || column + i == tiles[0].length - 1) {
                                    clearTrail();
                                    break;
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        
        animateError();
    }
    
    public void snake(boolean diagonalSteps) {
        for (int row = 0; row < tiles.length; row++) {
            for (int column = 0; column < tiles[0].length; column++) {
                String current = tiles[row][column].getText();
                if (current.length() <= target.length()
                        && current.equals(target.substring(0, current.length()))
                        && step(row, column, current.length(), diagonalSteps)) {
                    animatePathFound();
                    return;
                }
            }
        }
        
        animateError();
    }
    
    public boolean step(int row, int column, int index, boolean diagonalSteps) {
        // current row and index already checked
        addToTrail(tiles[row][column]);
        if (index == target.length())
            return true;
        else if (index > target.length()) {
            removeFromTrail();
            return false;
        }
        String current;
        
        boolean checkUp = row > 0,
                checkRight = column < tiles[0].length - 1,
                checkDown = row < tiles.length - 1,
                checkLeft = column > 0;
        
        if (checkUp && !trail.contains(row - 1, column)
                && (current = tiles[row - 1][column].getText()).length() + index <= target.length()
                && current.equals(target.substring(index, index + current.length()))
                && step(row - 1, column, index + current.length(), diagonalSteps)) return true;
        
        
        if (checkRight && !trail.contains(row, column + 1)
                && (current = tiles[row][column + 1].getText()).length() + index <= target.length()
                && current.equals(target.substring(index, index + current.length()))
                && step(row, column + 1, index + current.length(), diagonalSteps)) return true;
        
        
        if (checkDown && !trail.contains(row + 1, column)
                && (current = tiles[row + 1][column].getText()).length() + index <= target.length()
                && current.equals(target.substring(index, index + current.length()))
                && step(row + 1, column, index + current.length(), diagonalSteps)) return true;
        
        
        if (checkLeft && !trail.contains(row, column - 1)
                && (current = tiles[row][column - 1].getText()).length() + index <= target.length()
                && current.equals(target.substring(index, index + current.length()))
                && step(row, column - 1, index + current.length(), diagonalSteps)) return true;
        
        
        if (diagonalSteps) {
            if (checkUp && checkRight && !trail.contains(row - 1, column + 1)
                    && (current = tiles[row - 1][column + 1].getText()).length() + index <= target.length()
                    && current.equals(target.substring(index, index + current.length()))
                    && step(row - 1, column + 1, index + current.length(), true))
                return true;
            
            
            if (checkDown && checkRight && !trail.contains(row + 1, column + 1)
                    && (current = tiles[row + 1][column + 1].getText()).length() + index <= target.length()
                    && current.equals(target.substring(index, index + current.length()))
                    && step(row + 1, column + 1, index + current.length(), true))
                return true;
            
            
            if (checkDown && checkLeft && !trail.contains(row + 1, column - 1)
                    && (current = tiles[row + 1][column - 1].getText()).length() + index <= target.length()
                    && current.equals(target.substring(index, index + current.length()))
                    && step(row + 1, column - 1, index + current.length(), true))
                return true;
            
            
            if (checkUp && checkLeft && !trail.contains(row - 1, column - 1)
                    && (current = tiles[row - 1][column - 1].getText()).length() + index <= target.length()
                    && current.equals(target.substring(index, index + current.length()))
                    && step(row - 1, column - 1, index + current.length(), true))
                return true;
            System.out.println("mamamamsadkfslfkweufsdlikufewfjnosdilfuhenwufcnhliweuvsbfhldis,jknehlwiuvbfhwlieefiu");
        }
        
        removeFromTrail();
        return false;
    }
    
    /*---------------------------------------------*/
    /**---------------- Animation ----------------**/
    /*---------------------------------------------*/
    public void animateError() {
        animatePath.stop();
        animatePath.getChildren().clear();
        ParallelTransition pt = new ParallelTransition();
        for (Tile[] row : tiles)
            for (Tile tile : row)
                pt.getChildren().add(tile.getTrans(Color.RED, Color.LIGHTBLUE, Duration.millis(350)));
        animatePath.getChildren().add(pt);
        animatePath.play();
    }
    
    public void animatePathFound() {
        animatePath.stop();
        for (int t = 0; t < trail.size; t++)
            animatePath.getChildren().add(trail.path[t].getTrans(showParse.isSelected() ? Color.rgb(105, 234, 105) :
                    Color.LIGHTBLUE, Color.rgb(66, 134, 244), Duration.millis(300)));
        animatePath.play();
    }
    
    public void clearTrail() {
        if (showParse.isSelected()) {
            ParallelTransition pt = new ParallelTransition();
            for (int i = 0; i < trail.size; i++)
                pt.getChildren().add(trail.path[i].getTrans(Color.rgb(105, 234, 105), Color.LIGHTBLUE,
                        Duration.millis(PARSE_ANIMATION_BASE_SPEED - tiles.length * tiles[0].length)));
            animatePath.getChildren().add(pt);
        }
        trail.empty();
    }
    
    public void addToTrail(Tile t) {
        if (showParse.isSelected())
            animatePath.getChildren().add(t.getTrans(Color.LIGHTBLUE, Color.rgb(105, 234, 105),
                    Duration.millis(PARSE_ANIMATION_BASE_SPEED - tiles.length * tiles[0].length)));
        trail.push(t);
    }
    
    public void removeFromTrail() {
        if (showParse.isSelected())
            animatePath.getChildren().add(trail.pop().getTrans(Color.rgb(105, 234, 105), Color.LIGHTBLUE,
                    Duration.millis(PARSE_ANIMATION_BASE_SPEED - tiles.length * tiles[0].length)));
        else
            trail.pop();
    }
    
    public static void main(String[] args) {
        
        launch(args);
    }
}

class Trail {
    Tile[] path;
    int size = 0;
    
    public Trail(int size) {
        path = new Tile[size];
    }
    
    public void push(Tile c) {
        if (size < path.length) path[size++] = c;
    }
    
    public Tile pop() { return size > 0 ? path[--size] : null; }
    
    public void empty() {
        size = 0;
    }
    
    public boolean contains(int row, int column) {
        for (int i = 0; i < size; i++) if (path[i].row == row && path[i].column == column) return true;
        return false;
    }
}

class Tile extends StackPane {
    
    int row, column;
    
    Text text = new Text("");
    Rectangle backgroundColor;
    
    public Tile(int row, int column) {
        backgroundColor = new Rectangle(45, 45);
        backgroundColor.setStyle("-fx-fill: lightgray;" +
                "-fx-arc-height: 8;" +
                "-fx-arc-width: 8;");
        
        setTranslateX(130 + column * 50); // set aside 110 for buttons
        setTranslateY(10 + row * 50);
        
        this.row = row;
        this.column = column;
        
        getChildren().addAll(backgroundColor, text);
    }
    
    public void appendChar(String letter) {
        if (text.getText().length() < 3)
            text.setText(text.getText() + letter);
        else
            fillTrans(Color.RED, Color.GRAY);
    }
    
    public void removeChar() {
        if (text.getText().length() > 0)
            text.setText(text.getText().substring(0, text.getText().length() - 1));
        else
            fillTrans(Color.RED, Color.GRAY);
    }
    
    public void reset() {
        text.setText("");
        backgroundColor.setFill(Color.LIGHTGRAY);
    }
    
    public void fillTrans(Color from, Color to) {
        new FillTransition(Duration.millis(500), backgroundColor, from, to).play();
    }
    
    public FillTransition getTrans(Color from, Color to, Duration time) {
        return new FillTransition(time, backgroundColor, from, to);
    }
    
    public String getText() {
        return text.getText();
    }
}