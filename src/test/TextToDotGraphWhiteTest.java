package test;

import main.TextToDotGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextToDotGraphWhiteTest {

    private TextToDotGraph graphTool;

    @Before
    public void setUp() throws Exception {
        graphTool = new TextToDotGraph();
    }

    @Test
    public void testW1() {
        graphTool.readTxt("test_text.txt");
        assertEquals("No \"is\" and \"art\" in the graph!", graphTool.queryBridgeWords("is","art"));
    }

    @Test
    public void testW2() {
        graphTool.readTxt("test_text.txt");
        assertEquals("No \"is\" in the graph!", graphTool.queryBridgeWords("is","to"));
    }

    @Test
    public void testW3() {
        graphTool.readTxt("test_text.txt");
        assertEquals("No \"is\" in the graph!", graphTool.queryBridgeWords("to","is"));
    }

    @Test
    public void testW4() {
        graphTool.readTxt("test_text.txt");
        assertEquals("No bridge words from \"civilizations\" to \"to\"!", graphTool.queryBridgeWords("civilizations","to"));
    }

    @Test
    public void testW5() {
        graphTool.readTxt("test_text.txt");
        assertEquals("No bridge words from \"to\" to \"new\"!", graphTool.queryBridgeWords("to","new"));
    }

    @Test
    public void testW6() {
        graphTool.readTxt("test_text.txt");
        assertEquals("The bridge words from \"to\" to \"out\" is: seek.", graphTool.queryBridgeWords("to","out"));
    }

    @Test
    public void testW7() {
        graphTool.readTxt("test_text.txt");
        assertEquals("The bridge words from \"new\" to \"and\" are: worlds, life.", graphTool.queryBridgeWords("new","and"));
    }

}