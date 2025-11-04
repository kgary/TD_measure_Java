# Running Instructions:

The `run` command compiles and runs the code:

```
    ant run
```

If you've made changes, you can run the following command to compile the changes:

```
    ant rebuild-run
```
## Data Source Setup

**File Data Source:**

- Place a JSONL `.txt` file in the `/resources` directory
- Update `file.path` in `runtime.properties` to point to your file

**MongoDB Data Source:**

- Must be connected to the ASU VPN via SSL VPN
- Update MongoDB connection details in `runtime.properties`

**Kafka Data Source:**

- Ensure Kafka broker is accessible
- Update Kafka configuration in `runtime.properties`

# Technical Overview

This program can ingest data from three different sources- A text file, MongoDB, and Kafka. To change the data source simple modify the `data.source.type` to either in the *application.properties* file to one of the three accepted types-`file`, `mongo`, or `kafka`.

## Architecture

The system uses a **DAO pattern** with pluggable data sources and a *Strategy pattern* for parsing different data formats:

*File/Kafka sources*: Use `FileFormatParser` for individual JSON objects
*MongoDB source*: Uses `MongoBatchParser` for batched message format

## Data Flow

The processing pipeline follows these stages:

1. **Data Source** → Raw data from file, MongoDB, or Kafka
2. **Parser** → Converts raw data into STTC layered format ([time][layer][state])
3. **Entropy Calculator** → Computes windowed Shannon entropy using aggregation strategies
4. **Output File** → Results exported to CSV format

## Entropy Calculation
To calculate entropy, invoke the `computeWindowedEntropy(String[][][] data, int windowSize, AggregationStrategy strategy)` function. It takes the extracted data, window size, and concatenation strategy. The entropy calculation works by:

1. Taking all states at time instance _t_
2. Concatenating state values according to the strategy
3. Adding to a frequency counter
4. Using the counter to calculate probability and Shannon Entropy

## Aggregation Strategy

We support different concatenation strategies based on the entropy type:

1. **Layer-wise** (Casualty, Visual, Movement, Communication): Concatenates all values within a single layer
2. **Combined layers** (System, Team): Concatenates values across multiple specified layers
3. **Individual Entity** (Trainee T1, T2, T3): Concatenates values for a specific entity across specified layers
