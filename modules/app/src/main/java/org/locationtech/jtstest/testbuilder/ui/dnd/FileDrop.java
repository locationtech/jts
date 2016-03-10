/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui.dnd;

/**
 * This class makes it easy to drag and drop files from the operating
 * system to a Java program. Any <tt>java.awt.Component</tt> can be
 * dropped onto, but only <tt>javax.swing.JComponent</tt>s will indicate
 * the drop event with a changed border.
 * <p/>
 * To use this class, construct a new <tt>FileDrop</tt> by passing
 * it the target component and a <tt>Listener</tt> to receive notification
 * when file(s) have been dropped. Here is an example:
 * <p/>
 * <code><pre>
 *      JPanel myPanel = new JPanel();
 *      new FileDrop( myPanel, new FileDrop.Listener()
 *      {   public void filesDropped( java.io.File[] files )
 *          {   
 *              // handle file drop
 *              ...
 *          }   // end filesDropped
 *      }); // end FileDrop.Listener
 * </pre></code>
 * <p/>
 * You can specify the border that will appear when files are being dragged by
 * calling the constructor with a <tt>javax.swing.border.Border</tt>. Only
 * <tt>JComponent</tt>s will show any indication with a border.
 * <p/>
 * You can turn on some debugging features by passing a <tt>PrintStream</tt>
 * object (such as <tt>System.out</tt>) into the full constructor. A <tt>null</tt>
 * value will result in no extra debugging information being output.
 * <p/>
 *
 * <p>I'm releasing this code into the Public Domain. Enjoy.
 * </p>
 * <p><em>Original author: Robert Harder, rharder@usa.net</em></p>
 *
 * @author  Robert Harder
 * @author  rharder@usa.net
 * @version 1.0
 */
public class FileDrop
{
    private transient javax.swing.border.Border normalBorder;
    private transient java.awt.dnd.DropTargetListener dropListener;
    
    
    /** Discover if the running JVM is modern enough to have drag and drop. */
    private static Boolean supportsDnD;
    
    // Default border color
    private static java.awt.Color defaultBorderColor = new java.awt.Color( 0f, 0f, 1f, 0.25f );
    
