# FTR (Fast Transaction Recording) Format Specification

## Introduction

The FTR (Fast Transaction Recording) format was developed by MINRES Technologies GmbH, a leader in virtual prototyping and simulation solutions for electronic systems and System-on-Chip (SoC) design. Founded in 2014 and headquartered in Germany, MINRES specializes in creating high-performance simulation tools and methodologies for hardware/software co-design and verification.

MINRES Technologies is known for its expertise in:
- SystemC and Transaction Level Modeling (TLM) technologies
- Virtual prototyping solutions for embedded systems
- Performance optimization of complex hardware simulations
- Visualization and analysis tools for system-level debug

The FTR format represents one of MINRES' innovations in the field of hardware simulation data management, designed specifically to address the challenges of capturing, storing, and analyzing large volumes of transaction-based data from SystemC TLM simulations. By focusing on efficiency and performance, the format enables more effective debugging and analysis of complex hardware designs.

## Overview
The FTR (Fast Transaction Recording) format is a binary file format designed for efficiently storing transaction-based data, particularly from SystemC TLM (Transaction Level Modeling) simulations. It uses CBOR (Concise Binary Object Representation, RFC 7049) as its underlying encoding mechanism to provide a compact and efficiently parseable format.

## Version Information
The current specification describes FTR format version 1.0. There is no explicit version field in the format; identification is done by examining the CBOR tag 55799 (self-describe CBOR) at the beginning of the file.

## File Structure

The FTR file begins with CBOR tag 55799 (self-describe CBOR) followed by an indefinite-length array containing multiple tagged sections. The sections may appear in any order, but a typical file will have the header information first, followed by string dictionary, directory information, transaction chunks, and relations. Each section must be properly tagged to identify its type.

### Header Information (Tag 6)
- Contains metadata about the recording:
  - Time scale (used to calculate the scaling factor for time values)
  - Epoch information
- The time scale factor is calculated as: `10^(file_time_scale - database_time_scale)`
- Structure: `[time_scale, epoch_tagged_value]`
- The epoch is encoded with CBOR tag 1 (standard date/time) followed by an integer timestamp
- The default database_time_scale is typically -12 (picoseconds)

### String Dictionary (Tags 8 and 9)
- Contains a mapping of integer IDs to string values
- Tag 8: Uncompressed dictionary
- Tag 9: LZ4-compressed dictionary
- Dictionary entries are stored as CBOR map entries with integer keys and string values
- This dictionary is used throughout the file to refer to strings by their IDs, reducing file size

### Directory Information (Tags 10 and 11)
- Contains definitions of transaction streams and generators
- Tag 10: Uncompressed directory
- Tag 11: LZ4-compressed directory
- Directory entries can be:
  - Stream definitions (Tag 16): `[stream_id, name_id, kind_id]`
  - Generator definitions (Tag 17): `[generator_id, name_id, stream_id]`

### Transaction Chunks (Tags 12 and 13)
- Contains the actual transaction data organized by stream
- Tag 12: Uncompressed transaction data as `[stream_id, start_time, end_time, data]`
- Tag 13: LZ4-compressed transaction data as `[stream_id, start_time, end_time, uncompressed_size, data]`
- File offsets to chunks are stored in stream objects for random access

### Transactions Structure
Transaction data within chunks is stored as arrays with:
- Tag 6: Transaction metadata array `[txId, genId, startTime, endTime]`
- Attribute arrays, tagged by type:
  - Tag 7: BEGIN attributes `[name_id, type_id, value]`
  - Tag 8: RECORD attributes `[name_id, type_id, value]`
  - Tag 9: END attributes `[name_id, type_id, value]`

### Relations (Tags 14 and 15)
- Define relationships between transactions
- Tag 14: Uncompressed relations
- Tag 15: LZ4-compressed relations
- Each relation is encoded as `[type_id, from_id, to_id, from_fiber?, to_fiber?]`
  - The fiber fields are optional and only included if the relation includes fiber information


## Key Components

