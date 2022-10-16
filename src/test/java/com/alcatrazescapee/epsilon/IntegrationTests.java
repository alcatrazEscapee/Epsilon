package com.alcatrazescapee.epsilon;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.alcatrazescapee.epsilon.value.BoolValue;
import com.alcatrazescapee.epsilon.value.FloatValue;
import com.alcatrazescapee.epsilon.value.IntValue;
import com.alcatrazescapee.epsilon.value.TypeValue;

import static org.assertj.core.api.Assertions.*;

public class IntegrationTests
{
    @Test
    public void testLoadingModifiedAndDefaultConfig() throws Exception
    {
        final SpecBuilder<Spec> builder = Spec.builder();

        final BoolValue boolValue = builder.define("boolValue", false);
        final IntValue intValue = builder.define("intValue", 0);
        final FloatValue floatValue = builder.define("floatValue", 1.5f);
        final TypeValue<String> stringValue = builder.define("stringValue", "wibby wabble");
        final BoolValue valueWithComment = builder.comment("Example comment").define("valueWithComment", false);
        final BoolValue valueWithMultilineComment = builder.comment("Example", "multiline", "comment").define("valueWithMultilineComment", false);
        final IntValue intValueWithRangeComment = builder.define("intValueWithRangeComment", 7, 5, 10);
        final FloatValue floatValueWithRangeComment = builder.define("floatValueWithRangeComment", 7.5f, 5f, 10.54321f);

        final TypeValue<Day> enumValue = builder.define("enumValue", Day.MONDAY, Day.class);
        final TypeValue<Day> enumValueWithRestriction = builder.comment("Must be a weekday").define("enumValueWithRestriction", Day.TUESDAY, Day.class, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY);

        builder.push("category");

        final IntValue valueInCategory = builder.define("valueInCategory", 3);
        final IntValue valueInCategoryWithComment = builder.comment("Comment within category").define("valueInCategoryWithComment", 5, 1, 10);

        builder.push("nestedCategory");

        final IntValue valueInNestedCategory = builder.define("valueInNestedCategory", 10);

        builder.swap("otherNestedCategory");

        final IntValue valueInOtherNestedCategory = builder.comment("Another category").define("valueInOtherNestedCategory", 20);

        builder.pop(2);

        final Spec spec = builder.build();

        final Path exampleConfig = Path.of("./out/example_config.toml");
        final Path defaultConfig = getResource("default_config.toml");
        final Path modifiedConfig = getResource("modified_config.toml");

        EpsilonUtil.write(spec, exampleConfig, Assertions::fail);

        assertThat(exampleConfig).hasSameTextualContentAs(defaultConfig);

        EpsilonUtil.parse(spec, modifiedConfig, Assertions::fail, () -> fail("Should not overwrite"));

        assertThat(boolValue.getAsBoolean()).isTrue();
        assertThat(intValue.getAsInt()).isEqualTo(1);
        assertThat(stringValue.get()).isEqualTo("boobly booble");
        assertThat(floatValue.getAsFloat()).isEqualTo(3.14f);
        assertThat(valueWithComment.getAsBoolean()).isTrue();

        assertThat(enumValue.get()).isEqualTo(Day.SATURDAY);
        assertThat(enumValueWithRestriction.get()).isEqualTo(Day.WEDNESDAY);

        // Parse back to default values
        spec.parseDefaults();

        assertThat(boolValue.getAsBoolean()).isFalse();
        assertThat(intValue.getAsInt()).isEqualTo(0);
        assertThat(stringValue.get()).isEqualTo("wibby wabble");
        assertThat(floatValue.getAsFloat()).isEqualTo(1.5f);
        assertThat(valueWithComment.getAsBoolean()).isFalse();

        assertThat(enumValue.get()).isEqualTo(Day.MONDAY);
        assertThat(enumValueWithRestriction.get()).isEqualTo(Day.TUESDAY);
    }

    @Test
    public void testLoadingConfigWithInvalidValues() throws Exception
    {
        final SpecBuilder<Spec> builder = Spec.builder();

        final IntValue intValueOutOfRange = builder.define("intValueOutOfRange", 3, 1, 10);
        final FloatValue floatValueOutOfRange = builder.define("floatValueOutOfRange", 3.0f, 1.0f, 10.0f);

        final Spec spec = builder.build();

        final Path exampleConfig = Path.of("./out/example_invalid_config.toml");
        final Path defaultConfig = getResource("default_invalid_config.toml");
        final Path modifiedConfig = getResource("modified_invalid_config.toml");

        EpsilonUtil.write(spec, exampleConfig, Assertions::fail);

        assertThat(exampleConfig).hasSameTextualContentAs(defaultConfig);

        final List<String> errors = new ArrayList<>();
        final MutableBoolean overwrite = new MutableBoolean(false);
        EpsilonUtil.parse(spec, modifiedConfig, errors::add, overwrite::setTrue);

        assertThat(errors).containsExactly(
              "Reading intValueOutOfRange: Value 50 not in range [1, 10]",
              "Reading floatValueOutOfRange: Value 0.54321 not in range [1.0, 10.0]"
        );
        assertThat(overwrite.booleanValue()).isTrue();

        assertThat(intValueOutOfRange.getAsInt()).isEqualTo(3);
        assertThat(floatValueOutOfRange.getAsFloat()).isEqualTo(3.0f);
    }

    private Path getResource(String path) throws Exception
    {
        final URL resource = ClassLoader.getSystemClassLoader().getResource(path);
        assertThat(resource).isNotNull();

        return Path.of(resource.toURI());
    }

    enum Day
    {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }
}
