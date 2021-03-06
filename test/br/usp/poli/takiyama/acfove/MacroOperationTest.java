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
package br.usp.poli.takiyama.acfove;

import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import br.usp.poli.takiyama.acfove.AggParfactor.AggParfactorBuilder;
import br.usp.poli.takiyama.cfove.StdParfactor.StdParfactorBuilder;
import br.usp.poli.takiyama.common.Constraint;
import br.usp.poli.takiyama.common.InequalityConstraint;
import br.usp.poli.takiyama.common.Marginal;
import br.usp.poli.takiyama.common.Parfactor;
import br.usp.poli.takiyama.common.StdMarginal.StdMarginalBuilder;
import br.usp.poli.takiyama.prv.And;
import br.usp.poli.takiyama.prv.Constant;
import br.usp.poli.takiyama.prv.CountingFormula;
import br.usp.poli.takiyama.prv.LogicalVariable;
import br.usp.poli.takiyama.prv.Or;
import br.usp.poli.takiyama.prv.Prv;
import br.usp.poli.takiyama.prv.RandomVariableSet;
import br.usp.poli.takiyama.prv.StdLogicalVariable;
import br.usp.poli.takiyama.prv.StdPrv;
import br.usp.poli.takiyama.utils.Sets;
import br.usp.poli.takiyama.utils.TestUtils;

@RunWith(Enclosed.class)
public class MacroOperationTest {

	public static class UnificationBetweenStandardParfactors {
		
		@Ignore("Recalculate expected result")
		@Test
		public void test() {
			LogicalVariable x = StdLogicalVariable.getInstance("X", "x", 5);
			LogicalVariable y = StdLogicalVariable.getInstance("Y", "y", 6);
			LogicalVariable z = StdLogicalVariable.getInstance("Z", "y", 6);

			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			Constant y1 = Constant.getInstance("y1");
			
			Prv f_x_y = StdPrv.getBooleanInstance("f", x, y);
			Prv f_x_z = StdPrv.getBooleanInstance("f", x, z);
			Prv f_x1_y = StdPrv.getBooleanInstance("f", x1, y);
			Prv f_x1_z = StdPrv.getBooleanInstance("f", x1, z);
			Prv f_x1_y1 = StdPrv.getBooleanInstance("f", x1, y1);
			Prv q_y = StdPrv.getBooleanInstance("q", y);
			Prv q_y1 = StdPrv.getBooleanInstance("q", y1);
			Prv q_z = StdPrv.getBooleanInstance("q", z);
			Prv p_x = StdPrv.getBooleanInstance("p", x);
			
			Constraint x_x1 = InequalityConstraint.getInstance(x, x1);
			Constraint x_x2 = InequalityConstraint.getInstance(x, x2);
			Constraint y_y1 = InequalityConstraint.getInstance(y, y1);
			Constraint z_y1 = InequalityConstraint.getInstance(z, y1);
			
			double [] vals = {0.2, 0.3, 0.5, 0.7};
			
			Parfactor g1 = new StdParfactorBuilder().variables(f_x_y, q_y).constraints(x_x2).values(vals).build();
			Parfactor g2 = new StdParfactorBuilder().variables(p_x, f_x1_z).constraints(x_x1, z_y1).values(vals).build();
			
//			Parfactor g3 = new StdParfactorBuilder().variables(p_x, f_x1_z).constraints(x_x1, z_y1).values(vals).build();
//			Parfactor g4 = new StdParfactorBuilder().variables(f_x_z, q_z).constraints(x_x1, x_x2).values(vals).build();
//			Parfactor g5 = new StdParfactorBuilder().variables(f_x1_y1, q_y1).values(vals).build();
//			Parfactor g6 = new StdParfactorBuilder().variables(f_x1_z, q_z).constraints(z_y1).values(vals).build();
			
			Parfactor g3 = new StdParfactorBuilder().variables(p_x, f_x1_y).constraints(x_x1, y_y1).values(vals).build();
			Parfactor g4 = new StdParfactorBuilder().variables(f_x_y, q_y).constraints(x_x1, x_x2).values(vals).build();
			Parfactor g5 = new StdParfactorBuilder().variables(f_x1_y1, q_y1).values(vals).build();
			Parfactor g6 = new StdParfactorBuilder().variables(f_x1_y, q_y).constraints(y_y1).values(vals).build();
			
			Marginal input = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder(4).parfactors(g3, g4, g5, g6).build();
			
			assertEquals(expected, result);
		}
		
		/**
		 * Creates data structures to test unification between two counting
		 * formulas.
		 * <br>
		 * The input is
		 * <br>
		 * &Phi;<sub>0</sub> = {<br>
		 * < { }, { #<sub>A:{A &ne; x1}</sub>[f(A)], h(A) }, F1 >, [1] <br>
		 * < { }, { #<sub>A:{A &ne; x2}</sub>[f(A)] }, F2 > } [2] <br>
		 * <br>
		 * Where D(A) = {x1,x2,x3}.
		 * The result is
		 * <br>
		 * &Phi;<sub>1</sub> = {<br>
		 * < { }, { f(x0), f(x1) }, F2' >, [3] <br>
		 * < { A &ne; x2 }, { f(x0), f(x2), h(A) }, F1' > [4] <br>
		 * < { }, { f(x0), f(x2), h(x2) }, F1' > } [5] <br>
		 * 
		 */
		@Test
		public void testUnificationWithTwoCountingFormulas() {
			
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 3);
			
			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			Constant x3 = Constant.getInstance("x3");
			
			Constraint a_x2 = InequalityConstraint.getInstance(a, x2);
			Constraint a_x3 = InequalityConstraint.getInstance(a, x3);
			
