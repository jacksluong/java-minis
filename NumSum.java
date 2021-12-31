import java.util.*;

/**
 * Created by Jacky on 6/1/18.
 * This program will return the number of sets of k numbers among n numbers whose sum equal 0.
 * (i.e. TwoSum, ThreeSum)
 */

public class NumSum {
    
    public static final int THRESHOLD = 20000;
    
    public static void main(String[] args) {
        
        Scanner input = new Scanner(System.in);
        System.out.println("For sets of k among n numbers, provide values n and k: ");
        int n = input.nextInt(), k = input.nextInt(); // n > k, k > 1
        
        System.out.println("Number of reps? ");
        int rep = input.nextInt(); // rep > 0
        double[][] results = new double[rep][3];
        
        for (int a = 0; a < rep; a++) {
            
            // init
            System.out.println("Rep " + (a + 1) + ": Initializing array...");
            int count = 0, size = (int) (n * Math.pow(2, a));
            int[] nums = new int[size];
            if (a != 0 && size >= THRESHOLD && size / 2 < THRESHOLD) System.out.println("-- Algorithm switched --");
            for (int i = 0; i < size; i++) nums[i] = (int) (Math.random() * 2000000 - 1000000); // random 6-digit ints
            
            long startTime = System.currentTimeMillis();
            
            // algorithm
            if (size < THRESHOLD) { // threshold for switch to memory-friendlier algorithm
                // auxiliary set, search with hash code for last layer
                System.out.println("Executing time-friendly algorithm.");
                Set<Integer> s = new HashSet<>(size);
                for (int i : nums) s.add(i);
                count += algorithmS(0, k, 0, s);
            } else {
                // no auxiliary set, use binary search for last layer
                System.out.println("Executing memory-friendly algorithm.");
                Arrays.sort(nums);
                count += algorithmA(0, k, 0, size, nums);
            }
            
            // result
            results[a][0] = size;
            results[a][1] = count;
            results[a][2] = System.currentTimeMillis() - startTime;
            System.out.println();
            
        }
        
        System.out.print("Sets of " + k + " among " + n + " numbers that sum to 0.\n" +
                "Nums     Count       Time\n");
        for (double[] repe : results)
            System.out.printf("%-9d%-12d%-20.2f\n", (int) repe[0], (int) repe[1], repe[2]);
        
    }
    
    // Precondition: numsHeld will always be less than k because if it were equal to, then it would be checked within the loop
    private static int algorithmS(int numsHeld, int k, int accumulativeSum, Set<Integer> nums) {
        /* O(N^(k - 1)) */
        int count = 0;
        if (numsHeld < k - 1) {
            for (int i : nums) {
                if (Math.abs(accumulativeSum + i) < 1000000 * (k - numsHeld - 1))
                    // go to next layer
                    count += algorithmS(numsHeld + 1, k, i + accumulativeSum, nums);
            }
        } else {
            // the final layer
            if (nums.contains(-accumulativeSum))
                count++;
        }
        return count;
    }
    private static int algorithmA(int numsHeld, int k, int accumulativeSum, int size, int[] nums) {
        /* O(N^(k-1)log(N)) */
        int count = 0;
        if (numsHeld < k - 1) {
            for (int i : nums) {
                if (Math.abs(accumulativeSum + i) < 1000000 * (k - numsHeld - 1))
                    // go to next layer
                    count += algorithmA(numsHeld + 1, k, i + accumulativeSum, size, nums);
            }
        } else {
            // the final layer
            int target = -accumulativeSum, low = 0, high = size - 1, mid = (low + high) / 2;
            // binary search
            while (low < high && nums[mid] != target) {
                if (target > nums[mid])
                    low = mid + 1;
                else
                    high = mid;
                mid = (low + high) / 2;
            }
            if (nums[mid] == target) count++;
        }
        return count;
    }
    
}
