package kyui.util;
import java.util.LinkedList;
public class TaskManager {
  LinkedList<TaskSet> tasks=new LinkedList<TaskSet>();
  public TaskManager() {
  }
  public synchronized void addTask(Task task, Object data) {
    tasks.addLast(new TaskSet(task, data));
  }
  public synchronized void executeAll() {
    while (tasks.size() > 0) {
      tasks.pollFirst().execute();
    }
  }
  public LinkedList<TaskSet> getTaskSet(){
    return tasks;
  }
  public class TaskSet {
    public Task task;
    public Object data;
    public TaskSet(Task task_, Object data_) {
      task=task_;
      data=data_;
    }
    public void execute() {
      task.execute(data);
    }
  }
}
