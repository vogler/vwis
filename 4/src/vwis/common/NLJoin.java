package vwis.common;

import java.util.ArrayList;
import java.util.List;

public class NLJoin implements DBIterator {

    Object[] lefttuple, righttuple;
    DBIterator left, right;
    List<Integer> right_join_attr = new ArrayList<Integer>();
    List<Integer> left_join_attr = new ArrayList<Integer>();

    public NLJoin(DBIterator left, DBIterator right) throws Exception {
        this.left = left;
        this.right = right;
    }

    // use open() function to get left and right attribute names as string
    // arrays
    // check for identical names and store positions in lists
    // combine left attribute names with all non-duplicate right attribute names
    // step by step
    // give back string array
    @Override
    public String[] open() throws Exception {
        String[] leftattributes = this.left.open();
        String[] rightattributes = this.right.open();

        // check for identical attribute names
        for (int i = 0; i < leftattributes.length; i++) {
            for (int j = 0; j < rightattributes.length; j++) {
                if (leftattributes[i].equals(rightattributes[j])) {
                    this.left_join_attr.add(i);
                    this.right_join_attr.add(j);
                }
            }
        }

        // combine left attribute names with non-duplicate right attribute names
        String[] result = leftattributes;
        for (int i = 0; i < rightattributes.length; i++) {
            if (!this.right_join_attr.contains(i)) {
                Object[] result_obj = this.concat(result, rightattributes[i]);
                result = new String[result_obj.length];
                for (int j = 0; j < result_obj.length; j++) {
                    result[j] = (String) result_obj[j];
                }
            }
        }
        this.lefttuple = this.left.next();

        return result;
    }

    // get next left tuple and next right tuple
    // do nested loop: iterate over left side while not null (outside loop)
    // and iterate over right side while not null (inside loop)
    // for every two left and right tuples check if all join attributes match
    // if that is the case combine them and give back result
    @Override
    public Object[] next() throws Exception {

        this.righttuple = this.right.next();

        // do nested loop
        while (this.lefttuple != null) {
            while (this.righttuple != null) {

                // check if all join attributes match
                // if one does not match the loop stops
                // if all match left tuple and non-duplicate right tuple
                // attributes are combined
                // and the result is returned
                for (int i = 0; i < this.left_join_attr.size(); i++) {
                    if (!(this.lefttuple[this.left_join_attr.get(i)]
                            .equals(this.righttuple[this.right_join_attr.get(i)]))) {
                        break;
                    }
                    if (i == this.left_join_attr.size() - 1) {
                        Object[] result = this.lefttuple;
                        for (int j = 0; j < this.righttuple.length; j++) {
                            if (!this.right_join_attr.contains(j)) {
                                result = this.concat(result, this.righttuple[j]);
                            }
                        }
                        return result;
                    }

                }
                this.righttuple = this.right.next();
            }

            // get next left tuple if all right tuples were checked
            // and open right table again
            this.lefttuple = this.left.next();
            this.right.open();
            this.righttuple = this.right.next();
        }

        return null;
    }

    @Override
    public void close() throws Exception {
        this.left.close();
        this.right.close();
    }

    // add element to the right side of given array
    private Object[] concat(Object[] array, Object object) {
        Object[] result = new Object[array.length + 1];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        result[result.length - 1] = object;
        return result;
    }

}
