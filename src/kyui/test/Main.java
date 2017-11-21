package kyui.test;
public class Main {
  static int map[][]=new int[10][10];
  static void q(int num, int x, int y) {
    for (int a=1; a < x; a++) {
      if (map[a][y] == num) {
        return;
      }
    }
    for (int a=1; a < y; a++) {
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
    if (x == 9 && y == 9) {
      print();
      map[x][y]=0;
      return;
    }
    for (int a=1; a <= 9; a++) {
      q(a, (x == 9) ? 1 : (x + 1), (x == 9) ? (y + 1) : y);
    }
    map[x][y]=0;
  }
  static void print() {
    for (int a=1; a <= 9; a++) {
      for (int b=1; b <= 9; b++) {
        System.out.print(map[b][a]);
      }
      System.out.println();
    }
    System.out.println();
  }
  public static void main(String[] args) {
    q(1, 0, 1);
  }
}
