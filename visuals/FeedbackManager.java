package visuals;

import javafx.animation.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A class controlling feedback for a scene.
 */

public class FeedbackManager {
    
    private double xOffset, yOffset, maxWidth, duration = 4000;
    private int maxNumber = 20;
    private Pane parent;
    
    private FeedbackLinkedList messages = new FeedbackLinkedList();
    
    public FeedbackManager(Pane parent) {
        this(parent, 15, 20);
    }
    
    public FeedbackManager(Pane parent, double maxWidth) {
        this(parent, 15, 20);
        this.maxWidth = maxWidth;
    }
    
    public FeedbackManager(Pane parent, double xOffset, double yOffset) {
        this.parent = parent;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
    
    public void addMessage(String s) {
        if (messages.size == maxNumber) {
            FeedbackLinkedList.FeedbackNode removed = messages.removeLast();
            removed.pt.stop();
    
            removed.destinationY -= 20; // move up
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), removed);
            tt.setToY(removed.destinationY);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.play();
            
            FadeTransition ft = new FadeTransition(Duration.seconds(0.4), removed); // fade
            ft.setToValue(0);
            ft.setOnFinished(e -> parent.getChildren().remove(removed));
            ft.play();
        }
        messages.addFirst(s);
    }
    
    public void setLimit(int numberOfMessages) {
        maxNumber = numberOfMessages;
    }
    
    public void setDuration(double millis) {
        duration = millis;
    }
    
    public void setMaxNumber(int maxNumber) {
        this.maxNumber = maxNumber;
    }
    
    public void setMaxWidth(double maxWidth) {
        this.maxWidth = maxWidth;
    }
    
    public void setParent(Pane parent) {
        if (parent != this.parent) // remove everything from current parent
            for (FeedbackLinkedList.FeedbackNode current = messages.last; current != null; current = current.previous)
                parent.getChildren().remove(current);
        
        this.parent = parent;
        messages.first = messages.last = null;
    }
    
    private class FeedbackLinkedList {
        
        private FeedbackNode first, last;
        private int size = 0;
        
        protected void addFirst(String s) {
            // create the message
            FeedbackNode newText = new FeedbackNode(s, first, parent.getHeight() - yOffset);
            newText.setWrappingWidth(maxWidth);
            newText.translateXProperty().bind(parent.translateXProperty().multiply(-1).add(xOffset));
            newText.setTranslateY(parent.getHeight() - yOffset);
            newText.setFont(Font.font("Courier New", 12));
            
            // add into list and scene
            first = newText;
            if (last == null) last = first;
            parent.getChildren().add(newText);
            newText.toFront();
            
            // add animation
            PauseTransition pt = new PauseTransition();
            pt.setDuration(Duration.millis(duration));
            pt.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.seconds(0.4), newText);
                ft.setToValue(0);
                ft.setOnFinished(f -> {
                    removeLast();
                    parent.getChildren().remove(newText);
                });
                ft.play();
            });
            newText.pt = pt;
            pt.play();
            
            // animate all others
            FeedbackNode current = last;
            while (current != null) {
                current.destinationY -= 20;
                TranslateTransition tt = new TranslateTransition(Duration.millis(400), current);
                tt.setToY(current.destinationY);
                tt.setInterpolator(Interpolator.EASE_OUT);
                tt.play();
                current = current.previous;
            }
            
            size++;
        }
        
        protected FeedbackNode removeLast() {
            FeedbackNode removed = last;
            last = last.previous;
            size--;
            return removed;
        }
        
        private class FeedbackNode extends Text {
            
            protected FeedbackNode previous;
            protected PauseTransition pt;
            private double destinationY;
            
            protected FeedbackNode(String s, FeedbackNode next, double y) {
                setText(s);
                setStroke(Color.BLUE);
                destinationY = y;
                if (next != null) next.previous = this;
            }
            
        }
    }

}