### Streams
Streams represent transaction channels or signal paths in the simulation:
- Unique ID for identification
- Name (referenced from string dictionary)
- Kind/type (referenced from string dictionary)
- List of file offsets pointing to transaction chunks

### Generators
Generators are transaction sources within the simulation:
- Unique ID
- Name (referenced from string dictionary)
- Reference to their parent stream

### Transactions (FtrTx)
The core data elements in an FTR file:
- Unique ID
- References to parent stream and generator
- Start and end times (scaled by the time scale factor)
- Block information (block ID and offset) for efficient random access
- Associated attributes

### Attributes (TxAttribute)
Properties of transactions with:
- Type (BEGIN, RECORD, or END attribute)
  - BEGIN: Tag 7, corresponds to AssociationType value 0
  - RECORD: Tag 8, corresponds to AssociationType value 1
  - END: Tag 9, corresponds to AssociationType value 2
- Name (referenced from string dictionary)
- Data type (one of the supported data types)
- Value (encoded according to the data type)

### Supported Data Types
The following data types are supported, with their numerical encodings used in the `type_id` field:

| Index | Data Type | Description | Encoding in CBOR |
|-------|-----------|-------------|------------------|
| 0 | NONE | Reserved/undefined | N/A |
| 1 | BOOLEAN | Boolean values (true/false) | CBOR boolean value |
| 2 | INTEGER | Signed integer values | CBOR integer |
| 3 | UNSIGNED | Unsigned integer values | CBOR unsigned integer |
| 4 | FLOATING_POINT_NUMBER | IEEE 754 floating-point | CBOR float |
| 5 | FIXED_POINT_INTEGER | Fixed-point decimal values | CBOR float |
| 6 | UNSIGNED_FIXED_POINT_INTEGER | Unsigned fixed-point values | CBOR float |
| 7 | ENUMERATION | Enumerated values | Index to string dictionary (integer) |
| 8 | BIT_VECTOR | Binary bit vectors | Integer representing binary value |
| 9 | LOGIC_VECTOR | Logic vectors (with high-impedance or undefined states) | Integer encoding the vector |
| 10 | STRING | Text strings | Index to string dictionary (integer) |
| 11 | POINTER | Memory addresses | Integer representing address |
| 12 | TIME | Timestamp values | Integer representing time units |

### Relations (FtrRelation)
Define relationships between transactions:
- Relation type (stored in the string dictionary)
- Source transaction ID
- Target transaction ID
- Optional source and target fiber information

## Encoding Details

### CBOR Type Encoding
The FTR format uses CBOR encoding with the following major types:
- 0: Unsigned integers
- 1: Negative integers
- 2: Byte strings
- 3: Text strings
- 4: Arrays
- 5: Maps
- 6: Tagged items
- 7: Simple values and floats

### Transaction Events
Transaction events mark the beginning, end, or occurrence of transactions:
- BEGIN: Marks the start of a transaction
- END: Marks the end of a transaction
- SINGLE: Represents a transaction that occurs at a single point in time

### Compression
The format uses LZ4 block compression to reduce file size:
- Compressed sections include tags 9, 11, 13, and 15
- Each compressed section includes the uncompressed size for memory allocation
- BlockLZ4CompressorInputStream is used for decompression

## SystemC TLM Integration
The format is specifically designed to handle SystemC TLM transactions:
- Support for transaction phases (BEGIN, RECORD, END)
- Attribute types correspond to common TLM transaction properties
- Relations can represent TLM communication pathways
- Time representation is compatible with SystemC simulation time

## Random Access Features
- File offsets to chunks are stored in stream objects
- Each transaction records its block ID and offset within the block
- Chunks can be loaded independently as needed
- On-demand attribute loading for efficient memory usage

## Parser Implementation

### Parsing Algorithm
1. Verify the file starts with CBOR tag 55799 (self-describe CBOR)
2. Read the indefinite-length array
3. Process each tagged section according to its tag value:
   - Tag 6: Process header information
   - Tags 8/9: Process string dictionary (uncompressed/compressed)
   - Tags 10/11: Process directory information (uncompressed/compressed)
   - Tags 12/13: Process transaction chunks (uncompressed/compressed)
   - Tags 14/15: Process relations (uncompressed/compressed)
