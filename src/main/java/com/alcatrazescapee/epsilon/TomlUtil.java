package com.alcatrazescapee.epsilon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.text.StringEscapeUtils;

public class TomlUtil
{
    public static TomlParseResult parse(String text)
    {
        final Scanner scanner = new Scanner(text);
        final Parser parser = new Parser(scanner.outputs);

        scanner.scan();
        parser.parse();

        return new TomlParseResult(parser.values, scanner.error || parser.error);
    }

    public static <T> String writeValue(T value)
    {
        if (value instanceof List<?> list)
        {
            return "[%s]".formatted(list.stream().map(TomlUtil::writeValue).collect(Collectors.joining(", ")));
        }
        if (value instanceof String string)
        {
            return "\"%s\"".formatted(StringEscapeUtils.escapeJava(string));
        }
        return String.valueOf(value);
    }

    public record TomlParseResult(Map<String, Object> map, boolean errors) {}

    sealed interface Token permits
        TLiteral,
        TString,
        TName,
        TInt,
        TFloat,
        TList
    {
        Object value();
    }

    enum TLiteral implements Token
    {
        DOT,
        COMMA,
        EQUALS,
        LEFT_BRACKET,
        RIGHT_BRACKET,
        TRUE,
        FALSE,
        INVALID;

        @Override
        public Object value()
        {
            return this == TRUE ? true : (this == FALSE ? false : this);
        }
    }

    record TString(String value) implements Token {}
    record TName(String value) implements Token {}
    record TInt(Integer value) implements Token {}
    record TFloat(Float value) implements Token {}
    record TList(List<Token> value) implements Token {}

    final static class Scanner
    {
        final List<Token> outputs;

        final String text;

        int index;
        boolean error;

        Scanner(String text)
        {
            this.outputs = new ArrayList<>();
            this.text = text;
            this.index = 0;
            this.error = false;
        }

        void scan()
        {
            while (hasNext())
            {
                final char c = next();
                switch (c)
                {
                    case ' ', '\t', '\r', '\n' -> {}
                    case '[' -> push(TLiteral.LEFT_BRACKET);
                    case ']' -> push(TLiteral.RIGHT_BRACKET);
                    case '=' -> push(TLiteral.EQUALS);
                    case '.' -> push(TLiteral.DOT);
                    case ',' -> push(TLiteral.COMMA);
                    case '"' -> scanString();
                    case '#' -> scanComment();
                    default ->
                    {
                        if (isNumberPrefix(c))
                        {
                            scanNumber();
                        }
                        else if (isNamePrefix(c))
                        {
                            scanName();
                        }
                        else
                        {
                            error = true;
                        }
                    }
                }
            }
        }

        void scanString()
        {
            final int start = index;
            while (hasNext() && peek() != '"')
            {
                if (next() == '\\') next();
            }
            push(new TString(StringEscapeUtils.unescapeJava(text.substring(start, index))));
            next(); // Consume "
        }

        void scanComment()
        {
            while (hasNext() && peek() != '\n') next();
        }

        void scanNumber()
        {
            final int start = index - 1;
            while (isNumber(peek())) next();
            push(parseNumber(text.substring(start, index)));
        }

        Token parseNumber(String value)
        {
            try { return new TInt(Integer.parseInt(value)); }
            catch (NumberFormatException e) { /* ignored */ }
            try { return new TFloat(Float.parseFloat(value)); }
            catch (NumberFormatException e) { /* ignored */ }
            return TLiteral.INVALID;
        }

        void scanName()
        {
            final int start = index - 1;
            while (isName(peek())) next();
            final String value = text.substring(start, index);
            push(switch (value) {
                case "true" -> TLiteral.TRUE;
                case "false" -> TLiteral.FALSE;
                default -> new TName(value);
            });
        }

        boolean isNamePrefix(char c) { return Character.isLetter(c); }
        boolean isName(char c) { return Character.isLetterOrDigit(c) || c == '_' || c == '-'; }

        boolean isNumberPrefix(char c) { return c == '-' || c == '+' || isNumber(c); }
        boolean isNumber(char c) { return c == '.' || Character.isDigit(c); }

        boolean hasNext() { return index < text.length(); }
        char peek() { return hasNext() ? text.charAt(index) : '\0'; }
        char next()
        {
            final char c = peek();
            index++;
            return c;
        }

        void push(Token token) { outputs.add(token); }
    }

    final static class Parser
    {
        final List<Token> inputs;
        final Map<String, Object> values;

        String category;
        int index;
        boolean error;

        Parser(List<Token> inputs)
        {
            this.inputs = inputs;
            this.values = new HashMap<>();
            this.category = null;
            this.index = 0;
            this.error = false;
        }

        void parse()
        {
            while (hasNext())
            {
                final Token t = next();
                if (t == TLiteral.LEFT_BRACKET)
                {
                    parseCategory();
                }
                else if (t instanceof TName name)
                {
                    parseKeyValuePair(name.value);
                }
                else
                {
                    error = true;
                    next();
                }
            }
        }

        void parseCategory()
        {
            if (!hasNext() || !(peek() instanceof TName))
            {
                // Must have at least one entry
                error = true;
                return;
            }
            final List<String> category = new ArrayList<>();
            while (hasNext() && peek() instanceof TName name)
            {
                category.add(name.value);
                next();
                if (peek() == TLiteral.RIGHT_BRACKET)
                {
                    next();
                    this.category = String.join(".", category);
                    break;
                }
                else if (peek() == TLiteral.DOT)
                {
                    next();
                }
                else
                {
                    error = true;
                    break;
                }
            }
        }

        void parseKeyValuePair(String key)
        {
            if (next() != TLiteral.EQUALS)
            {
                error = true;
                return;
            }
            final Object value = parseValue();
            if (value == null)
            {
                error = true;
                return;
            }
            final String longKey = category == null ? key : category + "." + key;
            this.values.put(longKey, value);
        }

        Object parseValue()
        {
            if (peek() == TLiteral.LEFT_BRACKET)
            {
                next();
                return parseListValue();
            }
            else if (isValue(peek()))
            {
                return next().value();
            }
            return null;
        }

        List<Object> parseListValue()
        {
            final List<Object> list = new ArrayList<>();
            while (hasNext())
            {
                final Object value = parseValue();
                if (value == null) return null;
                list.add(value);
                if (peek() == TLiteral.RIGHT_BRACKET)
                {
                    next();
                    return list;
                }
                else if (peek() == TLiteral.COMMA)
                {
                    next();
                }
                else
                {
                    break;
                }
            }
            return null;
        }

        boolean isValue(Token t) { return t instanceof TString || t instanceof TInt || t instanceof TFloat || t == TLiteral.TRUE || t == TLiteral.FALSE; }

        boolean hasNext() { return index < inputs.size(); }
        Token peek() { return hasNext() ? inputs.get(index) : TLiteral.INVALID; }
        Token next()
        {
            final Token t = peek();
            index++;
            return t;
        }
    }
}
