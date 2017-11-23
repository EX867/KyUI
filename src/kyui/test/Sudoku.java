package kyui.test;
import java.util.Scanner;
public class Sudoku {
  static int map[][]=new int[10][10];
  static boolean fixed[][]=new boolean[10][10];
  static void q(int num, int x, int y) {
    if (!fixed[x][y]) {
      for (int a=1; a <= 9; a++) {
        if (map[a][y] == num) {
          return;
        }
      }
      for (int a=1; a <= 9; a++) {
        if (map[x][a] == num) {
          return;
        }
      }
      for (int a=x - (x - 1) % 3; a < x - (x - 1) % 3 + 2; a++) {
        for (int c=y - (y - 1) % 3; c < y - (y - 1) % 3 + 2; c++) {
          if (map[a][c] == num) {
            return;
          }
        }
      }
      map[x][y]=num;
    }
    if (x == 9 && y == 9) {
      print();
      if (!fixed[x][y]) {
        map[x][y]=0;
      }
      return;
    }
    int x_=(x == 9) ? 1 : (x + 1);
    int y_=(x == 9) ? (y + 1) : y;
    if (fixed[x_][y_]) {
      q(0, x_, y_);
    } else {
      for (int a=1; a <= 9; a++) {
        q(a, x_, y_);
      }
    }
    if (!fixed[x][y]) {
      map[x][y]=0;
    }
  }
  static void print() {
    for (int a=1; a <= 9; a++) {
      for (int b=1; b <= 9; b++) {
        System.out.print(map[b][a] + " ");
      }
      System.out.println();
    }
    System.out.println();
  }
  public static void main(String[] args) {
    Scanner s=new Scanner(System.in);
    for (int a=1; a <= 9; a++) {
      for (int b=1; b <= 9; b++) {
        map[b][a]=s.nextInt();
        if (map[b][a] != 0) {
          fixed[b][a]=true;
        }
      }
    }
    q(1, 0, 1);
  }
}
/*
7 0 0 0 0 6 0 4 0
0 6 0 0 7 0 0 0 5
4 0 8 2 0 3 6 7 0
0 3 1 0 0 4 8 0 0
0 0 7 0 1 0 5 0 0
0 0 6 7 0 5 4 1 0
0 7 9 8 0 1 2 0 6
6 0 0 0 3 0 0 8 0
0 8 0 5 0 0 0 0 4
 */