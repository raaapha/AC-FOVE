package br.usp.poli.takiyama.common;

import br.usp.poli.takiyama.prv.Binding;
import br.usp.poli.takiyama.prv.Constant;
import br.usp.poli.takiyama.prv.LogicalVariable;
import br.usp.poli.takiyama.prv.Term;

/**
 * This class represents inequality constraints of the form X &ne; Y, where
 * X is a Logical Variable and Y is a Term. The following inequalities are
 * valid: X &ne; Y, X &ne; t. 
 * <br>
 * If the inequality is written in the form t &ne; X, where t is a Constant,
 * we rewrite it as X &ne; t, although there is no direct way of doing that.
 * <br>
 * Inequalities with two constants, like t &ne; q, are invalid. If a method
 * detects a invalid constraint, it returns null.
 *  
 * @author ftakiyama
 *
 */
public class Constraint {
	
	private final LogicalVariable firstTerm;
	private final Term secondTerm;
	
	private Constraint(LogicalVariable firstTerm, Term secondTerm) {
		this.firstTerm = firstTerm;
		this.secondTerm = secondTerm;
	}
	
	/**
	 * Static factory of inequality constraints
	 * @param firstTerm
	 * @param secondTerm
	 * @return
	 */
	public static Constraint getInstance(LogicalVariable firstTerm, Term secondTerm) {
		return new Constraint(firstTerm, secondTerm);
	}
	
	/**
	 * Creates a inequality constraint based on substitution. For instance,
	 * if substitution is X/t, then the corresponding constraint is
	 * X &ne; t.
	 * @param substitution
	 * @return
	 */
	public static Constraint getInequalityConstraintFromBinding(Binding substitution) {
		if (substitution.getSecondTerm() instanceof Constant) { // ugly
			return new Constraint(substitution.getFirstTerm(), (Constant) substitution.getSecondTerm());
		} else if (substitution.getSecondTerm() instanceof LogicalVariable) { // argh
			return new Constraint(substitution.getFirstTerm(), (LogicalVariable) substitution.getSecondTerm());
		} else {
			// TODO: put a log here
			return null; // it should never get here
		}
	}
	
	/**
	 * Apply the substitution in this inequality.
	 * The following rules apply:<br>
	 * <li> X &ne; Y and X/q  returns Y &ne; q
	 * <li> X &ne; Y and Y/q  returns X &ne; q
	 * <li> X &ne; Y and X/W  returns W &ne; Y
	 * <li> X &ne; Y and Y/W  returns X &ne; W
	 * <li> X &ne; t and X/q  returns <b>null</b>
	 * <li> X &ne; t and Y/q  returns X &ne; t
	 * <li> X &ne; t and X/W  returns W &ne; t
	 * <li> X &ne; t and Y/W  returns X &ne; t
	 * <br>
	 * @param substitution The substitution to apply on the constraint
	 * @return The constraint that results from the application of the
	 * specified substitution to this constraint, following the rules 
	 * specified above.
	 */
	public Constraint applySubstitution(Binding substitution) {
		if (firstTerm.equals(substitution.getFirstTerm())) { // looks ugly
			if (secondTerm instanceof LogicalVariable && substitution.getSecondTerm() instanceof Constant) { // X!=Y && X/q 
				return new Constraint((LogicalVariable) secondTerm, substitution.getSecondTerm());
			} else if (secondTerm instanceof Constant && substitution.getSecondTerm() instanceof Constant) { // X!=t && X/q
				return null;
			} else {
				return new Constraint((LogicalVariable) substitution.getSecondTerm(), secondTerm);	
			}
		} else if (secondTerm.equals(substitution.getFirstTerm())) {
			return new Constraint(firstTerm, substitution.getSecondTerm());
		} else {
			return this;
		}
	}
	
	/**
	 * Check if the term is in the constraint
	 * @param term The term to check
	 * @return True if the term specified equals one of the terms of the 
	 * constraint, false otherwise.
	 */
	public boolean contains(Term term) {
		return (firstTerm.equals(term) || secondTerm.equals(term));
	}
	
	@Override
	public String toString() {
		return firstTerm.toString() + "!=" + secondTerm.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		// Tests if both refer to the same object
		if (this == other)
	    	return true;
		// Tests if the Object is an instance of this class
	    if (!(other instanceof Constraint))
	    	return false;
	    // Tests if both have the same attributes
	    Constraint targetObject = (Constraint) other;
	    return ((this.firstTerm == null) ? 
	    		 targetObject.firstTerm == null : 
		    		 this.firstTerm.equals(targetObject.firstTerm)) &&
    		   ((this.secondTerm == null) ? 
    		     targetObject.secondTerm == null : 
    		     this.secondTerm.equals(targetObject.secondTerm));	    		
	}
	
	@Override
	public int hashCode() { // Algorithm extracted from Bloch,J. Effective Java
		int result = 17;
		result = 31 + result + firstTerm.hashCode();
		result = 31 + result + secondTerm.hashCode();
		return result;
	}
}
