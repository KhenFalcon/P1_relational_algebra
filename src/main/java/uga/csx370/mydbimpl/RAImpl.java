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
        // Compatibility check
        List<Type> types1 = rel1.getTypes();
        List<Type> types2 = rel2.getTypes();

        if (types1.size() != types2.size()) {
            throw new IllegalArgumentException(
                "Relations are not compatible for set difference.");
        }
        for (int i = 0; i < types1.size(); i++) {
            if (types1.get(i) != types2.get(i)) {
                throw new IllegalArgumentException(
                    "Relations are not compatible for set difference.");
            }
        }

        // Build result with rel1's schema
        Relation result = new RelationBuilder()
                .attributeNames(rel1.getAttrs())
                .attributeTypes(rel1.getTypes())
                .build();

        // Collect rel2 rows
        Set<List<Cell>> rel2Rows = new HashSet<>();
        for (int i = 0; i < rel2.getSize(); i++) {
            rel2Rows.add(rel2.getRow(i));
        }

        // Insert rel1 rows not in rel2, skipping duplicates
        Set<List<Cell>> seen = new HashSet<>();
        for (int i = 0; i < rel1.getSize(); i++) {
            List<Cell> row = rel1.getRow(i);
            if (!rel2Rows.contains(row) && seen.add(row)) {
                result.insert(row);
            }
        }

        return result;
    }

    @Override
    public Relation rename(Relation rel, List<String> origAttr, List<String> renamedAttr) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'rename'");

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

        List<String> new_attrs = new ArrayList<>(rel_attrs); // list built to contain new attribute names
        for (int i = 0; i < origAttr.size(); i ++) { // looping through original attributes
            String oldColumnName = origAttr.get(i); // gets old column name
            String newColumnName = renamedAttr.get(i); // gets new column name 
            int index = new_attrs.indexOf(oldColumnName); // finds index of old column name in new_attrs
            if (index != -1) { 
                new_attrs.set(index, newColumnName);
                // if index is found, then we set the new column name at same index of old column name
            }
        }

        Relation rename_rel = new RelationBuilder().attributeNames(new_attrs).attributeTypes(rel.getTypes()).build();
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

        List<String> rel1_attrs = rel1.getAttrs();
        List<String> rel2_attrs = rel2.getAttrs();

        for (String attrs : rel1_attrs) {
            if(rel2_attrs.contains(attrs)) {
                throw new IllegalArgumentException("rel1 and rel2 have common attributes");
            }
        }
        
        
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

        
        return cartProd; // returns cartesian product


    }

    @Override
public Relation join(Relation rel1, Relation rel2) {

    List<String> attrs1 = rel1.getAttrs();
    List<String> attrs2 = rel2.getAttrs();

    // Find attributes shared by both relations
    List<String> commonAttrs = new ArrayList<>(attrs1);
    commonAttrs.retainAll(attrs2);

    // Build the resulting attribute names and types.
    // Start with everything from rel1.
    List<String> resultAttrs = new ArrayList<>(attrs1);
    List<Type> resultTypes = new ArrayList<>(rel1.getTypes());

    // Add only non-common attributes from rel2.
    for (int i = 0; i < attrs2.size(); i++) {
        if (!commonAttrs.contains(attrs2.get(i))) {
            resultAttrs.add(attrs2.get(i));
            resultTypes.add(rel2.getTypes().get(i));
        }
    }

    Relation result = new RelationBuilder()
            .attributeNames(resultAttrs)
            .attributeTypes(resultTypes)
            .build();

    // Compare every row of rel1 with every row of rel2.
    for (int i = 0; i < rel1.getSize(); i++) {
        List<Cell> row1 = rel1.getRow(i);

        for (int j = 0; j < rel2.getSize(); j++) {
            List<Cell> row2 = rel2.getRow(j);

            boolean matches = true;

            // All common attributes must have equal values.
            for (String attr : commonAttrs) {
                int index1 = rel1.getAttrIndex(attr);
                int index2 = rel2.getAttrIndex(attr);

                if (!row1.get(index1).equals(row2.get(index2))) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                List<Cell> joinedRow = new ArrayList<>(row1);

                // Add only the non-common cells from rel2.
                for (int k = 0; k < attrs2.size(); k++) {
                    if (!commonAttrs.contains(attrs2.get(k))) {
                        joinedRow.add(row2.get(k));
                    }
                }

                result.insert(joinedRow);
            }
        }
    }

    return result;
} 
    

    @Override
    public Relation join(Relation rel1, Relation rel2, Predicate p) {
        // TODO Auto-generated method stub
        //throw new UnsupportedOperationException("Unimplemented method 'join'");

        // do cartProd first and then do theta join

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
