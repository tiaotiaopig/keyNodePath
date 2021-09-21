import java.io.*;
import java.util.*;

/**
 * 计算图中 点 或者 边 的拓扑权重
 * 点的拓扑权重就是其相邻的边的权重和
 * 边的权重计算如下：
 *      先计算出图中任意两个节点存在的路径
 *      也就是图中所有可能的路径之和（不能存在环路）
 *
 * @author Lifeng
 * @date 2021/9/4 10:31
 */
public class Path {
    private final static int HOP_MAX = 13;      // 最大路径长度
    private boolean[] visited;    // 防止环路
    private List<List<Integer>> graph;    // 邻接表
    private List<List<Integer>> path;     // 存储所有路径
    private int[][] graph2;
    private int[][] path2;
    private int[] nodes;    // 所有节点编号
    private int[][] edgeWeight;     // 边的权重
    private int[][] nodeWeight;     // 节点权重 [?][0] 节点编号  [?][1] 节点权重
    private int[][] edges;

    public Path(String filename) {
        // 从文件读出所有边
        edges = readFileToArray(filename);
        // 获取所有节点编号
        nodes = getNodes();
        int idMax = 0;
        if (nodes != null) idMax = Arrays.stream(nodes).max().getAsInt();
        visited = new boolean[idMax + 1];
        edgeWeight = new int[idMax + 1][idMax + 1];
        nodeWeight = new int[idMax][2];
        // 数组形式的邻接表
    }

    public Path(int idMax) {
        graph = new ArrayList<>(idMax + 1);
        path = new ArrayList<>();
        visited = new boolean[idMax + 2];
        edgeWeight = new int[idMax + 1][idMax + 1];
        nodeWeight = new int[idMax][2];
        for (int i = 0; i <= idMax; i++) graph.add(new ArrayList<>());
    }

    /**
     * 从文件中读出所有边，并转化成 n * 2 的二维矩阵
     * @param filename
     * @return
     */
    private int[][] readFileToArray(String filename) {
        String line;
        ArrayList<Integer> list = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                String[] edge = line.split(" ");
                list.add(Integer.parseInt(edge[0]));
                list.add(Integer.parseInt(edge[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 转成 n * 2 数组返回
        int rows = list.size() / 2;
        int[][] edges = new int[rows][2];
        for (int i = 0, j = 0; i < rows; i++, j += 2) {
            edges[i][j % 2] = list.get(j);
            edges[i][(j + 1 % 2)] = list.get(j + 1);
        }
        return edges;
    }

    private int[] getNodes() {
        if (nodes != null) return nodes;
        HashSet<Integer> set = new HashSet<>();
        if (edges != null) {
            for (int[] edge : edges) {
                set.add(edge[0]);
                set.add(edge[1]);
            }
        }
        if (!set.isEmpty()) {
            int index = 0;
            int[] tmp = new int[set.size()];
            for (Integer integer : set) tmp[index++] = integer;
            return tmp;
        }
        return null;
    }

    /**
     * 寻找图中任意两个节点的路径数总和
     * @return
     */
    public int find() {
        int res = 0;
        for (int i = 0; i < nodes.length - 1; i++) {
            for (int j = i + 1; j < nodes.length; j++) {
                res += dfs(nodes[i], nodes[j], new ArrayList<>(), 0);
            }
        }
        return res;
    }

    public List<List<Integer>> getPath() {
        return path;
    }

    /**
     * 获取边 (source, target) 关联的所有路径数
     * 也即删除该边后，总路径数的减少量
     * @param source
     * @param target
     * @return
     */
    public int getEdgeWeight(int source, int target) {
        if (edgeWeight[source][target] != 0) return edgeWeight[source][target];
        int res = 0, left, right, len;
        for (List<Integer> list : path) {
            right = list.get(0);
            len = list.size();
            for (int i = 1; i < len; i++) {
                left = right;
                right = list.get(i);
                if ((source == left && target == right) || (source == right && target == left)) {
                    res++;
                    break;
                }
            }
        }
        edgeWeight[source][target] = res;
        edgeWeight[target][source] = res;
        return res;
    }

    public int getNodeWeight(int node) {
        int res = 0, len;
        for (List<Integer> list : path) {
            len = list.size();
            for (int i = 0; i < len; i++) {
                if (list.get(i) == node) {
                    res++;
                    break;
                }
            }
        }
        return res;
    }

    /**
     * 使用递归的深度搜索方法，寻找任意两个节点间的路径
     * @param curr 当前节点
     * @param target 目标节点
     * @param selected 已经走过的路径
     * @param hop 经过的跳数
     * @return 当前和目标的路径数
     */
    private int dfs(int curr, int target, List<Integer> selected, int hop) {
        if (hop == HOP_MAX) return 0;
        if (curr == target) {
            List<Integer> list = new ArrayList<>(selected);
            list.add(curr);
            path.add(list);
            return 1;
        }
        int res = 0;
        // 加入路径
        visited[curr] = true;
        selected.add(curr);
        // 基于邻接表，获取curr的所有邻接点
        List<Integer> list = graph.get(curr);
        for (Integer adjacent : list) {
            if (!visited[adjacent]) res += dfs(adjacent, target, selected, hop + 1);
        }
        // 恢复现场
        visited[curr] = false;
        selected.remove(selected.size() - 1);
        return res;
    }

    public void readFile(String filename) throws IOException {
        String line;
        HashSet<Integer> set = new HashSet<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            String[] edge = line.split(" ");
            // 解析出边上的两个节点
            int source = Integer.parseInt(edge[0]), target = Integer.parseInt(edge[1]);
            // 加入临界矩阵
            graph.get(source).add(target);
            graph.get(target).add(source);
            // 节点集
            set.add(source);
            set.add(target);
        }
        int index = 0;
        nodes = new int[set.size()];
        //
        for (Integer integer : set) nodes[index++] = integer;
    }

    public int[][] getNodeWeight() {
        int len = nodes.length;
        for (int i = 0; i < len; i++) {
            nodeWeight[i][0] = nodes[i];
            nodeWeight[i][1] = getNodeWeight(nodes[i]);
            Arrays.sort(nodeWeight, (a, b) -> b[1] - a[1]);
        }
        return nodeWeight;
    }

    public static void main(String[] args) throws IOException {
        String out = "result\\";
        File newFile;
        File[] files = new File("graph\\real").listFiles();
        for (File file : files) {
            if (file.isFile()) {
                Path obj = new Path(101);
                newFile = new File(out + file.getName());
                BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
                obj.readFile(file.getAbsolutePath());
                obj.find();
                int[][] nodeWeight = obj.getNodeWeight();
                for (int i = 0; i < 10; i++) {
                    writer.write("node-" + nodeWeight[i][0] + " : " + nodeWeight[i][1]);
                    writer.newLine();
                }
                writer.flush();
                writer.close();
            }
        }
    }
}
