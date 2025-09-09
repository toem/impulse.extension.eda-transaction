
# FTR (Fast Transaction Recording) Reader

> Status: Beta

The FTR Reader imports and analyzes transaction-based simulation data stored in the FTR (Fast Transaction Recording) binary format for the impulse framework. It parses CBOR-encoded simulation outputs and provides access to transaction streams for visualization and analysis.

Features:
- Supports large, compressed or uncompressed FTR files with random access to transaction streams
- Stream selection using include/exclude regular expressions
- Hierarchical browsing and visualization of transaction data
- Time-windowed imports (start/end) for focused analysis
- Lazy mode for deferred loading of transaction data
- Progress reporting, error diagnostics, and property integration

The FTR Reader is optimized for efficient, low-memory parsing of large datasets, with LZ4 compression and per-transaction attribute decoding.


**Stream Selection Properties**
- **Include**: Regular expression to include streams during import. Only streams matching this pattern are imported.
- **Exclude**: Regular expression to exclude streams during import. Streams matching this pattern are not imported.

**Time Range and Transformation Properties**
- **Start**: Start time for importing transactions (domain units: ns, us, ms). Only transactions at or after this time are imported.
- **End**: End time for importing transactions. Only transactions before or at this time are imported.
- **Delay**: Time offset applied to all timestamps during import (domain units). Positive values delay, negative values advance. Applied before scaling.
- **Dilate**: Time scaling factor for timestamps. Values > 1.0 slow down time, values < 1.0 speed up time. Applied after delay: (time + delay) * dilate.

**Structural Organization Properties**
- **Path Separator**: Character for splitting stream names into hierarchical scopes (default: ".").
- **Keep empty scopes**: Preserve empty hierarchical scopes in the stream tree even if they contain no streams or generators.

**Logging and Diagnostics Properties**
The parser integrates with impulse's console logging system. Console properties control the level of detail in parsing progress, timing statistics, and error information.


## Known Limitations

- Not all FTR features or attribute types may be fully supported
- Advanced or writer-specific metadata blocks may be recognized but not fully interpreted by the reader; such content is safely ignored
- Entire-file compression wrappers (rare) may require pre-decompression outside the reader

## Sources and Customization

The reader is delivered with full sources. You may modify, fix, and extend it to fit specific workflows, provided all changes comply with your end user license (EULA).

## Implementation Details

The FTR Reader implements a CBOR-based parser that transforms FTR’s binary structure into impulse records and stream writers. It uses efficient dictionary handling, incremental decompression, and per-transaction attribute decoding.

### CBOR-Based Parsing Architecture

FTR files are composed of tagged CBOR sections, each starting with a tag value. Core tags include:
- **Header (Tag 6)**: File-level metadata (time scale, epoch)
- **String Dictionary (Tags 8, 9)**: Mapping of integer IDs to string values (uncompressed or LZ4-compressed)
- **Directory (Tags 10, 11)**: Stream and generator definitions (uncompressed or LZ4-compressed)
- **Transaction Chunks (Tags 12, 13)**: Transaction data, organized by stream (uncompressed or LZ4-compressed)
- **Relations (Tags 14, 15)**: Relationships between transactions (optional, uncompressed or LZ4-compressed)

The reader extracts header fields, then iterates sections to build the dictionary, directory, and decode transaction data.

### Incremental Section and Buffer Processing

Parsing proceeds section-by-section. Compressed data is decompressed incrementally:
- LZ4 compression is supported for dictionary, directory, transaction, and relation sections
- Decompression is applied only to the needed section(s)
- Directory and dictionary sections are used to reconstruct stream/generator names and structure

### Stream and Generator Management

FTR uses integer IDs to reference streams and generators. The reader:
- Builds maps indexed by stream/generator ID for fast access
- Uses the dictionary to resolve names and types
- Creates impulse writers and attaches metadata (name/type) from the directory

### Transaction and Attribute Processing

Transactions are grouped into time-bounded chunks and use compact CBOR encodings:
- Each transaction includes core metadata (ID, generator, start/end time) and a list of attributes
- Attributes are typed (boolean, integer, float, string, enum, bit/logic vector, pointer, time, etc.) and may be tagged as BEGIN, RECORD, or END
- The reader decodes attributes and attaches them to the corresponding transaction in the impulse record

### Time Management

The header provides the global timescale and optional epoch offset. All transaction times are scaled accordingly. The Start/End properties allow filtering by time range, skipping out-of-window transactions.

### Error Handling and Robustness

The parser includes error contexts (section kind/offset, entry boundaries) to help diagnose malformed files. It validates consistency across header/dictionary/directory. Resources are cleaned up on cancellation or failures.

### Integration Points

The reader integrates with impulse’s:
- Progress reporting and cancellation
- Console logging (configurable verbosity)
- Sample writers for storage and retrieval
- Property model (include/exclude, time range, scope preservation)

---

For more details on the FTR format, see the [FTR Format Specification](../ftr-format.md).
