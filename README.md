<!---
title: "EDA Transaction Extension"
author: "Thomas Haber"
keywords: [EDA, impulse, transaction, pin-level, SystemC, TLM, AXI, OCP, protocol, log analysis, visualization, extension, signal processing]
description: "Provides an extension for the impulse framework to import, analyze, and visualize transaction-level and pin-level data from EDA simulations, supporting protocols such as SystemC TLM, AXI, OCP, and custom buses."
category: "impulse-extension"
tags:
  - extension
  - EDA
  - impulse
  - transaction
  - pin-level
  - protocol
  - analysis
  - visualization
docID: -1
--->

# EDA Transaction Extension

This extension for the [impulse](https://www.toem.io/impulse) framework enables import, analysis, and visualization of both transaction-level and pin-level data from Electronic Design Automation (EDA) simulations. It supports engineers and verification specialists working with SystemC TLM, protocol-level, and pin-level (e.g., AXI, OCP) flows.

## About impulse

impulse is a modular, open-source framework for handling signal and measurement data from diverse sources. It provides infrastructure for reading, writing, analyzing, and visualizing signals, supporting both simple and highly structured data. Its extensible architecture allows developers to add support for new data formats by implementing custom readers and writers.


## About EDA

**Electronic Design Automation (EDA)** refers to software tools for designing, simulating, and verifying electronic systems such as integrated circuits, FPGAs, and printed circuit boards. Modern EDA tools generate not only waveform data but also transaction-level and pin-level logs and traces. Transaction-level data captures high-level communication, protocol events, and relationships between operations; pin-level data records signal activity and protocol handshakes at the interface level. This extension enables importing, analyzing, and visualizing both types of simulation data.

## Purpose of this Extension

The EDA Transaction Extension integrates support for both transaction-level and pin-level data into impulse, enabling users to analyze simulation and measurement results from a wide range of EDA tools and flows. Supported protocols and modeling styles include:

- **SystemC TLM** (Transaction Level Modeling)
- **AXI**, **OCP**, and other standard or custom pin-level protocols

This extension is useful for anyone working with digital, mixed-signal, or protocol-driven simulation data, bridging the gap between EDA tool outputs and the advanced analysis capabilities of impulse.

## Provided Functionality

Features include:

- **SCV Reader**: Parses SystemC Verification (SCV) transaction logs (e.g., .scv, .txlog) and imports transaction streams, generators, attributes, and relations.
- **FTR Reader**: Parses Fast Transaction Recording (FTR) binary files, supporting efficient, compressed transaction data import and random access.
- **Transaction Analyzer**: Tools for analyzing transaction streams, extracting protocol events, and correlating transactions across streams.
- **Transaction Metrics**: Computes and visualizes metrics such as transaction counts, latencies, throughput, and protocol-specific statistics.

## Getting Started

To use this extension, install it into your impulse-enabled environment. Once installed, you can import supported EDA transaction or pin-level log files directly into impulse for analysis and visualization. For details on implementing or extending readers and writers, refer to the [impulse documentation](https://toem.io/category/resources/).

## Status

- **SCV Reader:** Beta
- **FTR Reader:** Beta
- **TLM Phase Analyzer:** Experimental
- **Transaction Metrics:** Experimental
- **Pin-level Analyzer:** In preparation

Contributions and feedback are welcome as this extension continues to evolve.

