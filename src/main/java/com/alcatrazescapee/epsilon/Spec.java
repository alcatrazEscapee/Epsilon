package com.alcatrazescapee.epsilon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.ArrayUtils;

import com.alcatrazescapee.epsilon.value.Value;
import org.jetbrains.annotations.Nullable;

public final class Spec
{
    public static SpecBuilder builder()
    {
        return new Builder();
    }

    private final Node root;

    Spec(Node root)
    {
        this.root = root;
    }

    void write(FileWriter writer) throws IOException
    {
        root.write(writer, 0);
    }

    void parse(Map<String, Object> element, Consumer<String> error)
    {
        root.parse(element, error);
    }

    void reset()
    {
        root.reset();
    }

    @FunctionalInterface
    interface FileWriter
    {
        void write(String text) throws IOException;
    }

    record Node(String name, @Nullable String[] comment, Map<String, Node> children, Map<String, TypedValue<?, ?, ?>> values)
    {
        Node(String name, @Nullable String[] comment)
        {
            this(name, comment, new LinkedHashMap<>(), new LinkedHashMap<>());
        }

        boolean containsKey(String key)
        {
            return children.containsKey(key) || values.containsKey(key);
        }

        void write(FileWriter writer, int depth) throws IOException
        {
            final String prefix = "    ".repeat(depth);
            for (final Map.Entry<String, TypedValue<?, ?, ?>> entry : values.entrySet())
            {
                final TypedValue<?, ?, ?> typed = entry.getValue();
                if (typed.comment() != null)
                {
                    for (final String line : typed.comment())
                    {
                        writer.write("%s# %s\n".formatted(prefix, line));
                    }
                }
                writer.write("%s%s = %s\n\n".formatted(prefix, typed.name(), typed.write()));
            }

            for (final Node value : children.values())
            {
                writer.write("\n");
                if (value.comment() != null)
                {
                    for (final String line : value.comment())
                    {
                        writer.write("%s# %s\n".formatted(prefix, line));
                    }
                }
                writer.write("%s[%s]\n\n".formatted(prefix, value.name));
                value.write(writer, depth + 1);
            }
        }

        void parse(Map<String, Object> map, Consumer<String> error)
        {
            for (final TypedValue<?, ?, ?> typed : values.values())
            {
                final Object value = map.get(typed.longName());
                if (value != null)
                {
                    typed.parse(value, error);
                }
                else
                {
                    error.accept("Missing value for: '%s'".formatted(typed.longName()));
                }
            }
            for (final Node value : children.values())
            {
                value.parse(map, error);
            }
        }

        void reset()
        {
            for (final TypedValue<?, ?, ?> value : values.values())
            {
                value.reset();
            }
            for (final Node value : children.values())
            {
                value.reset();
            }
        }
    }

    static class Builder implements SpecBuilder
    {
        private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9-_]*");

        private final List<Node> stack;
        private String[] comment;

        Builder()
        {
            this.stack = new ArrayList<>();
            this.stack.add(new Node("", null));
            this.comment = null;
        }

        @Override
        public SpecBuilder push(String name)
        {
            final Node top = peek();
            Preconditions.checkArgument(!name.isEmpty(), "Name is not allowed to be empty.");
            Preconditions.checkArgument(!top.containsKey(name), "Name '" + name + "' is already defined.");
            Preconditions.checkArgument(NAME_PATTERN.matcher(name).matches(), "Name must match the pattern [A-Za-z][A-Za-z0-9-_]*");
            final Node node = new Node(top.name.isEmpty() ? name : top.name + "." + name, comment);
            top.children.put(name, node);
            stack.add(node);
            this.comment = null;
            return this;
        }

        @Override
        public SpecBuilder pop()
        {
            Preconditions.checkArgument(stack.size() > 1, "Tried to pop from an empty stack.");
            stack.remove(stack.size() - 1);
            return this;
        }

        @Override
        public SpecBuilder comment(String... comment)
        {
            this.comment = this.comment == null ? comment : ArrayUtils.addAll(this.comment, comment);
            return this;
        }

        @Override
        public <T, U, V extends Value<U>> V define(String name, U defaultValue, ValueConverter<T, U, V> converter)
        {
            Preconditions.checkArgument(NAME_PATTERN.matcher(name).matches(), "Name must match the pattern [A-Za-z][A-Za-z0-9-_]*");
            Preconditions.checkArgument(!peek().containsKey(name), "Name '" + name + "' is already defined.");
            final V value = converter.create(defaultValue);
            final String longName = stack.size() <= 1 ? name : peek().name + "." + name;
            peek().values.put(name, new TypedValue<>(name, longName, comment, value, defaultValue, converter));
            value.set(defaultValue);
            this.comment = null;
            return value;
        }

        @Override
        public Spec build()
        {
            Preconditions.checkArgument(stack.size() == 1, "Unclosed categories in stack.");
            return new Spec(peek());
        }

        private Node peek()
        {
            return stack.get(stack.size() - 1);
        }
    }
}
