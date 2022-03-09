package jp.seo.diagram.core;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Seo-4d696b75
 * @version 2020/02/23
 */
public class KdTree<E extends Point> {

    public static class Node<E> {

        public Node(int depth, E point){
            this.depth = depth;
            this.point = point;
        }

        public final int depth;
        public final E point;
        Node<E> right, left;

        public Node<E> getRightChild(){
            return right;
        }

        public Node<E> getLeftChild(){
            return left;
        }

    }

    public KdTree(List<? extends E> list){
        mRoot = buildTree(0, new LinkedList<>(list));
    }

    public KdTree(Node<E> root){
        mRoot = root;
    }

    private Node<E> mRoot;

    private Node<E> buildTree(int depth, List<? extends E> list){
        if ( list.isEmpty() ) return null;
        if ( list.size() == 1 ){
            return new Node<E>(depth, list.get(0));
        }
        list.sort(Comparator.comparingDouble(depth%2 == 0 ? Point::getX : Point::getY));
        int mid = list.size()/2;
        Node<E> node = new Node<E>(depth, list.get(mid));
        node.left = buildTree(depth + 1, list.subList(0, mid));
        node.right = buildTree(depth + 1, list.subList(mid + 1, list.size()));
        return node;
    }

    public static class Neighbor<E> {

        private Neighbor(E point, double sqrtDist){
            this.point = point;
            this.sqrtDist = sqrtDist;
        }

        public final E point;
        public final double sqrtDist;

    }

    public static class Neighbors<E extends Point> {

        private Neighbors(int k, Point query){
            this.k = k;
            this.query = query;
            neighbors = new LinkedList<>();

        }

        public int k;
        public Point query;

        public List<Neighbor<E>> neighbors;

        private void checkNeighbor(E p){
            double dist = Math.pow(p.getX()- query.getX(), 2) + Math.pow(p.getY() - query.getY(), 2);
            int index = -1;
            int size = neighbors.size();
            if ( size > 0 && dist < neighbors.get(size-1).sqrtDist ){
                index = size-1;
                while ( index > 0 ){
                    if ( dist >= neighbors.get(index-1).sqrtDist ) break;
                    index--;
                }


            } else if ( size == 0 ){
                index = 0;
            }
            if ( index >= 0 ){
                Neighbor<E> n = new Neighbor<>(p, dist);
                neighbors.add(index, n);
                if ( size == k ) neighbors.remove(k);
            }
        }

        private double getMaxSqrtDist(){
            return neighbors.isEmpty() ? Double.MAX_VALUE : neighbors.get(neighbors.size()-1).sqrtDist;
        }

    }

    public Node<E> getRoot(){
        return mRoot;
    }

    public Neighbors<E> search(Point query, int k){
        Neighbors<E> result = new Neighbors<>(k, query);
        searchInternal(mRoot, result);
        return result;
    }

    private void searchInternal(Node<E> node, Neighbors<E> result){
        if ( node == null ) return;
        result.checkNeighbor(node.point);
        boolean x = node.depth % 2 == 0;
        double value = x ? result.query.getX() : result.query.getY();
        double threshold = x ? node.point.getX() : node.point.getY();
        searchInternal( value < threshold ? node.left : node.right, result );
        if ( Math.pow(Math.abs(value - threshold), 2) < result.getMaxSqrtDist() ){
            searchInternal( value < threshold ? node.right : node.left, result );
        }
    }

}
