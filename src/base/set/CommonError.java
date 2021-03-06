package base.set;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * 10个常见错误
 */
public class CommonError {
    public static void main(String[] args) {
        //error
        int[] arr = {1, 2, 3};
        List<int[]> arrList = Arrays.asList(arr);
        //right
        List arrList2 = new ArrayList(Arrays.asList(arr));
        //循环删除list元素

        List<String> arrList3 = new ArrayList(Arrays.asList("a", "ds", "n", "k"));
        for (int i = 0; i < arrList3.size(); i++) {
            arrList3.remove(i);
        }

        System.out.println(arrList3);
        //or
        for (String s : arrList3) {
            if (s.equals("a")) {
                arrList3.remove(s);
            }
        }

        System.out.println("foreach:" + arrList3);
        //right
        Iterator<String> it = arrList3.iterator();
        while (it.hasNext()) {
            String s = it.next();//先调用next,再调用remove
            if (s.equals("a")) {
                it.remove();
            }
        }


        //
        List<String> list4 = new ArrayList<>();
        add(list4, 2);
        String re = list4.get(0);


    }


    public static void add(List list, Object o) {
        list.add(o);
    }
}
