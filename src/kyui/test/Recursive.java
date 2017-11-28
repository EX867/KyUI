package kyui.test;
import java.util.Scanner;
public class Recursive {
  static int n;
  static int[] A;
  static boolean[] used;
  static void next(int num, int index) {
    //for (int a=1; a < index; a++) {
    //  if (A[a] == num) return;
    //}
    //if(used[num])return;
    A[index]=num;
    used[num]=true;
    if (index == n) {
      print();
    } else {
      for (int a=1; a <= n; a++) {
        if (!used[a]) next(a, index + 1);
      }
    }
    used[num]=false;
  }
  static void print() {
    for (int a=1; a <= n; a++) {
      System.out.print(A[a] + " ");
    }
    System.out.println();
  }
  public static void main(String[] args) {
    Scanner sc=new Scanner(System.in);
    n=sc.nextInt();
    sc.close();
    A=new int[n + 1];
    used=new boolean[n + 1];
    next(0, 0);
  }
}