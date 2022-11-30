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
        final SpecBuilder builder = Spec.builder();

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

        builder
            .comment("Example comment on a top level category")
            .comment("This comment", "has", "multiple", "lines")
            .push("category");

        final IntValue valueInCategory = builder.define("valueInCategory", 3);
        final IntValue valueInCategoryWithComment = builder.comment("Comment within category").define("valueInCategoryWithComment", 5, 1, 10);

        builder
            .comment("Example comment on a nested category")
            .comment("Note how the indent matches up nicely")
            .push("nestedCategory");

        final IntValue valueInNestedCategory = builder.define("valueInNestedCategory", 10);

        builder.swap("otherNestedCategory");

        final IntValue valueInOtherNestedCategory = builder.comment("Another category").define("valueInOtherNestedCategory", 20);

        builder.pop(2);

        final Spec spec = builder.build();

        final Path exampleConfig = Path.of("./build/example_config.toml");
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
        assertThat(valueWithMultilineComment.getAsBoolean()).isFalse();
        assertThat(intValueWithRangeComment.getAsInt()).isEqualTo(6);
        assertThat(floatValueWithRangeComment.getAsFloat()).isEqualTo(5.7654f);

        assertThat(enumValue.get()).isEqualTo(Day.SATURDAY);
        assertThat(enumValueWithRestriction.get()).isEqualTo(Day.WEDNESDAY);

        assertThat(valueInCategory.getAsInt()).isEqualTo(3);
        assertThat(valueInCategoryWithComment.getAsInt()).isEqualTo(5);
        assertThat(valueInNestedCategory.getAsInt()).isEqualTo(10);
        assertThat(valueInOtherNestedCategory.getAsInt()).isEqualTo(20);

        // Parse back to default values
        spec.parseDefaults();

        assertThat(boolValue.getAsBoolean()).isFalse();
        assertThat(intValue.getAsInt()).isEqualTo(0);
        assertThat(stringValue.get()).isEqualTo("wibby wabble");
        assertThat(floatValue.getAsFloat()).isEqualTo(1.5f);
        assertThat(valueWithComment.getAsBoolean()).isFalse();
        assertThat(valueWithMultilineComment.getAsBoolean()).isFalse();
        assertThat(intValueWithRangeComment.getAsInt()).isEqualTo(7);
        assertThat(floatValueWithRangeComment.getAsFloat()).isEqualTo(7.5f);

        assertThat(enumValue.get()).isEqualTo(Day.MONDAY);
        assertThat(enumValueWithRestriction.get()).isEqualTo(Day.TUESDAY);

        assertThat(valueInCategory.getAsInt()).isEqualTo(3);
        assertThat(valueInCategoryWithComment.getAsInt()).isEqualTo(5);
        assertThat(valueInNestedCategory.getAsInt()).isEqualTo(10);
        assertThat(valueInOtherNestedCategory.getAsInt()).isEqualTo(20);
    }

    @Test
    public void testLoadingConfigWithInvalidValues() throws Exception
    {
        final SpecBuilder builder = Spec.builder();

        final IntValue intValueOutOfRange = builder.define("intValueOutOfRange", 3, 1, 10);
        final FloatValue floatValueOutOfRange = builder.define("floatValueOutOfRange", 3.0f, 1.0f, 10.0f);

        final FloatValue floatValueCoercedFromInt = builder.define("floatValueCoercedFromInt", 3.0f);

        final TypeValue<Day> enumValueWithRestriction = builder.comment("Must be a weekday").define("enumValueWithRestriction", Day.TUESDAY, Day.class, Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY);
        final TypeValue<Day> enumValueInvalid = builder.define("enumValueInvalid", Day.MONDAY, Day.class);

        final Spec spec = builder.build();

        final Path exampleConfig = Path.of("./build/example_invalid_config.toml");
        final Path defaultConfig = getResource("default_invalid_config.toml");
        final Path modifiedConfig = getResource("modified_invalid_config.toml");

        EpsilonUtil.write(spec, exampleConfig, Assertions::fail);

        assertThat(exampleConfig).hasSameTextualContentAs(defaultConfig);

        final List<String> errors = new ArrayList<>();
        final MutableBoolean overwrite = new MutableBoolean(false);
        EpsilonUtil.parse(spec, modifiedConfig, errors::add, overwrite::setTrue);

        assertThat(errors).containsExactly(
            "Reading intValueOutOfRange: Value 50 not in range [1, 10]",
            "Reading floatValueOutOfRange: Value 0.54321 not in range [1.0, 10.0]",
            "Reading enumValueWithRestriction: Invalid value: 'SUNDAY', must be one of [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]",
            "Reading enumValueInvalid: Invalid value: 'monday', must be one of [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY]"
        );
        assertThat(overwrite.booleanValue()).isTrue();

        assertThat(intValueOutOfRange.getAsInt()).isEqualTo(3);
        assertThat(floatValueOutOfRange.getAsFloat()).isEqualTo(3.0f);
        assertThat(floatValueCoercedFromInt.getAsFloat()).isEqualTo(5.0f);
        assertThat(enumValueWithRestriction.get()).isEqualTo(Day.TUESDAY);
        assertThat(enumValueInvalid.get()).isEqualTo(Day.MONDAY);
    }

    @Test
    public void testModifyingConfigUsingSetThenSaving() throws Exception
    {
        final SpecBuilder builder = Spec.builder();

        final IntValue intValue = builder.define("intValue", 3, 1, 10);
        final FloatValue floatValue = builder.define("floatValue", 3.0f, 1.0f, 10.0f);

        final Spec spec = builder.build();

        final Path exampleConfig = Path.of("./build/example_set_config.toml");
        final Path defaultConfig = getResource("default_set_config.toml");

        intValue.set(7);
        floatValue.set(7.0f);

        // write() should use the config values exactly, and not reset to defaults
        EpsilonUtil.write(spec, exampleConfig, Assertions::fail);

        assertThat(exampleConfig).hasSameTextualContentAs(defaultConfig);

        assertThat(intValue.getAsInt()).isEqualTo(7);
        assertThat(floatValue.getAsFloat()).isEqualTo(7.0f);
    }

    @Test
    public void testMissingConfigOptionCausesWriting() throws Exception
    {
        final SpecBuilder builder = Spec.builder();

        final IntValue intValue = builder.define("intValue", 3);
        final FloatValue floatValue = builder.define("floatValue", 3.0f);

        builder.push("insideBox");

        final BoolValue boolValueInCategory = builder.define("boolValueInCategory", true);

        builder.pop();

        final Spec spec = builder.build();

        final Path defaultConfig = getResource("default_missing_options_config.toml");

        // write() should use the config values exactly, and not reset to defaults
        final List<String> errors = new ArrayList<>();
        final MutableBoolean overwrite = new MutableBoolean(false);

        EpsilonUtil.parse(spec, defaultConfig, errors::add, overwrite::setTrue);

        assertThat(errors).containsExactly(
            "Missing value for: 'intValue'",
            "Missing value for: 'floatValue'",
            "Missing value for: 'insideBox.boolValueInCategory'"
        );
        assertThat(overwrite.booleanValue()).isTrue();

        assertThat(intValue.getAsInt()).isEqualTo(3);
        assertThat(floatValue.getAsFloat()).isEqualTo(3.0f);
        assertThat(boolValueInCategory.getAsBoolean()).isTrue();
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
