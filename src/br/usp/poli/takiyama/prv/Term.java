package br.usp.poli.takiyama.prv;

/**
 * A term is either a variable or a constant.
 * For example X, kim, cs422, mome, or Raths can be terms. [Poole, 2010] 
 * @author ftakiyama
 *
 */
public interface Term {
	public String getValue();
}
