/*******************************************************************************
 * Copyright 2014 Felipe Takiyama
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package br.usp.poli.takiyama.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import br.usp.poli.takiyama.prv.Substitution;
import br.usp.poli.takiyama.utils.Sets;

/**
 * Standard implementation of {@link Distribution}. This implementation is a
 * immutable set of {@link Parfactor}s.
 * 
 * @author Felipe Takiyama
 *
 */
public final class StdDistribution implements Distribution {

	private Set<Parfactor> pSet;
	
	/* ************************************************************************
	 *    Constructors
	 * ************************************************************************/
	
	/**
	 * Creates an empty distribution with the specified initial capacity.
	 * @param capacity The initial capacity
	 */
	private StdDistribution(int capacity) {
		this.pSet = new HashSet<Parfactor>(capacity);
	}
	
	/**
	 * Creates a distribution with the elements of the specified set.
	 * @param p A set of parfactors whose elements will compose this 
	 * distribution
	 */
	private StdDistribution(Set<Parfactor> p) {
		pSet = new HashSet<Parfactor>(Math.max((int) (p.size()/.75f) + 1, 16));
		pSet.addAll(p);
	}
	
	/* ************************************************************************
	 *    Static factories
	 * ************************************************************************/
	
	/**
	 * Creates an empty distribution
	 */
	public static Distribution of() {
		return new StdDistribution(0);
	}
	
	/**
	 * Creates a distribution with one parfactor
	 * @param p1 A parfactor
	 * @throws NullPointerException If the specified parfators is <code>null</code>.
	 */
	public static Distribution of(Parfactor p1) throws NullPointerException {
		if (p1 == null) {
			throw new NullPointerException();
		}
		Set<Parfactor> set = new HashSet<Parfactor>(2);
		set.add(p1);
		return new StdDistribution(set);
	}
	
	/**
	 * Creates a distribution with two parfactors
	 * @param p1 The first parfactor to put in the distribution
	 * @param p2 The second parfactor to put in the distribution
	 * @throws NullPointerException If any of the specified parfators is 
	 * <code>null</code>.
	 */
	public static Distribution of(Parfactor p1, Parfactor p2) throws NullPointerException {
		if (p1 == null || p2 == null) {
			throw new NullPointerException();
		}
		Set<Parfactor> set = new HashSet<Parfactor>(2);
		set.add(p1);
		set.add(p2);
		return new StdDistribution(set);
	}
	
	/**
	 * Returns a distribution with the specified parfactors. 
	 * 
	 * @param parfactors An array of parfactors.
	 * @throws NullPointerException If any of the specified parfators is 
	 * <code>null</code>.
	 */
	public static Distribution of(Parfactor ... parfactors) throws NullPointerException {
		Set<Parfactor> set = new HashSet<Parfactor>((int) (parfactors.length / 0.75));
		for (Parfactor p : parfactors) {
			if (p == null) {
				throw new NullPointerException();
			} else {
				set.add(p);
			}
		}
		return new StdDistribution(set);
	}
	
	/**
	 * Creates a distribution with the specified collection of parfactors
	 * @param c A collection of parfactors
	 * @throws NullPointerException If the specified Collection contains a 
	 * <code>null</code> element.
	 */
	public static Distribution of(Collection<? extends Parfactor> c) throws NullPointerException {
		if (c.contains(null)) {
			throw new NullPointerException();
		}
		StdDistribution dist = new StdDistribution(Math.max((int) (c.size()/.75f) + 1, 16));
		dist.addAll(c);
		return dist;
	}
	
	/**
	 * Creates a distribution that has the same elements as the specified
	 * distribution. The order in which the elements are put in the new 
	 * distribution is not necessarily the same as the specified distribution.
	 * 
	 * @param d The distribution to "copy"
	 * @return A distribution with the same elements as the specified 
	 * distribution.
	 * @throws NullPointerException If the specified Distribution contains a 
	 * <code>null</code> element.
	 */
	public static Distribution of(Distribution d) throws NullPointerException {
		if (d.contains(null)) {
			throw new NullPointerException();
		}
		StdDistribution dist = new StdDistribution(Math.max((int) (d.size()/.75f) + 1, 16));
		dist.addAll(d.toSet());
		return dist;
	}
	
	/* ************************************************************************
	 *    Auxiliary methods
	 * ************************************************************************/
	
	private void addAll(Collection<? extends Parfactor> c) {
		pSet.addAll(c);
	}
	
	/* ************************************************************************
	 *    Interface methods
	 * ************************************************************************/
	
	@Override
	public Distribution add(Parfactor p) {
		Set<Parfactor> newSet = new HashSet<Parfactor>(pSet);
		newSet.add(p);
		return new StdDistribution(newSet);
	}
	
	@Override
	public Distribution addAll(Distribution d) {
		return new StdDistribution(Sets.union(d.toSet(), pSet));
	}
	
	@Override
	public boolean contains(Object o) throws NullPointerException {
		return pSet.contains(o);
	}

	@Override
	public boolean containsAll(Distribution d) {
		return pSet.containsAll(d.toSet());
	}

	@Override
	public boolean isEmpty() {
		return pSet.isEmpty();
	}

	@Override
	public Iterator<Parfactor> iterator() {
		return pSet.iterator();
	}

	@Override
	public int size() {
		return pSet.size();
	}
	
	@Override
	public Set<Parfactor> toSet() {
		return new HashSet<Parfactor>(pSet);
	}
	
	@Override
	public Distribution apply(Substitution s) {
		return new StdDistribution(Sets.apply(s, pSet));
	}

	/* ************************************************************************
	 *    hashCode, equals and toStrings
	 * ************************************************************************/
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pSet == null) ? 0 : pSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof StdDistribution)) {
			return false;
		}
		StdDistribution other = (StdDistribution) obj;
		if (pSet == null) {
			if (other.pSet != null) {
				return false;
			}
		} else if (!pSet.equals(other.pSet)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return pSet.toString();
	}
}
