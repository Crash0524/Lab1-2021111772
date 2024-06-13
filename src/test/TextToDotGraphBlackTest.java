package test;

import main.TextToDotGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.*;

import static org.junit.Assert.*;

public class TextToDotGraphBlackTest {

    private TextToDotGraph graphTool;

    @Before
    public void setUp() {
        graphTool = new TextToDotGraph();
        graphTool.graph = new HashMap<>();
    }

    @Test
    public void testE1() {
        // 输入的graph
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));
        // 输入的text
        String text = "to strange worlds";
        // 期望输出
        String expected = "to explore strange new worlds";
        // 对比期望输出和真实输出
        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testE2() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));
        graphTool.graph.put("seek", new HashMap<>(Map.of("out", 1)));
        graphTool.graph.put("out", new HashMap<>(Map.of("new", 1)));

        String text = "to chase strange worlds";
        String expected = "to chase strange new worlds";

        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testE3() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));

        String text = "To explore strange worlds";
        String expected = "to explore strange new worlds";

        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testE4() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));

        String text = "To explore strange new worlds";
        String expected = "to explore strange new worlds";

        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testE5() {
        graphTool.graph.put("to", new HashMap<>(Map.of("discover", 1, "embrace", 1, "uncover", 1)));
        graphTool.graph.put("discover", new HashMap<>(Map.of("the", 1)));
        graphTool.graph.put("embrace", new HashMap<>(Map.of("the", 1)));
        graphTool.graph.put("uncover", new HashMap<>(Map.of("the", 1)));

        String text = "To the stars";
        String expected1 = "to discover the stars";
        String expected2 = "to embrace the stars";
        String expected3 = "to uncover the stars";

        String result = graphTool.generateNewText(text);
        assert(result.equals(expected1) || result.equals(expected2) || result.equals(expected3));
    }

    @Test
    public void testI1() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));
        String text = "";
        String expected = "";

        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testI2() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));

        String text = "Unknown word and sentence";
        String expected = "unknown word and sentence";

        assertEquals(expected, graphTool.generateNewText(text));
    }

    @Test
    public void testI3() {
        graphTool.graph.put("to", new HashMap<>(Map.of("explore", 1)));
        graphTool.graph.put("explore", new HashMap<>(Map.of("strange", 1)));
        graphTool.graph.put("strange", new HashMap<>(Map.of("new", 1)));
        graphTool.graph.put("new", new HashMap<>(Map.of("worlds", 1)));

        String text = "To, explore; 123 strange! 4 worlds.";
        String expected = "to explore strange new worlds";

        assertEquals(expected, graphTool.generateNewText(text));
    }
}