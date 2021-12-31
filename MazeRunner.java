import java.util.Stack;

/**
 * Created by Jacky on 9/20/17.
 * Given a premade maze grid, this program will find a path from beginning to the end if one exists (using the
 * right-hand rule).
 */
public class MazeRunner {
    public static void main(String[] args) {
        /* Important:
         * Directions
         * 0: ^
         * 1: >
         * 2: v
         * 3: <
         *
         * Motions
         * 0: Right turn
         * 1: Go forward
         * 2: Left turn
         *
         * x and y coords are switched (first value is y coord and second value is x coord in 2D array)
         * true means unvisited and nonwall.
         */
        
        // Initialization.
        Stack<Integer> path = new Stack<>();
        ///////// Hardcoded maze construction, and remember, zero-based indexing
        int x = 1, y = 1, winX = 4, winY = 3, direction = 1, itrCount = 0;
        boolean[][] map = new boolean[5][5];
        map[1][1] = true;
        map[1][2] = true;
        map[1][3] = true;
        map[2][3] = true;
        map[3][3] = true;
        map[3][4] = true;
//        map[3][2] = true;
//        map[3][1] = true;
//        map[2][1] = true;
        
        // Solution.
        while ((x != winX || y != winY) && itrCount <= 200) {
            if (itrCount > 0 && path.size() == 0) {
                System.out.println("Conclusion: The maze is unsolvable.");
                return;
            }
            if (map[1][1])
                System.out.println("x: " + x + ", y: " + y + ", direction: " + direction);
            if (direction == 0)
                if (x != map[0].length - 1 && map[y][x + 1]) {
                    map[y][x + 1] = false;
                    path.push(0);
                    x++;
                    direction = 1;
                } else if (y != 0 && map[y - 1][x]) {
                    map[y - 1][x] = false;
                    path.push(1);
                    y--;
                } else if (x != 0 && map[y][x - 1]) {
                    map[y][x - 1] = false;
                    path.push(2);
                    x--;
                    direction = 3;
                } else {    // no available moves but to go back
                    int move = path.pop();
                    y++;
                    if (move == 0)
                        direction = 3;
                    else if (move == 2)
                        direction = 1;
                }
            else if (direction == 1)
                if (y != map.length - 1 && map[y + 1][x]) {
                    map[y + 1][x] = false;
                    path.push(0);
                    y++;
                    direction = 2;
                } else if (x != map[0].length - 1 && map[y][x + 1]) {
                    map[y][x + 1] = false;
                    path.push(1);
                    x++;
                } else if (y != 0 && map[y - 1][x]) {
                    map[y - 1][x] = false;
                    path.push(2);
                    y--;
                    direction = 0;
                } else {    // no available moves but to go back
                    int move = path.pop();
                    x--;
                    if (move == 0)
                        direction = 0;
                    else if (move == 2)
                        direction = 2;
                }
            else if (direction == 2)
                if (x != 0 && map[y][x - 1]) {
                    map[y][x - 1] = false;
                    path.push(0);
                    x--;
                    direction = 3;
                } else if (y != map.length - 1 && map[y + 1][x]) {
                    map[y + 1][x] = false;
                    path.push(1);
                    y++;
                } else if (x != map[0].length - 1 && map[y][x + 1]) {
                    map[y][x + 1] = false;
                    path.push(2);
                    x++;
                    direction = 1;
                } else {    // no available moves but to go back
                    int move = path.pop();
                    y--;
                    if (move == 0)
                        direction = 1;
                    else if (move == 2)
                        direction = 3;
                }
            else            // direction has to be 3
                if (y != 0 && map[y - 1][x]) {
                    map[y - 1][x] = false;
                    path.push(0);
                    y--;
                    direction = 0;
                } else if (x != 0 && map[y][x - 1]) {
                    map[y][x - 1] = false;
                    path.push(1);
                    x--;
                } else if (y != map.length - 1 && map[y + 1][x]) {
                    map[y + 1][x] = false;
                    path.push(2);
                    y++;
                    direction = 2;
                } else {    // no available moves but to go back
                    int move = path.pop();
                    x++;
                    if (move == 0)
                        direction = 2;
                    else if (move == 2)
                        direction = 0;
                }
            itrCount++;
        }
        if (itrCount > 200)
            throw new RuntimeException("Was unable to complete the small maze in under 200 moves.");
        System.out.println("The maze has been solved in " + itrCount + " moves.");
    }
}
