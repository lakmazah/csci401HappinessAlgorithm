import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Project;
import model.Ranking;
import model.user.Student;
import util.ProjectAssignment;

public class bruteForceEnumeration {
	
	public static ArrayList<Project> projects = new ArrayList<>();
	public static ArrayList<Student> students = new ArrayList<>();
	public static List<Ranking> rankings = new ArrayList<>();
	public static Set<Student[]> setArrays = new HashSet<Student[]>();
	public static Set<Student[]> tempArrays = new HashSet<Student[]>();
	public static Set<ArrayList<ArrayList<Student>>> solutionSet= new HashSet<ArrayList<ArrayList<Student>>>();
	
	public static void main(String[] args) {
		// populate projects, students, rankings
		ProjectAssignment currentRun = new ProjectAssignment(projects, students,rankings);
		currentRun.importDataLocallyAndPopulateDatabase();
		projects = currentRun.getProjects();
		students = currentRun.getStudents();
		rankings = currentRun.getAllRankings();
		
		bruteForce();
		
	}
	
	public static void bruteForce() {
		HashMap<Integer, Set<Student> > initialSets = new HashMap<Integer, Set<Student>>();
		for(Project p : projects) {
			initialSets.put(p.getProjectId(), new HashSet<Student>());
		}
		for(Student s : students) {
			for(String proj : s.getRankings().keySet()) {
				initialSets.get(Integer.parseInt(proj)).add(s);
			}
		}
		for(int el : initialSets.keySet()) {
			for(Student s : initialSets.get(el))
				System.out.print(s.getFirstName() + ", ");
			System.out.println("");
		}
		generateCombinations(initialSets);
		printMaxSolution();
	}
	
	public static void generateCombinations(HashMap<Integer, Set<Student> > initialSets) {

		ArrayList<ArrayList<ArrayList<Student>>> comboList = new ArrayList<ArrayList<ArrayList<Student>>>();
		
		for (int i = 1; i <= initialSets.size(); i++) {

			Set<Student> tmpSet = initialSets.get(i);
			//create combos
			
			Student[] array = new Student[tmpSet.size()];
			int count = 0;
			for (Student s : tmpSet) {
				array[count] = s;
				count++;
			}
	
			
			//all combinations of all sizes
			for(int j = projects.get(i-1).getMinSize(); j <= projects.get(i-1).getMaxSize(); j++) {
				Student[] currSet = new Student[j];
				combinationUtil(array, currSet, 0, initialSets.get(i).size()-1, 0, j);
			}
			
			int a = 0;
			ArrayList<ArrayList<Student>> singleProject = new ArrayList<ArrayList<Student>>();
			for (Student[] l : setArrays) {
				ArrayList<Student> combo = new ArrayList<Student>();
				for(Student student : l) {
					combo.add(student);
				}
				singleProject.add(combo);
			}
			singleProject.add(new ArrayList<Student>());
			comboList.add(singleProject);
			setArrays.clear();
		}
		for(ArrayList<ArrayList<Student>> project : comboList) {
			for(ArrayList<Student> set : project) {
				System.out.print("[");
				String studentSet = "";
				for(Student s : set) {
					studentSet += s.getUserId() + ", ";
				}
				if(studentSet.length() > 0)
					System.out.print(studentSet.substring(0,studentSet.length() - 2) + "] ");
				else {
					System.out.print("] ");
				}
			}
			System.out.println("");
		}
		getSolutions(comboList,0, new ArrayList<ArrayList<Student>>());
		System.out.println(solutionSet.size());
	}
	
	public static void combinationUtil(Student[] initalSet, Student[] currentSet, int start, int end, 
			int index, int r) {
		
		if (index == r) {
			Student[] tmp = new Student[r];
			for (int j = 0; j < r; j++) {
				tmp[j] = currentSet[j];
			}
			setArrays.add(tmp);
			
			return;
		}
		
		for (int i = start; i <= end & end-i+1 >= r-index; i++) {
			currentSet[index] = initalSet[i];
			combinationUtil(initalSet, currentSet, i+1, end, index+1, r);
		}
	}
	
	public static void getSolutions(ArrayList<ArrayList<ArrayList<Student>>> comboList, int projectLevel, ArrayList<ArrayList<Student>> currentAssignmentList) {
		//reached last project
		if(projectLevel == comboList.size()) {
			ArrayList<ArrayList<Student>> solution = new ArrayList<ArrayList<Student>>();
			for(ArrayList<Student> group : currentAssignmentList) {
				ArrayList<Student> assignment = new ArrayList<Student>();
				for(Student s : group) {
					assignment.add(s);
				}
				solution.add(assignment);
			}
			solutionSet.add(solution);
		}
		
		else {
			for(ArrayList<Student> combo: comboList.get(projectLevel)) {
				boolean dupe = false;
				for(Student s : combo) {
					for(ArrayList<Student> projects : currentAssignmentList)
						if(projects.contains(s))
							dupe = true;
				}
				if(!dupe) {
					currentAssignmentList.add(combo);
					getSolutions(comboList, projectLevel + 1, currentAssignmentList);
					currentAssignmentList.remove(currentAssignmentList.size()-1);
				}
			}
		}
	}
	
	public static int calculateScore(ArrayList<ArrayList<Student>> solution) {
		int score = 0;
		for(int i = 0; i < solution.size(); i++) {
			ArrayList<Student> group = solution.get(i);
			for(Student s : group) {
				if(s.getOrderedRankings().indexOf(i+"") <= 1) {
					//6-1
					score += 5.0;
				}
				
				else if(s.getOrderedRankings().indexOf(i+"") == 2){
					//6-2
					score += 4.0;
				}
				else if(s.getOrderedRankings().indexOf(i+"") == 3){
					//6-3
					score += 3.0;
				}
				else if(s.getOrderedRankings().indexOf(i+"") == 4){
					//6-5
					score += 1.0;
				}
			}
		}
		return score;
	}
	
	public static void printMaxSolution() {
		int maxScore = 0;
		ArrayList<ArrayList<Student>> maxSolution = new ArrayList<ArrayList<Student>>();
		for(ArrayList<ArrayList<Student>> solution : solutionSet) {
			int score = calculateScore(solution);
			if(score > maxScore) {
				maxScore = score;
				maxSolution = solution;
			}
		}
		System.out.println("Max Score - " + maxScore);
		for(int i = 0; i < maxSolution.size(); i++) {
			ArrayList<Student> group = maxSolution.get(i);
			System.out.print((i+1) + " - [");
			String studentSet = "";
			for(Student s : group) {
				studentSet += s.getUserId() + ", ";
			}
			if(group.size() > 0)
				System.out.println(studentSet.substring(0,studentSet.length() - 2) + "] ");
			else {
				System.out.println("] ");
			}
			}
	}
	
	public static void printSolutions() {
		for(ArrayList<ArrayList<Student>> solution : solutionSet) {
			for(int i = 0; i < solution.size(); i++) {
				ArrayList<Student> group = solution.get(i);
				System.out.print((i+1) + " - [");
				String studentSet = "";
				for(Student s : group) {
					studentSet += s.getUserId() + ", ";
				}
				if(group.size() > 0)
					System.out.println(studentSet.substring(0,studentSet.length() - 2) + "] ");
				else {
					System.out.println("] ");
				}
			}
			System.out.println("Score - " + calculateScore(solution));
			System.out.println("********");
		}
	}
}
