import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class HeapTest {

    public static void main(String[] args) {
        // Create a map with a Boolean key and Integer value
        Map<Boolean, Integer> map = new HashMap<Boolean, Integer>();

        // Fill the map with objects
        for (int i = 0; i < 1000; i++) {
            map.put(i % 2 == 0, i);
        }

        // Print memory usage before deletion
        // printMemoryUsage("Before deletion");

        // Delete objects based on a condition (e.g., delete if key is true)
        Iterator<Map.Entry<Boolean, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Boolean, Integer> entry = iterator.next();
            if (entry.getKey()) {
                iterator.remove();
            }
        }

        // Suggest garbage collection
        // System.gc();

        // Print memory usage after deletion
        // printMemoryUsage("After deletion");
    }

    private static void printMemoryUsage(String message) {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println(message + ": Memory used = " + memory);
    }
}