    /**
     * Constructs a {@link FileDrop} with a default light-blue border
     * and, if <var>c</var> is a {@link java.awt.Container}, recursively
     * sets all elements contained within as drop targets, though only
     * the top level container will change borders.
     *
     * @param c Component on which files will be dropped.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.awt.Component c,
    final Listener listener )
    {   this( null,  // Logging stream
              c,     // Drop target
              javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, defaultBorderColor ), // Drag border
              true, // Recursive
              listener );
    }   // end constructor
    
    
    
    
    /**
     * Constructor with a default border and the option to recursively set drop targets.
     * If your component is a <tt>java.awt.Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     *
     * @param c Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.awt.Component c,
    final boolean recursive,
    final Listener listener )
    {   this( null,  // Logging stream
              c,     // Drop target
              javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, defaultBorderColor ), // Drag border
              recursive, // Recursive
              listener );
    }   // end constructor
    
    
    /**
     * Constructor with a default border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.io.PrintStream out,
    final java.awt.Component c,
    final Listener listener )
    {   this( out,  // Logging stream
              c,    // Drop target
              javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, defaultBorderColor ), 
              false, // Recursive
              listener );
    }   // end constructor
    
        
    
    /**
     * Constructor with a default border, debugging optionally turned on
     * and the option to recursively set drop targets.
     * If your component is a <tt>java.awt.Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     * With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.io.PrintStream out,
    final java.awt.Component c,
    final boolean recursive,
    final Listener listener)
    {   this( out,  // Logging stream
              c,    // Drop target
              javax.swing.BorderFactory.createMatteBorder( 2, 2, 2, 2, defaultBorderColor ), // Drag border
              recursive, // Recursive
              listener );
    }   // end constructor
    
    
    
    
    /**
     * Constructor with a specified border 
     *
     * @param c Component on which files will be dropped.
     * @param dragBorder Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.awt.Component c,
    final javax.swing.border.Border dragBorder,
    final Listener listener) 
    {   this(
            null,   // Logging stream
            c,      // Drop target
            dragBorder, // Drag border
            false,  // Recursive
            listener );
    }   // end constructor
    
    
        
    
    /**
     * Constructor with a specified border and the option to recursively set drop targets.
     * If your component is a <tt>java.awt.Container</tt>, then each of its children
     * components will also listen for drops, though only the parent will change borders.
     *
     * @param c Component on which files will be dropped.
     * @param dragBorder Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.awt.Component c,
    final javax.swing.border.Border dragBorder,
    final boolean recursive,
    final Listener listener) 
    {   this(
            null,
            c,
            dragBorder,
            recursive,
            listener );
    }   // end constructor
    
            
    
    /**
     * Constructor with a specified border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param dragBorder Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.io.PrintStream out,
    final java.awt.Component c,
    final javax.swing.border.Border dragBorder,
    final Listener listener) 
    {   this(
            out,    // Logging stream
            c,      // Drop target
            dragBorder, // Drag border
            false,  // Recursive
            listener );
    }   // end constructor
    
    
    
    
    
    /**
     * Full constructor with a specified border and debugging optionally turned on.
     * With Debugging turned on, more status messages will be displayed to
     * <tt>out</tt>. A common way to use this constructor is with
     * <tt>System.out</tt> or <tt>System.err</tt>. A <tt>null</tt> value for
     * the parameter <tt>out</tt> will result in no debugging output.
     *
     * @param out PrintStream to record debugging info or null for no debugging.
     * @param c Component on which files will be dropped.
     * @param dragBorder Border to use on <tt>JComponent</tt> when dragging occurs.
     * @param recursive Recursively set children as drop targets.
     * @param listener Listens for <tt>filesDropped</tt>.
     * @since 1.0
     */
    public FileDrop(
    final java.io.PrintStream out,
    final java.awt.Component c,
    final javax.swing.border.Border dragBorder,
    final boolean recursive,
    final Listener listener) 
    {   
        
        if( supportsDnD() )
        {   // Make a drop listener
            dropListener = new java.awt.dnd.DropTargetListener()
            {	public void dragEnter( java.awt.dnd.DropTargetDragEvent evt )
                {	log( out, "FileDrop: dragEnter event." );

                    // Is this an acceptable drag event?
                    if( isDragOk( out, evt ) )
                    {
                        // If it's a Swing component, set its border
                        if( c instanceof javax.swing.JComponent )
                        {   javax.swing.JComponent jc = (javax.swing.JComponent) c;
                            normalBorder = jc.getBorder();
                            log( out, "FileDrop: normal border saved." );
                            jc.setBorder( dragBorder );
                            log( out, "FileDrop: drag border set." );
                        }   // end if: JComponent   

                        // Acknowledge that it's okay to enter
                        //evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY );
                        log( out, "FileDrop: event accepted." );
                    }   // end if: drag ok
                    else 
                    {   // Reject the drag event
                        evt.rejectDrag();
                        log( out, "FileDrop: event rejected." );
                    }   // end else: drag not ok
                }   // end dragEnter

                public void dragOver( java.awt.dnd.DropTargetDragEvent evt ) 
                {   // This is called continually as long as the mouse is
                    // over the drag target.
                }   // end dragOver

                public void drop( java.awt.dnd.DropTargetDropEvent evt )
                {   log( out, "FileDrop: drop event." );
                    try
                    {   // Get whatever was dropped
                        java.awt.datatransfer.Transferable tr = evt.getTransferable();

                        // Is it a file list?
                        if (tr.isDataFlavorSupported (java.awt.datatransfer.DataFlavor.javaFileListFlavor))
                        {
                            // Say we'll take it.
                            //evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                            evt.acceptDrop ( java.awt.dnd.DnDConstants.ACTION_COPY );
                            log( out, "FileDrop: file list accepted." );

                            // Get a useful list
                            java.util.List fileList = (java.util.List) 
                                tr.getTransferData(java.awt.datatransfer.DataFlavor.javaFileListFlavor);
                            java.util.Iterator iterator = fileList.iterator();

                            // Convert list to array
                            java.io.File[] filesTemp = new java.io.File[ fileList.size() ];
                            fileList.toArray( filesTemp );
                            final java.io.File[] files = filesTemp;

                            // Alert listener to drop.
                            if( listener != null )
                                listener.filesDropped( files );

                            // Mark that drop is completed.
                            evt.getDropTargetContext().dropComplete(true);
                            log( out, "FileDrop: drop complete." );
                        }   // end if: file list
                        else 
                        {   log( out, "FileDrop: not a file list - abort." );
                            evt.rejectDrop();
                        }   // end else: not a file list
                    }   // end try
                    catch ( java.io.IOException io) 
                    {   log( out, "FileDrop: IOException - abort:" );
                        io.printStackTrace( out );
                        evt.rejectDrop();
                    }   // end catch IOException
                    catch (java.awt.datatransfer.UnsupportedFlavorException ufe) 
                    {   log( out, "FileDrop: UnsupportedFlavorException - abort:" );
                        ufe.printStackTrace( out );
                        evt.rejectDrop();
                    }   // end catch: UnsupportedFlavorException
                    finally
                    {
                        // If it's a Swing component, reset its border
                        if( c instanceof javax.swing.JComponent )
                        {   javax.swing.JComponent jc = (javax.swing.JComponent) c;
                            jc.setBorder( normalBorder );
                            log( out, "FileDrop: normal border restored." );
                        }   // end if: JComponent
                    }   // end finally
                }   // end drop

                public void dragExit( java.awt.dnd.DropTargetEvent evt ) 
                {   log( out, "FileDrop: dragExit event." );
                    // If it's a Swing component, reset its border
                    if( c instanceof javax.swing.JComponent )
                    {   javax.swing.JComponent jc = (javax.swing.JComponent) c;
                        jc.setBorder( normalBorder );
                        log( out, "FileDrop: normal border restored." );
                    }   // end if: JComponent
                }   // end dragExit

                public void dropActionChanged( java.awt.dnd.DropTargetDragEvent evt ) 
                {   log( out, "FileDrop: dropActionChanged event." );
                    // Is this an acceptable drag event?
                    if( isDragOk( out, evt ) )
                    {   //evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY_OR_MOVE );
                        evt.acceptDrag( java.awt.dnd.DnDConstants.ACTION_COPY );
                        log( out, "FileDrop: event accepted." );
                    }   // end if: drag ok
                    else 
                    {   evt.rejectDrag();
                        log( out, "FileDrop: event rejected." );
                    }   // end else: drag not ok
                }   // end dropActionChanged
            }; // end DropTargetListener

            // Make the component (and possibly children) drop targets
            makeDropTarget( out, c, recursive );
        }   // end if: supports dnd
        else
        {   log( out, "FileDrop: Drag and drop is not supported with this JVM" );
        }   // end else: does not support DnD
    }   // end constructor

    
    private static boolean supportsDnD()
    {   // Static Boolean
        if( supportsDnD == null )
        {   
            boolean support = false;
            try
            {   Class arbitraryDndClass = Class.forName( "java.awt.dnd.DnDConstants" );
                support = true;
            }   // end try
            catch( Exception e )
            {   support = false;
            }   // end catch
            supportsDnD = new Boolean( support );
        }   // end if: first time through
        return supportsDnD.booleanValue();
    }   // end supportsDnD
    
    
    
    
    
