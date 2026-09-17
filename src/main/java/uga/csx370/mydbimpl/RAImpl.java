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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rename'");
    }

    @Override
    public Relation cartesianProduct(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cartesianProduct'");
    }

    @Override
    public Relation join(Relation rel1, Relation rel2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'join'");
    }

    @Override
    public Relation join(Relation rel1, Relation rel2, Predicate p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'join'");
    }

}