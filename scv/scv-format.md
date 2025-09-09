# SCV (SystemC Verification) Transaction Recording Format Specification

## Introduction

The SCV (SystemC Verification) transaction recording format is used to capture, store, and analyze transaction-level activity in SystemC-based simulations. It is commonly used in SystemC TLM (Transaction Level Modeling) environments to record the creation, attributes, and relationships of transactions for later analysis and debug. The format is supported by the SystemC Verification Library (SCV), an Accellera standard, and is widely used in the semiconductor and EDA industry for transaction-level tracing.

## Overview

The SCV transaction log format is a text-based (sometimes binary) format that records streams, generators, transactions, attributes, and relations. Each entry in the log describes a structural or event element of the simulation, such as the definition of a stream, the start of a transaction, or the setting of an attribute. The format is designed for efficient parsing and post-processing, and is often used as an interchange format between simulators and analysis tools.

## File Structure

An SCV transaction log typically consists of the following sections:

- **Stream Definitions**: Declare transaction streams (channels) with unique IDs, names, and kinds.
- **Generator Definitions**: Declare transaction generators (sources) with unique IDs, names, and associated streams. Generators may define begin and end attributes.
- **Transaction Events**: Mark the beginning and end of transactions, and record their timing and generator association.
- **Attributes**: Record key-value pairs associated with transactions, such as command, address, or protocol-specific fields.
- **Relations**: Define relationships between transactions, such as parent/child or predecessor/successor links.

### Example Structure

```
scv_tr_stream (ID 1, name "tb.intor_rec_bl", kind "[TLM][axi][b]")
scv_tr_generator (ID 2, name "read", scv_tr_stream 1,
	begin_attribute (ID 0, name "start_delay", type "UNSIGNED")
	end_attribute (ID 1, name "end_delay", type "UNSIGNED")
)
tx_begin 1 10 64660 ns
a "BEGIN_REQ"
tx_record_attribute 1 "delay" STRING = "0 s"
tx_relation "PRED/SUCC" 2 1
tx_end 1 10 64660 ns
```

## Key Components

### Streams
Streams represent logical transaction channels in the simulation. Each stream has:
- **ID**: Unique integer identifier
- **Name**: Human-readable name (often hierarchical)
- **Kind**: String describing the protocol or type (e.g., "[TLM][axi][b]")

### Generators
Generators are sources of transactions within a stream. Each generator has:
- **ID**: Unique integer identifier
- **Name**: Name of the generator (e.g., "read", "write")
- **Stream ID**: Reference to the parent stream
- **Attributes**: Optionally, lists of begin and end attributes (name, type)

### Transactions
Transactions are the core data elements, representing a communication or operation. Each transaction event includes:
- **Transaction ID**: Unique integer identifier
- **Generator ID**: Reference to the generator that created the transaction
- **Timestamp**: Time of the event (e.g., "64660 ns")
- **Event Type**: `tx_begin` (start), `tx_end` (end), or single-point event

### Attributes
Attributes are key-value pairs associated with transactions. Each attribute includes:
- **Transaction ID**: The transaction to which the attribute belongs
- **Name**: Attribute name (e.g., "address", "cmd")
- **Type**: Data type (e.g., STRING, UNSIGNED, BOOLEAN, POINTER)
- **Value**: The value assigned

### Relations
Relations define links between transactions, such as causality or hierarchy. Each relation includes:
- **Relation Type**: String (e.g., "PRED/SUCC", "PARENT/CHILD")
- **Source Transaction ID**
- **Target Transaction ID**

## Supported Data Types

| Type     | Description                        |
|----------|------------------------------------|
| STRING   | Text string                        |
| UNSIGNED | Unsigned integer                   |
| BOOLEAN  | Boolean value (true/false)         |
| POINTER  | Memory address (hex or integer)    |

## Example: Transaction Sequence

```
scv_tr_stream (ID 9, name "tb.intor_rec_nb", kind "[TLM][axi][nb]")
scv_tr_generator (ID 10, name "fw", scv_tr_stream 9,
	begin_attribute (ID 0, name "tlm_phase", type "STRING")
	end_attribute (ID 1, name "tlm_phase[return_path]", type "STRING")
)
tx_begin 2 24 64660 ns
a "BEGIN_REQ"
tx_record_attribute 2 "delay" STRING = "0 s"
tx_record_attribute 2 "trans.axi4.id" UNSIGNED = 0
tx_record_attribute 2 "trans.cmd" STRING = "READ"
tx_end 2 24 64660 ns
```

## Parsing Algorithm

1. Parse stream and generator definitions.
2. For each transaction event (`tx_begin`, `tx_end`):
	 - Record transaction ID, generator, and timestamp.
	 - Parse associated attributes.
	 - Parse any relations.
3. Continue until end of file.

## SystemC and TLM Integration

The SCV transaction format is tightly integrated with SystemC TLM-1.0/2.0 modeling. It records transaction phases, attributes, and relationships as they occur in simulation, enabling post-simulation analysis, debug, and visualization. The format is extensible and can be adapted to custom protocols and attributes.

## Error Handling

Malformed entries or missing fields are typically skipped by parsers. Attribute types and values should be validated against the generator and stream definitions.

## References

- SystemC Verification Library (SCV) [Accellera](https://www.accellera.org/downloads/standards/systemc)
- IEEE Std. 1666-2023 - SystemC Language Reference Manual
- Example: `axi_pinlevel.txlog`
