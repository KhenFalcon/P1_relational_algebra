package uga.csx370.mydbimpl;

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
        for(int i = 0; i < size && r.getSize() < 51; i++) { // set hard max of 50 rows to be returned.
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

        // get a list of all the columns-numbers selected
        int[] attrIndices = new int[attrs.size()];
        for (int i = 0; i < attrs.size(); i++)
            attrIndices[i] = rel.getAttrIndex(attrs.get(i));

        List<Type> relTypes = rel.getTypes();
        List<Type> attrTypes = relTypes.subList(0, 0);
        // --- read the next few comments backwards -----------------------------------------------
        for (int i = 0; i < attrs.size(); i++) {
            attrTypes.add( // ... and add only the selected types to attrTypes 
                    relTypes.get( // ... get its attribute-type from relTypes
                            attrIndices[i] // for each attribute selected to be projected...
                    )  
            );
        }
        // ----------------------------------------------------------------------------------------

        Relation r = new RelationBuilder()
                .attributeNames(attrs)
                .attributeTypes(attrTypes)
                .build();

        int size = rel.getSize(); // call method only once
        for(int i = 0; i < size && r.getSize() < 51; i++) { // set hard max of 50 rows to be returned.
            List<Cell> row = rel.getRow(i);
            List<Cell> proj_row = row.subList(0, 0); // creates empty list

            // --- read the next few comments backwards -------------------------------------------
            for (int j = 0; j < attrs.size(); j++) {
                proj_row.add( // ...and add the cell-data to proj_row
                        row.get( // ...grab its cell using its index in rel
                                attrIndices[j] // for each attribute selected to be projected...        
                        )
                );
            }
            // ------------------------------------------------------------------------------------
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