    private void makeDropTarget( final java.io.PrintStream out, final java.awt.Component c, boolean recursive )
    {
        // Make drop target
        final java.awt.dnd.DropTarget dt = new java.awt.dnd.DropTarget();
        try
        {   dt.addDropTargetListener( dropListener );
        }   // end try
        catch( java.util.TooManyListenersException e )
        {   e.printStackTrace();
            log(out, "FileDrop: Drop will not work due to previous error. Do you have another listener attached?" );
        }   // end catch
        
        // Listen for hierarchy changes and remove the drop target when the parent gets cleared out.
        c.addHierarchyListener( new java.awt.event.HierarchyListener()
        {   public void hierarchyChanged( java.awt.event.HierarchyEvent evt )
            {   log( out, "FileDrop: Hierarchy changed." );
                java.awt.Component parent = c.getParent();
                if( parent == null )
                {   c.setDropTarget( null );
                    log( out, "FileDrop: Drop target cleared from component." );
                }   // end if: null parent
                else
                {   new java.awt.dnd.DropTarget(c, dropListener);
                    log( out, "FileDrop: Drop target added to component." );
                }   // end else: parent not null
            }   // end hierarchyChanged
        }); // end hierarchy listener
        if( c.getParent() != null )
            new java.awt.dnd.DropTarget(c, dropListener);
        
        if( recursive && (c instanceof java.awt.Container ) )
        {   
            // Get the container
            java.awt.Container cont = (java.awt.Container) c;
            
            // Get it's components
            java.awt.Component[] comps = cont.getComponents();
            
            // Set it's components as listeners also
            for( int i = 0; i < comps.length; i++ )
                makeDropTarget( out, comps[i], recursive );
        }   // end if: recursively set components as listener
    }   // end dropListener
    
    
    
