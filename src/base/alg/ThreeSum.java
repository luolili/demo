package base.alg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {

    public static void main(String[] args) {

    }

    /**
     * 排序+双指针
     * 先固定第一个数，一他后面一个数为左指针，最后一个数为右指针，若结果 sum 大了，就移动右指针，让 sum减小
     *
     * @param nums
     * @return
     */
    public List<List<Integer>> threeSumV2(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        // 对数组排序
        Arrays.sort(nums);
        int len = nums.length;
        if (nums[0] > 0) {
            return null;
        }
        for (int k = 0; k < len - 2; k++) {
            if (k > 0 && nums[k] == nums[k - 1]) {
                continue;
            }
            int i = k + 1, j = len - 1;
            while (i < j) {
                int sum = nums[k] + nums[i] + nums[j];
                if (sum < 0) {
                    while (i < j && nums[i] == nums[++i]) {

                    }
                } else if (sum > 0) {
                    while (i < j && nums[j] == nums[--j]) {

                    }

                } else {
                    res.add(new ArrayList<>(Arrays.asList(nums[k], nums[i], nums[j])));
                    // 找到之后，还需要++i --j,否则出不了 while(i<j)
                    while (i < j && nums[i] == nums[++i]) {

                    }
                    while (i < j && nums[j] == nums[--j]) {

                    }
                }
            }
        }
        return res;
    }

    /**
     * a+b+c=0
     * 找出所有这样的数的集合
     * 暴力法
     *
     * @param nums
     * @return
     */
    public List<List<Integer>> threeSum(int[] nums) {
        List<List<Integer>> res = new ArrayList<>();
        for (int i = 0; i < nums.length - 2; i++) { // 每个人
            for (int j = i + 1; j < nums.length - 1; j++) { // 依次拉上其他每个人
                for (int k = j + 1; k < nums.length; k++) { // 去问剩下的每个人
                    if (nums[i] + nums[j] + nums[k] == 0) { // 我们是不是可以一起组队
                        List<Integer> list = new ArrayList<>();
                        list.addAll(Arrays.asList(nums[i], nums[j], nums[k]));
                        res.add(list);
                    }
                }
            }

        }
        return res;
    }
}