4. Continue until the break code (0xff) is encountered

### Required vs Optional Sections
- Header (Tag 6): Required
- String Dictionary (Tag 8 or 9): Required
- Directory Information (Tag 10 or 11): Required
- Transaction Chunks (Tags 12 or 13): Optional (file may contain no transactions)
- Relations (Tags 14 or 15): Optional

### Error Handling
- If the initial CBOR tag 55799 is missing, the file is not a valid FTR file
- If a compressed section is encountered (Tags 9, 11, 13, 15), but LZ4 decompression fails, the section should be skipped
- If a required section is missing, the parser should report an error
- For malformed entries within a section, the parser should skip the entry and continue

## CBOR Encoding Details

CBOR (Concise Binary Object Representation, RFC 7049) is a binary data format designed for small message size and implementation simplicity. The FTR format uses CBOR as its underlying encoding mechanism to provide efficient storage and parsing of transaction data.

### CBOR Major Types

CBOR encodes data items in a compact binary format using a typed approach. Each data item begins with a byte containing major type information (in the high-order 3 bits) and additional information (in the low-order 5 bits):

| Major Type | Binary Value | Description |
|------------|--------------|-------------|
| 0 | 000 | Unsigned integer |
| 1 | 001 | Negative integer |
| 2 | 010 | Byte string |
| 3 | 011 | Text string |
| 4 | 100 | Array |
| 5 | 101 | Map |
| 6 | 110 | Tag |
| 7 | 111 | Float/simple value |

### Length Encoding

For most major types, the additional information field determines the length:

| Additional Info | Meaning |
|----------------|---------|
| 0-23 | Direct value |
| 24 (0x18) | 1-byte value follows |
| 25 (0x19) | 2-byte value follows |
| 26 (0x1a) | 4-byte value follows |
| 27 (0x1b) | 8-byte value follows |
| 31 (0x1f) | Indefinite-length item |

### Data Type Representations

The FtrReader implementation decodes CBOR data types as follows:

#### Integers (Signed and Unsigned)
- Unsigned integers (major type 0): Directly decoded from the additional information field or from the following bytes
- Negative integers (major type 1): Decoded as -1 minus the unsigned value
- Example: The byte 0x18 0x64 represents the unsigned integer 100

#### Boolean Values
- Encoded as simple values in major type 7
- True: 0xf5 (major type 7, additional info 21)
- False: 0xf4 (major type 7, additional info 20)

#### Floating Point Numbers
- Major type 7 with appropriate additional information:
  - Half-precision (16-bit): 0xf9 followed by 2 bytes
  - Single-precision (32-bit): 0xfa followed by 4 bytes
  - Double-precision (64-bit): 0xfb followed by 8 bytes
- The FtrReader supports converting half-precision floats to regular float values

#### Text and Byte Strings
- Text strings (major type 3): Length followed by UTF-8 encoded bytes
- Byte strings (major type 2): Length followed by raw bytes
- Example: 0x62 0x68 0x69 represents the text string "hi" (length 2)

#### Arrays and Maps
- Arrays (major type 4): Length followed by sequence of data items
- Maps (major type 5): Length followed by key-value pairs (alternating keys and values)
- Both can be indefinite-length (0x9f for arrays, 0xbf for maps), terminated by the break code 0xff

#### Tags
- Tags (major type 6) are used to provide additional semantic information
- Example: 0xd8 0x1a (tag 26) might indicate a date/time value
- FTR uses specific tags (6, 8-15) to identify section types
- Tag 55799 (0xd9 0xd9 0xf7) is used to identify self-describe CBOR files

### Handling Special Cases

#### Indefinite-Length Items
- Indicated by the additional information value 31 (0x1f)
- Must be closed with a break code (0xff)
- Used in FTR for the root array and transaction chunks

#### Break Code
- Represented as 0xff
- Used to terminate indefinite-length items
- The FtrReader explicitly checks for this code when parsing indefinite-length structures

