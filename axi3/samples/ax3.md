# AXI3 Protocol Overview

## Introduction
AXI3 (Advanced eXtensible Interface version 3) is a high-performance, synchronous bus protocol developed by ARM as part of the AMBA (Advanced Microcontroller Bus Architecture) specification. It enables efficient communication between master and slave devices in System-on-Chip (SoC) designs, supporting high-frequency operations up to several GHz. AXI3 is backward compatible with AXI4 but lacks some advanced features like QoS signaling.

## Key Features
- **Separate Channels**: Independent read and write data paths for concurrent operations
- **Burst Transactions**: Supports single, incremental, wrapping, and fixed bursts
- **Out-of-Order Completion**: Multiple outstanding transactions with ID-based tracking
- **Flow Control**: Handshake-based valid/ready mechanism prevents data loss
- **Protection and Security**: Built-in support for secure and privileged accesses
- **Low Latency**: Optimized for minimal overhead in high-performance systems

## Transaction Types
- **Write Transactions**: Transfer data from master to slave
- **Read Transactions**: Transfer data from slave to master
- **Burst Lengths**: 1-16 beats per burst (configurable)
- **Data Widths**: 32, 64, 128, 256, 512, or 1024 bits
- **Address Widths**: Up to 64 bits

## Signal Groups

### Global Signals
- **ACLK**: System clock (all transfers are synchronous to this edge)
- **ARESETn**: Active-low reset (asynchronous assertion, synchronous deassertion)

### Write Address Channel (AW)
- **AWID**: Transaction ID (up to 4 bits in AXI3)
- **AWADDR**: Write address (byte address)
- **AWLEN**: Burst length (0-15, number of data transfers minus 1)
- **AWSIZE**: Burst size (1, 2, 4, 8, 16, 32, 64, 128 bytes)
- **AWBURST**: Burst type (FIXED, INCR, WRAP)
- **AWLOCK**: Lock type (normal, exclusive)
- **AWCACHE**: Cache type (device, non-cacheable, etc.)
- **AWPROT**: Protection type (secure/privileged/data/instruction)
- **AWVALID**: Address valid (master asserts when address is valid)
- **AWREADY**: Address ready (slave asserts when ready to accept address)

### Write Data Channel (W)
- **WID**: Transaction ID (matches AWID)
- **WDATA**: Write data
- **WSTRB**: Write strobe (byte enables, 1 bit per byte)
- **WLAST**: Last beat indicator (asserted on final data transfer)
- **WVALID**: Data valid (master asserts when data is valid)
- **WREADY**: Data ready (slave asserts when ready to accept data)

### Write Response Channel (B)
- **BID**: Transaction ID (matches AWID)
- **BRESP**: Response (OKAY, EXOKAY, SLVERR, DECERR)
- **BVALID**: Response valid (slave asserts when response is valid)
- **BREADY**: Response ready (master asserts when ready to accept response)

### Read Address Channel (AR)
- **ARID**: Transaction ID (up to 4 bits in AXI3)
- **ARADDR**: Read address (byte address)
- **ARLEN**: Burst length (0-15)
- **ARSIZE**: Burst size
- **ARBURST**: Burst type
- **ARLOCK**: Lock type
- **ARCACHE**: Cache type
- **ARPROT**: Protection type
- **ARVALID**: Address valid
- **ARREADY**: Address ready

### Read Data Channel (R)
- **RID**: Transaction ID (matches ARID)
- **RDATA**: Read data
- **RRESP**: Response (OKAY, EXOKAY, SLVERR, DECERR)
- **RLAST**: Last beat indicator
- **RVALID**: Data valid (slave asserts when data is valid)
- **RREADY**: Data ready (master asserts when ready to accept data)

## Handshake Protocol
AXI3 uses a two-way handshake for all channels:
1. Master asserts VALID when data/address is ready
2. Slave asserts READY when it can accept the transfer
3. Transfer occurs when both VALID and READY are high
4. Signals can be deasserted after transfer

## Burst Types
- **FIXED**: Same address for all beats (e.g., FIFO access)
- **INCR**: Incrementing addresses (most common)
- **WRAP**: Incrementing with wrap-around at 4KB boundaries

## Response Codes
- **OKAY**: Successful completion
- **EXOKAY**: Exclusive access successful
- **SLVERR**: Slave error
- **DECERR**: Decode error (no slave at address)

## Timing Considerations
- All signals are sampled on the rising edge of ACLK
- Minimum latency: 1 cycle per transfer
- Pipelining allows multiple outstanding transactions
- No maximum latency requirements (depends on system)

## Advantages
- High throughput through pipelining and separate channels
- Scalable for various data widths and frequencies
- Supports complex SoC interconnects
- Industry standard with wide tool support

## Limitations
- More complex than simpler protocols like APB
- Requires careful timing closure at high frequencies
- Higher pin count compared to basic interfaces

This protocol enables efficient, high-speed data transfers in modern embedded systems while maintaining flexibility for various application requirements.
