package com.alcatrazescapee.epsilon;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableBoolean;

public final class EpsilonUtil
{
    /**
     * Parse and load a config file. If the file does not exist, a new one will be created.
     * If either parse errors, or violations of the {@code spec} were present the file will be overwritten with a corrected file. This will preserve any modified values but correct any invalid ones.
     *
     * @param spec The config spec to be loaded.
     * @param path The path to the config file. Will be created if it does not exist.
     * @param onError A consumer for errors, either during parsing of the config file.
     */
    public static void parse(Spec spec, Path path, Consumer<String> onError)
    {
        parse(spec, path, onError, () -> write(spec, path, onError));
    }

    public static void parse(Spec spec, Path path, Consumer<String> onError, Runnable onWrite)
    {
        spec.parseDefaults();
        if (Files.notExists(path))
        {
            onWrite.run();
            return;
        }

        final String text;
        try
        {
            text = Files.readString(path);
        }
        catch (IOException e)
        {
            onError.accept("Unable to read file: '%s': %s".formatted(path, e));
            return;
        }

        final TomlUtil.TomlParseResult result = TomlUtil.parse(text);
        final MutableBoolean errors = new MutableBoolean(result.errors());
        final Map<String, Object> data = result.map();
        spec.parse(data, e -> {
            errors.setTrue();
            onError.accept(e.getMessage());
        });

        if (errors.booleanValue())
        {
            onWrite.run();
        }
    }

    public static void write(Spec spec, Path path, Consumer<String> onError)
    {
        try (final BufferedWriter writer = Files.newBufferedWriter(path))
        {
            spec.write(writer::write);
        }
        catch (IOException e)
        {
            onError.accept("Error writing file: '%s': %s".formatted(path, e));
        }
    }
}
