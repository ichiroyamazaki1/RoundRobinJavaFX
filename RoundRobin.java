package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Scanner;

public class RoundRobin extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("-----------------------");
			System.out.println("ROUND ROBIN SCHEDULING");
			System.out.println("-----------------------");

			System.out.print("\nEnter the number of Processes: ");
			int n = scanner.nextInt();

			int[] arrivalTime = new int[n];
			int[] burstTime = new int[n];
			int[] completionTime = new int[n];
			int[] waitingTime = new int[n];
			int[] turnaroundTime = new int[n];

			for (int i = 0; i < n; i++) {
				System.out.print("Enter Arrival Time for Process " + (i + 1) + ": ");
				arrivalTime[i] = scanner.nextInt();
				System.out.print("Enter Burst Time for Process " + (i + 1) + ": ");
				burstTime[i] = scanner.nextInt();
			}

			System.out.print("\nEnter Time Quantum: ");
			int timeQuantum = scanner.nextInt();

			int totalTime = roundRobinScheduling(arrivalTime, burstTime, completionTime, waitingTime, turnaroundTime,
					timeQuantum);

			System.out.println("\nProcess Table:");
			System.out.println(" -------|---------------|---------------|------------");
			System.out.println("  P\t| Arrival Time\t| Burst Time\t| Completion \n"
					+ " -------|---------------|---------------|------------");
			for (int i = 0; i < n; i++) {
				System.out.printf("  %d\t| %d\t\t| %d\t\t|     %d\t\n", i + 1, arrivalTime[i], burstTime[i],
						completionTime[i]);
			}
			System.out.println(" -------|---------------|---------------|------------");

			System.out.println("\nTotal Time: " + totalTime);

			System.out.println("\nP\t| WT\t| TA");
			for (int i = 0; i < n; i++) {
				System.out.printf("%d\t| %dms\t| %dms\n", i + 1, waitingTime[i], turnaroundTime[i]);
			}

			double averageWaitingTime = calculateAverage(waitingTime);
			double averageTurnaroundTime = calculateAverage(turnaroundTime);

			System.out.printf("\nThe Average WT is: %.2fms\n", averageWaitingTime);
			System.out.printf("The Average TA is: %.2fms\n", averageTurnaroundTime);

			// Display Gantt Chart
			primaryStage.close();
			launchGanttChartWindow(createProcesses(n, arrivalTime, burstTime, completionTime), totalTime,
					(float) averageWaitingTime, (float) averageTurnaroundTime);

			System.out.print("\nProcess completed. Do you want to restart? (Y/N): ");
			char restart = scanner.next().charAt(0);
			if (restart != 'Y' && restart != 'y') {
				break;
			}
		}

		scanner.close();
	}

	private static int roundRobinScheduling(int[] arrivalTime, int[] burstTime, int[] completionTime, int[] waitingTime,
			int[] turnaroundTime, int timeQuantum) {
		int n = arrivalTime.length;
		int[] remainingTime = new int[n];

		for (int i = 0; i < n; i++) {
			remainingTime[i] = burstTime[i];
			waitingTime[i] = 0;
		}

		int currentTime = 0;
		while (true) {
			boolean done = true;

			for (int i = 0; i < n; i++) {
				if (remainingTime[i] > 0) {
					done = false;

					if (remainingTime[i] > timeQuantum) {
						currentTime += timeQuantum;
						remainingTime[i] -= timeQuantum;
					} else {
						currentTime += remainingTime[i];
						waitingTime[i] = currentTime - arrivalTime[i] - burstTime[i];
						remainingTime[i] = 0;
						completionTime[i] = currentTime;
						turnaroundTime[i] = completionTime[i] - arrivalTime[i];
					}
				}
			}

			if (done) {
				break;
			}
		}

		int totalTime = 0;
		for (int time : completionTime) {
			if (time > totalTime) {
				totalTime = time;
			}
		}

		return totalTime;
	}

	private static double calculateAverage(int[] array) {
		int sum = 0;
		for (int value : array) {
			sum += value;
		}
		return array.length == 0 ? 0 : (double) sum / array.length;
	}

	private void launchGanttChartWindow(Process[] processes, int totalTime, float averageWaitingTime,
			float averageTurnaroundTime) {
		Stage primaryStage = new Stage();

		Pane root = new Pane();
		double scale = 50.0;

		Text title = new Text("Gantt Chart(Round Robin)");
		title.setFont(new Font("Arial", 15));
		title.setX(10);
		title.setY(30);

		Text avgWaitingTimeText = new Text("Average Waiting Time: " + String.format("%.2fms", averageWaitingTime));
		avgWaitingTimeText.setFont(new Font("Arial", 12));
		avgWaitingTimeText.setX(10);
		avgWaitingTimeText.setY(120);

		Text avgTurnaroundTimeText = new Text(
				"Average Turnaround Time: " + String.format("%.2fms", averageTurnaroundTime));
		avgTurnaroundTimeText.setFont(new Font("Arial", 12));
		avgTurnaroundTimeText.setX(10);
		avgTurnaroundTimeText.setY(140);

		root.getChildren().addAll(title, avgWaitingTimeText, avgTurnaroundTimeText);

		double xPos = 0;
		for (Process process : processes) {
			double width = (process.completionTime - (process.completionTime - process.burstTime)) * scale;

			Rectangle rect = new Rectangle(xPos, 50, width, 20);
			rect.setStroke(Color.BLACK);
			rect.setFill(Color.TRANSPARENT);

			Text text = new Text(xPos + 5, 65, "P" + process.id);

			root.getChildren().addAll(rect, text);
			xPos += width;
		}

		for (int i = 0; i <= totalTime; i++) {
			Text timeText = new Text(scale * i - (i < 10 ? 3 : 7), 85, String.valueOf(i));
			root.getChildren().add(timeText);
		}

		ScrollPane scrollPane = new ScrollPane(root);
		scrollPane.setPrefViewportWidth(600);
		scrollPane.setPrefViewportHeight(300);
		scrollPane.setPannable(true);

		primaryStage.setTitle("Gantt Chart");
		primaryStage.setScene(new Scene(scrollPane));
		primaryStage.show();
	}

	private Process[] createProcesses(int n, int[] arrivalTime, int[] burstTime, int[] completionTime) {
		Process[] processes = new Process[n];
		for (int i = 0; i < n; i++) {
			processes[i] = new Process(i + 1, arrivalTime[i], burstTime[i], completionTime[i]);
		}
		return processes;
	}
}

class Process {
	int id;
	int arrivalTime;
	int burstTime;
	int completionTime;

	public Process(int id, int arrivalTime, int burstTime, int completionTime) {
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.burstTime = burstTime;
		this.completionTime = completionTime;
	}
}