package test;

import main.TextToDotGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TextToDotGraphWhiteTest {

    private TextToDotGraph graphTool;

    @Before
    public void setUp() throws Exception {
        graphTool = new TextToDotGraph();
        // 输入的graph
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1, "seek", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1, "life", 1, "civilizations", 1)));
        graphTool.graph.put("worlds", new HashMap<>(Map.of("and", 1)));
        graphTool.graph.put("and", new HashMap<>(Map.of("to", 1, "new", 1)));
        graphTool.graph.put("seek", new HashMap<>(Map.of("out", 1)));
        graphTool.graph.put("out", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("life", new HashMap<>(Map.of("and", 1)));
        graphTool.graph.put("civilizations", new HashMap<>());
    }

    @Test
    public void testW1() {
        // 期望输出
        String expected = "No \"is\" and \"art\" in the graph!";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("is","art"));
    }

    @Test
    public void testW2() {
        // 期望输出
        String expected = "No \"is\" in the graph!";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("is","to"));
    }

    @Test
    public void testW3() {
        // 期望输出
        String expected = "No \"is\" in the graph!";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("to","is"));
    }

    @Test
    public void testW4() {
        // 期望输出
        String expected = "No bridge words from \"civilizations\" to \"to\"!";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("civilizations","to"));
    }

    @Test
    public void testW5() {
        // 期望输出
        String expected = "No bridge words from \"to\" to \"new\"!";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("to","new"));
    }

    @Test
    public void testW6() {
        // 期望输出
        String expected = "The bridge words from \"to\" to \"out\" is: seek.";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("to","out"));
    }

    @Test
    public void testW7() {
        // 期望输出
        String expected = "The bridge words from \"new\" to \"and\" are: worlds, life.";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.queryBridgeWords("new","and"));
    }

}