/* ********************************************************
 * This file automatically generated by PointList.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST PointList class
 *  - a collection of points in a Frame. 
 * The PointList class implements a Region which represents a collection
 * of points in a Frame.
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_PointList'>AST PointList</a>  
 */
public class PointList extends Region {
    /** 
     * Create a PointList.   
     * This function creates a new PointList object and optionally initialises 
     * its attributes.
     * <p>
     * A PointList object is a specialised type of Region which represents a 
     * collection of points in a coordinate Frame.
     * <h4>Notes</h4>
     * <br> - A null Object pointer (AST__NULL) will be returned if this
     * function is invoked with the AST error status set, or if it
     * should fail for any reason.
     * @param  frame  A pointer to the Frame in which the region is defined. A deep
     * copy is taken of the supplied Frame. This means that any
     * subsequent changes made to the Frame using the supplied pointer
     * will have no effect the Region.
     * 
     * @param  npnt  The number of points in the Region. 
     * 
     * @param  points  
     *             An array giving the coordinates in <code>frame</code> of the
     *             points.  <code>points</code> is an <code>naxes</code>-element 
     *             array of
     *             <code>npnt</code>-element <code>double</code> arrays, 
     *             where <code>naxes</code> is the number of axes in 
     *             <code>frame</code>.  The value of coordinate number 
     *             <code>icoord</code> for point number
     *             <code>ipoint</code> is therefore stored at 
     *             <code>points[icoord][ipoint]</code>.
     *          
     * @param  unc  An optional pointer to an existing Region which specifies the uncertainties 
     * associated with each point in the PointList being created. The 
     * uncertainty at any point in the PointList is found by shifting the 
     * supplied "uncertainty" Region so that it is centred at the point 
     * being considered. The area covered by the shifted uncertainty Region 
     * then represents the uncertainty in the position. The uncertainty is 
     * assumed to be the same for all points.
     * <p>
     * If supplied, the uncertainty Region must be of a class for which 
     * all instances are centro-symetric (e.g. Box, Circle, Ellipse, etc.) 
     * or be a Prism containing centro-symetric component Regions. A deep 
     * copy of the supplied Region will be taken, so subsequent changes to 
     * the uncertainty Region using the supplied pointer will have no 
     * effect on the created Box. Alternatively, 
     * a NULL Object pointer 
     * may be supplied, in which case a default uncertainty is used 
     * equivalent to a box 1.0E-6 of the size of the bounding box of the 
     * PointList being created.
     * <p>
     * The uncertainty Region has two uses: 1) when the 
     * astOverlap
     * function compares two Regions for equality the uncertainty
     * Region is used to determine the tolerance on the comparison, and 2)
     * when a Region is mapped into a different coordinate system and
     * subsequently simplified (using 
     * astSimplify),
     * the uncertainties are used to determine if the transformed boundary 
     * can be accurately represented by a specific shape of Region.
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public PointList( Frame frame, int npnt, double[][] points, Region unc ) {
        construct( frame, npnt, points, unc );
    }
    private native void construct( Frame frame, int npnt, double[][] points, Region unc );

}
