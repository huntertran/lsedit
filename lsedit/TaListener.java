package lsedit;

import java.util.Vector;
import javax.swing.undo.UndoableEdit;


/* This interface decouples UndoableTa.java and TemporalTa.java from LandscapeEditorCore
 * That allows it to be used in other products more easily
 */

public interface TaListener {

	/* Types of things to listen for -- The category sorts the listener vector */

	public	final static int	DIAGRAMLISTENER             =  0;	/* Only listen for diagram switching							*/
	public	final static int	SCHEMALISTENER              =  1;	/* Only listen for changes to EntityClasses and RelationClasses */
	public	final static int	ENTITYLISTENER		  		=  2;	/* Listen also for changes in location of Instances counts etc.	*/
	public	final static int	INSTANCELISTENER            =  3;	/* Listen also for changes in Relation counts					*/ 
	public	final static int	ENTITYATTRIBUTELISTENER     =  4;	/* Listen also for changes in Other entity attributes			*/ 
	public	final static int	ATTRIBUTELISTENER           =  5;	/* Listen also for changes in Other relation attributes			*/
	
	public  final static int	CATEGORIES                  =  6;          


	public  final static int    EC_NEW_SIGNAL				=  0;
	public  final static int	EC_DELETE_SIGNAL			=  1;
	public  final static int	EC_UNDELETE_SIGNAL          =  2;
	public  final static int	RC_NEW_SIGNAL				=  3;
	public  final static int	RC_DELETE_SIGNAL			=  4;
	public	final static int	RC_UNDELETE_SIGNAL          =  5;
	public  final static int	RC_IOFACTOR_SIGNAL          =  6;
	public	final static int	CONTAINS_CHANGING_SIGNAL	=  7;
	public	final static int	CONTAINS_CHANGED_SIGNAL		=  8;

	public	final static int	DRAWROOT_CUTTING_SIGNAL		=  9;

	public  final static int	ENTITY_NEW_SIGNAL           = 10;
	public	final static int	ENTITY_CUTTING_SIGNAL		= 11;
	public  final static int    ENTITY_CUT_SIGNAL			= 12;
	public  final static int    ENTITY_PASTED_SIGNAL		= 13;
	public  final static int    CONTAINER_CUTTING_SIGNAL    = 14;
	public  final static int    CONTAINER_CUT_SIGNAL		= 15;
	public	final static int	CONTAINER_PASTED_SIGNAL		= 16;
	public	final static int	ENTITY_RELOCATING_SIGNAL	= 17;
	public	final static int	ENTITY_RELOCATED_SIGNAL     = 18;

	public  final static int	RELATION_NEW_SIGNAL         = 19;
	public	final static int	RELATION_SRC_CUT_SIGNAL		= 20;
	public	final static int	RELATION_SRC_PASTED_SIGNAL	= 21;
	public	final static int	RELATION_DST_CUT_SIGNAL		= 22;
	public	final static int	RELATION_DST_PASTED_SIGNAL	= 23;
	public  final static int	RELATION_CUT_SIGNAL         = 24;
	public	final static int	RELATION_PASTED_SIGNAL      = 25;


	public	final static int	POSITION_SIGNAL				= 26;	/* Change of X,Y attribute or both */	
	public	final static int	SIZE_SIGNAL					= 27;	/* Change of Width, Height or both */
	public	final static int	BOUNDS_SIGNAL				= 28;	/* Change of POSITION and/or SIZE  */
		
	public  final static int	PARENTCLASS_SIGNAL			= 29;
	public  final static int    STYLE_SIGNAL				= 30;
	public  final static int    LABEL_SIGNAL				= 31;
	public  final static int    DESCRIPTION_SIGNAL			= 32;

	public  final static int    COLOR_SIGNAL				= 33;
	public  final static int    LABEL_COLOR_SIGNAL			= 34;
	public  final static int    OPEN_COLOR_SIGNAL			= 35;
	public  final static int    FONT_DELTA_SIGNAL			= 36;
	public  final static int    INHERITS_SIGNAL				= 37;
	public  final static int    IO_FACTOR_SIGNAL			= 38;
	public  final static int	ARROW_COLOR_SIGNAL			= 39;
	public  final static int	VIEW_CHANGED  				= 40;

	public  final static int	EC_IMAGE_SIGNAL				= 41;
	public  final static int	EC_ANGLE_SIGNAL				= 42;
	public  final static int    EC_ICON_SIGNAL              = 43;
	public  final static int	DIAGRAM_CHANGED             = 44;

	abstract void diagramChanging(Diagram diagram);
	abstract void diagramChanged(Diagram diagram, int signal);
	abstract void updateBegins();
	abstract void updateEnds();											
	abstract void entityClassChanged(EntityClass ec, int signal);
	abstract void relationClassChanged(RelationClass rc, int signal);
	abstract void entityParentChanged(EntityInstance e, EntityInstance parent, int signal);
	abstract void relationParentChanged(RelationInstance ri, int signal);
	abstract void entityInstanceChanged(EntityInstance e, int signal);
	abstract void relationInstanceChanged(RelationInstance ri, int signal);
}

