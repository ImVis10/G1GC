import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

public class HeapTest {

    public static void main(String[] args) {
        Map<Boolean, Integer> map = new HashMap<Boolean, Integer>();

        for (int i = 0; i < 1000; i++) {
            map.put(i % 2 == 0, i);
        }


        // Delete if key is true
        Iterator<Map.Entry<Boolean, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Boolean, Integer> entry = iterator.next();
            if (entry.getKey()) {
                iterator.remove();
            }
        }
    }

    private static void printMemoryUsage(String message) {
        Runtime runtime = Runtime.getRuntime();
        long memory = runtime.totalMemory() - runtime.freeMemory();
        System.out.println(message + ": Memory used = " + memory);
    }
}

