import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.Node;
import java.util.*;

public class CandyCrushApp extends Application {
    
    private static final int ROWS = 8;
    private static final int COLS = 8;
    private static final int CELL_SIZE = 60;
    private static final int GAP = 4;
    
    private Candy[][] board = new Candy[ROWS][COLS];
    private GridPane gridPane;
    private Label scoreLabel;
    private Label movesLabel;
    private Label levelLabel;
    private Label targetLabel;
    private ProgressBar progressBar;
    private int score = 0;
    private int moves = 30;
    private int level = 1;
    private int targetScore = 1000;
    private boolean isProcessing = false;
    private int combo = 0;
    private Candy selectedCandy = null;
    private StackPane selectedPane = null;
    
    private final String[] CANDY_TYPES = {"🍎", "🫐", "🥝", "🍋", "🍇", "🍊"};
    private final String[] CANDY_COLORS = {"#FF6B6B", "#4ECDC4", "#95E1D3", "#FFD93D", "#C7CEEA", "#FFA07A"};
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Candy Crush Java");
        
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background: linear-gradient(to bottom right, #667eea, #764ba2);");
        
        // Header
        VBox header = createHeader();
        root.setTop(header);
        
        // Game Board
        gridPane = createGrid();
        StackPane centerPane = new StackPane(gridPane);
        centerPane.setPadding(new Insets(20));
        root.setCenter(centerPane);
        
        // Controls
        HBox controls = createControls();
        root.setBottom(controls);
        
        Scene scene = new Scene(root, 600, 750);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        
        initializeBoard();
    }
    
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(20));
        
        Label title = new Label("🍬 Candy Crush Java 🍬");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setStyle("-fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        // Stats Box
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER);
        
        VBox scoreBox = createStatBox("Score", "0");
        scoreLabel = (Label) scoreBox.getChildren().get(1);
        
        VBox movesBox = createStatBox("Moves", "30");
        movesLabel = (Label) movesBox.getChildren().get(1);
        
        VBox levelBox = createStatBox("Level", "1");
        levelLabel = (Label) levelBox.getChildren().get(1);
        
        stats.getChildren().addAll(scoreBox, movesBox, levelBox);
        
        // Progress
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: linear-gradient(to right, #f093fb, #f5576c);");
        
        HBox targetBox = new HBox(10);
        targetBox.setAlignment(Pos.CENTER);
        Label targetText = new Label("Target:");
        targetText.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        targetLabel = new Label("1000");
        targetLabel.setStyle("-fx-text-fill: #FFD93D; -fx-font-weight: bold; -fx-font-size: 16;");
        targetBox.getChildren().addAll(targetText, targetLabel);
        
        header.getChildren().addAll(title, stats, progressBar, targetBox);
        return header;
    }
    
    private VBox createStatBox(String label, String value) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-padding: 10 20; -fx-background-radius: 10;");
        
        Label lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 12;");
        lbl.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
        val.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        
        box.getChildren().addAll(lbl, val);
        return box;
    }
    
    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(GAP);
        grid.setVgap(GAP);
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-padding: 15; -fx-background-radius: 15;");
        
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                StackPane cell = createCell(row, col);
                grid.add(cell, col, row);
            }
        }
        
        return grid;
    }
    
    private StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        
        // Add shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(0, 0, 0, 0.2));
        cell.setEffect(shadow);
        
        cell.setOnMouseClicked(e -> handleCellClick(row, col, cell));
        
        // Hover effect
        cell.setOnMouseEntered(e -> {
            if (!isProcessing) {
                cell.setScaleX(1.1);
                cell.setScaleY(1.1);
            }
        });
        cell.setOnMouseExited(e -> {
            cell.setScaleX(1.0);
            cell.setScaleY(1.0);
        });
        
        return cell;
    }
    
    private void initializeBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = createRandomCandy();
            }
        }
        
        // Resolve initial matches
        while (findMatches().size() > 0) {
            resolveMatchesImmediate();
        }
        
        renderBoard();
    }
    
    private Candy createRandomCandy() {
        int type = (int) (Math.random() * CANDY_TYPES.length);
        return new Candy(CANDY_TYPES[type], CANDY_COLORS[type], type);
    }
    
    private void renderBoard() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                StackPane cell = getCell(row, col);
                cell.getChildren().clear();
                
                Candy candy = board[row][col];
                if (candy != null) {
                    Label candyLabel = new Label(candy.getSymbol());
                    candyLabel.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 32));
                    candyLabel.setStyle("-fx-text-fill: " + candy.getColor() + ";");
                    cell.getChildren().add(candyLabel);
                }
            }
        }
        
        updateStats();
    }
    
    private StackPane getCell(int row, int col) {
        return (StackPane) gridPane.getChildren().get(row * COLS + col);
    }
    
    private void handleCellClick(int row, int col, StackPane cell) {
        if (isProcessing || moves <= 0) return;
        
        if (selectedCandy == null) {
            selectedCandy = board[row][col];
            selectedPane = cell;
            cell.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10; -fx-border-color: #ffc107; -fx-border-width: 3; -fx-border-radius: 10;");
            
            // Pulse animation
            ScaleTransition pulse = new ScaleTransition(Duration.millis(500), cell);
            pulse.setFromX(1.0);
            pulse.setFromY(1.0);
            pulse.setToX(1.1);
            pulse.setToY(1.1);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(Animation.INDEFINITE);
            pulse.play();
            cell.setUserData(pulse);
            
        } else {
            // Clear selection
            if (selectedPane != null) {
                selectedPane.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
                if (selectedPane.getUserData() instanceof Animation) {
                    ((Animation) selectedPane.getUserData()).stop();
                }
                selectedPane.setScaleX(1.0);
                selectedPane.setScaleY(1.0);
            }
            
            int prevRow = GridPane.getRowIndex(selectedPane);
            int prevCol = GridPane.getColumnIndex(selectedPane);
            
            if (prevRow == row && prevCol == col) {
                selectedCandy = null;
                selectedPane = null;
                return;
            }
            
            if (isAdjacent(prevRow, prevCol, row, col)) {
                attemptSwap(prevRow, prevCol, row, col);
            } else {
                selectedCandy = board[row][col];
                selectedPane = cell;
                cell.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10; -fx-border-color: #ffc107; -fx-border-width: 3; -fx-border-radius: 10;");
                
                ScaleTransition pulse = new ScaleTransition(Duration.millis(500), cell);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.1);
                pulse.setToY(1.1);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();
                cell.setUserData(pulse);
            }
        }
    }
    
    private boolean isAdjacent(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2) == 1;
    }
    
    private void attemptSwap(int r1, int c1, int r2, int c2) {
        isProcessing = true;
        selectedCandy = null;
        selectedPane = null;
        
        // Swap in board
        Candy temp = board[r1][c1];
        board[r1][c1] = board[r2][c2];
        board[r2][c2] = temp;
        
        renderBoard();
        
        Set<Point> matches = findMatches();
        
        if (matches.size() > 0) {
            moves--;
            combo = 0;
            resolveMatches();
        } else {
            // Swap back after delay
            PauseTransition pause = new PauseTransition(Duration.millis(300));
            pause.setOnFinished(e -> {
                temp = board[r1][c1];
                board[r1][c1] = board[r2][c2];
                board[r2][c2] = temp;
                renderBoard();
                isProcessing = false;
            });
            pause.play();
        }
    }
    
    private Set<Point> findMatches() {
        Set<Point> matches = new HashSet<>();
        
        // Horizontal
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS - 2; col++) {
                Candy c1 = board[row][col];
                if (c1 == null) continue;
                
                int matchLen = 1;
                while (col + matchLen < COLS && board[row][col + matchLen] != null &&
                       board[row][col + matchLen].getType() == c1.getType()) {
                    matchLen++;
                }
                
                if (matchLen >= 3) {
                    for (int i = 0; i < matchLen; i++) {
                        matches.add(new Point(row, col + i));
                    }
                }
            }
        }
        
        // Vertical
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS - 2; row++) {
                Candy c1 = board[row][col];
                if (c1 == null) continue;
                
                int matchLen = 1;
                while (row + matchLen < ROWS && board[row + matchLen][col] != null &&
                       board[row + matchLen][col].getType() == c1.getType()) {
                    matchLen++;
                }
                
                if (matchLen >= 3) {
                    for (int i = 0; i < matchLen; i++) {
                        matches.add(new Point(row + i, col));
                    }
                }
            }
        }
        
        return matches;
    }
    
    private void resolveMatches() {
        Set<Point> matches = findMatches();
        if (matches.isEmpty()) {
            isProcessing = false;
            checkGameState();
            return;
        }
        
        combo++;
        int points = matches.size() * 10 * combo;
        score += points;
        
        // Show combo text
        if (combo > 1) {
            showComboText(matches.iterator().next(), "Combo x" + combo + "!");
        }
        
        // Animate matches
        List<StackPane> matchedCells = new ArrayList<>();
        for (Point p : matches) {
            StackPane cell = getCell(p.row, p.col);
            matchedCells.add(cell);
            
            ScaleTransition scale = new ScaleTransition(Duration.millis(300), cell);
            scale.setToX(1.3);
            scale.setToY(1.3);
            scale.setAutoReverse(true);
            scale.setCycleCount(2);
            scale.play();
        }
        
        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            // Remove matches
            for (Point p : matches) {
                board[p.row][p.col] = null;
            }
            renderBoard();
            
            // Drop and fill
            dropCandies();
            fillEmpty();
            renderBoard();
            
            // Check for cascading
            PauseTransition next = new PauseTransition(Duration.millis(300));
            next.setOnFinished(ev -> resolveMatches());
            next.play();
        });
        pause.play();
    }
    
    private void resolveMatchesImmediate() {
        Set<Point> matches = findMatches();
        for (Point p : matches) {
            board[p.row][p.col] = createRandomCandy();
        }
    }
    
    private void dropCandies() {
        for (int col = 0; col < COLS; col++) {
            int emptyRow = ROWS - 1;
            for (int row = ROWS - 1; row >= 0; row--) {
                if (board[row][col] != null) {
                    if (row != emptyRow) {
                        board[emptyRow][col] = board[row][col];
                        board[row][col] = null;
                    }
                    emptyRow--;
                }
            }
        }
    }
    
    private void fillEmpty() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (board[row][col] == null) {
                    board[row][col] = createRandomCandy();
                }
            }
        }
    }
    
    private void showComboText(Point p, String text) {
        StackPane cell = getCell(p.row, p.col);
        Label comboLabel = new Label(text);
        comboLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        comboLabel.setStyle("-fx-text-fill: #e74c3c; -fx-background-color: rgba(255,255,255,0.8); -fx-padding: 5 10; -fx-background-radius: 5;");
        
        cell.getChildren().add(comboLabel);
        
        TranslateTransition floatUp = new TranslateTransition(Duration.millis(1000), comboLabel);
        floatUp.setByY(-50);
        floatUp.setByX(0);
        
        FadeTransition fade = new FadeTransition(Duration.millis(1000), comboLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        
        ParallelTransition parallel = new ParallelTransition(floatUp, fade);
        parallel.setOnFinished(e -> cell.getChildren().remove(comboLabel));
        parallel.play();
    }
    
    private void checkGameState() {
        if (score >= targetScore) {
            showLevelComplete();
        } else if (moves <= 0) {
            showGameOver();
        }
    }
    
    private void showLevelComplete() {
        int stars = score >= targetScore * 1.5 ? 3 : score >= targetScore * 1.2 ? 2 : 1;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Level Complete!");
        alert.setHeaderText(null);
        alert.setContentText("Score: " + score + "\nStars: " + "⭐".repeat(stars));
        
        ButtonType nextBtn = new ButtonType("Next Level");
        ButtonType againBtn = new ButtonType("Play Again");
        
        alert.getButtonTypes().setAll(nextBtn, againBtn);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == nextBtn) {
            nextLevel();
        } else {
            resetGame();
        }
    }
    
    private void showGameOver() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(null);
        alert.setContentText("You needed " + (targetScore - score) + " more points!\nFinal Score: " + score);
        
        ButtonType againBtn = new ButtonType("Try Again");
        alert.getButtonTypes().setAll(againBtn);
        
        alert.showAndWait();
        resetGame();
    }
    
    private void nextLevel() {
        level++;
        targetScore = 1000 + (level - 1) * 500;
        moves = 30;
        score = 0;
        resetBoard();
    }
    
    private void resetGame() {
        level = 1;
        score = 0;
        moves = 30;
        targetScore = 1000;
        resetBoard();
    }
    
    private void resetBoard() {
        isProcessing = false;
        combo = 0;
        selectedCandy = null;
        selectedPane = null;
        
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = null;
            }
        }
        
        initializeBoard();
    }
    
    private void updateStats() {
        scoreLabel.setText(String.valueOf(score));
        movesLabel.setText(String.valueOf(moves));
        levelLabel.setText(String.valueOf(level));
        targetLabel.setText(String.valueOf(targetScore));
        
        double progress = Math.min((double) score / targetScore, 1.0);
        progressBar.setProgress(progress);
    }
    
    private HBox createControls() {
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(20));
        
        Button shuffleBtn = createButton("🔀 Shuffle", "#667eea");
        shuffleBtn.setOnAction(e -> shuffle());
        
        Button resetBtn = createButton("🔄 New Game", "#764ba2");
        resetBtn.setOnAction(e -> resetGame());
        
        Button hintBtn = createButton("💡 Hint", "#f5576c");
        hintBtn.setOnAction(e -> showHint());
        
        controls.getChildren().addAll(shuffleBtn, resetBtn, hintBtn);
        return controls;
    }
    
    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 25; -fx-cursor: hand;");
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        btn.setEffect(shadow);
        
        btn.setOnMouseEntered(e -> btn.setScaleX(1.05));
        btn.setOnMouseExited(e -> btn.setScaleX(1.0));
        
        return btn;
    }
    
    private void shuffle() {
        if (isProcessing || moves < 2) return;
        
        moves -= 2;
        
        List<Candy> flat = new ArrayList<>();
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                flat.add(board[row][col]);
            }
        }
        
        Collections.shuffle(flat);
        
        int idx = 0;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                board[row][col] = flat.get(idx++);
            }
        }
        
        renderBoard();
        
        if (findMatches().size() > 0) {
            isProcessing = true;
            resolveMatches();
        }
        
        checkGameState();
    }
    
    private void showHint() {
        if (isProcessing) return;
        
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int[][] dirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
                
                for (int[] dir : dirs) {
                    int newRow = row + dir[0];
                    int newCol = col + dir[1];
                    
                    if (newRow >= 0 && newRow < ROWS && newCol >= 0 && newCol < COLS) {
                        // Try swap
                        Candy temp = board[row][col];
                        board[row][col] = board[newRow][newCol];
                        board[newRow][newCol] = temp;
                        
                        if (findMatches().size() > 0) {
                            // Swap back and highlight
                            temp = board[row][col];
                            board[row][col] = board[newRow][newCol];
                            board[newRow][newCol] = temp;
                            
                            StackPane cell1 = getCell(row, col);
                            StackPane cell2 = getCell(newRow, newCol);
                            
                            String original1 = cell1.getStyle();
                            String original2 = cell2.getStyle();
                            
                            cell1.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10;");
                            cell2.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10;");
                            
                            PauseTransition pause = new PauseTransition(Duration.millis(1000));
                            pause.setOnFinished(e -> {
                                cell1.setStyle(original1);
                                cell2.setStyle(original2);
                            });
                            pause.play();
                            
                            return;
                        }
                        
                        // Swap back
                        temp = board[row][col];
                        board[row][col] = board[newRow][newCol];
                        board[newRow][newCol] = temp;
                    }
                }
            }
        }
    }
    
    // Inner classes
    private static class Candy {
        private String symbol;
        private String color;
        private int type;
        
        public Candy(String symbol, String color, int type) {
            this.symbol = symbol;
            this.color = color;
            this.type = type;
        }
        
        public String getSymbol() { return symbol; }
        public String getColor() { return color; }
        public int getType() { return type; }
    }
    
    private static class Point {
        int row, col;
        
        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point point = (Point) o;
            return row == point.row && col == point.col;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }
}