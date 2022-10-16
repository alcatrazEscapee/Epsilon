package com.alcatrazescapee.epsilon;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TomlParseTests
{
    @Test
    public void testEmpty()
    {
        assertEquals(result(Map.of(), false), parse());
    }

    @Test
    public void testEmptyLine()
    {
        assertEquals(result(Map.of(), false), parse(
            ""
        ));
    }

    @Test
    public void testWhitespaceLine()
    {
        assertEquals(result(Map.of(), false), parse(
            "  \t  "
        ));
    }

    @Test
    public void testComment()
    {
        assertEquals(result(Map.of(), false), parse(
            "# Comment"
        ));
    }

    @Test
    public void testCommentLeadingWhitespace()
    {
        assertEquals(result(Map.of(), false), parse(
            "    \t  # Comment"
        ));
    }

    @Test
    public void testCategoryOpen()
    {
        assertEquals(result(Map.of(), true), parse(
            "["
        ));
    }

    @Test
    public void testEmptyCategory()
    {
        assertEquals(result(Map.of(), true), parse(
            "[]"
        ));
    }

    @Test
    public void testCategoryNoClose()
    {
        assertEquals(result(Map.of(), true), parse(
            "[foo"
        ));
    }

    @Test
    public void testCategoryInvalidChar()
    {
        assertEquals(result(Map.of(), true), parse(
            "[foo bar]"
        ));
    }

    @Test
    public void testCategory()
    {
        assertEquals(result(Map.of("foo.key", true), false), parse(
            "[foo]",
            "    key = true"
        ));
    }

    @Test
    public void testCategoryDotCategory()
    {
        assertEquals(result(Map.of("foo.bar.key", false), false), parse(
            "[foo.bar]",
            "    key = false"
        ));
    }

    @Test
    public void testNullCategory()
    {
        assertEquals(result(Map.of("key", 0), false), parse(
            "key = 0"
        ));
    }

    @Test
    public void testNestedCategory()
    {
        assertEquals(result(Map.of("foo.int", 1, "bar.float", 1.0f), false), parse(
            "[foo]",
            "    int = 1",
            "[bar]",
            "    float = 1.0"
        ));
    }

    @Test
    public void testStringValue()
    {
        assertEquals(result(Map.of("key", "stuff"), false), parse(
            "key = \"stuff\""
        ));
    }

    @Test
    public void testStringValueWithEscapeCharacters()
    {
        assertEquals(result(Map.of("key", "\t\n\r and \""), false), parse(
            "key = \"\\t\\n\\r and \\\"\""
        ));
    }

    @Test
    public void testIntListValue()
    {
        assertEquals(result(Map.of("foo", List.of(1, 2, 3)), false), parse(
            "foo = [",
            "  1, 2, 3",
            "]"
        ));
    }

    @Test
    public void testMixedListValue()
    {
        assertEquals(result(Map.of("foo", List.of(1, "wibby wabble", 3.14f)), false), parse(
            "foo = [",
            "  1, \"wibby wabble\", 3.14",
            "]"
        ));
    }

    @Test
    public void testUnclosedListValue()
    {
        assertEquals(result(Map.of(), true), parse(
            "foo = [ 3, 2, 1"
        ));
    }

    @Test
    public void testListValueMissingComma()
    {
        assertEquals(result(Map.of(), true), parse(
            "foo = [ 3, 2 1 ]"
        ));
    }

    @Test
    public void testListValueExtraComma()
    {
        assertEquals(result(Map.of(), true), parse(
            "foo = [ 3, 2, 1, ]"
        ));
    }

    private TomlUtil.TomlParseResult parse(String... lines)
    {
        return TomlUtil.parse(String.join("\n", lines));
    }

    private TomlUtil.TomlParseResult result(Map<String, Object> map, boolean errors)
    {
        return new TomlUtil.TomlParseResult(map, errors);
    }
}
