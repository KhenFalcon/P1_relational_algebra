package uga.csx370.mydbimpl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uga.csx370.mydb.Cell;
import uga.csx370.mydb.Predicate;
import uga.csx370.mydb.RA;
import uga.csx370.mydb.Relation;
import uga.csx370.mydb.RelationBuilder;
import uga.csx370.mydb.Type;

public class RAImpl implements RA {

    @Override
    public Relation select(Relation rel, Predicate p) {
        // create a new relation to return as results
        Relation r = new RelationBuilder()
                .attributeNames(rel.getAttrs())
                .attributeTypes(rel.getTypes())
                .build();

        int size = rel.getSize(); // call method only once
        for(int i = 0; i < size; i++) { // set hard max of 50 rows to be returned.
            List<Cell> row = rel.getRow(i);
            if(p.check(row)) { // add the row to resultant if it passes the predicate's check
                r.insert(row);
            }
        }
        return r;
    }

    @Override
    public Relation project(Relation rel, List<String> attrs) {
        if(attrs == null || attrs.isEmpty())
            throw new IllegalArgumentException("Attribute list cannot be null or empty.");

        // get the types of the projected attributes from the original relation
        List<Type> relTypes = rel.getTypes();
        List<Type> attrTypes = new ArrayList<>();
        for (int i = 0; i < attrs.size(); i++) {
            int attr_index = rel.getAttrIndex(attrs.get(i)); // for each attribute selected to be projected...
            Type attr_type = relTypes.get(attr_index); // ... get its attribute-type from relTypes
            attrTypes.add(attr_type); // ... and add only the selected types to attrTypes 
        }

        // create a new relation to return as results
        Relation r = new RelationBuilder()
                .attributeNames(attrs)
                .attributeTypes(attrTypes)
                .build();

        // Debugging output
        // System.out.println("Projecting attributes: " + attrs);
        // System.out.println("Full relation attributes: " + rel.getAttrs());

        int size = rel.getSize(); // call method only once
        for(int i = 0; i < size; i++) {
            List<Cell> row = rel.getRow(i);
            List<Cell> proj_row = new ArrayList<>(); // creates empty list

            for (int j = 0; j < attrs.size(); j++) {
                int proj_cell_index = rel.getAttrIndex(attrs.get(j)); // grab the index of the projected attribute in rel...
                Cell proj_cell = row.get(proj_cell_index); // ...grab the cell in row using that index
                proj_row.add(proj_cell); // ...and add the cell-data to proj_row
            }
            r.insert(proj_row);
        }

        return r;
    }

    @Override
    public Relation union(Relation rel1, Relation rel2) {
        checkCompatible(rel1, rel2);

        Relation output = new RelationBuilder()
                .attributeNames(rel1.getAttrs())
                .attributeTypes(rel1.getTypes())
                .build();

        Set<List<Cell>> uniqueRows = new HashSet<>();

        for (int i = 0; i < rel1.getSize(); i++) uniqueRows.add(rel1.getRow(i));
        for (int i = 0; i < rel2.getSize(); i++) uniqueRows.add(rel2.getRow(i));

        for (List<Cell> uniqueRow : uniqueRows) output.insert(uniqueRow);

        return output;
    }

    @Override
    public Relation intersect(Relation rel1, Relation rel2) {
        checkCompatible(rel1, rel2);

        Relation output = new RelationBuilder()
                .attributeNames(rel1.getAttrs())
                .attributeTypes(rel1.getTypes())
                .build();

        Set<List<Cell>> uniqueRows = new HashSet<>();

        Set<List<Cell>> rel1Rows = new HashSet<>();
        for (int i = 0; i < rel1.getSize(); i++) rel1Rows.add(rel1.getRow(i));

        for (int i = 0; i < rel2.getSize(); i++) {
            List<Cell> rel2Row = rel2.getRow(i);
            if (rel1Rows.contains(rel2Row)) {
                uniqueRows.add(rel2Row);
            }
        }

        for (List<Cell> uniqueRow : uniqueRows) output.insert(uniqueRow);

        return output;
    }

