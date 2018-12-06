package com.ibm.benders;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.LongAnnotation;


public class Main
{
    public static void main( String[] args ) throws IloException
    {
        IloCplex cplex = new IloCplex();
        int M=4, N=3;

        Table<Integer,Integer,IloNumVar> x = HashBasedTable.create();
        double c[][] = {{2.0, 3.0, 4.0},
                        {3.0, 2.0, 1.0},
                        {1.0, 4.0, 3.0},
                        {4.0, 5.0, 2.0}};

        Table<Integer,Integer,IloIntVar> y = HashBasedTable.create();
        double f[][] = {{10.0, 30.0, 20.0},
                        {10.0, 30.0, 20.0},
                        {10.0, 30.0, 20.0},
                        {10.0, 30.0, 20.0}};
        
        double s[] = {10,
                      30,
                      40,
                      20};
        
        double d[] = {20,
                      50,
                      30};

        cplex.setParam(IloCplex.Param.Benders.Strategy, IloCplex.BendersStrategy.Full);
        LongAnnotation benders = cplex.newLongAnnotation("cpxBendersPartition", IloCplex.CPX_BENDERS_MASTERVALUE);

        IloLinearNumExpr obj = cplex.linearNumExpr();

        for (int j = 0; j < N; j++) {
            for (int i = 0; i < M; i++) {
                x.put(i, j, cplex.numVar(0.0, Double.MAX_VALUE, "x(" + i + ", " + j + ")"));
                y.put(i, j, cplex.intVar(0, 1, "y(" + i + ", " + j + ")"));

                cplex.addLe(x.get(i, j), cplex.prod(Math.min(s[i], d[j]), y.get(i, j)));

                obj.addTerm(c[i][j], x.get(i, j));
                obj.addTerm(f[i][j], y.get(i, j));
            }
        }
        cplex.addMinimize(obj);


        for (int i = 0; i < M; i++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int j = 0; j < N; j++) {
                sum.addTerm(1.0, x.get(i, j));
            }
            cplex.addEq(sum, s[i]);
        }

        for (int j = 0; j < N; j++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int i = 0; i < M; i++) {
                sum.addTerm(1.0, x.get(i, j));
            }
            cplex.addEq(sum, d[j]);
        }

        for (int i = 0; i < M; i++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int j = 0; j < N; j++) {
                sum.addTerm(d[j], y.get(i, j));
            }
            cplex.addGe(sum, s[i]);
        }

        for (int j = 0; j < N; j++) {
            IloLinearNumExpr sum = cplex.linearNumExpr();
            for (int i = 0; i < M; i++) {
                sum.addTerm(s[i], y.get(i, j));
            }
            cplex.addEq(sum, d[j]);
        }

        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                cplex.setAnnotation(benders, y.get(i, j), IloCplex.CPX_BENDERS_MASTERVALUE);
                cplex.setAnnotation(benders, x.get(i, j), IloCplex.CPX_BENDERS_MASTERVALUE + 1);
            }
        }

        System.out.println(cplex.getModel());

        boolean resolveu = cplex.solve();
        if (resolveu) {
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    System.out.println(x.get(i, j) + ": " + cplex.getValue(x.get(i, j)));
                    
                }
            }
        }
    }
}