#### Nested Structures
- CBOR allows for nested arrays, maps, and tags
- In FTR, complex data structures like transaction chunks contain several nested levels of arrays and maps

### Attribute Data Type Encoding

FTR attribute types are encoded in CBOR as follows:

| Attribute Type | CBOR Encoding |
|----------------|---------------|
| BOOLEAN | Boolean value (0xf4 or 0xf5) |
| INTEGER | Integer (major types 0 or 1) |
| UNSIGNED | Unsigned integer (major type 0) |
| FLOATING_POINT_NUMBER | Float (major type 7, additional info 25-27) |
| FIXED_POINT_INTEGER | Float representation |
| UNSIGNED_FIXED_POINT_INTEGER | Float representation |
| ENUMERATION | Integer index to string dictionary |
| BIT_VECTOR | Integer representation |
| LOGIC_VECTOR | Integer representation |
| STRING | Integer index to string dictionary |
| POINTER | Integer address representation |
| TIME | Integer time unit representation |

### Byte-Level Parsing Example

For a simple FTR file beginning:
```
d9d9f7 # Tag 55799 (self-describe CBOR)
9f     # Start indefinite-length array
d8 06  # Tag 6 (Info section)
...
```

The CborDecoder would:
1. Read 0xd9d9f7 as Tag 55799
2. Read 0x9f as an indefinite-length array
3. Read 0xd806 as Tag 6, indicating the start of the info section
4. Continue parsing according to the FTR structure specification


# Prompts 

I want to implement an impulse ftr reader in one java file (FtrReader.impl.java - the attched file to edit - edit ONLY this file).
Create an impulse reader sleleton for an FtrReader.
* Use the impulse manual.
* Read carefully the "Implementing Reader" chapter.
* Study the reader examples.
* No signal creation or writing at this step.
* Dont search the code bases.

--- 

I want to implement an impulse ftr reader in one java file (FtrReader.impl.java - the attched file to edit - edit ONLY this file).
* The skeleton allready exists. (dont touch existing code)
* There is an existing implemetation of the ftr reader in VP/FTR/temp_scviewer/plugins/com.minres.scviewer.database.ftr/src/com/minres/scviewer/database/ftr.
* There is an existing cbor implementation in VP/FTR/temp_scviewer/plugins/com.minres.scviewer.database.ftr/src/jacob.
As a first step implement the required cbor classes, methods and definitions required for the existing ftr implementation. 
* Read the complete implementation of the cbor implemenation.
* Dont search the code bases beside the folders given.

---


I want to implement an impulse ftr reader in one java file (FtrReader.impl.java - the attched file to edit - edit ONLY this file).
* The skeleton and the reuired CBOR decoder allready exists.
* There is an existing implemetation of the ftr reader in VP/FTR/temp_scviewer/plugins/com.minres.scviewer.database.ftr/src/com/minres/scviewer/database/ftr.
* REad the existing code and spec carefully.
* Use the impulse manual "Implementing Reader" for more about using compression in an impulse reader
Implement the decompress infratstructure "BlockLZ4CompressorInputStream" as it is used in the existing reader.
* Dont search the code bases beside the folders given.

---

I want to implement an impulse ftr reader in one java file (FtrReader.impl.java - the attched file to edit - edit ONLY this file).
* The skeleton and the reuired CBOR decoder allready exists.
* There is an existing implemetation of the ftr reader in VP/FTR/temp_scviewer/plugins/com.minres.scviewer.database.ftr/src/com/minres/scviewer/database/ftr.
* There is a derived specification file in VP/FTR/ftr.md
* REad the existing code and spec carefully.
Implemnet the root parsing routine of the ftr file as it is "parseInput" (keep the exact structure) in VP/FTR/temp_scviewer/plugins/com.minres.scviewer.database.ftr/src/com/minres/scviewer/database/ftr/FtrDbLoader.java.
* implement this in the existing parse method.
* Use the existing CBOR implementation. 
* Use the existing BlockLZ4CompressorInputStream
* add stubs for the parseInput called sub decode methods like parseDict,...
* Dont search the code bases beside the folders given.
* Implement everything as it is in parseInput

