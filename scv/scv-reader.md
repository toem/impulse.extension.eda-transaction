# SCV (SystemC Verification) Transaction Reader

> Status: Beta

The SCV Reader parses and analyzes transaction-based simulation data stored in the SCV (SystemC Verification) text log format for the impulse framework. It processes event streams, generators, transactions, attributes, and relations as produced by SystemC TLM simulations and compatible EDA tools.

Features:
- Parses SCV text log files (e.g., .scv, .txlog) with support for streams, generators, events, and attributes
- Hierarchical signal creation from stream definitions, using a configurable path separator
- Transaction event processing with timestamp synchronization and attribute extraction
- Filtering for streams, generators, and attributes (include/exclude regular expressions)
- Attribute layering and grouping for visualization
- Backlog/history management for efficient memory usage
- Relation tracking between transactions
- Console logging and progress reporting

The SCV Reader is designed for efficient parsing of large text-based transaction logs, supporting attribute filtering, generator/stream filtering, and relation parsing. It uses TimeBase.ps (picoseconds) as the base time unit for all signals.

**Stream and Generator Selection Properties**
- **Include Streams** / **Exclude Streams**: Regular expressions to include or exclude streams during import.
- **Include Generators** / **Exclude Generators**: Regular expressions to include or exclude generators during import.

**Attribute Selection Properties**
- **Include Attributes** / **Exclude Attributes**: Regular expressions to include or exclude attributes during import.
- **Use Text**: Attributes to be treated as text for display.

**Time Range and Transformation Properties**
- **Start**: Start time for importing transactions (picoseconds). Only transactions at or after this time are imported.
- **End**: End time for importing transactions. Only transactions before or at this time are imported.

**Structural Organization Properties**
- **Path Separator**: Character for splitting stream names into hierarchical scopes (default: ".").
- **Backlog**: Number of events to buffer for efficient parsing.
- **History**: Maximum number of events to keep in memory.
- **Max Attributes**: Maximum number of attributes per generator.
- **Layer By Generator**: Optionally group signals by generator.

**Relation and Attribute Handling**
- **Exclude All Relations**: Option to ignore all transaction relations.
- **Exclude All Record Attributes**: Option to ignore all record attributes.

**Logging and Diagnostics Properties**
The parser integrates with impulse's console logging system. Console properties control the level of detail in parsing progress, timing statistics, and error information.

## Known Limitations

- Not all SCV log variants or custom extensions may be fully supported
- Attribute and relation filtering is based on regular expressions and may require tuning for specific logs
- Some malformed or incomplete entries may be skipped

## Sources and Customization

The reader is delivered with full sources. You may modify, fix, and extend it to fit specific workflows, provided all changes comply with your end user license (EULA).

## Implementation Details

The SCV Reader implements a line-based parser that transforms SCV log entries into impulse records and signal writers. It uses efficient buffering, attribute filtering, and event vector management for scalable parsing.

### Parsing Architecture

SCV log files are parsed line-by-line. Key elements include:
- **Stream Definitions**: Parsed and mapped by ID for hierarchical signal creation
- **Generator Definitions**: Parsed and mapped by ID, with begin/end attributes
- **Transaction Events**: `tx_begin` and `tx_end` mark transaction lifecycles, with generator and timestamp association
- **Attributes**: Key-value pairs parsed and attached to transactions, with type and value
- **Relations**: Parsed and attached to transactions as links (e.g., PRED/SUCC, PARENT/CHILD)

### Filtering and Layering

Filtering is applied to streams, generators, and attributes using regular expressions. Layering by generator is supported for visualization grouping. Attribute filtering can be used to focus on relevant fields or reduce memory usage.

### Backlog and History Management

The reader uses a configurable backlog and history size to manage memory usage and event buffering. This allows efficient parsing of large logs without excessive memory consumption.

### Error Handling and Robustness

Malformed or incomplete entries are typically skipped. Attribute types and values are validated against generator and stream definitions where possible. Console logging provides diagnostics and progress reporting.

### Integration Points

The reader integrates with impulse’s:
- Progress reporting and cancellation
- Console logging (configurable verbosity)
- Sample writers for storage and retrieval
- Property model (include/exclude, time range, scope preservation)

---

For more details on the SCV format, see the [SCV Format Specification](scv-format.md).
