package base.alg;

import java.util.*;

/**
 * 广度/宽度优先搜索
 */
public class BreadthFirstSearch {
    public List<List<Integer>> breadthFirstSearchV2(TreeNode root) {
        List<List<Integer>> levels = new ArrayList<List<Integer>>();
        if (root == null) {
            return levels;
        }
        Queue<TreeNode> q = new LinkedList<>();
        ((LinkedList<TreeNode>) q).add(root);
        int level = 0;
        while (!q.isEmpty()) {
            //在加入每一层（从第1层开始）的节点之前，先准备好容量
            levels.add(new ArrayList<>());
            //每一层节点的数量：第0层 1个
            int nodeNumEveryLevel = q.size();
            //不要用 迭代器，否则跑错
            for (int i = 0; i < nodeNumEveryLevel; i++) {
                TreeNode node = q.poll();
                levels.get(level).add(node.value);
                if (node.left != null) {
                    q.add(node.left);
                }
                if (node.right != null) {
                    q.add(node.right);
                }
            }
            level++;

        }
        return levels;
    }

    public List<List<Integer>> breadthFirstSearch(TreeNode root) {
        List<List<Integer>> levels = new ArrayList<List<Integer>>();
        if (root == null) {
            return levels;
        }
        helper(root, 0, levels);
        return levels;
    }


    /**
     * 递归
     *
     * @param root
     * @param level
     * @param levels
     */
    private void helper(TreeNode root, int level, List<List<Integer>> levels) {
        if (level == levels.size()) {
            levels.add(new ArrayList<>());
        }
        //level 初始为0
        levels.get(level).add(root.value);
        // 从左到右 遍历子节点
        if (root.left != null) {
            helper(root.left, level + 1, levels);
        }
        if (root.right != null) {
            helper(root.right, level + 1, levels);
        }
    }

    //层序遍历：从上到下打印出二叉树的每个节点，同一层的节点按照从左到右的顺序打印。

    public int[] levelOrder(TreeNode root) {
        //NPE
        if (root == null) {
            return new int[0];
        }
        List<Integer> list = new ArrayList<>();
        Queue<TreeNode> q = new LinkedList<>();
        ((LinkedList<TreeNode>) q).add(root);
        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            list.add(node.value);
            if (node.left != null) {
                q.offer(node.left);
            }
            if (node.right != null) {
                q.offer(node.right);
            }
        }
        int[] res = new int[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public List<List<Integer>> levelOrderX(TreeNode root) {
        List<List<Integer>> res = new LinkedList<>();
        Deque<TreeNode> q = new LinkedList<>();
        if (root == null) {
            return res;
        }
        if (root != null) {
            q.offer(root);
        }
        TreeNode flag = root;
        List<Integer> row = new ArrayList<>();
        while (!q.isEmpty()) {
            TreeNode node = q.poll();
            row.add(node.value);
            if (node.left != null) {
                q.offer(node.left);
            }
            if (node.right != null) {
                q.offer(node.right);
            }
            if (flag == node) {
                flag = q.peekLast();
                res.add(row);
                row = new ArrayList<>();
            }

        }
        return res;
    }
}
