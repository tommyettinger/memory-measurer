package memorymeasurer;

import com.github.tommyettinger.digital.Base;
import com.github.tommyettinger.ds.*;
import com.google.common.base.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.*;
import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import objectexplorer.MemoryMeasurer;
import objectexplorer.ObjectGraphMeasurer;
import objectexplorer.ObjectGraphMeasurer.Footprint;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ElementCostOfDataStructures {
  public static void main(String[] args) throws Exception {
    Utilities.setDefaultLoadFactor(0.75f);

    caption(String.format("    %2s-bit architecture   ", System.getProperty("sun.arch.data.model")));
    caption("  Basic Lists, Sets, Maps ");

    analyze(new CollectionPopulator(defaultSupplierFor(ArrayList.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(ObjectList.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectArrayList.class)));
    analyze(new ImmutableListPopulator());

    analyze(new CollectionPopulator(defaultSupplierFor(HashSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(LinkedHashSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(ObjectSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(ObjectOrderedSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectOpenHashSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectArraySet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(NumberedSet.class)));
    analyze(new ImmutableSetPopulator());

      analyze(new CollectionPopulator(defaultSupplierFor(CaseInsensitiveSet.class), new EntryFactory() {
        final int[] index = {0};
        public Class<?> getEntryType() {
          return String.class;
        }

        public String get() {
          return Base.BASE16.unsigned(index[0]++);
        }
      }));
      analyze(new CollectionPopulator(defaultSupplierFor(CaseInsensitiveOrderedSet.class), EntryFactories.STRING));
    analyze(new CollectionPopulator(defaultSupplierFor(TreeSet.class), EntryFactories.COMPARABLE));
    analyze(new ImmutableSortedSetPopulator());

    analyze(new MapPopulator(defaultSupplierFor(HashMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(LinkedHashMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(ObjectObjectMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(ObjectObjectOrderedMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap.class)));
    analyze(new MapPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap.class)));
    analyze(new ImmutableMapPopulator());

    analyze(new MapPopulator(defaultSupplierFor(TreeMap.class), EntryFactories.COMPARABLE));
    analyze(new MapPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap.class), EntryFactories.COMPARABLE));
    analyze(new MapPopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap.class), EntryFactories.COMPARABLE));
    analyze(new ImmutableSortedMapPopulator());

    caption("ConcurrentHashMap/MapMaker");

    analyze(new MapPopulator(defaultSupplierFor(ConcurrentHashMap.class)));
    analyze("MapMaker", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().build().asMap(); } }));
    analyze("MapMaker_Expires", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.DAYS).build().asMap(); } }));
    analyze("MapMaker_Evicts", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).build().asMap(); } }));
    analyze("MapMaker_Expires_Evicts", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(3, TimeUnit.DAYS).build().asMap(); } }));
    analyze("MapMaker_WeakKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().weakKeys().build().asMap(); } }));
    analyze("MapMaker_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().softValues().build().asMap(); } }));
    analyze("MapMaker_WeakKeysSoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().weakKeys().softValues().build().asMap(); } }));
    analyze("MapMaker_Evicts_WeakKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).weakKeys().build().asMap(); } }));
    analyze("MapMaker_Evicts_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).softValues().build().asMap(); } }));
    analyze("MapMaker_Evicts_WeakKeysSoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).weakKeys().softValues().build().asMap(); } }));
    analyze("MapMaker_Expires_WeakKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).weakKeys().build().asMap(); } }));
    analyze("MapMaker_Expires_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).softValues().build().asMap(); } }));
    analyze("MapMaker_Expires_WeakKeysSoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.DAYS).weakKeys().softValues().build().asMap(); } }));
    analyze("MapMaker_Expires_Evicts_WeakKeys", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(3, TimeUnit.DAYS).weakKeys().build().asMap(); } }));
    analyze("MapMaker_Expires_Evicts_SoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(3, TimeUnit.DAYS).softValues().build().asMap(); } }));
    analyze("MapMaker_Expires_Evicts_WeakKeysSoftValues", new MapPopulator(new Supplier<Map>() { public Map get() { return
      CacheBuilder.newBuilder().maximumSize(1000000).expireAfterWrite(3, TimeUnit.DAYS).weakKeys().softValues().build().asMap(); } }));

    caption("        Multisets         ");

    analyze("HashMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
      HashMultiset.create(); } }));
    analyze("LinkedHashMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
      LinkedHashMultiset.create(); } }));
    analyze("TreeMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
      TreeMultiset.create(); } }, EntryFactories.COMPARABLE));
    analyze("ConcurrentHashMultiset_Worst", new MultisetPopulator_Worst(new Supplier<Multiset>() { public Multiset get() { return
      ConcurrentHashMultiset.create(); } }));

    System.out.println();

    analyze("HashMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
      HashMultiset.create(); } }));
    analyze("LinkedHashMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
      LinkedHashMultiset.create(); } }));
    analyze("TreeMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
      TreeMultiset.create(); } }, EntryFactories.COMPARABLE));
    analyze("ConcurrentHashMultiset_Best ", new MultisetPopulator_Best(new Supplier<Multiset>() { public Multiset get() { return
      ConcurrentHashMultiset.create(); } }));

    caption("        Multimaps         ");

    analyze("HashMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
      HashMultimap.create(); } }));
    analyze("LinkedHashMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
      LinkedHashMultimap.create(); } }));
    analyze("TreeMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
      TreeMultimap.create(); } }, EntryFactories.COMPARABLE));
    analyze("ArrayListMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
      ArrayListMultimap.create(); } }));
    analyze("LinkedListMultimap_Worst", new MultimapPopulator_Worst(new Supplier<Multimap>() { public Multimap get() { return
      LinkedListMultimap.create(); } }));
    analyze(new ImmutableMultimapPopulator_Worst());
    analyze(new ImmutableListMultimapPopulator_Worst());
    analyze(new ImmutableSetMultimapPopulator_Worst());

    System.out.println();

    analyze("HashMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
      HashMultimap.create(); } }));
    analyze("LinkedHashMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
      LinkedHashMultimap.create(); } }));
    analyze("TreeMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
      TreeMultimap.create(); } }, EntryFactories.COMPARABLE));
    analyze("ArrayListMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
      ArrayListMultimap.create(); } }));
    analyze("LinkedListMultimap_Best ", new MultimapPopulator_Best(new Supplier<Multimap>() { public Multimap get() { return
      LinkedListMultimap.create(); } }));
    analyze(new ImmutableMultimapPopulator_Best());
    analyze(new ImmutableListMultimapPopulator_Best());
    analyze(new ImmutableSetMultimapPopulator_Best());

    caption("          Tables          ");

    analyze("HashBasedTable", new TablePopulator_Worst(new Supplier<Table>() { public Table get() { return
      HashBasedTable.create(); } } ));
    analyze("TreeBasedTable", new TablePopulator_Worst(new Supplier<Table>() { public Table get() { return
      TreeBasedTable.create(); } }, EntryFactories.COMPARABLE));

    caption("          BiMaps          ");

    analyze("HashBiMap", new MapPopulator(new Supplier<Map>() { public Map get() { return
      HashBiMap.create(); } }));
    analyze(new ImmutableBiMapPopulator());

    caption("           Misc           ");

    analyze(new MapPopulator(defaultSupplierFor(WeakHashMap.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(LinkedList.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(ArrayDeque.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(ObjectDeque.class)));
    analyze(new FastUtilPriorityQueuePopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue.class)));
    analyze(new FastUtilPriorityQueuePopulator(defaultSupplierFor(it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue.class)));



    analyze(new CollectionPopulator(defaultSupplierFor(PriorityQueue.class), EntryFactories.COMPARABLE));
    analyze(new CollectionPopulator(defaultSupplierFor(BinaryHeap.class), new EntryFactory() {
      @Override
      public Class<?> getEntryType() {
        return NodeItem.class;
      }

      @Override
      public @Nullable NodeItem get() {
        return new NodeItem();
      }
    }));
    analyze(new CollectionPopulator(defaultSupplierFor(PriorityBlockingQueue.class), EntryFactories.COMPARABLE));
    analyze(new CollectionPopulator(defaultSupplierFor(ConcurrentSkipListSet.class), EntryFactories.COMPARABLE));
    analyze(new CollectionPopulator(defaultSupplierFor(CopyOnWriteArrayList.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(CopyOnWriteArraySet.class)));
    analyze(new CollectionPopulator(defaultSupplierFor(DelayQueue.class), EntryFactories.DELAYED));
    analyze(new CollectionPopulator(defaultSupplierFor(LinkedBlockingQueue.class)));

    caption("  Synchronization Structures");

    analyzeOneOff("ReentrantLock", new ReentrantLock(true));
    analyzeOneOff("Semaphore", new Semaphore(1, true));
    analyzeOneOff("ReadWriteLock", new ReentrantReadWriteLock(true));

    caption("  Atomic Primitives");

    analyzeOneOff("AtomicBoolean", new AtomicBoolean(true));
    analyzeOneOff("AtomicInteger", new AtomicInteger(1));
    analyzeOneOff("AtomicDouble", new AtomicDouble(1));
    analyzeOneOff("AtomicLong", new AtomicLong(1));
  }

  private static void caption(String caption) {
    System.out.println();
    System.out.println("========================================== " + caption
        + " ==========================================");
    System.out.println();
  }

  private static void analyzeMapMaker(String caption, Supplier<CacheBuilder> supplier) {
    analyze(caption, new MapPopulator(new MapSupplier(supplier)));
    analyze(caption + "_Computing", new MapPopulator(new ComputingMapSupplier(supplier)));
  }

  private static class MapSupplier implements Supplier<Map> {
    private final Supplier<CacheBuilder> mapMakerSupplier;
    MapSupplier(Supplier<CacheBuilder> mapMakerSupplier) {
      this.mapMakerSupplier = mapMakerSupplier;
    }

    public Map get() {
      return mapMakerSupplier.get().build().asMap();
    }
  }

  private static class ComputingMapSupplier implements Supplier<Map> {
    private final Supplier<CacheBuilder> mapMakerSupplier;
    ComputingMapSupplier(Supplier<CacheBuilder> mapMakerSupplier) {
      this.mapMakerSupplier = mapMakerSupplier;
    }

    public Map get() {
      return mapMakerSupplier.get().build(new CacheLoader() {
        @Override
        public Object load(Object key) throws Exception {
          return key;
        }
      }).asMap();
    }
  }

  static void analyze(Populator<?> populator) {
    analyze(populator.toString(), populator);
  }

  static void analyze(String caption, Populator<?> populator) {
    AvgEntryCost cost = averageEntryCost(populator, 16, 256 * 31);
    System.out.printf("%40s :: Bytes = %6.2f, Objects = %5.2f Refs = %5.2f Primitives = %s%n",
        caption, cost.bytes, cost.objects, cost.refs, cost.primitives);
  }

  static void analyzeOneOff(String caption, Object o) {
    Footprint footprint = ObjectGraphMeasurer.measure(o);
    long bytes = MemoryMeasurer.measureBytes(o);
    System.out.printf("%40s :: Bytes = %6d, Objects = %5d Refs = %5d Primitives = %s%n",
        caption, bytes, footprint.getObjects(), footprint.getReferences(), footprint.getPrimitives());
  }

  static AvgEntryCost averageEntryCost(Populator<?> populator, int initialEntries, int entriesToAdd) {
    Preconditions.checkArgument(initialEntries >= 0, "initialEntries negative");
    Preconditions.checkArgument(entriesToAdd > 0, "entriesToAdd negative or zero");

    Predicate<Object> predicate = Predicates.not(Predicates.instanceOf(
        populator.getEntryType()));

    Object collection1 = populator.construct(initialEntries);
    Footprint footprint1 = ObjectGraphMeasurer.measure(collection1, predicate);
    long bytes1 = MemoryMeasurer.measureBytes(collection1, predicate);

    Object collection2 = populator.construct(initialEntries + entriesToAdd);
    Footprint footprint2 = ObjectGraphMeasurer.measure(collection2, predicate);
    long bytes2 = MemoryMeasurer.measureBytes(collection2, predicate);

    double objects = (footprint2.getObjects() - footprint1.getObjects()) / (double) entriesToAdd;
    double refs = (footprint2.getReferences() - footprint1.getReferences()) / (double) entriesToAdd;
    double bytes = (bytes2 - bytes1) / (double)entriesToAdd;

    Map<Class<?>, Double> primitives = Maps.newHashMap();
    for (Class<?> primitiveType : primitiveTypes) {
      int initial = footprint1.getPrimitives().count(primitiveType);
      int ending = footprint2.getPrimitives().count(primitiveType);
      if (initial != ending) {
        primitives.put(primitiveType, (ending - initial) / (double) entriesToAdd);
      }
    }

    return new AvgEntryCost(objects, refs, primitives, bytes);
  }

  private static final ImmutableSet<Class<?>> primitiveTypes = ImmutableSet.<Class<?>>of(
      boolean.class, byte.class, char.class, short.class,
      int.class, float.class, long.class, double.class);

  private static class AvgEntryCost {
    final double objects;
    final double refs;
    final ImmutableMap<Class<?>, Double> primitives;
    final double bytes;
    AvgEntryCost(double objects, double refs, Map<Class<?>, Double> primitives, double bytes) {
      this.objects = objects;
      this.refs = refs;
      this.primitives = ImmutableMap.copyOf(primitives);
      this.bytes = bytes;
    }
  }

  private static class DefaultConstructorSupplier<C> implements Supplier<C> {
    private final Constructor<C> constructor;
    DefaultConstructorSupplier(Class<C> clazz) throws NoSuchMethodException {
      this.constructor = clazz.getConstructor();
    }
    public C get() {
      try {
        return constructor.newInstance();
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }

    @Override
    public String toString() {
      return constructor.getDeclaringClass().getSimpleName();
    }
  }

  static <C> DefaultConstructorSupplier<C> defaultSupplierFor(Class<C> clazz) throws NoSuchMethodException {
    return new DefaultConstructorSupplier(clazz);
  }
}

interface Populator<C> {
  Class<?> getEntryType();

  C construct(int entries);
}

abstract class AbstractPopulator<C> implements Populator<C> {
  private final EntryFactory entryFactory;
  AbstractPopulator() { this(EntryFactories.REGULAR); }
  AbstractPopulator(EntryFactory entryFactory) {
    this.entryFactory = entryFactory;
  }

  protected Object newEntry() {
    return entryFactory.get();
  }

  public Class<?> getEntryType() {
    return entryFactory.getEntryType();
  }
}

abstract class MutablePopulator<C> extends AbstractPopulator<C> {
  private final Supplier<? extends C> factory;

  MutablePopulator(Supplier<? extends C> factory) {
    this(factory, EntryFactories.REGULAR);
  }

  MutablePopulator(Supplier<? extends C> factory, EntryFactory entryFactory) {
    super(entryFactory);
    this.factory = factory;
  }

  protected abstract void addEntry(C target);

  public C construct(int entries) {
    C collection = factory.get();
    for (int i = 0; i < entries; i++) {
      addEntry(collection);
    }
    return collection;
  }

  @Override
  public String toString() {
    return factory.toString();
  }
}

class MapPopulator extends MutablePopulator<Map> {
  MapPopulator(Supplier<? extends Map> mapFactory) {
    super(mapFactory);
  }

  MapPopulator(Supplier<? extends Map> mapFactory, EntryFactory entryFactory) {
    super(mapFactory, entryFactory);
  }

  public void addEntry(Map map) {
    map.put(newEntry(), newEntry());
  }
}

class CollectionPopulator extends MutablePopulator<Collection> {
  CollectionPopulator(Supplier<? extends Collection> collectionFactory) {
    super(collectionFactory);
  }

  CollectionPopulator(Supplier<? extends Collection> collectionFactory, EntryFactory entryFactory) {
    super(collectionFactory, entryFactory);
  }

  public void addEntry(Collection collection) {
    collection.add(newEntry());
  }
}

class MultimapPopulator_Worst extends MutablePopulator<Multimap> {
  MultimapPopulator_Worst(Supplier<? extends Multimap> multimapFactory) {
    super(multimapFactory);
  }
  MultimapPopulator_Worst(Supplier<? extends Multimap> multimapFactory, EntryFactory entryFactory) {
    super(multimapFactory, entryFactory);
  }

  public void addEntry(Multimap multimap) {
    multimap.put(newEntry(), newEntry());
  }
}

class MultimapPopulator_Best extends MutablePopulator<Multimap> {
  MultimapPopulator_Best(Supplier<? extends Multimap> multimapFactory) {
    super(multimapFactory);
  }
  MultimapPopulator_Best(Supplier<? extends Multimap> multimapFactory, EntryFactory entryFactory) {
    super(multimapFactory, entryFactory);
  }

  private final Object key = newEntry();
  public void addEntry(Multimap multimap) {
    multimap.put(key, newEntry());
  }
}

class MultisetPopulator_Worst extends MutablePopulator<Multiset> {
  MultisetPopulator_Worst(Supplier<? extends Multiset> multisetFactory) {
    super(multisetFactory);
  }
  MultisetPopulator_Worst(Supplier<? extends Multiset> multisetFactory, EntryFactory entryFactory) {
    super(multisetFactory, entryFactory);
  }

  public void addEntry(Multiset multiset) {
    multiset.add(newEntry());
  }
}

class MultisetPopulator_Best extends MutablePopulator<Multiset> {
  MultisetPopulator_Best(Supplier<? extends Multiset> multisetFactory) {
    super(multisetFactory);
  }
  MultisetPopulator_Best(Supplier<? extends Multiset> multisetFactory, EntryFactory entryFactory) {
    super(multisetFactory, entryFactory);
  }

  private final Object key = newEntry();
  public void addEntry(Multiset multiset) {
    multiset.add(key);
  }
}

class TablePopulator_Worst extends MutablePopulator<Table> {
  TablePopulator_Worst(Supplier<? extends Table> tableFactory) {
    super(tableFactory);
  }
  TablePopulator_Worst(Supplier<? extends Table> tableFactory, EntryFactory entryFactory) {
    super(tableFactory, entryFactory);
  }

  public void addEntry(Table table) {
    table.put(newEntry(), newEntry(), newEntry());
  }
}

/** Immutable classes */

class ImmutableListPopulator extends AbstractPopulator<ImmutableList> {
  public ImmutableList construct(int entries) {
    ImmutableList.Builder builder = ImmutableList.builder();
    for (int i = 0; i < entries; i++) {
      builder.add(newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableList";
  }
}

class ImmutableSetPopulator extends AbstractPopulator<ImmutableSet> {
  public ImmutableSet construct(int entries) {
    ImmutableSet.Builder builder = ImmutableSet.builder();
    for (int i = 0; i < entries; i++) {
      builder.add(newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableSet";
  }
}

class ImmutableMapPopulator extends AbstractPopulator<ImmutableMap> {
  public ImmutableMap construct(int entries) {
    ImmutableMap.Builder builder = ImmutableMap.builder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableMap";
  }
}

class ImmutableSortedSetPopulator extends AbstractPopulator<ImmutableSortedSet> {
  ImmutableSortedSetPopulator() {
    super(EntryFactories.COMPARABLE);
  }

  public ImmutableSortedSet construct(int entries) {
    ImmutableSortedSet.Builder builder = ImmutableSortedSet.<Comparable>naturalOrder();
    for (int i = 0; i < entries; i++) {
      builder.add(newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableSortedSet";
  }
}

class ImmutableSortedMapPopulator extends AbstractPopulator<ImmutableSortedMap> {
  ImmutableSortedMapPopulator() {
    super(EntryFactories.COMPARABLE);
  }

  public ImmutableSortedMap construct(int entries) {
    ImmutableSortedMap.Builder builder = ImmutableSortedMap.<Comparable, Object>naturalOrder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableSortedMap";
  }
}

class ImmutableBiMapPopulator extends AbstractPopulator<ImmutableBiMap> {
  public ImmutableBiMap construct(int entries) {
    ImmutableBiMap.Builder builder = ImmutableBiMap.builder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableBiMap";
  }
}

class ImmutableMultimapPopulator_Worst extends AbstractPopulator<ImmutableMultimap> {
  public ImmutableMultimap construct(int entries) {
    ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableMultimap_Worst";
  }
}

class ImmutableMultimapPopulator_Best extends AbstractPopulator<ImmutableMultimap> {
  public ImmutableMultimap construct(int entries) {
    ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
    Object key = newEntry();
    for (int i = 0; i < entries; i++) {
      builder.put(key, newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableMultimap_Best ";
  }
}

class ImmutableListMultimapPopulator_Worst extends AbstractPopulator<ImmutableListMultimap> {
  public ImmutableListMultimap construct(int entries) {
    ImmutableListMultimap.Builder builder = ImmutableListMultimap.builder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableListMultimap_Worst";
  }
}

class ImmutableListMultimapPopulator_Best extends AbstractPopulator<ImmutableListMultimap> {
  public ImmutableListMultimap construct(int entries) {
    ImmutableListMultimap.Builder builder = ImmutableListMultimap.builder();
    Object key = newEntry();
    for (int i = 0; i < entries; i++) {
      builder.put(key, newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableListMultimap_Best ";
  }
}

class ImmutableSetMultimapPopulator_Worst extends AbstractPopulator<ImmutableSetMultimap> {
  public ImmutableSetMultimap construct(int entries) {
    ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
    for (int i = 0; i < entries; i++) {
      builder.put(newEntry(), newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableSetMultimap_Worst";
  }
}

class ImmutableSetMultimapPopulator_Best extends AbstractPopulator<ImmutableSetMultimap> {
  public ImmutableSetMultimap construct(int entries) {
    ImmutableSetMultimap.Builder builder = ImmutableSetMultimap.builder();
    Object key = newEntry();
    for (int i = 0; i < entries; i++) {
      builder.put(key, newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableSetMultimap_Best ";
  }
}

class ImmutableMultisetPopulator_Worst extends AbstractPopulator<ImmutableMultiset> {
  public ImmutableMultiset construct(int entries) {
    ImmutableMultiset.Builder builder = ImmutableMultiset.builder();
    for (int i = 0; i < entries; i++) {
      builder.add(newEntry());
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableMultiset_Worst";
  }
}

class ImmutableMultisetPopulator_Best extends AbstractPopulator<ImmutableMultiset> {
  public ImmutableMultiset construct(int entries) {
    ImmutableMultiset.Builder builder = ImmutableMultiset.builder();
    Object key = newEntry();
    for (int i = 0; i < entries; i++) {
      builder.add(key);
    }
    return builder.build();
  }

  @Override
  public String toString() {
    return "ImmutableMultiset_Best ";
  }
}

interface EntryFactory extends Supplier {
  Class<?> getEntryType();
}

enum EntryFactories implements EntryFactory {
  REGULAR {
    public Class<?> getEntryType() { return Element.class; }
    public Element get() { return new Element(); }
  },
  COMPARABLE {
    public Class<?> getEntryType() { return ComparableElement.class; }
    public ComparableElement get() { return new ComparableElement(); }
  },
  DELAYED {
    public Class<?> getEntryType() { return DelayedElement.class; }
    public DelayedElement get() { return new DelayedElement(); }
  },
  STRING {
    final int[] index = {0};
    public Class<?> getEntryType() {
      return String.class;
    }

    public String get() {
      return Base.BASE16.unsigned(index[0]++);
    }
  }
}

class Element { }

class ComparableElement extends Element implements Comparable {
  public int compareTo(Object o) { return Integer.compare(hashCode(), o.hashCode()); }
}

class DelayedElement extends Element implements Delayed {
  public long getDelay(TimeUnit unit) { return 0; }
  public int compareTo(Delayed o) { if (this == o) return 0; else return 1; }
}

class NodeItem extends BinaryHeap.Node {
  public NodeItem() {
    super(0);
    value = Float.intBitsToFloat(System.identityHashCode(this) & 0xFEFFFFFF);
  }
}

class FastUtilPriorityQueuePopulator extends MutablePopulator<it.unimi.dsi.fastutil.PriorityQueue> {
  FastUtilPriorityQueuePopulator(Supplier<? extends it.unimi.dsi.fastutil.PriorityQueue> collectionFactory) {
    super(collectionFactory, EntryFactories.COMPARABLE);
  }

  FastUtilPriorityQueuePopulator(Supplier<? extends it.unimi.dsi.fastutil.PriorityQueue> collectionFactory, EntryFactory entryFactory) {
    super(collectionFactory, EntryFactories.COMPARABLE);
  }

  public void addEntry(it.unimi.dsi.fastutil.PriorityQueue collection) {
    collection.enqueue(newEntry());
  }
}