    /** Determine if the dragged data is a file list. */
    private boolean isDragOk( final java.io.PrintStream out, final java.awt.dnd.DropTargetDragEvent evt )
    {   boolean ok = false;
        
        // Get data flavors being dragged
        java.awt.datatransfer.DataFlavor[] flavors = evt.getCurrentDataFlavors();
        
        // See if any of the flavors are a file list
        int i = 0;
        while( !ok && i < flavors.length )
        {   // Is the flavor a file list?
            if( flavors[i].equals( java.awt.datatransfer.DataFlavor.javaFileListFlavor ) )
                ok = true;            
            i++;
        }   // end while: through flavors
        
        // If logging is enabled, show data flavors
        if( out != null )
        {   if( flavors.length == 0 )
                log( out, "FileDrop: no data flavors." );
            for( i = 0; i < flavors.length; i++ )
                log( out, flavors[i].toString() );
        }   // end if: logging enabled
        
        return ok;
    }   // end isDragOk
    
    
    /** Outputs <tt>message</tt> to <tt>out</tt> if it's not null. */
    private static void log( java.io.PrintStream out, String message )
    {   // Log message if requested
        if( out != null )
            out.println( message );
    }   // end log

    
    
    
    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     * This will recursively unregister all components contained within
     * <var>c</var> if <var>c</var> is a {@link java.awt.Container}.
     *
     * @param c The component to unregister as a drop target
     * @since 1.0
     */
    public static boolean remove( java.awt.Component c)
    {   return remove( null, c, true );
    }   // end remove
    
    
    
    /**
     * Removes the drag-and-drop hooks from the component and optionally
     * from the all children. You should call this if you add and remove
     * components after you've set up the drag-and-drop.
     *
     * @param out Optional {@link java.io.PrintStream} for logging drag and drop messages
     * @param c The component to unregister
     * @param recursive Recursively unregister components within a container
     * @since 1.0
     */
    public static boolean remove( java.io.PrintStream out, java.awt.Component c, boolean recursive )
    {   // Make sure we support dnd.
        if( supportsDnD() )
        {   log( out, "FileDrop: Removing drag-and-drop hooks." );
            c.setDropTarget( null );
            if( recursive && ( c instanceof java.awt.Container ) )
            {   java.awt.Component[] comps = ((java.awt.Container)c).getComponents();
                for( int i = 0; i < comps.length; i++ )
                    remove( out, comps[i], recursive );
                return true;
            }   // end if: recursive
            else return false;
        }   // end if: supports DnD
        else return false;
    }   // end remove
    
    
    
    
    

    /** Runs a sample program that shows dropped files */
    public static void main( String[] args )
    {
        javax.swing.JFrame frame = new javax.swing.JFrame( "FileDrop" );
        //javax.swing.border.TitledBorder dragBorder = new javax.swing.border.TitledBorder( "Drop 'em" );
        final javax.swing.JTextArea text = new javax.swing.JTextArea();
        frame.getContentPane().add( 
            new javax.swing.JScrollPane( text ), 
            java.awt.BorderLayout.CENTER );
        
        new FileDrop( System.out, text, /*dragBorder,*/ new FileDrop.Listener()
        {   public void filesDropped( java.io.File[] files )
            {   for( int i = 0; i < files.length; i++ )
                {   try
                    {   text.append( files[i].getCanonicalPath() + "\n" );
                    }   // end try
                    catch( java.io.IOException e ) {}
                }   // end for: through each dropped file
            }   // end filesDropped
        }); // end FileDrop.Listener

        frame.setBounds( 100, 100, 300, 400 );
        frame.setDefaultCloseOperation( frame.EXIT_ON_CLOSE );
        frame.show();
    }   // end main




    
/* ********  I N N E R   I N T E R F A C E   L I S T E N E R  ******** */    
    
    
    /**
     * Implement this inner interface to listen for when files are dropped. For example
     * your class declaration may begin like this:
     * <code><pre>
     *      public class MyClass implements FileDrop.Listener
     *      ...
     *      public void filesDropped( java.io.File[] files )
     *      {
     *          ...
     *      }   // end filesDropped
     *      ...
     * </pre></code>
     *
     * @since 1.0
     */
    public interface Listener
    {   
        /**
         * This method is called when files have been successfully dropped.
         *
         * @param files An array of <tt>File</tt>s that were dropped.
         * @since 1.0
         */
        public abstract void filesDropped( java.io.File[] files );
    }   // end inner-interface Listener
    
}   // end class FileDrop