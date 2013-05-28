package br.usp.poli.takiyama.sandbox;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import br.usp.poli.takiyama.acfove.operator.And;
import br.usp.poli.takiyama.common.InequalityConstraint;
import br.usp.poli.takiyama.common.Parfactors;
import br.usp.poli.takiyama.common.Pool;
import br.usp.poli.takiyama.common.ParfactorI;
import br.usp.poli.takiyama.common.Constraint;
import br.usp.poli.takiyama.prv.Binding;
import br.usp.poli.takiyama.prv.Bool;
import br.usp.poli.takiyama.prv.Constant;
import br.usp.poli.takiyama.prv.LogicalVariable;
import br.usp.poli.takiyama.prv.OldCountingFormula;
import br.usp.poli.takiyama.prv.LogicalVariableNameGenerator;
import br.usp.poli.takiyama.prv.Operator;
import br.usp.poli.takiyama.prv.Or;
import br.usp.poli.takiyama.prv.ParameterizedRandomVariable;
import br.usp.poli.takiyama.prv.RangeElement;
import br.usp.poli.takiyama.prv.StdLogicalVariable;
import br.usp.poli.takiyama.prv.Substitution;
import br.usp.poli.takiyama.prv.Term;
import br.usp.poli.takiyama.utils.Lists;
import br.usp.poli.takiyama.utils.MathUtils;
import br.usp.poli.takiyama.utils.Sets;


@RunWith(Enclosed.class)
public class Sandbox {
	
	@RunWith(Parameterized.class)
	public static class Test1 {
		@Parameters(name = "{index}: {0} * {0} = {1}")
		public static Collection<Object[]> data() {
			Object data[][] = new Object[][] { 
					{1, 1},  
					{2, 4},  
					{3, 9} 
					};
			return Arrays.asList(data);
		}
		
		private int a;
		private int b;
		
		public Test1(int a, int b) {
			this.a = a;
			this.b = b;
		}
		
		@Test
		public void test() {
			assertEquals(a*a, b);
		}
	}
	
	@RunWith(Parameterized.class)
	public static class Test2 {
		@Parameters(name = "{index}: {0} multi {0} = {1}")
		public static Collection<Object[]> data() {
			Object data[][] = new Object[][] { 
					{1, 1},  
					{2, 4},  
					{3, 9} 
					};
			return Arrays.asList(data);
		}
		
		private int a;
		private int b;
		
		public Test2(int a, int b) {
			this.a = a;
			this.b = b;
		}
		
		@Test
		public void test() {
			assertEquals(a*a, b);
		}
	}
	
	public static class ListTest {
		
		@Test
		public void testEqualityOfEmptyLists() {
			List<BigDecimal> list1 = new ArrayList<BigDecimal>(0);
			List<BigDecimal> list2 = new ArrayList<BigDecimal>(0);
			assertTrue(Lists.areEqual(list1, list2));
		}
		
		@Test
		public void testEquality() {
			List<BigDecimal> list1 = new ArrayList<BigDecimal>(1);
			List<BigDecimal> list2 = new ArrayList<BigDecimal>(1);
			
			list1.add(BigDecimal.ONE);
			list2.add(BigDecimal.valueOf(1));
			
			assertTrue(Lists.areEqual(list1, list2));
		}
		
		@Test
		public void testEqualityTwoElements() {
			List<BigDecimal> list1 = new ArrayList<BigDecimal>(2);
			List<BigDecimal> list2 = new ArrayList<BigDecimal>(2);
			
			list1.add(BigDecimal.ONE);
			list1.add(BigDecimal.ONE);
			list2.add(BigDecimal.valueOf(1));
			list2.add(BigDecimal.valueOf(1.0));
			
			assertTrue(Lists.areEqual(list1, list2));
		}
		
		@Test
		public void testSubstitution() {
			LogicalVariable a = StdLogicalVariable.getInstance("A", "x", 10);
			LogicalVariable b = StdLogicalVariable.getInstance("B", "x", 10);
			
			Constant x1 = Constant.getInstance("x1");
			Substitution s = Substitution.getInstance(Binding.getInstance(b, x1));
			
			Constraint c = InequalityConstraint.getInstance(a, b);
			List<Constraint> list = Lists.listOf(c);
			
			Constraint ans = InequalityConstraint.getInstance(a, x1);
			List<Constraint> expected = Lists.listOf(ans);
			
			assertEquals(expected, Lists.apply(s, list));
		}
	}
	
	public static class NumberTest {
		@Test
		public void testBigDecimalScale() {
			BigDecimal n = BigDecimal.valueOf(0.2345);
			BigDecimal m = n.setScale(2, BigDecimal.ROUND_HALF_EVEN);
			int x;
		}
	}

	public static class SetTest {
		@Test
		public void testEmptySet() {
			Set<String> setWithInitialCapacity = new HashSet<String>(0);
			Set<String> setWithoutInitialCapacity = new HashSet<String>();
			
			assertFalse(setWithInitialCapacity.contains(null));
			assertFalse(setWithoutInitialCapacity.contains(null));
		}
		
		private static class Person {
			private String name;
			private Person(String name) {
				this.name = name;
			}
			private void setName(String name) {
				this.name = name;
			}
			public String toString() {
				return name;
			}
		}
		
		@Test
		public void testSetReference() {
			Person person = new Person("John");
			Set<Person> s1 = Sets.setOf(person);
			Set<Person> s2 = new HashSet<Person>(s1);
			System.out.println("Before");
			System.out.println(s1);
			System.out.println(s2);
			person.setName("James");
			System.out.println("After");
			System.out.println(s1);
			System.out.println(s2);
		}
	}

	public static class AssertTest {
		@Test
		public void testDirect() {
			assertEquals(1, 2);
			assertEquals(1, 1);
		}
		
		@Test
		public void testInverse() {
			assertEquals(1, 1);
			assertEquals(1, 2);
		}
	}

	public static class MutabilityTest {
		@Test
		public void accessAndModifyValueInHashMap() {
			// Creating a map whose value is a mutable object
			HashMap<String, Set<String>> map = new HashMap<String, Set<String>>();
			// Creating the value
			Set<String> value = new HashSet<String>();
			value.add("a");
			value.add("b");
			// Putting the value into the map
			map.put("key", value);
			// Retrieving the value and modifying it
			map.get("key").add("c");
			System.out.println(map);
		}
	}
}