    private void checkCompatible(Relation rel1, Relation rel2) {
        if (rel1.getAttrs().size() != rel2.getAttrs().size()) {
            throw new IllegalArgumentException("Relations are not compatible: different arity.");
        }

        if (!rel1.getTypes().equals(rel2.getTypes())) {
            throw new IllegalArgumentException("Relations are not compatible: different attribute types.");
        }
    }

    @Override
    public Relation diff(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'diff'");
    }

    @Override
    public Relation rename(Relation rel, List<String> origAttr, List<String> renamedAttr) {

        List<String> rel_attrs = rel.getAttrs(); // gets rel attributes
        if (origAttr.size() != renamedAttr.size()) {
            throw new IllegalArgumentException("Argument counts do not match for origAttr and renamedAttr");
        }
        for (String attr: origAttr) {
            if (!rel_attrs.contains(attr)) {
                throw new IllegalArgumentException("Attribute in origAttr is not in rel");
            }
            // checking if attr in origAttr is in rel_attrs, if not, it throws exception

        }


        Relation rename_rel = new RelationBuilder().attributeNames(renamedAttr).attributeTypes(rel.getTypes()).build();
        // constructs new relation based on given renamedAttr and infers same types from rel

        for (int i = 0; i < rel.getSize(); i++) {
            rename_rel.insert(rel.getRow(i));
            // adds rows from rel to rename_rel
        }

        return rename_rel;

    }

    @Override
    public Relation cartesianProduct(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'cartesianProduct'");
        //List<String> attrs_rel1_rename = new ArrayList<>();
        //List<String> attrs_rel2_rename = new ArrayList<>();

        List<String> rel1_attrs = rel1.getAttrs();
        List<String> rel2_attrs = rel2.getAttrs();

        for (String attrs : rel1_attrs) {
            if(rel2_attrs.contains(attrs)) {
                throw new IllegalArgumentException("rel1 and rel2 have common attributes");
            }
        }
        
        /* 
        for (String attrs : rel1.getAttrs()) {
            String rel1_rename = "rel1." + attrs; // adds prefix to specifc attribute
            attrs_rel1_rename.add(rel1_rename); // adds the prefixed attribute to the ArrayList of renamed attributes for rel1
        } // for loop to add prefix 'rel1.' to each attribute in order to have unique column names
        
        for (String attrs : rel2.getAttrs()) {
            String rel2_rename = "rel2." + attrs; // adds prefix to each attribute
            attrs_rel2_rename.add(rel2_rename); // adds prefixed attributes to ArrayList of renamed attributes for rel2
        } // for loop to rename attributes in rel2 
        */

        /* 
        List<String> attrs_combined = new ArrayList<>(attrs_rel1_rename);
        attrs_combined.addAll(attrs_rel2_rename);
        System.out.println(attrs_combined); // ArrayList of combined attributes to make new relation
        */
       List<String> attrs_combined = new ArrayList<>(rel1_attrs);
       attrs_combined.addAll(rel2_attrs);

        List<Type> attrs_types_rel1 = rel1.getTypes();
        List<Type> attrs_types_rel2 = rel2.getTypes();

        List<Type> attrs_types_combined = new ArrayList<>(attrs_types_rel1);
        attrs_types_combined.addAll(attrs_types_rel2); // ArrayList of combined attribute types to make new relation
        System.out.println(attrs_types_combined);

        // builds new relation to store the cartesian product 
        Relation cartProd = new RelationBuilder().attributeNames(attrs_combined).attributeTypes(attrs_types_combined).build();
        
        for (int i = 0; i < rel1.getSize(); i++) {
            List<Cell> row_rel1 = rel1.getRow(i); // current row in rel1
            for (int j = 0; j < rel2.getSize(); j++) {
                List<Cell>  row_rel2 = rel2.getRow(j); // current row in rel2
                
                List<Cell> row1_row2_comb = new ArrayList<>(row_rel1);
                row1_row2_comb.addAll(row_rel2); // combine values in rows

                cartProd.insert(row1_row2_comb); //inserts new row into relation cartProd
            } // all rows of rel2

        } // outer for loop for row in rel1 -> combines specific row with all the rows in rel2

        /* 
        HashSet<String> attrs = new HashSet<>(attrs_combined);
        if (attrs_combined.size() != attrs.size()) {
            throw new IllegalArgumentException("rel1 and re12 have common attributes");
        }
        */


        return cartProd; // returns cartesian product
    }

