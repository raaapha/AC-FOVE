package br.usp.poli.takiyama.common;

import java.util.HashSet;
import java.util.Set;

import br.usp.poli.takiyama.prv.Constant;
import br.usp.poli.takiyama.prv.OldCountingFormula;
import br.usp.poli.takiyama.prv.StdLogicalVariable;
import br.usp.poli.takiyama.prv.ParameterizedRandomVariable;

/**
 * Represents the set of random variables represented by a parameterized
 * random variable and a set of constraints.
 * <br>
 * For instance, given the parameterized random variable f(A) with 
 * D(A) = {a1, a2, a3} and the set of constraints C = {A &ne; a1}, the
 * random variable set f(A):C equals to {f(a2), f(a3)}.
 * @author ftakiyama
 *
 */
public final class RandomVariableSetOld {
	
	private final ParameterizedRandomVariable prv;
	private final HashSet<Constraint> constraints;
	
	/**
	 * Private constructor. Use the static factory to get instances outside
	 * this class.
	 * <br>
	 * Constraints that do not involve parameters from the specified 
	 * parameterized random variable are not stored in the instance.
	 * <br>
	 * Constraints that involve two logical variables are also discarded.
	 * 
	 * @param prv A parameterizedRandomVariable 
	 * @param constraints A set of constraints. 
	 */
	private RandomVariableSetOld (
			ParameterizedRandomVariable prv, 
			Set<Constraint> constraints) {
		this.prv = prv;
		this.constraints = new HashSet<Constraint>();
		for (Constraint c : constraints) {
			if (c.secondTerm() instanceof Constant && prv.contains(c.firstTerm()))
				this.constraints.add(c);
		}
	}
	
	/**
	 * 
	 * Static factory of RandomVariableSet. Returns an instance of
	 * RandomVariableSet.
	 * <br>
	 * Constraints that do not involve parameters from the specified 
	 * parameterized random variable are not stored in the instance.
	 * <br>
	 * Constraints that involve two logical variables are also discarded.
	 * <br>
	 * <br>
	 * If the parameterized random variable has no parameters, it returns 
	 * a RandomVariableSet with the specified PRV and an empty set of
	 * constraints.
	 * 
	 * @param prv A parameterizedRandomVariable 
	 * @param constraints A set of constraints. 
	 * @return An instance of RandomVariableSet with the specified parameters
	 */
	public static RandomVariableSetOld getInstance (
			ParameterizedRandomVariable prv, 
			Set<Constraint> constraints) {
		
		if (prv.getParameters().size() == 0) {
			return new RandomVariableSetOld(prv, new HashSet<Constraint>());
		}
		
		int maxNumConstraints = 0;
		for (StdLogicalVariable lv : prv.getParameters()) {
			maxNumConstraints = maxNumConstraints + lv.population().size();
		}
		if (maxNumConstraints == constraints.size())
			return getEmptyInstance();
		else
			return new RandomVariableSetOld(prv, constraints);
	}
	
	/**
	 * Static factory of RandomVariableSet. Returns an instance of
	 * RandomVariableSet that is identical to the specified RandomVariableSet.
	 * @param rvs The RandomVariableSet to copy.
	 * @return An instance of RandomVariableSet that is identical to the 
	 * specified RandomVariableSet.
	 */
	public static RandomVariableSetOld getInstance(RandomVariableSetOld rvs) {
		ParameterizedRandomVariable prv = ParameterizedRandomVariable.getInstance(rvs.prv);
		Set<Constraint> constraints = new HashSet<Constraint>(rvs.constraints);
		return new RandomVariableSetOld(prv, constraints);
	}
	
	/**
	 * Builds an empty instance of RandomVariableSet.
	 * <br>
	 * An empty instance contains an empty PRV (no parameters, nameless functor)
	 * and an empty set of constraints.
	 * @return An empty instance of RandomVariableSet.
	 */
	private static RandomVariableSetOld getEmptyInstance() {
		return new RandomVariableSetOld(
				ParameterizedRandomVariable.getEmptyInstance(), 
				new HashSet<Constraint>(0));
	}
	
	/**
	 * Returns the set of constraints in this set.
	 * @return The set of constraints in this set.
	 */
	public Set<Constraint> getConstraints() {
		return new HashSet<Constraint>(this.constraints);
	}
	
	/**
	 * Returns the prv bound to this set.
	 * @return The prv bound to this set.
	 */
	public ParameterizedRandomVariable getPrv() {
		return this.prv;
	}
	
