package research.smoke.fixture;

public class InheritedFieldVirtualDispatch {
  interface Dispatch {
    void hit();
  }

  static class Base {
    Dispatch receiver;

    void setReceiver(Dispatch receiver) {
      this.receiver = receiver;
    }
  }

  static class Child extends Base {
    void invokeReceiver() {
      receiver.hit();
    }
  }

  static class Target implements Dispatch {
    @Override
    public void hit() {}
  }

  public static void main(String[] args) {
    Child child = new Child();
    child.setReceiver(new Target());
    child.invokeReceiver();
  }
}
