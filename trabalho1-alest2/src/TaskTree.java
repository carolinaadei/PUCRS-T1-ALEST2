import java.io.*;
import java.util.*;

class Task {
    String idTask;
    int durationTask;
    List<Task> children = new ArrayList<>();

    Task(String idTask, int durationTask) {
        this.idTask = idTask;
        this.durationTask = durationTask;
    }
}

public class TaskTree {
    public static Map<String, Task> tasks = new HashMap<>();

    public static int parseFile(String entrada) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(entrada));
        String line;
        int numProc = 0;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("#")) {
                numProc = Integer.parseInt(line.split(" ")[2]);
                continue;
            }

            if (!line.contains("->")) continue;

            String[] parts = line.split("->");
            String[] left = parts[0].trim().split("_");
            String[] right = parts[1].trim().split("_");

            String leftTaskId = left[0];
            int leftTaskDuration = Integer.parseInt(left[1]);
            String rightTaskId = right[0];
            int rightTaskDuration = Integer.parseInt(right[1]);

            tasks.putIfAbsent(leftTaskId, new Task(leftTaskId, leftTaskDuration));
            tasks.putIfAbsent(rightTaskId, new Task(rightTaskId, rightTaskDuration));

            tasks.get(leftTaskId).children.add(tasks.get(rightTaskId));
        }
        return numProc;
    }

    static int schedule(int numProc, boolean minPolicy) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, Integer> remaining = new HashMap<>();

        for (Task t : tasks.values()) {
            indegree.put(t.idTask, 0);
            remaining.put(t.idTask, t.durationTask);
        }

        for (Task t : tasks.values()) {
            for (Task child : t.children) {
                indegree.put(child.idTask, indegree.get(child.idTask) + 1);
            }
        }

        PriorityQueue<String> ready = new PriorityQueue<>((a, b) ->
                minPolicy ? Integer.compare(remaining.get(a), remaining.get(b))
                        : Integer.compare(remaining.get(b), remaining.get(a)));

        for (Task t : tasks.values()) {
            if (indegree.get(t.idTask) == 0) {
                ready.add(t.idTask);
            }
        }

        Map<String, Integer> running = new HashMap<>();
        int totalTime = 0;

        while (!ready.isEmpty() || !running.isEmpty()) {
            while (running.size() < numProc && !ready.isEmpty()) {
                String task = ready.poll();
                running.put(task, remaining.get(task));
            }

            int timeSlice = Collections.min(running.values());
            totalTime += timeSlice;

            Set<String> finished = new HashSet<>();
            for (String task : running.keySet()) {
                int newTime = running.get(task) - timeSlice;
                if (newTime == 0) {
                    finished.add(task);
                } else {
                    running.put(task, newTime);
                    remaining.put(task, newTime);
                }
            }

            for (String task : finished) {
                running.remove(task);
                for (Task child : tasks.get(task).children) {
                    indegree.put(child.idTask, indegree.get(child.idTask) - 1);
                    if (indegree.get(child.idTask) == 0) {
                        ready.add(child.idTask);
                    }
                }
            }
        }
        return totalTime;
    }

    public static void main(String[] args) {
        try {
            int numProc = parseFile("entrada.txt");
            System.out.println("MIN: " + schedule(numProc, true));
            System.out.println("MAX: " + schedule(numProc, false));
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
        }
    }
}