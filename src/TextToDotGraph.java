import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;


public class TextToDotGraph {
    // 用于存储图的邻接表
    private Map<String, Map<String, Integer>> graph = new HashMap<>();
    private String rootWord = null; //用于保存第一个单词(固定根节点为第一个单词)
    //    private Random random = new Random(); //用于随机选择桥接词
    private Random random = ThreadLocalRandom.current();  // 随机选择桥接词及随机游走时用到, 适用于在多线程环境
    private boolean stopRandomWalk = false;  //用于控制随机游走
    private Thread stopListenerThread;

    // 读取文本文件并构建有向图
    public void readTxt(String txtFile) {
        try {
            Scanner scanner = new Scanner(new File(txtFile));

            String lastWord = null; //用于保存前一行的最后一个单词

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().toLowerCase();
                String[] Words = line.split("[^a-zA-Z]+");
//                for (String word : words) {
//                    System.out.println(word);
//                }
                // 过滤句首的空字符
                String[] filteredWords = Arrays.stream(Words)
                        .filter(word -> !word.isEmpty())
                        .toArray(String[]::new);

                if (filteredWords.length == 0) continue;
                //固定图的根节点
                if (rootWord == null) {
                    rootWord = filteredWords[0];
                }

                // 记录每行最后一个单词, 并与下一行首单词间添加边
                if (lastWord != null) {
                    addEdge(lastWord, filteredWords[0], 1);
                }

                for (int i = 0; i < filteredWords.length - 1; i++) {
                    addEdge(filteredWords[i], filteredWords[i + 1], 1);
                }
                lastWord = filteredWords[filteredWords.length - 1];

                // 添加空边(最后一个单词没有出边, 其value值为0, 访问其会出现null错误)
                graph.putIfAbsent(lastWord, new HashMap<>());
            }
            scanner.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 向图中添加边
    private void addEdge(String from, String to, int weight) {
        graph.putIfAbsent(from, new HashMap<>());
        Map<String, Integer> edges = graph.get(from);
        edges.put(to, edges.getOrDefault(to, 0) + weight);
    }

    // 将图保存为DOT语言文件
    public void saveToDotFile(String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("digraph G {");
            // 固定根节点
            if (rootWord != null) {
                writer.printf("    \"%s\" [root=true];\n", rootWord);
            }
            for (String from : graph.keySet()) {
                for (String to : graph.get(from).keySet()) {
                    int weight = graph.get(from).get(to);
                    writer.printf("    \"%s\" -> \"%s\" [label=\"%d\"];\n", from, to, weight);
                }
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDirectedGraph(String dotFilePath, String outputImagePath) {
        try {
            // 构造 Graphviz 命令
            String[] cmd = {
                    "dot", "-Tpng", dotFilePath, "-o", outputImagePath
            };

            // 执行命令
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();

            // 输出 Graphviz 的错误流（如果有）
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            // 根据系统类型展示生成有向图
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                Runtime.getRuntime().exec("cmd /c start " + outputImagePath);
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("open " + outputImagePath);
            } else {
                Runtime.getRuntime().exec("xdg-open " + outputImagePath);
            }

            System.out.println("DOT file successfully converted to image.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 查询桥接词
    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsKey(word1) && !graph.containsKey(word2)) {
//            System.out.printf("No \"%s\" and \"%s\" in the graph!\n", word1, word2);
            return String.format("No \"%s\" and \"%s\" in the graph!", word1, word2);
        } else if (!graph.containsKey(word1)) {
//            System.out.printf("No \"%s\" in the graph!\n", word1);
            return String.format("No \"%s\" in the graph!", word1);
        }
        else if (!graph.containsKey(word2)) {
//            System.out.printf("No \"%s\" in the graph!\n", word2);
            return String.format("No \"%s\" in the graph!", word2);
        }
        Set<String> bridgeWords = new HashSet<>();
        Map<String, Integer> word1Edges = graph.get(word1);

        for (String word3 : word1Edges.keySet()) {
            Map<String, Integer> word3Edges = graph.get(word3);
            if (word3Edges.containsKey(word2)) {
                bridgeWords.add(word3);
            }
        }

        if (bridgeWords.isEmpty()) {
//            System.out.println("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
            return "No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!";
        } else {
//            System.out.print("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
            StringBuilder result = new StringBuilder("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are: ");
            int i = 0;
            for (String word : bridgeWords) {
                if (i > 0) result.append(", ");
                result.append(word);
                i++;
            }
            result.append(".");
            return result.toString();
        }
    }

    // 生成新的文本
    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase().split("[^a-zA-Z]+");
        StringBuilder newText = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            newText.append(words[i]).append(" ");
            String bridgeWord = getBridgeWord(words[i], words[i + 1]);
            if (bridgeWord != null) {
                newText.append(bridgeWord).append(" ");
            }
        }
        newText.append(words[words.length - 1]); //加入文本中的最后一个单词

        return newText.toString();
    }

    // 获取桥接词
    private String getBridgeWord(String word1, String word2) {
        if (!graph.containsKey(word1)) {
            return null;
        }
        List<String> bridgeWords = new ArrayList<>();
        // 获取从 word1 出发的所有边
        Map<String, Integer> word1Edges = graph.get(word1);
        // 遍历从 word1 出发的每个目标单词word3
        for (String word3 : word1Edges.keySet()) {
            // 如果从 word3 到 word2 存在边，将 word3 添加到桥接词列表中
            if (graph.get(word3) != null && graph.get(word3).containsKey(word2)) {
                bridgeWords.add(word3);
            }
        }

        if (bridgeWords.isEmpty()) {
            return null;
        }
        //随机选择一个桥接词
        return bridgeWords.get(random.nextInt(bridgeWords.size()));
    }

    // 最短路径部分实现
    // 将图保存为带有标记路径的DOT文件
    public void saveToDotFile_color(String outputFile, List<List<String>> shortestPaths, int pathLength) {
        List<String> dotLines = new ArrayList<>();
        // 不同最短路径的颜色也不同
        List<String> color = new ArrayList<>(Arrays.asList("blue", "red", "green", "orange", "pink"));

        int num_shortPath = shortestPaths.size();

        // 添加固定根节点
        if (rootWord != null) {
            dotLines.add(String.format("    \"%s\" [root=true];", rootWord));
        }

        // 遍历图中的每条边
        for (String from : graph.keySet()) {
            for (String to : graph.get(from).keySet()) {
                int weight = graph.get(from).get(to);
                int flag = -1;
                int index1, index2;
                for(int i = 0; i < num_shortPath; i++)
                {
                    List<String> shortestPath = shortestPaths.get(i);
                    if((index1 = shortestPath.indexOf(from)) != -1 && (index2 = shortestPath.indexOf(to)) != -1)
                    {
                        if(index1 + 1 == index2)
                        {
                            if(flag != -1)
                            {
                                flag = -2;
                            }
                            else {
                                flag = i;
                            }
                        }
                    }
                }
                if (flag == -2) {
                    dotLines.add(String.format("    \"%s\" -> \"%s\" [label=\"%d\", color=\"yellow\"];", from, to, weight));
                } else if(flag == -1) {
                    dotLines.add(String.format("    \"%s\" -> \"%s\" [label=\"%d\"];", from, to, weight));
                }else{
                    dotLines.add(String.format("    \"%s\" -> \"%s\" [label=\"%d\", color=\"%s\"];", from, to, weight, color.get(flag) ));
                }
            }
        }

        // 添加路径长度注释（使用黑色）
        dotLines.add(String.format("    \"Path length = %d\" [label=\"Path length = %d\", color=\"black\", shape=none];", pathLength, pathLength));

        // 将所有 DOT 语句一次性写入文件
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writer.println("digraph G {");
            for (String line : dotLines) {
                writer.println(line);
            }
            writer.println("}");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用迪杰斯特拉算法计算最短路径
    public List<List<String>> shortestPaths(String startWord, String endWord, int[] pathLength) {
        startWord = startWord.toLowerCase();
        endWord = endWord.toLowerCase();

        if (!graph.containsKey(startWord) && !graph.containsKey(endWord)) {
            System.out.printf("No \"%s\" and \"%s\" in the graph!\n", startWord, endWord);
            return null;
        } else if (!graph.containsKey(startWord)) {
            System.out.printf("No \"%s\" in the graph!\n", startWord);
            return null;
        }
        else if (!graph.containsKey(endWord)) {
            System.out.printf("No \"%s\" in the graph!\n", endWord);
            return null;
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>();
        Set<String> visited = new HashSet<>();
        PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        // 初始化距离和前驱节点
        for (String word : graph.keySet()) {
            distances.put(word, Integer.MAX_VALUE);
            predecessors.put(word, new ArrayList<>());
        }

        distances.put(startWord, 0);
        queue.add(startWord);

        // 迪杰斯特拉算法主循环
        while (!queue.isEmpty()) {
            String currentWord = queue.poll();
            if (visited.contains(currentWord)) {
                continue;
            }
            visited.add(currentWord);

            Map<String, Integer> neighbors = graph.get(currentWord);
            if (neighbors == null) {
                continue;
            }
            for (String neighbor : neighbors.keySet()) {
                if (!visited.contains(neighbor)) {
                    int newDistance = distances.get(currentWord) + neighbors.get(neighbor);
                    if (newDistance < distances.get(neighbor)) {
                        distances.put(neighbor, newDistance);
                        queue.add(neighbor);
                        predecessors.get(neighbor).clear();
                        predecessors.get(neighbor).add(currentWord);
                    } else if (newDistance == distances.get(neighbor)) {
                        predecessors.get(neighbor).add(currentWord);
                    }
                }
            }
        }

        // 构建所有从起点到终点的最短路径
        List<List<String>> shortestPaths = new ArrayList<>();
        buildPaths(predecessors, shortestPaths, new LinkedList<>(), endWord, startWord, distances, pathLength);

        if (shortestPaths.isEmpty())
        {
            System.out.printf("there is no way form \"%s\" to \"%s\"\n", startWord, endWord);
        }

        return shortestPaths;
    }

    // 构建最短路径
    private void buildPaths(Map<String, List<String>> predecessors, List<List<String>> paths, LinkedList<String> path,
                            String current, String start, Map<String, Integer> distances, int[] pathLength) {
        path.addFirst(current);
        if (current.equals(start)) {
            paths.add(new ArrayList<>(path));
            pathLength[0] = distances.get(path.getLast());
        } else {
            if (predecessors.get(current) == null) return;
            for (String predecessor : predecessors.get(current)) {
                buildPaths(predecessors, paths, path, predecessor, start, distances, pathLength);
            }
        }
        path.removeFirst();
    }


    // 随机游走部分实现
    // 开启监听
    public void startStopListener() {
        stopListenerThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Press any key to stop the random walk...");
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    if (System.in.available() > 0) {
                        stopRandomWalk = true;
                        break; // 停止循环
                    }
                    Thread.sleep(100); // 等待100毫秒
                }

                // 检查是否收到了中断信号
                if (Thread.currentThread().isInterrupted()) {
                    System.out.println("Listener thread is interrupted. Exiting...");
                    return; // 退出线程
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 重新设置中断状态
            }
        });
        stopListenerThread.start();
    }
    // 停止监听
    public void stopStopListener() {
        if (stopListenerThread != null && stopListenerThread.isAlive()) {
            stopListenerThread.interrupt();
            try {
                stopListenerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public String randomWalk(String outputFile) {
        List<String> nodes = new ArrayList<>(graph.keySet());
        if (nodes.isEmpty()) {
//            System.out.println("The graph is empty!");
            return "The graph is empty!";
        }
        stopRandomWalk = false;
        // 开启监听
        startStopListener();

        String current = nodes.get(random.nextInt(nodes.size()));
        Set<String> visitedEdges = new HashSet<>();
        List<String> path = new ArrayList<>();

        while (!stopRandomWalk) {
            path.add(current);
            Map<String, Integer> edges = graph.get(current);

            if (edges == null || edges.isEmpty()) {
                stopRandomWalk = true;
                break;
            }

            // 减慢随机游走速度
            try {
                Thread.sleep(100); // 毫秒
            } catch (InterruptedException e) {
                // 处理异常
            }

            List<String> nextNodes = new ArrayList<>(edges.keySet());
            String next = nextNodes.get(random.nextInt(nextNodes.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) {
                path.add(next);
                //把重复的边的的node2也进行输出
                stopRandomWalk = true;
                break;
            }

            visitedEdges.add(edge);
            current = next;
        }
        // 满足条件结束随机游走时自动停止监听
        stopStopListener();

        // 构建路径字符串
        StringBuilder result = new StringBuilder("The random walk path is: ");
//        System.out.print("The random walk path is: ");
//        for (String word : path) {
//            System.out.printf("%s ", word);
//        }
//        System.out.print("\n");
        for (String word : path) {
            result.append(word).append(" ");
        }

        savePathToFile(path, outputFile);
//        System.out.println("Random walk stopped. Path saved to " + outputFile);
        result.append("\nRandom walk stopped. Path saved to ").append(outputFile);
        return result.toString();
    }

    // 保存随机游走路径到文件
    private void savePathToFile(List<String> path, String outputFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            for (String word : path) {
                writer.print(word + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        TextToDotGraph graph = new TextToDotGraph();

        // 读取用户的命令行输入
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // 用于清空buff
            try {
                while(System.in.available() > 0)
                {
                    scanner.nextLine();
                }
            }
            catch (IOException ignored)
            {

            }
            System.out.println("Select an option:");
            System.out.println("1. 读入文本并生成有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 根据桥接词生成新文本");
            System.out.println("4. 计算两单词间的最短路径");
            System.out.println("5. 随机游走");
            System.out.println("6. 退出");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Enter the text file path: ");
                    String txtFile = scanner.nextLine();
                    graph.readTxt(txtFile);
                    graph.saveToDotFile("./out/text/output.dot");
                    graph.showDirectedGraph("./out/text/output.dot", "./out/png/graph.png");
                    break;

                case "2":
                    System.out.print("Enter the first word: ");
                    String word1 = scanner.nextLine();
                    System.out.print("Enter the second word: ");
                    String word2 = scanner.nextLine();
                    String result = graph.queryBridgeWords(word1, word2);
                    System.out.println(result);
                    break;

                case "3":
                    System.out.print("Enter the input text: ");
                    String inputText = scanner.nextLine();
                    String newText = graph.generateNewText(inputText);
                    System.out.println("Generated new text: " + newText);
                    break;

                case "4":
                    System.out.print("Enter the first word: ");
                    word1 = scanner.nextLine();
                    System.out.print("Enter the second word: ");
                    word2 = scanner.nextLine();

                    int[] pathLength = new int[1];
                    List<List<String>> shortestPaths = graph.shortestPaths(word1, word2, pathLength);

                    if (shortestPaths != null && !shortestPaths.isEmpty()) {
                        graph.saveToDotFile_color("./out/text/output_with_path.dot", shortestPaths, pathLength[0]);
                        graph.showDirectedGraph("./out/text/output_with_path.dot", "./out/png/shortest_paths.png");
                    }
                    break;

                case "5":
                    System.out.print("Enter the output file path for random walk: ");
                    String outputFile = scanner.nextLine();
                    String result2 = graph.randomWalk(outputFile);
                    System.out.println(result2);
                    break;

                case "6":
                    scanner.close();
                    System.out.println("Exiting...");
                    return;

                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }

//        String fileName = "input.txt";

//        // 测试桥接词查询
//        graph.findBridgeWords("explore", "new");
//        graph.findBridgeWords("to", "worlds");
//        graph.findBridgeWords("life", "unknown");
//
//        // 测试生成新文本
//        String inputText = "Seek to explore new and exciting synergies";
//        String newText = graph.generateNewText(inputText);
//        System.out.printf("The new text is: %s\n", newText);
//
//        // 最短路迪杰斯特拉
//        List<List<String>> shortestPaths = graph.shortestPaths("to", "and");

//        // 保存随机游走路径
//        graph.randomWalk("random_walk.txt");
    }
}