package lsedit;

import java.awt.event.MouseEvent;

public abstract class LandscapeModeHandler extends Object
{
	protected LandscapeEditorCore	m_ls = null;

	public LandscapeModeHandler(LandscapeEditorCore ls) 
	{
		m_ls = ls;
	}

	//  Invoked when the handler is first selected

	public void activate()
	{
	}

	//	Invoked when ESCAPE is pressed

	public void cleanup() 
	{
	}

	public void processKey(int key, int modifiers, Object object)
	{
	}

	public void entityPressed(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("LandscapeModeHandler entityPressed\n");
	}

	public void entityReleased(MouseEvent ev, EntityInstance e, int x, int y) 
	{
//		System.out.println("LandscapeModeHandler entityReleased\n");
	}

	public void relationPressed(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
	}

	public void relationReleased(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
//		System.out.println("LandscapeModeHandler relationReleased\n");
	}

	public void entityDragged(MouseEvent ev, EntityInstance e, int x, int y)
	{
	}

	public void relationDragged(MouseEvent ev, RelationInstance ri, int x, int y) 
	{
	}

	public void movedOverThing(MouseEvent ev, Object thing, int x, int y) 
	{
	}
}