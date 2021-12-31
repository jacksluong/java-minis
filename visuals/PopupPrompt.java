package visuals;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Created by Jacks on 01/30/19.
 * A popup node.
 */

public class PopupPrompt extends GridPane {
    
    /* ----------------------------------- *
     * ----- Fields and Constructors ----- *
     * ----------------------------------- */
    
    final int MAXIMUM_PROMPTS = 5;
    
    private int size;
    private final Text titleText;
    private final Label[] prompts = new Label[MAXIMUM_PROMPTS];
    private final TextField[] answers = new TextField[MAXIMUM_PROMPTS];
    private final AlertCondition[] alertConditions = new AlertCondition[MAXIMUM_PROMPTS];
    private final Button quit = new Button("Exit");
    private final Button submit = new Button("Submit");
    private final Text alert = new Text();
    
    public PopupPrompt() {
        this("Popup", new String[]{});
    }
    
    public PopupPrompt(String[] prompts) {
        this("Popup", prompts);
    }
    
    public PopupPrompt(String title, String[] prompts) { // any prompts beyond index 4 is ignored
        
        setMaxWidth(340);
        setPrefWidth(340);
        setMaxHeight(148 + 47 * prompts.length);
        setPrefHeight(148 + 47 * prompts.length);
        
        size = prompts.length;
        titleText = new Text(title);
        StackPane titlePane = new StackPane(titleText);
    
        add(titlePane, 0, 0, 2, 1);
    
        for (int i = 0; i < (prompts.length > 5 ? 5 : prompts.length); i++) {
            Label prompt = new Label(prompts[i]);
            TextField answer = new TextField();
            prompt.setMinWidth(140);
            this.prompts[i] = prompt;
            answers[i] = answer;
            addRow(i + 1, prompt, answer);
        }
    
        add(quit, 0, prompts.length + 1);
        add(submit, 1, prompts.length + 1);
        add(alert, 0, prompts.length + 2, 2,1);
    
        // Format
        setHalignment(titlePane, HPos.CENTER);
        setHalignment(alert, HPos.RIGHT);
        setHalignment(quit, HPos.CENTER);
        setHalignment(submit, HPos.CENTER);
        titlePane.setStyle("-fx-background-color: #404040");
        titlePane.setPadding(new Insets(10));
        titleText.setStroke(Color.WHITE);
        titleText.setFill(Color.WHITE);
        titleText.setFont(Font.font("Bradley Hand", 20));
        alert.setFill(Color.RED); // alert
        
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(11.0);
        dropShadow.setOffsetX(10.0);
        dropShadow.setOffsetY(12.0);
        dropShadow.setColor(Color.color(0.4, 0.5, 0.5));
        
        setEffect(dropShadow);
        setHgap(10);
        setVgap(20);
        setPadding(new Insets(10));
        setStyle("-fx-background-color: darkgray; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #b35900; " +
                "-fx-border-radius: 5; " +
                "-fx-border-width: 3");
    }
    
    /* -------------------------- *
     * ----- Grid Modifiers ----- *
     * -------------------------- */
    
    public void addPrompt(String prompt) {
        addPrompt(size + 1, prompt);
    }
    
    public void addPrompt(int promptIndex, String prompt) {
        if (size > 4) {
            System.out.println("Maximum capacity of prompts reached.");
            return;
        }
        if (promptIndex < size + 1) checkIndex(promptIndex);
    
        for (Node child : getChildren()) {
            Integer rowIndex = getRowIndex(child);
        
            if (rowIndex > promptIndex) setRowIndex(child, rowIndex + 1);
        }
    
        Label promptLabel = new Label(prompt);
        TextField answer = new TextField();
        promptLabel.setMinWidth(140);
        for (int i = 4; i > promptIndex; i--) {
            prompts[i] = prompts[i - 1];
            answers[i] = answers[i - 1];
            alertConditions[i] = alertConditions[i - 1];
        }
        prompts[promptIndex] = promptLabel;
        answers[promptIndex] = answer;
        addRow(promptIndex + 1, promptLabel, answer);
        size++;
    }
    
    public void deletePrompt(final int promptIndex) {
        if (size < 1) {
            System.out.println("No prompts to remove.");
            return;
        }
        checkIndex(promptIndex);
        Node[] deleteNodes = new Node[2];
        for (Node child : getChildren()) {
            Integer rowIndex = getRowIndex(child); // all children here have row indices
            
            if (rowIndex > promptIndex + 1)
                GridPane.setRowIndex(child, rowIndex - 1);
            else if (rowIndex == promptIndex + 1)
                deleteNodes[getColumnIndex(child)] = child;
        }
    
        for (int i = promptIndex; i < size - 1; i++) {
            prompts[i] = prompts[i + 1];
            answers[i] = answers[i + 1];
            alertConditions[i] = alertConditions[i + 1];
        }
        
        getChildren().removeAll(deleteNodes);
        size--;
    }
    
    public void setQuitVisible(boolean value) {
        quit.setVisible(value);
    }
    
    public void clearFields() {
        for (int i = 0; i < size; i++) answers[i].setText("");
        alert.setText("");
    }
    
    /* ------------------------------- *
     * ----- Getters and Setters ----- *
     * ------------------------------- */
    
    public String[] getAnswers() {
        String[] answers = new String[size];
        for (int i = 0; i < size; i++) answers[i] = this.answers[i].getText();
        return answers;
    }
    
    public int getSize() {
        return size;
    }
    
    public boolean noEmptyFields() {
        for (int i = 0; i < size; i++) if (answers[i].getText().isEmpty()) return false;
        return true;
    }
    
    public void triggerSubmit() {
        submit.fire();
    }
    
    public void triggerQuit() {
        quit.fire();
    }
    
    public void setTitle(String text) {
        titleText.setText(text);
    }
    
    public void setPrompt(int promptIndex, String text) {
        checkIndex(promptIndex);
        prompts[promptIndex].setText(text);
    }
    
    public void setPlaceholder(int promptIndex, String text) {
        checkIndex(promptIndex);
        answers[promptIndex].setPromptText(text);
    }
    
    public void setAlert(int promptIndex, AlertCondition condition) {
        checkIndex(promptIndex);
        alertConditions[promptIndex] = condition;
    }
    
    public void setQuitAction(EventHandler<ActionEvent> value) { quit.setOnAction(value); }
    
    public void setSubmitAction(EventHandler<ActionEvent> value) {
        submit.setOnAction(e -> {
            for (int i = 0; i < size; i++) {
                if (alertConditions[i] != null) {
                    String s;
                    if (!(s = alertConditions[i].test(answers[i].getText())).isEmpty()) {
                        alert.setText(s);
                        alert.setVisible(true);
                        return;
                    }
                }
            }
            alert.setText("");
            alert.setVisible(false);
            value.handle(e);
        });
    }
    
    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IllegalArgumentException("Illegal prompt index.");
    }
    
    private void updateHeight() {
        setMaxHeight(148 - 47 * prompts.length);
        setPrefHeight(148 - 47 * prompts.length);
    }
    
    public interface AlertCondition {
        String test(String s);
    }

}
