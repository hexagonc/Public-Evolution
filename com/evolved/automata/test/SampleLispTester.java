package com.evolved.automata.test;

import java.util.*;

import com.evolved.automata.alisp.*;


public class SampleLispTester {
	public static void main(String[] args)
	{
		Environment env;
		
		
		try
		{
			env = new Environment();
			regression(env);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void regression(Environment env) 
	{
		
		Argument output = null;
		String inputArg, outputString;
		inputArg = "(setq a (make-array 10))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf (nth a 0) 10)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(nth a 0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq yes 1)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(cond ((equals 1 yes) (format F \"first\")) ((equals 2 yes) (format F \"second\")))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq yes 2)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(cond ((equals 1 yes) (format F \"first\")) ((equals 2 yes) (format F \"second\")))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(string yes)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq greater-than-thresh 255.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(if greater-than-thresh \"greater\" \"less\")";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq b 13)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq b a)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf a 12)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf b 20)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
	
		inputArg = "(list a b F)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format 1 \"%1$s\" (random 0 1))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setq leftTach 10)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(+ 15 (resolve \"leftTach\"))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(split \"left left right\" \" \")";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf a 10)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(defun output (x newValue) (setf (id x) newValue))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format 1 \"%1$s\" a)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(output \"a\" 15)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format 1 \"%1$s\" a)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format F \"%1$s\" (switch 1 (1 \"one\") (2 \"two\") (12 (format 1 \"Yo, twelve!\n\" ))))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(defun fact (x)  (if (> 2 x) 1 (* x (fact (- x 1)))))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(fact 4)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(defun make-adder (x) (lambda (y) (+ x y)))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf my-adder (make-adder 10))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(funcall \"my-adder\" (1))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(cos 1)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf x 1.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		
		inputArg = "(setf x (+ 1 x))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format 1 \"%1$s\n\" x)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf x 1.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf y 20.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
	
		inputArg = "(list y x)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(equals y x)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(if (equals y x) \"same\" \"different\")";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf x 20.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(if (equals y x) \"same\" \"different\")";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(format 1 \"%1$s\" \"great!\n\")";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(for x (1 2 3 4 5) \"done\" (format 1 \"%1$s\n\" x))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "x";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf x 1)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf y 5.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(while (> y x) (format 1 \"%1$s\n\" x) (setf x (+ x 1)))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(list x y z)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(let ((x -7) (z 1)) (setf z -12) (setf x 10) (mapcar y (list x z) (+ y 10)))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(list x y z)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		
		inputArg = "(list x y z)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(+ x y)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(+ 10 20)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(equals 12 12)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(append 12 (list 12 -34.4 (+ 12 34))  )";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(let ((x 10)) (format 1 \"let outer compiled x: %1$s\n\" x) (format 1 \"sum :%1$s\" (+ x 10)) (for x (list 99 98 97 96) \"done\" (format 1 \"Scoped inner x: %1$s\n\" x)) (format 1 \"final let outer compiled x: %1$s\n\" x))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		env.bindScalarValue("collision", 100);
		env.bindScalarValue("ultrasonic", 20);
		env.bindScalarValue("tol",3);
		env.bindScalarValue("distance", 30);
		inputArg = "(second (some x (list (list (> collision 500) \"collision\") (list (> tol (abs (- ultrasonic distance))) \"done\")) (first x)))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(second (some x (list (list (> collision 500) \"collision\") (list (> tol (abs (- ultrasonic distance))) \"done\")) (first x)))";
		env.bindScalarValue("collision", 600);
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		env.bindScalarValue("collision", 100);
		env.bindScalarValue("distance", 22);
		inputArg = "(second (some x (list (list (> collision 500) \"collision\") (list (> tol (abs (- ultrasonic distance))) \"done\")) (first x)))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(defun map-match (map-list)  (second (some x map-list (first x))))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);

		env.bindScalarValue("collision", 600);
		inputArg = "(map-match (list (list (> collision 500) \"collision\") (list (> tol (abs (- ultrasonic distance))) \"done\")))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(map-match (list (list (> collision 500) \"collision\") (list (> tol (abs (- ultrasonic distance))) \"done\")))";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "x";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf x 1)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		inputArg = "(setf y 5.0)";
		output = env.getParsedResult(inputArg);
		outputString = (output==null)?"null":output.toString();
		System.out.println(inputArg+ " --> " + outputString);
		
		
		
		continuationTest2(env);
	}
	
	
	public static void continuationTest2(Environment env)
	{
		Integer key;
		String outputString, inputArg = "(process)";
		env.getParsedResult("(setf speech F)");
		env.getParsedResult("(setf action F)");
		env.getParsedResult("(progn (defun process () (setf action F) (unless (equals speech \"hello\") (if (not action) (for x (list \"hello\" \"\") F (unless (equals x action)  (setf action x)))))))");
		CompiledEvaluator evaluator =  env.parserIntoEvaluator(inputArg);
		
		Argument out=null, output;
		
		Hashtable<Integer, String> events = new Hashtable<Integer, String>(); 
		events.put(4, "hello");
		
		for (int i=0;i<10;i++)
		{
			if (events.containsKey(new Integer(i)))
			{
				env.bindScalarValue("speech", "hello");
			}
			else
				env.bindScalarValue("speech", null);
			if (out==null||!out.isContinuation())
				out = env.getPreParsedResult(evaluator, false);
			else
				out = env.getPreParsedResult(evaluator, true);
			output = env.getVariableValue("action");
			outputString = (output==null)?"null":output.toString();
			System.out.println(inputArg+ " --> " + outputString);
		}
		
		
	}
}
