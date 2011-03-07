/*
 * Copyright 2011 Benjamin Manes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.concurrentlinkedhashmap.caches;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * A non-thread safe bounded {@link LinkedHashMap}.
 *
 * @author ben.manes@gmail.com (Ben Manes)
 */
final class BoundedLinkedHashMap<K, V> extends LinkedHashMap<K, V> implements ConcurrentMap<K, V> {
  private static final long serialVersionUID = 1L;
  private final int maximumCapacity;

  enum AccessOrder {
    FIFO(false), LRU(true);

    final boolean accessOrder;
    private AccessOrder(boolean accessOrder) {
      this.accessOrder = accessOrder;
    }
    boolean get() {
      return accessOrder;
    }
  }

  public BoundedLinkedHashMap(AccessOrder accessOrder, CacheBuilder builder) {
    super(builder.initialCapacity, 0.75f, accessOrder.get());
    this.maximumCapacity = builder.maximumCapacity;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > maximumCapacity;
  }

  @Override
  public V putIfAbsent(K key, V value) {
    V currentValue = get(key);
    return (currentValue == null)
        ? put(key, value)
        : currentValue;
  }

  @Override
  public boolean remove(Object key, Object value) {
    if (value.equals(get(key))) {
      remove(key);
      return true;
    }
    return false;
  }

  @Override
  public V replace(K key, V value) {
    return containsKey(key)
        ? put(key, value)
        : null;
  }

  @Override
  public boolean replace(K key, V oldValue, V newValue) {
    V currentValue = get(key);
    if (oldValue.equals(currentValue)) {
      put(key, newValue);
      return true;
    }
    return false;
  }
}
