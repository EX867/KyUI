package kyui.util;
public interface TypeChanger<TypeA, TypeB> {
  public TypeA changeBtoA(TypeB in);
  public TypeB changeAtoB(TypeA in);
}
