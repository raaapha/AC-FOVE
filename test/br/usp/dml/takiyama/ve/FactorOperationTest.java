package br.usp.dml.takiyama.ve;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class FactorOperationTest {
	
	private ArrayList<RandomVariable> randomVariables;
	private Factor factor;
	
	private ArrayList<RandomVariable> bigListRandomVariables;
	private Factor bigFactor;
	
	@Before
	public void initialSetup() {
		randomVariables = new ArrayList<RandomVariable>();
		
		String name = "rv1";
		ArrayList<String> domain = new ArrayList<String>();
		ArrayList<BigDecimal> values = new ArrayList<BigDecimal>();
		domain.add("blue");
		domain.add("green");
		domain.add("red");
		values.add(new BigDecimal(0.2));
		values.add(new BigDecimal(0.11111));
		values.add(new BigDecimal(0.456));
		
		randomVariables.add(RandomVariable.createRandomVariable(name, domain, values));
		
		name = "rv2";
		domain.clear();
		domain.add("true");
		domain.add("false");
		values.clear();
		values.add(new BigDecimal(0.02));
		values.add(new BigDecimal(0.98));
		
		randomVariables.add(RandomVariable.createRandomVariable(name, domain, values));		
	
		ArrayList<BigDecimal> mapping = new ArrayList<BigDecimal>();
		mapping.add(new BigDecimal(0.1));
		mapping.add(new BigDecimal(0.2));
		mapping.add(new BigDecimal(0.3));
		mapping.add(new BigDecimal(0.4));
		mapping.add(new BigDecimal(0.5));
		mapping.add(new BigDecimal(0.6));
		
		// ====================================================================
		// Creating a big factor
		// ====================================================================
		
		/*
		 * This table is valid for the example created here
		 * =============================
		 * numberVariables   factor size
		 * ----------------------------- 
		 * 				 2			   6
		 *				 4			  36
		 *				 6			 216
		 *				 8			1296
		 *  		    10		    7776
		 *              12		   46656
		 *              14        279936
		 *              16       1679616
		 *              18   	10077696
		 *              20	    60466176
		 *              22     362797056
		 * =============================
		 */
		final int numberVariables = 10; // put an even value
		
		ArrayList<String> domainForBigFactor1 = new ArrayList<String>();
		domainForBigFactor1.add("blue");
		domainForBigFactor1.add("green");
		domainForBigFactor1.add("red");
		ArrayList<BigDecimal> valuesForBigFactor1 = new ArrayList<BigDecimal>();
		valuesForBigFactor1.add(new BigDecimal(0.2));
		valuesForBigFactor1.add(new BigDecimal(0.11111));
		valuesForBigFactor1.add(new BigDecimal(0.456));
		
		ArrayList<String> domainForBigFactor2 = new ArrayList<String>();
		domainForBigFactor2.add("true");
		domainForBigFactor2.add("false");
		ArrayList<BigDecimal> valuesForBigFactor2 = new ArrayList<BigDecimal>();
		valuesForBigFactor2.add(new BigDecimal(0.02));
		valuesForBigFactor2.add(new BigDecimal(0.98));
		
		bigListRandomVariables = new ArrayList<RandomVariable>();
		for (int i = 0; i < numberVariables; i++) {
			if (i % 2 == 0) {
				bigListRandomVariables.add(RandomVariable.createRandomVariable("X" + i, domainForBigFactor1, valuesForBigFactor1));
			} else {
				bigListRandomVariables.add(RandomVariable.createRandomVariable("X" + i, domainForBigFactor2, valuesForBigFactor2));
			}
		}
		
		ArrayList<BigDecimal> mappingForBigFactor = new ArrayList<BigDecimal>();
		final int sizeBigMapping = (int) (
				Math.pow(domainForBigFactor1.size(), numberVariables/2) *
				Math.pow(domainForBigFactor2.size(), numberVariables/2)
			);
		for (int i = 0; i < sizeBigMapping; i++) {
			mappingForBigFactor.add(BigDecimal.valueOf(Math.random()));
		}
		
		try {
			factor = new Factor("MyFactor", randomVariables, mapping);
			bigFactor = new Factor("Big Factor", bigListRandomVariables, mappingForBigFactor);
			//System.out.println(factor.toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			System.err.println("Unexpected error when creating the Factor.\n" + e);
			System.exit(-1);
		}
	}
	
	@Test
	public void basicTestSumOut() {
		System.out.println("BEFORE:\n" + factor.toString());
		
		// Sums out rv1
		System.out.println("SUM OUT rv1:\n" + 
				FactorOperation.sumOut(factor, randomVariables.get(0)));
		
		// Sums out rv2
		System.out.println("SUM OUT rv2:\n" + 
				FactorOperation.sumOut(factor, randomVariables.get(1)));
	}
	
	@Test
	public void bigTestSumOut() {
		System.out.println("BEFORE:\n" + bigFactor.toString());
		
		// Sums out rv1
		System.out.println("SUM OUT X0:\n" + 
				FactorOperation.sumOut(bigFactor, bigListRandomVariables.get(0)));		
	}
	
}