			Prv f = StdPrv.getBooleanInstance("f", a);
			Prv f_x1 = StdPrv.getBooleanInstance("f", x1);
			Prv f_x2 = StdPrv.getBooleanInstance("f", x2);
			Prv f_x3 = StdPrv.getBooleanInstance("f", x3);
			Prv h = StdPrv.getBooleanInstance("h", a);
			Prv h_x3 = StdPrv.getBooleanInstance("h", x3);
			Prv cf1 = CountingFormula.getInstance(a, f, a_x2);
			Prv cf2 = CountingFormula.getInstance(a, f, a_x3);
			
			double [] f1 = {2, 3, 5, 7, 11, 13};
			double [] f3 = {2, 3, 5, 7, 5, 7, 11, 13};
			
			Parfactor g1 = new StdParfactorBuilder().variables(cf1, h).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(cf2).values(2, 3, 5).build();
			Parfactor g3 = new StdParfactorBuilder().variables(f_x1, f_x2).values(2, 3, 3, 5).build();
			Parfactor g4 = new StdParfactorBuilder().variables(f_x1, f_x3, h).values(f3).constraints(a_x3).build();
			Parfactor g5 = new StdParfactorBuilder().variables(f_x1, f_x3, h_x3).values(f3).build();
			
			Marginal input = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder(3).parfactors(g3, g4, g5).build();
			
			assertEquals(expected, result);
		}
		
		/**
		 * Creates data structures to test unification between two counting
		 * formulas in a bigger population.
		 * <br>
		 * The input is
		 * <br>
		 * &Phi;<sub>0</sub> = {<br>
		 * < { }, { #<sub>A:{A &ne; x1}</sub>[f(A)], h(A) }, F1 >, [1] <br>
		 * < { }, { #<sub>A:{A &ne; x2}</sub>[f(A)] }, F2 > } [2] <br>
		 * <br>
		 * Where D(A) = {x1,x2,x3,x4}.
		 * The result is
		 * <br>
		 * &Phi;<sub>1</sub> = {<br>
		 * < { }, { #<sub>A:{A &ne; x1, A &ne; x2}</sub>[f(A)], f(x1) }, F2' >, [3] <br>
		 * < { A &ne; x2 }, { #<sub>A:{A &ne; x1, A &ne; x2}</sub>[f(A)], f(x2), h(A) }, F1' > [4] <br>
		 * < { }, { #<sub>A:{A &ne; x1, A &ne; x2}</sub>[f(A)], f(x2), h(x2) }, F1' > } [5] <br>
		 */
		@Test
		public void testUnificationWithTwoCountingFormulasAndBiggerPopulation() {
			
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 4);
			
			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			
			Constraint a_x1 = InequalityConstraint.getInstance(a, x1);
			Constraint a_x2 = InequalityConstraint.getInstance(a, x2);
			
			Set<Constraint> a_x1_a_x2 = Sets.setOf(a_x1, a_x2);
			
			Prv f = StdPrv.getBooleanInstance("f", a);
			Prv f_x1 = StdPrv.getBooleanInstance("f", x1);
			Prv f_x2 = StdPrv.getBooleanInstance("f", x2);
			Prv h = StdPrv.getBooleanInstance("h", a);
			Prv h_x2 = StdPrv.getBooleanInstance("h", x2);
			Prv cf1 = CountingFormula.getInstance(a, f, a_x1);
			Prv cf2 = CountingFormula.getInstance(a, f, a_x2);
			Prv cf3 = CountingFormula.getInstance(a, f, a_x1_a_x2);
			
			double [] f1 = {2, 3, 5, 7, 11, 13, 17, 23};
			double [] f2 = {2, 3, 5, 7};
			double [] f3 = {2, 3, 3, 5, 5, 7};
			double [] f4 = {2, 3, 5, 7, 5, 7, 11, 13, 11, 13, 17, 23};
			
			Parfactor g1 = new StdParfactorBuilder().variables(cf1, h).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(cf2).values(f2).build();
			Parfactor g3 = new StdParfactorBuilder().variables(cf3, f_x1).values(f3).build();
			Parfactor g4 = new StdParfactorBuilder().variables(cf3, f_x2, h).values(f4).constraints(a_x2).build();
			Parfactor g5 = new StdParfactorBuilder().variables(cf3, f_x2, h_x2).values(f4).build();
			
			Marginal input = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder(3).parfactors(g3, g4, g5).build();
			
