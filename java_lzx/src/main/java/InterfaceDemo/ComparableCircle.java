package InterfaceDemo;

/**
 * @author lzx
 * @date 2023/3/12 18:21
 * @description: TODO
 */
public class ComparableCircle extends Circle implements CompareObject{
    public static void main(String[] args) throws IllegalAccessException {
        ComparableCircle c1 = new ComparableCircle(2.3);
        ComparableCircle c2 = new ComparableCircle(3.4);
        System.out.println(c2.compareTo(c1));
    }
    public ComparableCircle() {
    }

    public ComparableCircle(double radius) {
        super(radius);
    }

    //根据半径大小 比较对象大小
    @Override
    public int compareTo(Object o) throws IllegalAccessException {
        if (this == o){
            return 0;
        }
        if (o instanceof ComparableCircle){
            ComparableCircle o1 = (ComparableCircle) o;
            return Double.compare(this.getRadius(), o1.getRadius());
        }else {
            throw new IllegalAccessException("输入参数不合法");
        }
    }
}