    @Override
    public Relation join(Relation rel1, Relation rel2) {

        List<String> attrs_rel1 = rel1.getAttrs(); // gets rel1 attributes
        List<String> attrs_rel2 = rel2.getAttrs(); // gets rel2 attributes

        List<String> common_attrs = new ArrayList<>(attrs_rel1); 
        common_attrs.retainAll(attrs_rel2); // gets common attributes from both rel1 and rel2 relations
        System.out.println(common_attrs); // [dept_name]

        List<String> attrs_rel2_rename = new ArrayList<>();
        for (String attrs : rel2.getAttrs()) {
            String rel2_rename = "rel2." + attrs; // adds prefix to each attribute
            attrs_rel2_rename.add(rel2_rename); // adds prefixed attributes to ArrayList of renamed attributes for rel2
        } // for loop to rename attributes in rel2 

        Relation rel2_new = rename(rel2, attrs_rel2, attrs_rel2_rename);

        Relation cartProduct = cartesianProduct(rel1, rel2_new); // cartesian product between rel1 and rel2
        List<Integer> index_vals = new ArrayList<>(); // indexes of common attributes 

        
        index_vals.add(0, cartProduct.getAttrIndex(common_attrs.get(0)));
        index_vals.add(1, cartProduct.getAttrIndex("rel2." + common_attrs.get(0)));     
        System.out.println(index_vals); // [2, 6]

        Predicate p = new PredicateImpl(index_vals.get(0), "=", index_vals.get(1));
        Relation theta_join = select(cartProduct, p); 
        theta_join.print();

        
        List<String> tj_attrs = theta_join.getAttrs();
        List<Integer> attrs_index = new ArrayList<>();
        for (int i = 0; i < tj_attrs.size(); i ++) {
            attrs_index.add(i); // gives indices 0 to n-1
        }
        System.out.println(attrs_index); //[0,1,2,3,4,5,6,7]

        List<Integer> odd_ins = new ArrayList<>();

        for (int i = 0; i < index_vals.size(); i ++) {
            if (i %2 != 0) {
                odd_ins.add(index_vals.get(i));
            }
            //gets odd value indicies from index_vals
        }
        System.out.println(odd_ins); // [6]

        List<Integer> unique_inds = new ArrayList<>(attrs_index);
        unique_inds.removeAll(odd_ins); // removes common rel2 attrs 
        System.out.println(unique_inds); //[0,1,2,3,4,5,7]

        List<String> natural_join_attrs = new ArrayList<>(); // new attrs for natural join merging common attrs

        for (int i = 0; i < unique_inds.size(); i ++) {
            natural_join_attrs.add(i, tj_attrs.get(unique_inds.get(i)));
            // gets attribute names 
        }
        System.out.println(natural_join_attrs);

        Relation natural_join = project(theta_join, natural_join_attrs); // natural join

        return natural_join;
        
    }

    @Override
    public Relation join(Relation rel1, Relation rel2, Predicate p) {
        // do cartProd first and then do theta join

        List<String> rel1_attrs = rel1.getAttrs();
        List<String> rel2_attrs = rel2.getAttrs(); 

        for (String attr: rel1_attrs) {
            if (rel2_attrs.contains(attr)) {
                throw new IllegalArgumentException("rel1 and rel2 have common attributes");
            }
        }

        List<String> rel1_attrs = rel1.getAttrs();
        List<String> rel2_attrs = rel2.getAttrs(); 

        for (String attr: rel1_attrs) {
            if (rel2_attrs.contains(attr)) {
                throw new IllegalArgumentException("rel1 and rel2 have common attributes");
            }
        }

        Relation cartProduct = cartesianProduct(rel1, rel2); //cartesian product between rel1 and rel2
        Relation theta_join = select(cartProduct, p); // specific rows are selected based on the predicate

        return theta_join; //returns theta_join with all columns 
    }

}