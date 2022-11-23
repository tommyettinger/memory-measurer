ObjectExplorer, by Dimitris Andreou (jim.andreou@gmail.com)

# Introduction

## [Javadocs](http://tommyettinger.github.io/memory-measurer/apidocs/index.html)

A small tool that is very handy when e.g. you design data structures and want to see how much memory each one uses.
To do this, it uses a simple reflection-based object-traversing framework
([ObjectExplorer](https://github.com/tommyettinger/memory-measurer/blob/main/src/main/java/objectexplorer/ObjectExplorer.java)).
On it, it builds two facilities:

  * [MemoryMeasurer](https://github.com/tommyettinger/memory-measurer/blob/main/src/main/java/objectexplorer/MemoryMeasurer.java),
    which can estimate the memory footprint of an object graph _in bytes_. This requires installing a javaagent when
    running the JVM, e.g. by passing `-javaagent:path/to/object-explorer.jar`. 

  * [ObjectGraphMeasurer](https://github.com/tommyettinger/memory-measurer/blob/main/src/main/java/objectexplorer/ObjectGraphMeasurer.java)
    does not need a javaagent, and can also give a much more qualitative measurement than !MemoryMeasurer - it counts
    the number of objects, references, and primitives (of each kind) that an object graph entails.

Another item of interest is the synergy with this project (Dimitris Andreou) : [JBenchy](http://code.google.com/p/jbenchy/)
(although the link is probably not great...) 

Put together, they allow you to easily and systematically run and analyze benchmarks regarding data structures.

## How to use

An extremely simple example:

```java
long memory = MemoryMeasurer.measureBytes(new HashMap());
```

or

```java
Footprint footprint = ObjectGraphMeasurer.measure(new HashMap());
```

Quick tip: To use the MemoryMeasurer (to measure the footprint of an object
graph in bytes), this parameter needs to be passed to the VM:

`-javaagent:path/to/object-explorer.jar`