			assertEquals(expected, result);
		}
		
		/**
		 * Creates data structures to test unification between a counting formula
		 * and a standard parameterized random variable.
		 * <br>
		 * The input is
		 * <br>
		 * &Phi;<sub>0</sub> = {<br>
		 * < { }, { #<sub>A:{A &ne; x1}</sub>[f(A)], h(A) }, F1 >, [1] <br>
		 * < { }, { f(A) }, F2 > } [2] <br>
		 * <br>
		 * The result is
		 * <br>
		 * &Phi;<sub>1</sub> = {<br>
		 * < { }, { #<sub>X2:{X2 &ne; x1}</sub>[f(X2)], h(X2) }, F1 >, [3] <br>
		 * < { }, { f(x1) }, F2 >, [4] <br>
		 * < { X2 &ne; x1 }, { f(X2) }, F2 > } [5] <br>
		 * 
		 */
		@Test
		public void testUnificationTestWithCountingFormulaAndPrv() {
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 3);
			
			Constant x1 = Constant.getInstance("x1");
			
			Constraint a_x1 = InequalityConstraint.getInstance(a, x1);
			
			Prv f = StdPrv.getBooleanInstance("f", a);
			Prv f_x1 = StdPrv.getBooleanInstance("f", x1);
			Prv h = StdPrv.getBooleanInstance("h", a);
			Prv cf1 = CountingFormula.getInstance(a, f, a_x1);
			
			double [] f1 = {2, 3, 5, 7, 11, 13};
			double [] f2 = {2, 3};
			
			Parfactor g1 = new StdParfactorBuilder().variables(cf1, h).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(f).values(f2).build();
			Parfactor g3 = new StdParfactorBuilder().variables(cf1, h).values(f1).build();
			Parfactor g4 = new StdParfactorBuilder().variables(f_x1).values(f2).build();
			Parfactor g5 = new StdParfactorBuilder().variables(f).values(f2).constraints(a_x1).build();
			
			Marginal input = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder(3).parfactors(g3, g4, g5).build();
			
			assertEquals(expected, result);			
		}
	}
	
	/**
	 * Tests unification/shattering between aggregation parfactors and
	 * standard parfactors.
	 */
	@RunWith(Parameterized.class)
	public static class UnificationTest {
		
		@Parameters
		public static Collection<Object[]> data() {
			
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 10);
			LogicalVariable b = StdLogicalVariable.getInstance("B", "x", 10);
			LogicalVariable e = StdLogicalVariable.getInstance("E", "x", 10);
			LogicalVariable f = StdLogicalVariable.getInstance("F", "x", 10);
			
			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			Constant x3 = Constant.getInstance("x3");
			Constant x4 = Constant.getInstance("x4");
			
			Prv p_a_b = StdPrv.getBooleanInstance("p", a, b);
			Prv c_b = StdPrv.getBooleanInstance("c", b);
			Prv cPrime = StdPrv.getBooleanInstance("c'", b);
			Prv u = StdPrv.getBooleanInstance("u");
			Prv v_f = StdPrv.getBooleanInstance("v", f);
			Prv w_b = StdPrv.getBooleanInstance("w", b);
			
			Prv p_a_x1 = StdPrv.getBooleanInstance("p", a, x1);
			Prv p_a_x3 = StdPrv.getBooleanInstance("p", a, x3);
			Prv p_a_e = StdPrv.getBooleanInstance("p", a, e);
			Prv p_x1_x2 = StdPrv.getBooleanInstance("p", x1, x2);
			Prv p_x1_x3 = StdPrv.getBooleanInstance("p", x1, x3);
			Prv p_x1_e = StdPrv.getBooleanInstance("p", x1, e);
			Prv p_x1_b = StdPrv.getBooleanInstance("p", x1, b);
			Prv p_x2_x1 = StdPrv.getBooleanInstance("p", x2, x1);
			Prv p_x4_x1 = StdPrv.getBooleanInstance("p", x4, x1);
			Prv p_f_e = StdPrv.getBooleanInstance("p", f, e);
			Prv c_x1 = StdPrv.getBooleanInstance("c", x1);
			Prv c_x3 = StdPrv.getBooleanInstance("c", x3);
			Prv c_e = StdPrv.getBooleanInstance("c", e);
			Prv cPrime_x1 = StdPrv.getBooleanInstance("c'", x1);
			Prv cPrime_x3 = StdPrv.getBooleanInstance("c'", x3);
			Prv cPrime_e = StdPrv.getBooleanInstance("c'", e);
			
			Constraint a_x1 = InequalityConstraint.getInstance(a, x1);
			Constraint a_x2 = InequalityConstraint.getInstance(a, x2);
			Constraint a_x4 = InequalityConstraint.getInstance(a, x4);
			Constraint b_x1 = InequalityConstraint.getInstance(b, x1);
			Constraint b_x2 = InequalityConstraint.getInstance(b, x2);
			Constraint b_x3 = InequalityConstraint.getInstance(b, x3);
			Constraint e_x1 = InequalityConstraint.getInstance(e, x1);
			Constraint e_x2 = InequalityConstraint.getInstance(e, x2);
			Constraint e_x3 = InequalityConstraint.getInstance(e, x3);
			Constraint f_x3 = InequalityConstraint.getInstance(f, x3);
			
			Parfactor g_1_in_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_1_in_2 = new StdParfactorBuilder().variables(p_a_x1).build();
			Parfactor g_1_out_1 = new AggParfactorBuilder(p_a_x1, c_x1, Or.OR).build();
			Parfactor g_1_out_2 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).constraint(b_x1).build();
			Parfactor g_1_out_3 = new StdParfactorBuilder().variables(p_a_x1).build();
			
			Marginal in1 = new StdMarginalBuilder().parfactors(g_1_in_1, g_1_in_2).build();
			Marginal out1 = new StdMarginalBuilder().parfactors(g_1_out_1, g_1_out_2, g_1_out_3).build();
			
			Parfactor g_2_in_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_2_in_2 = new StdParfactorBuilder().variables(p_x1_e).build();
			Parfactor g_2_out_1 = new AggParfactorBuilder(p_a_e, cPrime_e, Or.OR).constraint(a_x1).build();
			Parfactor g_2_out_2 = new StdParfactorBuilder().variables(p_x1_e, cPrime_e, c_e).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build();
			Parfactor g_2_out_3 = new StdParfactorBuilder().variables(p_x1_e).build();
			Parfactor g_2_out_4 = new AggParfactorBuilder(p_a_b, cPrime, Or.OR).constraint(a_x1).build();
			Parfactor g_2_out_5 = new StdParfactorBuilder().variables(p_x1_b, cPrime, c_b).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build();
			Parfactor g_2_out_6 = new StdParfactorBuilder().variables(p_x1_b).build();
			
			Marginal in2 = new StdMarginalBuilder().parfactors(g_2_in_1, g_2_in_2).build();
			Marginal out2_1 = new StdMarginalBuilder().parfactors(g_2_out_1, g_2_out_2, g_2_out_3).build();
			Marginal out2_2 = new StdMarginalBuilder().parfactors(g_2_out_4, g_2_out_5, g_2_out_6).build();
						
			Parfactor g_3_in_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).constraints(b_x2, a_x4).build();
			Parfactor g_3_in_2 = new StdParfactorBuilder().variables(p_x1_e).constraints(e_x3).build();
			
			Marginal in3 = new StdMarginalBuilder().parfactors(g_3_in_1, g_3_in_2).build();
			
			Set<Parfactor> out = new HashSet<Parfactor>();
			
			out.add(new AggParfactorBuilder(p_a_b, cPrime, Or.OR).constraints(b_x2, b_x3, a_x1, a_x4).build());
			out.add(new AggParfactorBuilder(p_a_x3, cPrime_x3, Or.OR).constraints(a_x1, a_x4).build());
			out.add(new StdParfactorBuilder().variables(p_x1_x3, cPrime_x3, c_x3).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build());
			out.add(new StdParfactorBuilder().variables(p_x1_b, cPrime, c_b).constraints(b_x2, b_x3).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build());
			out.add(new StdParfactorBuilder().variables(p_x1_x2).build());
			out.add(new StdParfactorBuilder().variables(p_x1_b).constraints(b_x3, b_x2).build());
			
			Marginal out3_1 = new StdMarginalBuilder().parfactors(out).build();
			
			out.clear();
			out.add(new AggParfactorBuilder(p_a_e, cPrime_e, Or.OR).constraints(e_x2, e_x3, a_x1, a_x4).build());
			out.add(new AggParfactorBuilder(p_a_x3, cPrime_x3, Or.OR).constraints(a_x1, a_x4).build());
			out.add(new StdParfactorBuilder().variables(p_x1_x3, cPrime_x3, c_x3).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build());
			out.add(new StdParfactorBuilder().variables(p_x1_e, cPrime_e, c_e).constraints(e_x2, e_x3).values(new double[] {1, 0, 0, 1, 0, 1, 0, 1}).build());
			out.add(new StdParfactorBuilder().variables(p_x1_x2).build());
			out.add(new StdParfactorBuilder().variables(p_x1_e).constraints(e_x3, e_x2).build());
									
			Marginal out3_2 = new StdMarginalBuilder().parfactors(out).build();
			
			Parfactor g_4_in_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_4_in_2 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).build();
			// expected result is g1,g2 or g3,g4 where:
			// - g.4.out.1 == g.4.out.2 
			// - g.4.out.3 == g.4.out.4
			Parfactor g_4_out_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_4_out_2 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_4_out_3 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).build();
			Parfactor g_4_out_4 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).build();
			
			Marginal in4 = new StdMarginalBuilder().parfactors(g_4_in_1, g_4_in_2).build();
			Marginal out4_1 = new StdMarginalBuilder().parfactors(g_4_out_1, g_4_out_2).build();
			Marginal out4_2 = new StdMarginalBuilder().parfactors(g_4_out_3, g_4_out_4).build();
			
			Parfactor g_5_in_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).context(u).build();
			Parfactor g_5_in_2 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).build();
			// expected result is g1,g2 or g3,g4
			Parfactor g_5_out_1 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).context(u).build();
			Parfactor g_5_out_2 = new AggParfactorBuilder(p_a_b, c_b, Or.OR).build();
			Parfactor g_5_out_3 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).context(u).build();
			Parfactor g_5_out_4 = new AggParfactorBuilder(p_f_e, c_e, Or.OR).build();
			
			Marginal in5 = new StdMarginalBuilder().parfactors(g_5_in_1, g_5_in_2).build();
			Marginal out5_1 = new StdMarginalBuilder().parfactors(g_5_out_1, g_5_out_2).build();
			Marginal out5_2 = new StdMarginalBuilder().parfactors(g_5_out_3, g_5_out_4).build();
			
			Parfactor g_6_in_1 = new AggParfactorBuilder(p_a_x1, c_x1, Or.OR).context(u).build();
			Parfactor g_6_in_2 = new AggParfactorBuilder(p_a_e, c_e, Or.OR).context(v_f).build();
			Parfactor g_6_out_1 = new AggParfactorBuilder(p_a_x1, c_x1, Or.OR).context(u).build();
			Parfactor g_6_out_2 = new AggParfactorBuilder(p_a_x1, c_x1, Or.OR).context(v_f).build();
			Parfactor g_6_out_3 = new AggParfactorBuilder(p_a_e, c_e, Or.OR).context(v_f).constraint(e_x1).build();
			
			Marginal in6 = new StdMarginalBuilder().parfactors(g_6_in_1, g_6_in_2).build();
			Marginal out6 = new StdMarginalBuilder().parfactors(g_6_out_1, g_6_out_2, g_6_out_3).build();
			
			Parfactor g_7_in_1 = new AggParfactorBuilder(p_a_x1, c_x1, Or.OR).context(w_b).constraints(b_x1, a_x2).build();
			Parfactor g_7_in_2 = new AggParfactorBuilder(p_a_e, c_e, Or.OR).context(v_f).constraints(f_x3, a_x4).build();
			Parfactor g_7_out_1 = new AggParfactorBuilder(p_a_x1, cPrime_x1, Or.OR).context(v_f).constraints(f_x3, a_x2, a_x4).build();
			Parfactor g_7_out_2 = new StdParfactorBuilder().variables(p_x2_x1, v_f, cPrime_x1, c_x1).constraints(f_x3).values(new double[] {1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}).build();
			Parfactor g_7_out_3 = new AggParfactorBuilder(p_a_x1, cPrime_x1, Or.OR).context(w_b).constraints(b_x1, a_x2, a_x4).build();
			Parfactor g_7_out_4 = new StdParfactorBuilder().variables(p_x4_x1, w_b, cPrime_x1, c_x1).constraints(b_x1).values(new double[] {1, 0, 0, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}).build();
			Parfactor g_7_out_5 = new AggParfactorBuilder(p_a_e, c_e, Or.OR).context(v_f).constraints(f_x3, a_x4, e_x1).build();
						
			Marginal in7 = new StdMarginalBuilder().parfactors(g_7_in_1, g_7_in_2).build();
			Marginal out7 = new StdMarginalBuilder().parfactors(g_7_out_1, g_7_out_2, g_7_out_3, g_7_out_4, g_7_out_5).build();
			
			return Arrays.asList(new Object[][] {
					{in1, new Object[] {out1}},
					{in2, new Object[] {out2_1, out2_2}},
					{in3, new Object[] {out3_1, out3_2}},
					{in4, new Object[] {out4_1, out4_2}},
					{in5, new Object[] {out5_1, out5_2}},
					{in6, new Object[] {out6}},
					{in7, new Object[] {out7}},
			});
		}
		
		private Marginal input;
		private List<Object> expected;
		
		public UnificationTest(Marginal in, Object ... out) {
			input = in;
			expected = Arrays.asList(out);
		}
				
		@Test
		public void testUnification() {
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			org.junit.Assert.assertThat(result, isIn(expected));
		}
		
	}
	
	/**
	 * Example 2.19 to 2.22 from Kisynki[2010] - unification of standard
	 * parfactors.
	 */
	public static class ShatterTest {
		
		@Test
		public void testShatter() {
			LogicalVariable lot = StdLogicalVariable.getInstance("Lot", "lot", 15);
			Constant lot1 = Constant.getInstance("lot1");
			
			Prv rain = StdPrv.getBooleanInstance("rain");
			Prv sprinkler = StdPrv.getBooleanInstance("sprinkler", lot);
			Prv wet_grass = StdPrv.getBooleanInstance("wet_grass", lot);
			Prv wet_grass_lot1 = StdPrv.getBooleanInstance("wet_grass", lot1);
			Prv sprinkler_lot1 = StdPrv.getBooleanInstance("sprinkler", lot1);
			
			Constraint lot_lot1 = InequalityConstraint.getInstance(lot, lot1);
			
			double [] f1 = {0.8, 0.2};
			double [] f2 = {0.6, 0.4};
			double [] f3 = {1.0, 0.0, 0.2, 0.8, 0.1, 0.9, 0.01, 0.99};
			double [] f4 = {0, 1};
			
			Parfactor g1 = new StdParfactorBuilder().variables(rain).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(sprinkler).values(f2).build();
			Parfactor g3 = new StdParfactorBuilder().variables(rain, sprinkler, wet_grass).values(f3).build();
			Parfactor g4 = new StdParfactorBuilder().variables(wet_grass_lot1).values(f4).build();
			Parfactor g5 = new StdParfactorBuilder().variables(rain, sprinkler_lot1, wet_grass_lot1).values(f3).build();
			Parfactor g6 = new StdParfactorBuilder().variables(rain, sprinkler, wet_grass).constraints(lot_lot1).values(f3).build();
			Parfactor g7 = new StdParfactorBuilder().variables(sprinkler_lot1).values(f2).build();
			Parfactor g8 = new StdParfactorBuilder().variables(sprinkler).constraints(lot_lot1).values(f2).build();
			
			Marginal marginal = new StdMarginalBuilder().parfactors(g1, g2, g3, g4).build();
			
			MacroOperation shatter = new Shatter(marginal);
			
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g1, g4, g5, g6, g7, g8).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testShatterThatResultsInPropositionalization() {
			LogicalVariable lot = StdLogicalVariable.getInstance("Lot", "lot", 3);
			Constant lot1 = Constant.getInstance("lot1");
			Constant lot2 = Constant.getInstance("lot2");
			Constant lot3 = Constant.getInstance("lot3");
			
			Prv rain = StdPrv.getBooleanInstance("rain");
			Prv sprinkler = StdPrv.getBooleanInstance("sprinkler", lot);
			Prv wet_grass_lot1 = StdPrv.getBooleanInstance("wet_grass", lot1);
			Prv wet_grass_lot2 = StdPrv.getBooleanInstance("wet_grass", lot2);
			Prv wet_grass_lot3 = StdPrv.getBooleanInstance("wet_grass", lot3);
			Prv sprinkler_lot1 = StdPrv.getBooleanInstance("sprinkler", lot1);
			Prv sprinkler_lot2 = StdPrv.getBooleanInstance("sprinkler", lot2);
			Prv sprinkler_lot3 = StdPrv.getBooleanInstance("sprinkler", lot3);
			
			double [] f1 = {0.8, 0.2};
			double [] f2 = {0.6, 0.4};
			double [] f3 = {1.0, 0.0, 0.2, 0.8, 0.1, 0.9, 0.01, 0.99};
			double [] f4 = {0.0, 1.0};
			
			Parfactor g1 = new StdParfactorBuilder().variables(rain).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(sprinkler).values(f2).build();
			Parfactor g4 = new StdParfactorBuilder().variables(wet_grass_lot1).values(f4).build();
			
			Parfactor g2_1 = new StdParfactorBuilder().variables(sprinkler_lot1).values(f2).build();
			Parfactor g2_2 = new StdParfactorBuilder().variables(sprinkler_lot2).values(f2).build();
			Parfactor g2_3 = new StdParfactorBuilder().variables(sprinkler_lot3).values(f2).build();
			
			Parfactor g3_1 = new StdParfactorBuilder().variables(rain, sprinkler_lot1, wet_grass_lot1).values(f3).build();
			Parfactor g3_2 = new StdParfactorBuilder().variables(rain, sprinkler_lot2, wet_grass_lot2).values(f3).build();
			Parfactor g3_3 = new StdParfactorBuilder().variables(rain, sprinkler_lot3, wet_grass_lot3).values(f3).build();
			
			Marginal marginal = new StdMarginalBuilder().parfactors(g1, g2, g3_1, g3_2, g3_3, g4).build();
			MacroOperation shatter = new Shatter(marginal);
			
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g1, g4, g2_1, g2_2, g2_3, g3_1, g3_2, g3_3).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testSimpleTrickyShatter() {
			
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 2);
			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			
			Prv f = StdPrv.getBooleanInstance("f", a);
			Prv f_x1 = StdPrv.getBooleanInstance("f", x1);
			Prv f_x2 = StdPrv.getBooleanInstance("f", x2);
			
			double [] val = {0, 1};
			Parfactor g1 = new StdParfactorBuilder().variables(f).values(val).build();
			Parfactor g2 = new StdParfactorBuilder().variables(f_x1).values(val).build();
			Parfactor g3 = new StdParfactorBuilder().variables(f_x2).values(val).build();
			
			Marginal input = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g2, g2, g3).build();
			assertEquals(expected, result);
		}
		
		@Test
		public void testShatterWithCountingFormula() {
			
			int n = 3;
			LogicalVariable x = StdLogicalVariable.getInstance("X", "x", n);
			LogicalVariable y = StdLogicalVariable.getInstance("Y", "x", n);
			
			Constant x1 = Constant.getInstance("x1");
			
			Constraint x_x1 = InequalityConstraint.getInstance(x, x1);
			
			Prv b = StdPrv.getBooleanInstance("b", x);
			Prv c = StdPrv.getBooleanInstance("c", x);
			Prv e = StdPrv.getBooleanInstance("e", x);
			Prv e_x1 = StdPrv.getBooleanInstance("e", x1);
			Prv r = StdPrv.getBooleanInstance("r", x, y);
			Prv r_x1_y = StdPrv.getBooleanInstance("r", x1, y);
			Prv cf = CountingFormula.getInstance(y, r);
			Prv cf_x1 = CountingFormula.getInstance(y, r_x1_y);
			
			Parfactor g1 = new StdParfactorBuilder().variables(cf, e).build();
			Parfactor g2 = new StdParfactorBuilder().variables(b, e, c).constraints(x_x1).build();
			Parfactor g3 = new StdParfactorBuilder().variables(cf, e).constraints(x_x1).build();
			Parfactor g4 = new StdParfactorBuilder().variables(cf_x1, e_x1).build();
			
			Marginal input = new StdMarginalBuilder().parfactors(g1, g2).build();
			
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();

			Marginal expected = new StdMarginalBuilder().parfactors(g2, g3, g4).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testShatterOnCralcExample() {
			
			int n = 3;
			LogicalVariable x = StdLogicalVariable.getInstance("X", "x", n);
			LogicalVariable y = StdLogicalVariable.getInstance("Y", "x", n);
			
			Constant x1 = Constant.getInstance("x1");
			
			Prv a = StdPrv.getBooleanInstance("a", x);
			Prv b = StdPrv.getBooleanInstance("b", x);
			Prv c = StdPrv.getBooleanInstance("c", x);
			Prv d = StdPrv.getBooleanInstance("d", x);
			Prv e = StdPrv.getBooleanInstance("e", x);
			Prv f = StdPrv.getBooleanInstance("f", x);
			Prv r = StdPrv.getBooleanInstance("r", x, y);
			Prv r1 = StdPrv.getBooleanInstance("r1", x, y);
			Prv r2 = StdPrv.getBooleanInstance("r2", x, y);
			Prv c1 = StdPrv.getBooleanInstance("c", x1);
			
			List<BigDecimal> f1 = TestUtils.toBigDecimalList(0.1, 0.9);
			List<BigDecimal> f2 = TestUtils.toBigDecimalList(1.0, 0.0, 0.55, 0.45);
			List<BigDecimal> f3 = TestUtils.toBigDecimalList(1, 0, 0, 1, 0, 1, 0, 1);
			List<BigDecimal> f5 = TestUtils.toBigDecimalList(1, 0, 0, 1);
			List<BigDecimal> f7 = TestUtils.toBigDecimalList(1, 0, 0, 1, 0, 1, 0, 1);
			List<BigDecimal> f8 = TestUtils.toBigDecimalList(0.7, 0.3);
			List<BigDecimal> f9 = TestUtils.toBigDecimalList(1, 0, 1, 0, 1, 0, 0, 1);
			
			Parfactor g1 = new StdParfactorBuilder().variables(a).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(a, b).values(f2).build();
			Parfactor g3 = new StdParfactorBuilder().variables(b, e, c).values(f3).build();
			Parfactor g4 = new AggParfactorBuilder(r1, f, And.AND).build();
			Parfactor g5 = new StdParfactorBuilder().variables(f, d).values(f5).build();
			Parfactor g6 = new AggParfactorBuilder(r2, e, Or.OR).build();
			Parfactor g7 = new StdParfactorBuilder().variables(a, r, r1).values(f7).build();
			Parfactor g8 = new StdParfactorBuilder().variables(r).values(f8).build();
			Parfactor g9 = new StdParfactorBuilder().variables(d, r, r2).values(f9).build();
			Parfactor g10 = new StdParfactorBuilder().variables(c1).build();
			
			RandomVariableSet query = RandomVariableSet.getInstance(c1, new HashSet<Constraint>(0));
			Marginal input = new StdMarginalBuilder().parfactors(g1, g2, g3, g4, g5, g6, g7, g8, g9, g10).preservable(query).build();
			
//			MacroOperation conversion = new ConvertToStdParfactors(input);
//			input = conversion.run();
			MacroOperation shatter = new Shatter(input);
			Marginal result = shatter.run();
			
			System.out.print(result);
		}
	}
	
		
	public static class FullExpandTest {
		
		@Test
		public void testFullExpand() {
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 3);
			LogicalVariable b = StdLogicalVariable.getInstance("B", "x", 3);
			
			Constant x1 = Constant.getInstance("x1");
			Constant x2 = Constant.getInstance("x2");
			Constant x3 = Constant.getInstance("x3");
			
			Constraint a_x1 = InequalityConstraint.getInstance(a, x1);
			Constraint a_x2 = InequalityConstraint.getInstance(a, x2);
			Constraint a_x3 = InequalityConstraint.getInstance(a, x3);
			
			Prv f_a = StdPrv.getBooleanInstance("f", a);
			Prv f_x2 = StdPrv.getBooleanInstance("f", x2);
			Prv f_x3 = StdPrv.getBooleanInstance("f", x3);
			Prv h_b = StdPrv.getBooleanInstance("h", b);
			Prv cf = CountingFormula.getInstance(a, f_a, a_x1);
			
			Parfactor g1 = new StdParfactorBuilder().variables(f_a).values(0.4, 0.6).build();
			Parfactor g2 = new StdParfactorBuilder().variables(cf, h_b).values(1, 2, 3, 4, 5, 6).build();
			Parfactor g3 = new StdParfactorBuilder().variables(f_a).constraints(a_x2, a_x3).values(0.4, 0.6).build();
			Parfactor g4 = new StdParfactorBuilder().variables(f_x2).values(0.4, 0.6).build();
			Parfactor g5 = new StdParfactorBuilder().variables(f_x3).values(0.4, 0.6).build();
			Parfactor g6 = new StdParfactorBuilder().variables(f_x3, f_x2, h_b).values(1, 2, 3, 4, 3, 4, 5, 6).build();
			
			Marginal marginal = new StdMarginalBuilder(2).parfactors(g1, g2).build();
			
			MacroOperation fullExpand = new FullExpand(marginal, g2, cf);
			
			Marginal result = fullExpand.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g3, g4, g5, g6).build();

			assertEquals(expected, result);
		}
	}

	public static class PropositionalizationTest {
		@Test
		public void testPropositionalize() {
			LogicalVariable lot = StdLogicalVariable.getInstance("Lot", "lot", 3);
			Constant lot1 = Constant.getInstance("lot1");
			Constant lot2 = Constant.getInstance("lot2");
			Constant lot3 = Constant.getInstance("lot3");
			
			Prv rain = StdPrv.getBooleanInstance("rain");
			Prv sprinkler = StdPrv.getBooleanInstance("sprinkler", lot);
			Prv wet_grass = StdPrv.getBooleanInstance("wet_grass", lot);
			Prv wet_grass_lot1 = StdPrv.getBooleanInstance("wet_grass", lot1);
			Prv wet_grass_lot2 = StdPrv.getBooleanInstance("wet_grass", lot2);
			Prv wet_grass_lot3 = StdPrv.getBooleanInstance("wet_grass", lot3);
			Prv sprinkler_lot1 = StdPrv.getBooleanInstance("sprinkler", lot1);
			Prv sprinkler_lot2 = StdPrv.getBooleanInstance("sprinkler", lot2);
			Prv sprinkler_lot3 = StdPrv.getBooleanInstance("sprinkler", lot3);
			
			double [] f1 = {0.8, 0.2};
			double [] f2 = {0.6, 0.4};
			double [] f3 = {1.0, 0.0, 0.2, 0.8, 0.1, 0.9, 0.01, 0.99};
			double [] f4 = {0.0, 1.0};
			
			Parfactor g1 = new StdParfactorBuilder().variables(rain).values(f1).build();
			Parfactor g2 = new StdParfactorBuilder().variables(sprinkler).values(f2).build();
			Parfactor g3 = new StdParfactorBuilder().variables(rain, sprinkler, wet_grass).values(f3).build();
			Parfactor g4 = new StdParfactorBuilder().variables(wet_grass_lot1).values(f4).build();
			
			Parfactor g2_1 = new StdParfactorBuilder().variables(sprinkler_lot1).values(f2).build();
			Parfactor g2_2 = new StdParfactorBuilder().variables(sprinkler_lot2).values(f2).build();
			Parfactor g2_3 = new StdParfactorBuilder().variables(sprinkler_lot3).values(f2).build();
			
			Parfactor g3_1 = new StdParfactorBuilder().variables(rain, sprinkler_lot1, wet_grass_lot1).values(f3).build();
			Parfactor g3_2 = new StdParfactorBuilder().variables(rain, sprinkler_lot2, wet_grass_lot2).values(f3).build();
			Parfactor g3_3 = new StdParfactorBuilder().variables(rain, sprinkler_lot3, wet_grass_lot3).values(f3).build();
			
			Marginal marginal = new StdMarginalBuilder().parfactors(g1, g2, g3, g4).build();
			MacroOperation propositionalize = new Propositionalize(marginal, g3, lot);
			
			Marginal result = propositionalize.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g1, g4, g2_1, g2_2, g2_3, g3_1, g3_2, g3_3).build();
			
			assertEquals(expected, result);
		}
	}

	/**
	 * Tests counting convert.
	 */
	public static class CountingConvertTest {
		
		/**
		 * Creates example in section 2.5.2.7 of Kisynski (2010).
		 * Only sets &Phi;<sub>4</sub> and &Phi;<sub>5</sub> are used.
		 * <p>
		 * Parfactors [09] and [12] have been changed to use integer numbers, 
		 * otherwise comparison between doubles would complicate the test.
		 * Precision is not the objective of this test.
		 * </p>
		 * <p>
		 * The population of logical variable Lot is set to 16 individuals. 
		 * Factor F7 will have 32 rows.
		 * </p>
		 */
		@Test
		public void testSimpleCountingConvert() {
			
			LogicalVariable lot = StdLogicalVariable.getInstance("Lot", "lot", 16);
			Constant lot1 = Constant.getInstance("lot1");

			Constraint lot_lot1 = InequalityConstraint.getInstance(lot, lot1);
			
			Prv rain = StdPrv.getBooleanInstance("rain");
			Prv wet_grass = StdPrv.getBooleanInstance("wet_grass", lot);
			Prv formula = CountingFormula.getInstance(lot, wet_grass, lot_lot1);
			
			double [] f1 = {0.8, 0.2};
			double [] f5 = {2, 3, 5, 7};
			double [] f6 = {0.32, 0.936};
			double [] f7 = {32768, 49152, 73728, 110592, 165888, 248832, 373248,
					559872, 839808, 1259712, 1889568, 2834352, 4251528, 6377292, 
					9565938, 14348907, 30517578125.0, 42724609375.0, 59814453125.0, 
					83740234375.0, 117236328125.0, 164130859375.0, 229783203125.0,
					321696484375.0, 450375078125.0, 630525109375.0, 882735153125.0,
					1235829214375.0, 1730160900125.0, 2422225260175.0, 
					3391115364245.0, 4747561509943.0};
			
			Parfactor g1 = new StdParfactorBuilder().variables(rain).values(f1).build();
			Parfactor g9 = new StdParfactorBuilder().variables(rain, wet_grass).constraints(lot_lot1).values(f5).build();
			Parfactor g11 = new StdParfactorBuilder().variables(rain).values(f6).build();
			Parfactor g12 = new StdParfactorBuilder().variables(rain, formula).values(f7).build();
			
			Marginal input = new StdMarginalBuilder().parfactors(g1, g9, g11).build();
			MacroOperation countingConvert = new CountingConvert(input, g9, lot);
			
			Marginal result = countingConvert.run();
			Marginal expected = new StdMarginalBuilder().parfactors(g1, g11, g12).build();
			
			assertEquals(expected, result);
		}
	}

	/**
	 * Tests global sum out.
	 */
	public static class GlobalSumOutTest {
		
		private final LogicalVariable lot = StdLogicalVariable.getInstance("Lot", "lot", 10);
		private final Constant lot1 = Constant.getInstance("lot1");

		private final Constraint lot_lot1 = InequalityConstraint.getInstance(lot, lot1);
		
		private final Prv rain = StdPrv.getBooleanInstance("rain");
		private final Prv another_rain = StdPrv.getBooleanInstance("another_rain");
		private final Prv sprinkler = StdPrv.getBooleanInstance("sprinkler", lot);
		private final Prv sprinkler_lot1 = StdPrv.getBooleanInstance("sprinkler", lot1);
		private final Prv wet_grass = StdPrv.getBooleanInstance("wet_grass", lot);
		private final Prv wet_grass_lot1 = StdPrv.getBooleanInstance("wet_grass", lot1);
		
		private final double [] f1 = {1, 2};
		private final double [] f2 = {2, 3};
		private final double [] f3 = {1, 2, 3, 4, 5, 6, 7, 8};
		private final double [] f6 = {3, 5};
		private final double [] f7 = {2, 3, 5, 7};
		private final double [] f8 = {56, 79};
		private final double [] f9 = {11, 16, 31, 36};
		
		private final Parfactor g1 = new StdParfactorBuilder().variables(rain).values(f1).build();
		private final Parfactor g4 = new StdParfactorBuilder().variables(wet_grass_lot1).values(f1).build();
		private final Parfactor g5 = new StdParfactorBuilder().variables(rain, sprinkler_lot1, wet_grass_lot1).values(f3).build();
		private final Parfactor g6 = new StdParfactorBuilder().variables(rain, sprinkler, wet_grass).values(f3).constraints(lot_lot1).build();
		private final Parfactor g7 = new StdParfactorBuilder().variables(sprinkler_lot1).values(f2).build();
		private final Parfactor g8 = new StdParfactorBuilder().variables(sprinkler).values(f2).constraints(lot_lot1).build();
		private final Parfactor g9 = new StdParfactorBuilder().variables(rain, wet_grass).values(f9).constraints(lot_lot1).build();
		private final Parfactor g10 = new StdParfactorBuilder().variables(rain, wet_grass_lot1).values(f9).build();
		private final Parfactor g11 = new StdParfactorBuilder().variables(rain).values(f6).build();
		private final Parfactor g12 = new StdParfactorBuilder().variables(rain, another_rain).values(f7).build();
		private final Parfactor g13 = new StdParfactorBuilder().variables(another_rain).values(f8).build();
		
		
		@Test
		public void testGlobalSumOutWithConstraintSimplified() {
			Marginal input = new StdMarginalBuilder(2).parfactors(g6, g8).build();
			
			Set<Constraint> constraints = Sets.setOf(lot_lot1);
			RandomVariableSet eliminables = RandomVariableSet.getInstance(sprinkler, constraints);
			MacroOperation globalSumOut = new GlobalSumOut(input, eliminables);
			Marginal result = globalSumOut.run();
			
			Marginal expected = new StdMarginalBuilder(5).parfactors(g9).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testGlobalSumOutWithConstraint() {
			
			Marginal input = new StdMarginalBuilder(6).parfactors(g1, g4, g5, g6, g7, g8).build();
			
			Set<Constraint> constraints = Sets.setOf(lot_lot1);
			RandomVariableSet eliminables = RandomVariableSet.getInstance(sprinkler, constraints);
			MacroOperation globalSumOut = new GlobalSumOut(input, eliminables);
			Marginal result = globalSumOut.run();
			
			Marginal expected = new StdMarginalBuilder(5).parfactors(g1, g4, g5, g7, g9).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testGlobalSumOutNoConstraints() {
			
			Marginal input = new StdMarginalBuilder(5).parfactors(g1, g4, g5, g7, g9).build();
			
			Set<Constraint> constraints = Sets.getInstance(0);
			RandomVariableSet eliminables = RandomVariableSet.getInstance(sprinkler_lot1, constraints);
			MacroOperation globalSumOut = new GlobalSumOut(input, eliminables);
			Marginal result = globalSumOut.run();
			
			Marginal expected = new StdMarginalBuilder(4).parfactors(g1, g4, g9, g10).build();
			
			assertEquals(expected, result);
		}
		
		@Test
		public void testGlobalSumWithThreeStdParfactors() {
			
			Marginal input = new StdMarginalBuilder(3).parfactors(g1, g11, g12).build();
			
			Set<Constraint> constraints = Sets.getInstance(0);
			RandomVariableSet eliminables = RandomVariableSet.getInstance(rain, constraints);
			MacroOperation globalSumOut = new GlobalSumOut(input, eliminables);
			Marginal result = globalSumOut.run();
			
			Marginal expected = new StdMarginalBuilder(1).parfactors(g13).build();
			
			assertEquals(expected, result);
		}
	}

	
}
