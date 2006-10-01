package example;

public class B extends BofA
{
  class M implements example.p.A
  {
    public A getA()
    {
      return null;
    }
  }
  
  public A getA()
  {
    return new A();
  }
}