	/**
	 * Returns the complement set of this set.
	 * <br>
	 * <b>Attention!</b> This method is inefficient in that it depends on the
	 * size of the population of each parameter from the PRV.
	 * @return The complement set of this set.
	 */
	public RandomVariableSetOld getComplement() {
		HashSet<Constraint> constraints = new HashSet<Constraint>();
		for (StdLogicalVariable lv : this.prv.getParameters()) {
			for (Constant c : lv.individualsSatisfying(this.constraints)) {
				constraints.add(InequalityConstraint.getInstance(lv, c));
			}
		}
		return new RandomVariableSetOld(this.prv, constraints);
	}
	
	/**
	 * Returns the intersection of this set and the specified RandomVariableSet.
	 * @param rvSet A random variable set
	 * @return The intersection of this set and the specified RandomVariableSet.
	 */
	public RandomVariableSetOld intersect(RandomVariableSetOld rvSet) {
		if (!this.prv.equals(rvSet.prv)) {
			return getEmptyInstance();
		} else {
			HashSet<Constraint> constraints = 
					new HashSet<Constraint>(this.constraints);
			constraints.addAll(rvSet.constraints);
			return new RandomVariableSetOld(this.prv, constraints);
		}
	}
	
	/**
	 * Returns the difference between this set and the specified set.
	 * @param rvSet A random variable set
	 * @return The difference between this set and the specified set.
	 */
	public RandomVariableSetOld minus(RandomVariableSetOld rvSet) {
		if (!this.prv.equals(rvSet.prv)) {
			return this;
		} else {
			if (this.constraints.equals(rvSet.constraints)) {
				return getEmptyInstance();
			} else {
				RandomVariableSetOld r = rvSet.getComplement();
				HashSet<Constraint> constraints = 
						new HashSet<Constraint>(r.constraints);
				constraints.addAll(this.constraints);
				return new RandomVariableSetOld(r.prv, constraints);
			}
		}
	}
	
	/**
	 * Returns the union of this set and the specified set.
	 * @param rvSet A random variable set.
	 * @return The union of this set and the specified set.
	 */
	public Set<RandomVariableSetOld> union(RandomVariableSetOld rvSet) {
		HashSet<RandomVariableSetOld> r = new HashSet<RandomVariableSetOld>(2);
		if (!this.prv.equals(rvSet.prv)) {
			r.add(this);
			r.add(rvSet);
		} else {
			HashSet<Constraint> constraints = new HashSet<Constraint>();
			if (this.constraints.equals(rvSet.constraints)) {
				constraints.addAll(this.constraints);
			} else { 
				constraints.addAll(this.constraints);
				constraints.retainAll(rvSet.constraints);
			}
			r.add(new RandomVariableSetOld(this.prv, constraints));
		}
		return r;
	}
	
	/**
	 * Returns true if this set is empty, false otherwise.
	 * Empty sets are created using the 
	 * {@link RandomVariableSetOld#getEmptyInstance()} method.
	 * <br>
	 * An empty set can also be created by adding all possible constraints
	 * involving all parameters of the associated parameterized random variable.
	 * @return True if this set is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return this.prv.getParameters().isEmpty() && this.constraints.isEmpty();
	}
	
	/**
	 * Returns true if this set contains the specified parameterized random
	 * variable.
	 * @param prv
	 * @return
	 */
//	public boolean contains(ParameterizedRandomVariable prv) {
//		// TODO all unification process, again?
//		Substitution mgu = null;
//		try {
//			mgu = this.prv.getMgu(prv);
//		} catch (IllegalArgumentException e) {
//			// firstVariable and secondVariable represent disjoint sets
//			return false;
//		}
//		return false;
//	}
	
	/**
	 * Returns true if this set represents the same set of random variables
	 * from the specified counting formula.
	 * @param cf The counting formula to compare
	 * @return True if this set represents the same set of random variables
	 * from the specified counting formula.
	 */
	public boolean isEquivalent(OldCountingFormula cf) {
		return (this.prv.equals(cf.getPrv()) 
				&& this.constraints.equals(cf.getConstraints()));
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) 
			return true;
		if (!(other instanceof RandomVariableSetOld))
			return false;
		RandomVariableSetOld o = (RandomVariableSetOld) other;
		return ((this.prv == null) 
						? (o.prv == null) 
						: (this.prv.equals(o.prv)))
				&& ((this.constraints == null) 
						? (o.constraints == null) 
						: (this.constraints.equals(o.constraints)));
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(this.prv.toString());
		result.append(":").append(this.constraints);
		return result.toString();
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = 31 + result + constraints.hashCode();
		result = 31 + result + prv.hashCode();
		return result;
	}
}