# Epsilon

This is a minimal library for handling simple configuration files. It was designed with a few key principles in mind:

- Config files should be in an easy-to-read, easy-to-write, and simple language. A custom subset of the [Toml](https://github.com/toml-lang/toml) language was chosen for this purpose.
- Configuration options should be specified using a simple builder-style API, and allow default values, comments, and custom types.
- File reading should happen once, on an explicit invocation of `EpsilonUtil.parse()`.
- Accessing configuration options should be as simple and as performant as possible, once the configuration file has been loaded, performing no automatic unboxing (i.e. `Integer` -> `int`), no required casts (i.e. `double` -> `float`), or complex operations such as map lookups or string comparisons, and suitable for high performance code paths.


### Usage

```java
import com.alcatrazescapee.epsilon.*;

// Define a spec, which is a representation of all config options, comments, and value restrictions.
final SpecBuilder builder = Spec.builder();

// builder.define() can be invoked with int, boolean, float, String, or List types.
// The returned value, i.e. IntValue, is later used to access the value of the config option.
final IntValue intValue = builder.comment("An example integer value").define("intValue", 5);

// push() and pop() groups config options into categories.
builder.push("category");

// This value is grouped under the category 'category'
final BoolValue boolValue = builder.define("boolValue", false);

builder.pop();

Spec spec = builder.build();

// When desired, parse the config file
// The LOGGER::warn is used to record errors either during parsing, or invalid config values
EpsilonUtil.parse(spec, Path.of("config-file.toml"), LOGGER::warn)

// Access of the config values can be done at any time after that.
int value = intValue.getAsInt();

```